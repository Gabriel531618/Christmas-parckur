package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.BonusStats
import com.example.model.GameStats
import com.example.model.HEROES_CATALOG
import com.example.model.PersistentProgress
import com.example.model.SantaQuestStats
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.parkourDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "parkour_ultra_cosmic_v2_5"
)

class ProgressRepository(private val context: Context) {
    private val keyState = stringPreferencesKey("progress_json")

    suspend fun loadProgress(): PersistentProgress {
        return try {
            val prefs = context.parkourDataStore.data.first()
            val raw = prefs[keyState] ?: return PersistentProgress()
            decode(raw)
        } catch (_: Exception) {
            PersistentProgress()
        }
    }

    suspend fun saveProgress(progress: PersistentProgress) {
        try {
            val encoded = encode(progress)
            context.parkourDataStore.edit { prefs ->
                prefs[keyState] = encoded
            }
        } catch (_: Exception) {
        }
    }

    private fun encode(p: PersistentProgress): String {
        val root = JSONObject()
        root.put("coins", p.coins)
        root.put("diamonds", p.diamonds)
        root.put("galactic", p.galactic)
        root.put("currentLevel", p.currentLevel)
        root.put("unlockedLevel", p.unlockedLevel)
        root.put("completedLevels", JSONArray(p.completedLevels.toList()))
        root.put("ownedHeroes", JSONArray(p.ownedHeroes.toList()))
        root.put("equippedHero", p.equippedHero)
        root.put("passXP", p.passXP)
        root.put("premiumUnlocked", p.premiumUnlocked)
        root.put("claimedFree", JSONArray(p.claimedFree.toList()))
        root.put("claimedPrem", JSONArray(p.claimedPrem.toList()))
        root.put("lastDailyTs", p.lastDailyTs)
        root.put("lastBonusQuestTs", p.lastBonusQuestTs)
        root.put("checkpointLevel", p.checkpointLevel)
        root.put("checkpointX", p.checkpointX.toDouble())
        root.put("checkpointY", p.checkpointY.toDouble())

        val st = JSONObject()
        st.put("levelsDone", p.stats.levelsDone)
        st.put("intrudersKilled", p.stats.intrudersKilled)
        st.put("coinsGot", p.stats.coinsGot)
        st.put("jumpsMade", p.stats.jumpsMade)
        root.put("stats", st)

        val bst = JSONObject()
        bst.put("levelsDone", p.bonusStats.levelsDone)
        bst.put("intrudersKilled", p.bonusStats.intrudersKilled)
        bst.put("coinsGot", p.bonusStats.coinsGot)
        root.put("bonusStats", bst)

        val sst = JSONObject()
        sst.put("giftsDelivered", p.santaStats.giftsDelivered)
        sst.put("housesVisited", p.santaStats.housesVisited)
        sst.put("bubbleJumps", p.santaStats.bubbleJumps)
        sst.put("sleighCoins", p.santaStats.sleighCoins)
        sst.put("claimedSantaQuests", JSONArray(p.santaStats.claimedSantaQuests.toList()))
        sst.put("grandSleighClaimed", p.santaStats.grandSleighClaimed)
        root.put("santaStats", sst)

        return root.toString()
    }

    private fun decode(raw: String): PersistentProgress {
        val root = JSONObject(raw)
        val completed = mutableSetOf<Int>()
        root.optJSONArray("completedLevels")?.let { arr ->
            for (i in 0 until arr.length()) completed.add(arr.optInt(i))
        }
        val owned = mutableSetOf("om_de_zapada")
        root.optJSONArray("ownedHeroes")?.let { arr ->
            for (i in 0 until arr.length()) {
                val id = arr.optString(i)
                if (id.isNotBlank() && HEROES_CATALOG.any { it.id == id }) owned.add(id)
            }
        }
        val claimedF = mutableSetOf<Int>()
        root.optJSONArray("claimedFree")?.let { arr ->
            for (i in 0 until arr.length()) claimedF.add(arr.optInt(i))
        }
        val claimedP = mutableSetOf<Int>()
        root.optJSONArray("claimedPrem")?.let { arr ->
            for (i in 0 until arr.length()) claimedP.add(arr.optInt(i))
        }

        val eq = root.optString("equippedHero", "om_de_zapada")
        val validEquipped = if (HEROES_CATALOG.any { it.id == eq }) eq else "om_de_zapada"

        val stObj = root.optJSONObject("stats")
        val stats = GameStats(
            levelsDone = stObj?.optInt("levelsDone", 0) ?: 0,
            intrudersKilled = stObj?.optInt("intrudersKilled", 0) ?: 0,
            coinsGot = stObj?.optInt("coinsGot", 0) ?: 0,
            jumpsMade = stObj?.optInt("jumpsMade", 0) ?: 0
        )

        val bstObj = root.optJSONObject("bonusStats")
        val bonusStats = BonusStats(
            levelsDone = bstObj?.optInt("levelsDone", 0) ?: 0,
            intrudersKilled = bstObj?.optInt("intrudersKilled", 0) ?: 0,
            coinsGot = bstObj?.optInt("coinsGot", 0) ?: 0
        )

        val sstObj = root.optJSONObject("santaStats")
        val claimedSanta = mutableSetOf<String>()
        sstObj?.optJSONArray("claimedSantaQuests")?.let { arr ->
            for (i in 0 until arr.length()) {
                val id = arr.optString(i)
                if (id.isNotBlank()) claimedSanta.add(id)
            }
        }
        val santaStats = SantaQuestStats(
            giftsDelivered = sstObj?.optInt("giftsDelivered", 0) ?: 0,
            housesVisited = sstObj?.optInt("housesVisited", 0) ?: 0,
            bubbleJumps = sstObj?.optInt("bubbleJumps", 0) ?: 0,
            sleighCoins = sstObj?.optInt("sleighCoins", 0) ?: 0,
            claimedSantaQuests = claimedSanta,
            grandSleighClaimed = sstObj?.optBoolean("grandSleighClaimed", false) ?: false
        )

        return PersistentProgress(
            coins = root.optInt("coins", 0),
            diamonds = root.optInt("diamonds", 0),
            galactic = root.optInt("galactic", 0),
            currentLevel = root.optInt("currentLevel", 1).coerceAtLeast(1),
            unlockedLevel = root.optInt("unlockedLevel", 1).coerceAtLeast(1),
            completedLevels = completed,
            ownedHeroes = owned,
            equippedHero = validEquipped,
            passXP = root.optInt("passXP", 0),
            premiumUnlocked = root.optBoolean("premiumUnlocked", false),
            claimedFree = claimedF,
            claimedPrem = claimedP,
            lastDailyTs = root.optLong("lastDailyTs", 0L),
            lastBonusQuestTs = root.optLong("lastBonusQuestTs", 0L),
            checkpointLevel = root.optInt("checkpointLevel", 0).coerceAtLeast(0),
            checkpointX = root.optDouble("checkpointX", 0.0).toFloat().coerceAtLeast(0f),
            checkpointY = root.optDouble("checkpointY", 0.0).toFloat(),
            stats = stats,
            bonusStats = bonusStats,
            santaStats = santaStats
        )
    }
}
