package com.example.ui

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.audio.CosmicAudioEngine
import com.example.data.CloudCheckpointRepository
import com.example.data.ProgressRepository
import com.example.data.awaitResult
import com.example.game.EngineCallbacks
import com.example.game.ParkourEngine
import com.example.game.WorldDimension
import com.example.model.AbilityType
import com.example.model.BonusQuestDef
import com.example.model.BonusStats
import com.example.model.DAY_MILLIS
import com.example.model.HeroMeta
import com.example.model.HeroRarity
import com.example.model.PREMIUM_COST_COINS
import com.example.model.PassReward
import com.example.model.PersistentProgress
import com.example.model.QuestDef
import com.example.model.SANTA_QUESTS
import com.example.model.SantaGrandSleighBonus
import com.example.model.SantaQuestDef
import com.example.model.SeasonTier
import com.example.model.findHeroMeta
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

enum class ActiveModal {
    NONE,
    LEVELS,
    QUESTS,
    SHOP,
    PASS,
    SANTA_QUESTS,
    SETTINGS,
    PROFILE
}

data class VictorySummary(
    val level: Int,
    val coinsEarned: Int,
    val xpEarned: Int,
    val diamondsEarned: Int
)

data class HudUiState(
    val progress: PersistentProgress = PersistentProgress(),
    val frameTick: Long = 0L,
    val showEntranceMenu: Boolean = true,
    val touchEffectsEnabled: Boolean = true,
    val activeModal: ActiveModal = ActiveModal.NONE,
    val shopRarityFilter: HeroRarity? = null,
    val victorySummary: VictorySummary? = null,
    val toastMessage: String? = null,
    val isMuted: Boolean = false,
    val isLandscapeLocked: Boolean = false,
    val abilityCdLeft: Float = 0f,
    val abilityMaxCd: Float = 4f,
    val activeAbilityKind: AbilityType? = null,
    val activeAbilityTimeLeft: Float = 0f,
    val solarShieldActive: Boolean = false,
    val dailyRemainingMs: Long = 0L,
    val bonusQuestRemainingMs: Long = 0L,
    val inSantaVillage: Boolean = false,
    val nearTreeDoor: Boolean = false,
    val nearReturnDoor: Boolean = false,
    val nearSanta: Boolean = false,
    val grinchBossHp: Int? = null,
    val grinchBossMaxHp: Int = 2000,
    val grinchBossName: String = "BOSS GRINCH",
    val isSignedIn: Boolean = false,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val isAuthLoading: Boolean = false,
    val isCloudSyncing: Boolean = false,
    val lastCloudSyncMs: Long? = null,
    val authErrorMessage: String? = null
)

class ParkourViewModel(
    private val repo: ProgressRepository,
    private val cloudRepo: CloudCheckpointRepository
) : ViewModel() {
    val audio = CosmicAudioEngine()

    private val _uiState = MutableStateFlow(HudUiState())
    val uiState: StateFlow<HudUiState> = _uiState.asStateFlow()

    private var toastJob: Job? = null
    private var saveJob: Job? = null
    private var cloudSyncJob: Job? = null
    private var cloudObserverJob: Job? = null
    private val localLoadedSignal = CompletableDeferred<Unit>()
    private var activeUid: String? = null

    val engine: ParkourEngine = ParkourEngine(
        audio = audio,
        callbacks = object : EngineCallbacks {
            override fun onCoinsCollected(coinsDelta: Int, xpDelta: Int) {
                updateProgress { p ->
                    p.copy(
                        coins = p.coins + coinsDelta,
                        passXP = p.passXP + xpDelta,
                        stats = p.stats.copy(coinsGot = p.stats.coinsGot + coinsDelta),
                        bonusStats = p.bonusStats.copy(coinsGot = p.bonusStats.coinsGot + coinsDelta),
                        santaStats = p.santaStats.copy(sleighCoins = p.santaStats.sleighCoins + coinsDelta)
                    )
                }
            }

            override fun onDiamondCollected(diamondsDelta: Int, xpDelta: Int) {
                updateProgress { p ->
                    p.copy(
                        diamonds = p.diamonds + diamondsDelta,
                        passXP = p.passXP + xpDelta
                    )
                }
            }

            override fun onIntruderStomped(coinsDelta: Int) {
                updateProgress { p ->
                    p.copy(
                        coins = p.coins + coinsDelta,
                        stats = p.stats.copy(intrudersKilled = p.stats.intrudersKilled + 1),
                        bonusStats = p.bonusStats.copy(intrudersKilled = p.bonusStats.intrudersKilled + 1),
                        santaStats = p.santaStats.copy(sleighCoins = p.santaStats.sleighCoins + coinsDelta)
                    )
                }
            }

            override fun onStatIncrement(key: String, amount: Int) {
                updateProgress { p ->
                    when (key) {
                        "jumpsMade" -> p.copy(stats = p.stats.copy(jumpsMade = p.stats.jumpsMade + amount))
                        "intrudersKilled" -> p.copy(
                            stats = p.stats.copy(intrudersKilled = p.stats.intrudersKilled + amount),
                            bonusStats = p.bonusStats.copy(intrudersKilled = p.bonusStats.intrudersKilled + amount)
                        )
                        "giftsDelivered" -> p.copy(
                            santaStats = p.santaStats.copy(giftsDelivered = p.santaStats.giftsDelivered + amount)
                        )
                        "housesVisited" -> p.copy(
                            santaStats = p.santaStats.copy(housesVisited = p.santaStats.housesVisited + amount)
                        )
                        "bubbleJumps" -> p.copy(
                            santaStats = p.santaStats.copy(bubbleJumps = p.santaStats.bubbleJumps + amount)
                        )
                        else -> p
                    }
                }
            }

            override fun onLevelCompleted(lvl: Int, runCoins: Int, runDiamonds: Int) {
                val lvlCoins = 10 + min(15, (lvl * 0.5f).toInt())
                val lvlXP = 70 + min(90, lvl * 2)

                updateProgress(immediateCloudBackup = true) { p ->
                    val newCompleted = if (p.completedLevels.size < 5000) p.completedLevels + lvl else p.completedLevels
                    val newUnlocked = max(p.unlockedLevel, lvl + 1)
                    p.copy(
                        coins = p.coins + lvlCoins,
                        passXP = p.passXP + lvlXP,
                        unlockedLevel = newUnlocked,
                        completedLevels = newCompleted,
                        checkpointLevel = 0,
                        checkpointX = 0f,
                        checkpointY = 0f,
                        stats = p.stats.copy(levelsDone = p.stats.levelsDone + 1),
                        bonusStats = p.bonusStats.copy(levelsDone = p.bonusStats.levelsDone + 1)
                    )
                }

                viewModelScope.launch {
                    delay(380L)
                    _uiState.update {
                        it.copy(
                            victorySummary = VictorySummary(
                                level = lvl,
                                coinsEarned = runCoins,
                                xpEarned = lvlXP,
                                diamondsEarned = runDiamonds
                            )
                        )
                    }
                }
            }

            override fun onCheckpointActivated(lvl: Int, spawnX: Float, spawnY: Float) {
                updateProgress(immediateCloudBackup = true) { p ->
                    p.copy(
                        checkpointLevel = lvl,
                        checkpointX = spawnX,
                        checkpointY = spawnY
                    )
                }
            }

            override fun onToast(message: String) {
                showToast(message)
            }

            override fun onOpenSantaQuests() {
                audio.playSantaSleigh()
                openModal(ActiveModal.SANTA_QUESTS)
            }
        }
    )

    init {
        viewModelScope.launch {
            val loaded = repo.loadProgress()
            _uiState.update { it.copy(progress = loaded) }
            loadEngineWithProgressCheckpoint(loaded)
            refreshTimers()
            localLoadedSignal.complete(Unit)
        }

        viewModelScope.launch {
            authStateFlow().collect { user ->
                handleAuthStateChanged(user)
            }
        }

        // 1-second timer tick for 24h daily & bonus quest cooldowns
        viewModelScope.launch {
            while (true) {
                refreshTimers()
                delay(1000L)
            }
        }
    }

    private fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val auth = Firebase.auth
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private suspend fun handleAuthStateChanged(user: FirebaseUser?) {
        cloudObserverJob?.cancel()
        cloudObserverJob = null

        if (user == null) {
            activeUid = null
            _uiState.update {
                it.copy(
                    isSignedIn = false,
                    userDisplayName = null,
                    userEmail = null,
                    isAuthLoading = false,
                    isCloudSyncing = false,
                    lastCloudSyncMs = null
                )
            }
            return
        }

        activeUid = user.uid
        val displayName = user.displayName.orEmpty().ifBlank { "Jucător Google" }
        _uiState.update {
            it.copy(
                isSignedIn = true,
                userDisplayName = displayName,
                userEmail = user.email,
                isAuthLoading = false,
                authErrorMessage = null
            )
        }

        localLoadedSignal.await()
        performInitialCloudReconcile(user.uid, displayName)
        startCloudCheckpointObserver(user.uid)
    }

    private suspend fun performInitialCloudReconcile(uid: String, displayName: String) {
        if (Firebase.auth.currentUser?.uid != uid) return
        _uiState.update { it.copy(isCloudSyncing = true) }
        val result = cloudRepo.loadCheckpoint(uid)
        result.fold(
            onSuccess = { snapshot ->
                val local = _uiState.value.progress
                if (snapshot == null) {
                    val saveRes = cloudRepo.saveCheckpoint(uid, local, displayName)
                    _uiState.update {
                        it.copy(
                            isCloudSyncing = false,
                            lastCloudSyncMs = saveRes.getOrNull() ?: it.lastCloudSyncMs
                        )
                    }
                    showToast("☁️ Checkpoint inițial salvat în Cloud!")
                } else {
                    val cloud = snapshot.progress
                    if (isCloudAheadOfLocal(cloud, local)) {
                        applyRestoredProgress(cloud, snapshot.updatedAtMs)
                        showToast("☁️ Progres restaurat din Cloud (Nivel ${cloud.currentLevel})!")
                    } else if (isCloudAheadOfLocal(local, cloud)) {
                        val saveRes = cloudRepo.saveCheckpoint(uid, local, displayName)
                        _uiState.update {
                            it.copy(
                                isCloudSyncing = false,
                                lastCloudSyncMs = saveRes.getOrNull() ?: snapshot.updatedAtMs
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCloudSyncing = false,
                                lastCloudSyncMs = snapshot.updatedAtMs.takeIf { ts -> ts > 0L }
                            )
                        }
                    }
                }
            },
            onFailure = {
                _uiState.update { it.copy(isCloudSyncing = false) }
            }
        )
    }

    private fun startCloudCheckpointObserver(uid: String) {
        cloudObserverJob?.cancel()
        cloudObserverJob = viewModelScope.launch {
            cloudRepo.observeCheckpoint(uid)
                .catch { e ->
                    Log.w("ParkourVM", "Cloud checkpoint listener stopped: ${e.message}")
                }
                .collect { snapshot ->
                    if (snapshot != null && snapshot.updatedAtMs > 0L) {
                        _uiState.update { it.copy(lastCloudSyncMs = snapshot.updatedAtMs) }
                    }
                }
        }
    }

    private fun isCloudAheadOfLocal(candidate: PersistentProgress, baseline: PersistentProgress): Boolean {
        if (candidate.unlockedLevel != baseline.unlockedLevel) {
            return candidate.unlockedLevel > baseline.unlockedLevel
        }
        val candScore = candidate.completedLevels.size * 1000L +
            candidate.ownedHeroes.size * 200L +
            candidate.passXP +
            candidate.coins +
            candidate.diamonds * 20L +
            if (candidate.checkpointLevel == candidate.currentLevel && candidate.checkpointX > 70f) 10L else 0L
        val baseScore = baseline.completedLevels.size * 1000L +
            baseline.ownedHeroes.size * 200L +
            baseline.passXP +
            baseline.coins +
            baseline.diamonds * 20L +
            if (baseline.checkpointLevel == baseline.currentLevel && baseline.checkpointX > 70f) 10L else 0L
        return candScore > baseScore
    }

    private suspend fun applyRestoredProgress(restored: PersistentProgress, syncMs: Long) {
        repo.saveProgress(restored)
        _uiState.update {
            it.copy(
                progress = restored,
                isCloudSyncing = false,
                lastCloudSyncMs = syncMs.takeIf { ts -> ts > 0L } ?: System.currentTimeMillis()
            )
        }
        loadEngineWithProgressCheckpoint(restored)
        refreshTimers()
    }

    private fun loadEngineWithProgressCheckpoint(p: PersistentProgress) {
        val useSavedCp = p.checkpointLevel == p.currentLevel && p.checkpointX > 70f
        engine.loadLevel(
            lvlNum = p.currentLevel,
            heroId = p.equippedHero,
            savedCheckpointX = if (useSavedCp) p.checkpointX else null,
            savedCheckpointY = if (useSavedCp) p.checkpointY else null
        )
    }

    fun stepFrame(viewW: Float, viewH: Float, dtScale: Float) {
        engine.stepSimulation(viewW, viewH, dtScale)
        val ps = engine.powerState
        _uiState.update {
            it.copy(
                frameTick = it.frameTick + 1L,
                abilityCdLeft = ps.cdLeft,
                abilityMaxCd = ps.maxCd,
                activeAbilityKind = ps.kind,
                activeAbilityTimeLeft = ps.activeLeft,
                solarShieldActive = ps.solarShield,
                inSantaVillage = engine.lvlData.dimension == WorldDimension.SANTAS_VILLAGE,
                nearTreeDoor = engine.nearTreeDoor,
                nearReturnDoor = engine.nearReturnDoor,
                nearSanta = engine.nearSanta,
                grinchBossHp = engine.lvlData.grinchBoss?.takeIf { b -> b.alive }?.hp,
                grinchBossMaxHp = engine.lvlData.grinchBoss?.maxHp ?: 2000,
                grinchBossName = engine.lvlData.grinchBoss?.name ?: "BOSS GRINCH"
            )
        }
    }

    fun openTreeDoor() {
        engine.triggerTreeDoor()
    }

    fun returnFromVillage() {
        engine.returnToMainLevel()
    }

    fun openSantaQuests() {
        audio.playSantaSleigh()
        openModal(ActiveModal.SANTA_QUESTS)
    }

    fun loadLevel(lvlNum: Int, clearCheckpoint: Boolean = false) {
        val safe = lvlNum.coerceIn(1, 999_999_999)
        val cur = _uiState.value.progress
        val levelChanged = safe != cur.currentLevel
        val resetCp = clearCheckpoint || levelChanged
        val updated = cur.copy(
            currentLevel = safe,
            unlockedLevel = max(cur.unlockedLevel, safe),
            checkpointLevel = if (resetCp) 0 else cur.checkpointLevel,
            checkpointX = if (resetCp) 0f else cur.checkpointX,
            checkpointY = if (resetCp) 0f else cur.checkpointY
        )
        commitProgress(updated, immediateCloudBackup = true)
        loadEngineWithProgressCheckpoint(updated)
        _uiState.update {
            it.copy(
                victorySummary = null,
                activeModal = ActiveModal.NONE,
                grinchBossHp = engine.lvlData.grinchBoss?.takeIf { b -> b.alive }?.hp,
                grinchBossMaxHp = engine.lvlData.grinchBoss?.maxHp ?: 2000,
                grinchBossName = engine.lvlData.grinchBoss?.name ?: "BOSS GRINCH"
            )
        }
        if (levelChanged) {
            showToast("🏙️ Nivelul $safe: ${engine.lvlData.theme.title}!")
        }
    }

    fun restartCurrentLevel() {
        loadLevel(_uiState.value.progress.currentLevel, clearCheckpoint = true)
        showToast("Nivel reînceput!")
    }

    fun saveCheckpointNow() {
        audio.playCheckpoint()
        val (cpX, cpY) = engine.captureCheckpointPosition()
        val cur = _uiState.value.progress
        val updated = cur.copy(
            checkpointLevel = cur.currentLevel,
            checkpointX = cpX,
            checkpointY = cpY
        )
        commitProgress(updated, immediateCloudBackup = true)
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            showToast("☁️ Checkpoint salvat local și sincronizat cu Google!")
        } else {
            showToast("💾 Checkpoint salvat local la Nivelul ${updated.currentLevel}!")
        }
    }

    fun pushProgressToCloud() {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudSyncing = true) }
            val displayName = user.displayName.orEmpty().ifBlank { "Jucător Google" }
            val res = cloudRepo.saveCheckpoint(user.uid, _uiState.value.progress, displayName)
            res.fold(
                onSuccess = { ts ->
                    _uiState.update { it.copy(isCloudSyncing = false, lastCloudSyncMs = ts) }
                    showToast("☁️ Progresul a fost trimis în Cloud!")
                },
                onFailure = {
                    _uiState.update { it.copy(isCloudSyncing = false) }
                    showToast("⚠️ Nu s-a putut salva în Cloud. Verifică internetul!")
                }
            )
        }
    }

    fun pullProgressFromCloud() {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudSyncing = true) }
            val res = cloudRepo.loadCheckpoint(user.uid)
            res.fold(
                onSuccess = { snap ->
                    if (snap == null) {
                        _uiState.update { it.copy(isCloudSyncing = false) }
                        showToast("ℹ️ Nu există încă un Checkpoint salvat în Cloud.")
                    } else {
                        applyRestoredProgress(snap.progress, snap.updatedAtMs)
                        showToast("🔄 Checkpoint restaurat: Nivelul ${snap.progress.currentLevel}!")
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isCloudSyncing = false) }
                    showToast("⚠️ Eroare la citirea Checkpoint-ului din Cloud.")
                }
            )
        }
    }

    fun setAuthLoading(loading: Boolean) {
        _uiState.update { it.copy(isAuthLoading = loading, authErrorMessage = if (loading) null else it.authErrorMessage) }
    }

    fun setAuthError(message: String?) {
        _uiState.update { it.copy(isAuthLoading = false, authErrorMessage = message) }
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                Firebase.auth.signInWithCredential(credential).awaitResult()
                _uiState.update { it.copy(isAuthLoading = false, authErrorMessage = null) }
                audio.playWin()
                showToast("✅ Conectat cu contul Google!")
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.e("Auth", "Firebase Google signInWithCredential failed", e)
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = e.localizedMessage ?: "Autentificarea Google a eșuat."
                    )
                }
            }
        }
    }

    fun signOut(credentialManager: CredentialManager) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                cloudObserverJob?.cancel()
                cloudObserverJob = null
                cloudSyncJob?.cancel()
                cloudSyncJob = null
                activeUid = null
                Firebase.auth.signOut()
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w("Auth", "clearCredentialState warning: ${e.message}")
                }
            }
            _uiState.update {
                it.copy(
                    isSignedIn = false,
                    userDisplayName = null,
                    userEmail = null,
                    isAuthLoading = false,
                    isCloudSyncing = false,
                    lastCloudSyncMs = null,
                    authErrorMessage = null
                )
            }
            showToast("Te-ai deconectat din contul Google.")
        }
    }

    fun openModal(modal: ActiveModal) {
        _uiState.update { it.copy(activeModal = modal) }
    }

    fun closeModal() {
        _uiState.update { it.copy(activeModal = ActiveModal.NONE) }
    }

    fun setShopFilter(rarity: HeroRarity?) {
        _uiState.update { it.copy(shopRarityFilter = rarity) }
    }

    fun toggleSound() {
        val next = !_uiState.value.isMuted
        audio.muted = next
        _uiState.update { it.copy(isMuted = next) }
        showToast(if (next) "Sunet Oprit" else "Sunet Pornit")
    }

    fun startGameFromEntrance() {
        audio.playClick()
        _uiState.update { it.copy(showEntranceMenu = false, activeModal = ActiveModal.NONE) }
    }

    fun returnToEntranceMenu() {
        audio.playClick()
        _uiState.update { it.copy(showEntranceMenu = true, activeModal = ActiveModal.NONE) }
    }

    fun toggleTouchEffects() {
        val next = !_uiState.value.touchEffectsEnabled
        audio.playClick()
        _uiState.update { it.copy(touchEffectsEnabled = next) }
        showToast(if (next) "✨ Efecte de atingere activate!" else "Efecte de atingere dezactivate")
    }

    fun toggleLandscapeLock() {
        val next = !_uiState.value.isLandscapeLocked
        audio.playClick()
        _uiState.update { it.copy(isLandscapeLocked = next) }
        showToast(if (next) "Mod Orizontal (Landscape) activat! 🔄" else "Mod Normal activat!")
    }

    fun claimDailyBonus() {
        val p = _uiState.value.progress
        val rem = max(0L, DAY_MILLIS - (System.currentTimeMillis() - p.lastDailyTs))
        if (rem > 0L) {
            showToast("Ai colectat deja azi! Revino în ${formatTimer(rem)} ⏳")
            return
        }
        val updated = p.copy(
            lastDailyTs = System.currentTimeMillis(),
            coins = p.coins + 50
        )
        commitProgress(updated, immediateCloudBackup = true)
        audio.playCoin()
        refreshTimers()
        showToast("🎁 Câștig Zilnic colectat: +50 🪙!")
    }

    fun claimStandardQuest(q: QuestDef) {
        val p = _uiState.value.progress
        val curVal = when (q.statKey) {
            "levelsDone" -> p.stats.levelsDone
            "intrudersKilled" -> p.stats.intrudersKilled
            "coinsGot" -> p.stats.coinsGot
            "jumpsMade" -> p.stats.jumpsMade
            else -> 0
        }
        if (curVal < q.target) return

        val nextStats = when (q.statKey) {
            "levelsDone" -> p.stats.copy(levelsDone = max(0, p.stats.levelsDone - q.target))
            "intrudersKilled" -> p.stats.copy(intrudersKilled = max(0, p.stats.intrudersKilled - q.target))
            "coinsGot" -> p.stats.copy(coinsGot = max(0, p.stats.coinsGot - q.target))
            "jumpsMade" -> p.stats.copy(jumpsMade = max(0, p.stats.jumpsMade - q.target))
            else -> p.stats
        }
        val updated = p.copy(
            stats = nextStats,
            coins = p.coins + q.rewardCoins,
            passXP = p.passXP + 50
        )
        commitProgress(updated, immediateCloudBackup = true)
        audio.playCoin()
        showToast("Ai primit +${q.rewardCoins} 🪙 pentru Premium Pass! 🪙")
    }

    fun claimBonusQuest() {
        val p = _uiState.value.progress
        val rem = max(0L, DAY_MILLIS - (System.currentTimeMillis() - p.lastBonusQuestTs))
        val done = p.bonusStats.levelsDone >= BonusQuestDef.TARGET_LEVELS &&
            p.bonusStats.intrudersKilled >= BonusQuestDef.TARGET_INTRUDERS &&
            p.bonusStats.coinsGot >= BonusQuestDef.TARGET_COINS
        if (rem > 0L || !done) return

        val updated = p.copy(
            lastBonusQuestTs = System.currentTimeMillis(),
            bonusStats = BonusStats(),
            coins = p.coins + BonusQuestDef.REWARD_COINS,
            passXP = p.passXP + 150
        )
        commitProgress(updated, immediateCloudBackup = true)
        audio.playWin()
        refreshTimers()
        showToast("🌟 INCREDIBIL! Ai revendicat +${BonusQuestDef.REWARD_COINS} 🪙 din Questul Bonus!")
    }

    fun claimSantaQuest(q: SantaQuestDef) {
        val p = _uiState.value.progress
        if (q.id in p.santaStats.claimedSantaQuests) return
        val curVal = when (q.statKey) {
            "giftsDelivered" -> p.santaStats.giftsDelivered
            "housesVisited" -> p.santaStats.housesVisited
            "bubbleJumps" -> p.santaStats.bubbleJumps
            "sleighCoins" -> p.santaStats.sleighCoins
            else -> 0
        }
        if (curVal < q.target) return

        val updated = p.copy(
            coins = p.coins + q.rewardCoins,
            passXP = p.passXP + 75,
            santaStats = p.santaStats.copy(
                claimedSantaQuests = p.santaStats.claimedSantaQuests + q.id
            )
        )
        commitProgress(updated, immediateCloudBackup = true)
        audio.playSantaSleigh()
        showToast("🎅 Moș Crăciun te-a răsplătit: +${q.rewardCoins} 🪙 și +75 XP!")
    }

    fun claimSantaGrandSleighBonus() {
        val p = _uiState.value.progress
        if (p.santaStats.grandSleighClaimed) return
        val allClaimed = SANTA_QUESTS.all { it.id in p.santaStats.claimedSantaQuests }
        if (!allClaimed) return

        val updated = p.copy(
            coins = p.coins + SantaGrandSleighBonus.REWARD_COINS,
            passXP = p.passXP + SantaGrandSleighBonus.REWARD_XP,
            santaStats = p.santaStats.copy(grandSleighClaimed = true)
        )
        commitProgress(updated, immediateCloudBackup = true)
        audio.playWin()
        showToast("🛷 HO HO HO! Marea Binecuvântare a Săniei: +${SantaGrandSleighBonus.REWARD_COINS} 🪙 și +${SantaGrandSleighBonus.REWARD_XP} XP!")
    }

    fun equipHero(hero: HeroMeta) {
        val p = _uiState.value.progress
        if (hero.id !in p.ownedHeroes) return
        val updated = p.copy(equippedHero = hero.id)
        commitProgress(updated, immediateCloudBackup = true)
        syncHeroInEngine(hero.id)
        audio.playClick()
        showToast("Ai echipat: ${hero.name}!")
    }

    fun buyHeroWithCoins(hero: HeroMeta) {
        val p = _uiState.value.progress
        if (p.coins < hero.price) {
            showToast("Ai nevoie de ${hero.price} monede!")
            return
        }
        val updated = p.copy(
            coins = p.coins - hero.price,
            ownedHeroes = p.ownedHeroes + hero.id,
            equippedHero = hero.id
        )
        commitProgress(updated, immediateCloudBackup = true)
        syncHeroInEngine(hero.id)
        audio.playClick()
        showToast("Ai deblocat ${hero.name}! 🎉")
    }

    fun buyHeroWithDiamonds(hero: HeroMeta) {
        val gp = hero.gemPrice ?: return
        val p = _uiState.value.progress
        if (p.diamonds < gp) {
            showToast("Ai nevoie de $gp diamante!")
            return
        }
        val updated = p.copy(
            diamonds = p.diamonds - gp,
            ownedHeroes = p.ownedHeroes + hero.id,
            equippedHero = hero.id
        )
        commitProgress(updated, immediateCloudBackup = true)
        syncHeroInEngine(hero.id)
        audio.playDiamond()
        showToast("Ai deblocat ${hero.name} cu 💎! 🔥")
    }

    fun unlockPremiumPass() {
        val p = _uiState.value.progress
        if (p.premiumUnlocked) return
        if (p.coins < PREMIUM_COST_COINS) {
            val missing = PREMIUM_COST_COINS - p.coins
            showToast("Ai nevoie de $PREMIUM_COST_COINS 🪙 din Questuri (îți mai lipsesc $missing 🪙)!")
            return
        }
        val updated = p.copy(
            coins = p.coins - PREMIUM_COST_COINS,
            premiumUnlocked = true
        )
        commitProgress(updated, immediateCloudBackup = true)
        audio.playWin()
        showToast("👑 Ai deblocat PREMIUM PASS! Revendică cei 5 Eroi Exclusivi!")
    }

    fun claimPassSlot(item: SeasonTier, isPremiumRow: Boolean) {
        val p = _uiState.value.progress
        val tierReached = min(15, p.passXP / 100 + 1)

        if (isPremiumRow) {
            val taken = item.tier in p.claimedPrem
            val premRew = item.premReward
            if (taken && premRew is PassReward.Hero) {
                val updated = p.copy(equippedHero = premRew.heroId)
                commitProgress(updated, immediateCloudBackup = true)
                syncHeroInEngine(premRew.heroId)
                audio.playClick()
                showToast("Ai echipat eroul din Pass: ${premRew.text}! ⚡")
                return
            }
            if (!p.premiumUnlocked) {
                showToast("Deblochează mai întâi Premium Pass cu $PREMIUM_COST_COINS 🪙 din Questuri!")
                return
            }
            if (item.tier > tierReached) {
                showToast("Avansează până la Tier ${item.tier} în Pass!")
                return
            }
            if (taken) return
            val looted = applyPassLoot(p.copy(claimedPrem = p.claimedPrem + item.tier), premRew)
            commitProgress(looted, immediateCloudBackup = true)
        } else {
            if (item.tier > tierReached) {
                showToast("Avansează până la Tier ${item.tier} în Pass!")
                return
            }
            if (item.tier in p.claimedFree) return
            val looted = applyPassLoot(p.copy(claimedFree = p.claimedFree + item.tier), item.freeReward)
            commitProgress(looted, immediateCloudBackup = true)
        }
    }

    private fun applyPassLoot(p: PersistentProgress, loot: PassReward): PersistentProgress {
        audio.playClick()
        return when (loot) {
            is PassReward.Coins -> {
                showToast("Ai primit +${loot.amount} Monede! 🪙")
                p.copy(coins = p.coins + loot.amount)
            }
            is PassReward.Diamonds -> {
                showToast("Ai primit +${loot.amount} Diamante! 💎")
                p.copy(diamonds = p.diamonds + loot.amount)
            }
            is PassReward.Galactic -> {
                showToast("Ai primit +${loot.amount} Nestemate Galactice! ✨")
                p.copy(galactic = p.galactic + loot.amount)
            }
            is PassReward.Hero -> {
                syncHeroInEngine(loot.heroId)
                showToast("🏆 Ai revendicat și echipat eroul: ${loot.text}!")
                p.copy(
                    ownedHeroes = p.ownedHeroes + loot.heroId,
                    equippedHero = loot.heroId
                )
            }
        }
    }

    private fun syncHeroInEngine(heroId: String) {
        engine.equippedHeroId = heroId
        val meta = findHeroMeta(heroId)
        engine.powerState.solarShield = (meta.id == "phoenix_festiv")
    }

    private fun refreshTimers() {
        val now = System.currentTimeMillis()
        val p = _uiState.value.progress
        _uiState.update {
            it.copy(
                dailyRemainingMs = max(0L, DAY_MILLIS - (now - p.lastDailyTs)),
                bonusQuestRemainingMs = max(0L, DAY_MILLIS - (now - p.lastBonusQuestTs))
            )
        }
    }

    private inline fun updateProgress(
        immediateCloudBackup: Boolean = false,
        transform: (PersistentProgress) -> PersistentProgress
    ) {
        val next = transform(_uiState.value.progress)
        commitProgress(next, immediateCloudBackup)
    }

    private fun commitProgress(next: PersistentProgress, immediateCloudBackup: Boolean = false) {
        _uiState.update { it.copy(progress = next) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(120L)
            repo.saveProgress(next)
        }

        val uid = activeUid
        val user = Firebase.auth.currentUser
        if (uid != null && user != null && user.uid == uid) {
            cloudSyncJob?.cancel()
            cloudSyncJob = viewModelScope.launch {
                if (!immediateCloudBackup) delay(2500L)
                val displayName = user.displayName.orEmpty().ifBlank { "Jucător Google" }
                val res = cloudRepo.saveCheckpoint(uid, next, displayName)
                res.onSuccess { ts ->
                    _uiState.update { it.copy(lastCloudSyncMs = ts) }
                }
            }
        }
    }

    fun showToast(msg: String) {
        toastJob?.cancel()
        _uiState.update { it.copy(toastMessage = msg) }
        toastJob = viewModelScope.launch {
            delay(2500L)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                val dbId = appContext.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(dbId)
                ParkourViewModel(
                    repo = ProgressRepository(appContext),
                    cloudRepo = CloudCheckpointRepository(firestore)
                )
            }
        }

        fun formatTimer(ms: Long): String {
            val s = max(0L, (ms + 999L) / 1000L)
            val hh = s / 3600L
            val mm = (s % 3600L) / 60L
            val ss = s % 60L
            return String.format("%02d:%02d:%02d", hh, mm, ss)
        }
    }
}
