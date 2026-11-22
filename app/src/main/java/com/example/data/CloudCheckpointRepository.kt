package com.example.data

import android.util.Log
import com.example.model.BonusStats
import com.example.model.GameStats
import com.example.model.HEROES_CATALOG
import com.example.model.PersistentProgress
import com.example.model.SantaQuestStats
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE,
    GET,
    LIST,
    WRITE
}

data class CloudCheckpointSnapshot(
    val progress: PersistentProgress,
    val displayName: String,
    val updatedAtMs: Long
)

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        cont.cancel()
    }
}

class CloudCheckpointRepository(
    private val db: FirebaseFirestore
) {
    private fun checkpointPath(uid: String): String = "users/$uid/checkpoints/main"

    private fun checkpointDoc(uid: String) =
        db.collection("users").document(uid).collection("checkpoints").document("main")

    fun handleFirestoreError(
        exception: Throwable,
        operationType: OperationType,
        path: String?
    ) {
        val currentUser = try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        }
        val providerIds = currentUser?.providerData
            ?.map { it.providerId }
            ?.filter { it != "firebase" }
            ?: emptyList()

        val payload = JSONObject().apply {
            put("error", exception.message ?: exception.toString())
            put("code", (exception as? FirebaseFirestoreException)?.code?.name ?: "UNKNOWN")
            put("operationType", operationType.name.lowercase())
            put("path", path ?: "")
            put("uid", currentUser?.uid ?: "")
            put("providers", providerIds.joinToString(","))
        }
        Log.e("FirestoreError", "Firestore Operation Failed: $payload", exception)
    }

    suspend fun saveCheckpoint(
        uid: String,
        progress: PersistentProgress,
        displayName: String
    ): Result<Long> {
        val activeUid = Firebase.auth.currentUser?.uid
        if (uid.isBlank() || activeUid != uid) {
            return Result.failure(IllegalStateException("Utilizator neautentificat."))
        }

        val path = checkpointPath(uid)
        val docRef = checkpointDoc(uid)
        var isUpdate = false

        return try {
            val existing = docRef.get().awaitResult()
            isUpdate = existing.exists()

            val mutableFields = buildMutableFieldsMap(progress, displayName)
            if (isUpdate) {
                docRef.update(mutableFields).awaitResult()
            } else {
                val createFields = HashMap<String, Any>(mutableFields.size + 2).apply {
                    putAll(mutableFields)
                    put("userId", uid)
                    put("createdAt", FieldValue.serverTimestamp())
                }
                docRef.set(createFields).awaitResult()
            }
            Result.success(System.currentTimeMillis())
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isUpdate) OperationType.UPDATE else OperationType.CREATE,
                path
            )
            Result.failure(e)
        }
    }

    suspend fun loadCheckpoint(uid: String): Result<CloudCheckpointSnapshot?> {
        val activeUid = Firebase.auth.currentUser?.uid
        if (uid.isBlank() || activeUid != uid) {
            return Result.failure(IllegalStateException("Utilizator neautentificat."))
        }

        val path = checkpointPath(uid)
        return try {
            val snap = checkpointDoc(uid).get().awaitResult()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(parseSnapshot(snap))
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    fun observeCheckpoint(uid: String): Flow<CloudCheckpointSnapshot?> = callbackFlow {
        val activeUid = Firebase.auth.currentUser?.uid
        if (uid.isBlank() || activeUid != uid) {
            close()
            return@callbackFlow
        }

        val path = checkpointPath(uid)
        val registration = checkpointDoc(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(parseSnapshot(snapshot))
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    private fun buildMutableFieldsMap(
        p: PersistentProgress,
        displayName: String
    ): Map<String, Any> {
        val safeCompleted = p.completedLevels.take(5000).map { it.coerceIn(1, 999_999_999) }
        val safeOwned = buildList {
            add("om_de_zapada")
            p.ownedHeroes.forEach { id ->
                if (id != "om_de_zapada" && HEROES_CATALOG.any { it.id == id } && size < 50) {
                    add(id)
                }
            }
        }
        val safeEquipped = if (HEROES_CATALOG.any { it.id == p.equippedHero }) {
            p.equippedHero
        } else {
            "om_de_zapada"
        }
        val safeClaimedFree = p.claimedFree.take(30)
        val safeClaimedPrem = p.claimedPrem.take(30)
        val safeClaimedSanta = p.santaStats.claimedSantaQuests.take(20)

        return hashMapOf(
            "displayName" to displayName.ifBlank { "Jucător Crăciun" }.take(120),
            "coins" to p.coins.coerceIn(0, 1_000_000_000),
            "diamonds" to p.diamonds.coerceIn(0, 1_000_000_000),
            "galactic" to p.galactic.coerceIn(0, 1_000_000_000),
            "currentLevel" to p.currentLevel.coerceIn(1, 999_999_999),
            "unlockedLevel" to p.unlockedLevel.coerceIn(1, 999_999_999),
            "completedLevels" to safeCompleted,
            "ownedHeroes" to safeOwned,
            "equippedHero" to safeEquipped,
            "passXP" to p.passXP.coerceIn(0, 1_000_000_000),
            "premiumUnlocked" to p.premiumUnlocked,
            "claimedFree" to safeClaimedFree,
            "claimedPrem" to safeClaimedPrem,
            "lastDailyTs" to p.lastDailyTs.coerceAtLeast(0L),
            "lastBonusQuestTs" to p.lastBonusQuestTs.coerceAtLeast(0L),
            "checkpointLevel" to p.checkpointLevel.coerceIn(0, 999_999_999),
            "checkpointX" to p.checkpointX.toDouble().coerceIn(0.0, 1_000_000.0),
            "checkpointY" to p.checkpointY.toDouble().coerceIn(-10_000.0, 100_000.0),
            "statsLevelsDone" to p.stats.levelsDone.coerceAtLeast(0),
            "statsIntrudersKilled" to p.stats.intrudersKilled.coerceAtLeast(0),
            "statsCoinsGot" to p.stats.coinsGot.coerceAtLeast(0),
            "statsJumpsMade" to p.stats.jumpsMade.coerceAtLeast(0),
            "bonusLevelsDone" to p.bonusStats.levelsDone.coerceAtLeast(0),
            "bonusIntrudersKilled" to p.bonusStats.intrudersKilled.coerceAtLeast(0),
            "bonusCoinsGot" to p.bonusStats.coinsGot.coerceAtLeast(0),
            "santaGiftsDelivered" to p.santaStats.giftsDelivered.coerceAtLeast(0),
            "santaHousesVisited" to p.santaStats.housesVisited.coerceAtLeast(0),
            "santaBubbleJumps" to p.santaStats.bubbleJumps.coerceAtLeast(0),
            "santaSleighCoins" to p.santaStats.sleighCoins.coerceAtLeast(0),
            "claimedSantaQuests" to safeClaimedSanta,
            "grandSleighClaimed" to p.santaStats.grandSleighClaimed,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun parseSnapshot(snap: DocumentSnapshot): CloudCheckpointSnapshot {
        val data = snap.data ?: emptyMap()

        fun intField(key: String, default: Int = 0): Int =
            (data[key] as? Number)?.toInt() ?: default

        fun longField(key: String, default: Long = 0L): Long =
            (data[key] as? Number)?.toLong() ?: default

        fun floatField(key: String, default: Float = 0f): Float =
            (data[key] as? Number)?.toFloat() ?: default

        fun boolField(key: String, default: Boolean = false): Boolean =
            data[key] as? Boolean ?: default

        val completed = (data["completedLevels"] as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }
            ?.toSet()
            ?: emptySet()

        val owned = buildSet {
            add("om_de_zapada")
            (data["ownedHeroes"] as? List<*>)
                ?.mapNotNull { it as? String }
                ?.filter { id -> HEROES_CATALOG.any { it.id == id } }
                ?.forEach { add(it) }
        }

        val eqRaw = data["equippedHero"] as? String ?: "om_de_zapada"
        val equipped = if (HEROES_CATALOG.any { it.id == eqRaw }) eqRaw else "om_de_zapada"

        val claimedF = (data["claimedFree"] as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }
            ?.toSet()
            ?: emptySet()

        val claimedP = (data["claimedPrem"] as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }
            ?.toSet()
            ?: emptySet()

        val claimedSanta = (data["claimedSantaQuests"] as? List<*>)
            ?.mapNotNull { it as? String }
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()

        val progress = PersistentProgress(
            coins = intField("coins", 0).coerceAtLeast(0),
            diamonds = intField("diamonds", 0).coerceAtLeast(0),
            galactic = intField("galactic", 0).coerceAtLeast(0),
            currentLevel = intField("currentLevel", 1).coerceAtLeast(1),
            unlockedLevel = intField("unlockedLevel", 1).coerceAtLeast(1),
            completedLevels = completed,
            ownedHeroes = owned,
            equippedHero = equipped,
            passXP = intField("passXP", 0).coerceAtLeast(0),
            premiumUnlocked = boolField("premiumUnlocked", false),
            claimedFree = claimedF,
            claimedPrem = claimedP,
            lastDailyTs = longField("lastDailyTs", 0L).coerceAtLeast(0L),
            lastBonusQuestTs = longField("lastBonusQuestTs", 0L).coerceAtLeast(0L),
            checkpointLevel = intField("checkpointLevel", 0).coerceAtLeast(0),
            checkpointX = floatField("checkpointX", 0f).coerceAtLeast(0f),
            checkpointY = floatField("checkpointY", 0f),
            stats = GameStats(
                levelsDone = intField("statsLevelsDone", 0).coerceAtLeast(0),
                intrudersKilled = intField("statsIntrudersKilled", 0).coerceAtLeast(0),
                coinsGot = intField("statsCoinsGot", 0).coerceAtLeast(0),
                jumpsMade = intField("statsJumpsMade", 0).coerceAtLeast(0)
            ),
            bonusStats = BonusStats(
                levelsDone = intField("bonusLevelsDone", 0).coerceAtLeast(0),
                intrudersKilled = intField("bonusIntrudersKilled", 0).coerceAtLeast(0),
                coinsGot = intField("bonusCoinsGot", 0).coerceAtLeast(0)
            ),
            santaStats = SantaQuestStats(
                giftsDelivered = intField("santaGiftsDelivered", 0).coerceAtLeast(0),
                housesVisited = intField("santaHousesVisited", 0).coerceAtLeast(0),
                bubbleJumps = intField("santaBubbleJumps", 0).coerceAtLeast(0),
                sleighCoins = intField("santaSleighCoins", 0).coerceAtLeast(0),
                claimedSantaQuests = claimedSanta,
                grandSleighClaimed = boolField("grandSleighClaimed", false)
            )
        )

        val updatedAtMs = snap.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.toDate()
            ?.time
            ?: 0L

        return CloudCheckpointSnapshot(
            progress = progress,
            displayName = (data["displayName"] as? String).orEmpty().ifBlank { "Jucător Crăciun" },
            updatedAtMs = updatedAtMs
        )
    }
}
