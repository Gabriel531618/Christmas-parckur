package com.example

import com.example.audio.CosmicAudioEngine
import com.example.game.EngineCallbacks
import com.example.game.ParkourEngine
import com.example.game.PlatformKind
import com.example.game.WorldDimension
import com.example.game.buildLevel
import com.example.game.buildSantaVillageLevel
import com.example.model.AbilityType
import com.example.model.BonusQuestDef
import com.example.model.HEROES_CATALOG
import com.example.model.HeroRarity
import com.example.model.PREMIUM_COST_COINS
import com.example.model.PassReward
import com.example.model.SANTA_QUESTS
import com.example.model.SEASON_TIERS
import com.example.model.STANDARD_QUESTS
import com.example.model.SantaGrandSleighBonus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun heroCatalog_has24HeroesAnd8DivineAbilities() {
        assertEquals(24, HEROES_CATALOG.size)
        val divines = HEROES_CATALOG.filter { it.rarity == HeroRarity.DIVIN }
        assertEquals(8, divines.size)
        assertTrue(divines.all { it.abilityType != null && it.cooldownSec > 0f })
        assertEquals(8, divines.mapNotNull { it.abilityType }.toSet().size)
    }

    @Test
    fun quests_awardCoinsOnlyForPremiumPass() {
        assertTrue(STANDARD_QUESTS.all { it.rewardCoins > 0 })
        assertTrue(SANTA_QUESTS.all { it.rewardCoins > 0 })
        assertTrue(BonusQuestDef.REWARD_COINS > 0)
        assertTrue(SantaGrandSleighBonus.REWARD_COINS > 0)
        assertTrue(PREMIUM_COST_COINS > 0)
    }

    @Test
    fun seasonPass_has15TiersWith5ExclusiveHeroes() {
        assertEquals(15, SEASON_TIERS.size)
        val passHeroes = SEASON_TIERS.mapNotNull { it.premReward as? PassReward.Hero }
        assertEquals(5, passHeroes.size)
    }

    @Test
    fun levelGenerator_buildsValidStartAndFinish() {
        val lvl1 = buildLevel(1)
        assertEquals(WorldDimension.MAIN_LEVEL, lvl1.dimension)
        assertEquals(PlatformKind.CYBER_SOLID, lvl1.platforms.first().kind)
        assertEquals(PlatformKind.FINISH_ZONE, lvl1.platforms.last().kind)
        assertTrue(lvl1.coins.isNotEmpty())
        assertTrue(lvl1.elfGoblins.isNotEmpty())
        assertTrue(lvl1.lasers.isNotEmpty())
        assertTrue(lvl1.checkpoints.isNotEmpty())
        assertTrue(lvl1.startTree.x > 0f)
        assertTrue(lvl1.startTree.hasDoor)
    }

    @Test
    fun santaVillage_containsSnowHousesSantaSleighAndQuests() {
        val village = buildSantaVillageLevel(1)
        assertEquals(WorldDimension.SANTAS_VILLAGE, village.dimension)
        assertTrue(village.houses.size >= 7)
        assertTrue(village.houses.all { it.lightColors.isNotEmpty() })
        assertTrue(village.giftBoxes.size >= 10)
        assertNotNull(village.santaSleigh)
        assertNotNull(village.returnDoor)
        assertEquals(4, SANTA_QUESTS.size)
    }

    @Test
    fun treeDoor_andNewDivineAbilities_workCorrectly() {
        val audio = CosmicAudioEngine().apply { muted = true }
        var bonusCoinsAdded = 0
        val callbacks = object : EngineCallbacks {
            override fun onCoinsCollected(coinsDelta: Int, xpDelta: Int) {
                bonusCoinsAdded += coinsDelta
            }
            override fun onDiamondCollected(diamondsDelta: Int, xpDelta: Int) {}
            override fun onIntruderStomped(coinsDelta: Int) {}
            override fun onStatIncrement(key: String, amount: Int) {}
            override fun onLevelCompleted(lvl: Int, runCoins: Int, runDiamonds: Int) {}
            override fun onToast(message: String) {}
            override fun onOpenSantaQuests() {}
        }
        val engine = ParkourEngine(audio, callbacks)
        engine.loadLevel(1, "jack_frost_divin")
        assertEquals(WorldDimension.MAIN_LEVEL, engine.lvlData.dimension)

        engine.useDivinePower()
        assertEquals(AbilityType.BLIZZARD, engine.powerState.kind)

        engine.loadLevel(1, "craciunita_stelara")
        engine.useDivinePower()
        assertEquals(AbilityType.PRESENT_BARRAGE, engine.powerState.kind)
        assertTrue(bonusCoinsAdded >= 3)

        engine.triggerTreeDoor()
        repeat(45) {
            engine.stepSimulation(800f, 480f, 1f)
        }
        assertEquals(WorldDimension.SANTAS_VILLAGE, engine.lvlData.dimension)

        engine.returnToMainLevel()
        assertEquals(WorldDimension.MAIN_LEVEL, engine.lvlData.dimension)
    }

    @Test
    fun finishDoor_opensWithAnimation_andLevel20GrinchBossRequiresHeadStomps() {
        val audio = CosmicAudioEngine().apply { muted = true }
        var wonLevel = -1
        val callbacks = object : EngineCallbacks {
            override fun onCoinsCollected(coinsDelta: Int, xpDelta: Int) {}
            override fun onDiamondCollected(diamondsDelta: Int, xpDelta: Int) {}
            override fun onIntruderStomped(coinsDelta: Int) {}
            override fun onStatIncrement(key: String, amount: Int) {}
            override fun onLevelCompleted(lvl: Int, runCoins: Int, runDiamonds: Int) {
                wonLevel = lvl
            }
            override fun onToast(message: String) {}
            override fun onOpenSantaQuests() {}
        }
        val engine = ParkourEngine(audio, callbacks)

        // Level 1 Finish Door opens smoothly before completing level
        engine.loadLevel(1, "om_de_zapada")
        assertEquals(0f, engine.finishDoorOpenProgress)
        val g1 = engine.lvlData.goal
        engine.heroObj.x = g1.x - engine.heroObj.w / 2f
        engine.heroObj.y = g1.y - engine.heroObj.h
        engine.stepSimulation(800f, 480f, 1f)
        assertTrue(engine.finishDoorOpenProgress > 0f)
        repeat(40) {
            if (!engine.isWon) engine.stepSimulation(800f, 480f, 1f)
        }
        assertTrue(engine.isWon)
        assertEquals(1, wonLevel)

        // Level 20 has 2,000 HP Grinch Boss & locks Finish Door until defeated by jumping on his head
        engine.loadLevel(20, "om_de_zapada")
        val boss = engine.lvlData.grinchBoss
        assertNotNull(boss)
        assertEquals(2000, boss!!.hp)
        assertEquals(2000, boss.maxHp)
        assertTrue(boss.alive)

        // Finish Door stays locked while Grinch Boss is alive
        val g20 = engine.lvlData.goal
        engine.heroObj.x = g20.x - engine.heroObj.w / 2f
        engine.heroObj.y = g20.y - engine.heroObj.h
        engine.stepSimulation(800f, 480f, 1f)
        assertTrue(!engine.isWon)
        assertEquals(0f, engine.finishDoorOpenProgress)

        // Jump on Grinch Boss's head repeatedly (8 head stomps x 250 HP = 2,000 HP)
        repeat(8) {
            boss.hurtCooldown = 0f
            engine.heroObj.x = boss.x + boss.w / 2f - engine.heroObj.w / 2f
            engine.heroObj.y = boss.y - engine.heroObj.h + 4f
            engine.heroObj.vy = 5f
            engine.stepSimulation(800f, 480f, 1f)
        }
        assertEquals(0, boss.hp)
        assertTrue(!boss.alive)

        // Now Finish Door opens and completes Level 20!
        engine.heroObj.x = g20.x - engine.heroObj.w / 2f
        engine.heroObj.y = g20.y - engine.heroObj.h
        engine.heroObj.vy = 0f
        repeat(45) {
            if (!engine.isWon) engine.stepSimulation(800f, 480f, 1f)
        }
        assertTrue(engine.isWon)
        assertEquals(20, wonLevel)
    }

    @Test
    fun firstHeroEffect_jumpSnowEffect_movingLasers_andLevel10Boss400Hp() {
        val firstHero = HEROES_CATALOG.first()
        assertEquals("om_de_zapada", firstHero.id)
        assertNotNull(firstHero.effectLabel)

        val audio = CosmicAudioEngine().apply { muted = true }
        var wonLevel = -1
        val callbacks = object : EngineCallbacks {
            override fun onCoinsCollected(coinsDelta: Int, xpDelta: Int) {}
            override fun onDiamondCollected(diamondsDelta: Int, xpDelta: Int) {}
            override fun onIntruderStomped(coinsDelta: Int) {}
            override fun onStatIncrement(key: String, amount: Int) {}
            override fun onLevelCompleted(lvl: Int, runCoins: Int, runDiamonds: Int) {
                wonLevel = lvl
            }
            override fun onToast(message: String) {}
            override fun onOpenSantaQuests() {}
        }
        val engine = ParkourEngine(audio, callbacks)
        engine.loadLevel(10, "om_de_zapada")

        // Jumping creates cool snow burst particles
        engine.heroObj.grounded = true
        engine.requestJump()
        engine.stepSimulation(800f, 480f, 1f)
        assertTrue(engine.jumpSnowflakes.isNotEmpty())

        // Moving lasers animate positions over simulation steps
        val laser = engine.lvlData.lasers.first()
        val initialX = laser.x
        val initialY = laser.y
        repeat(15) { engine.stepSimulation(800f, 480f, 1f) }
        assertTrue(laser.x != initialX || laser.y != initialY)

        // Level 10 Boss has 400 HP and locks Finish Door until defeated
        val boss10 = engine.lvlData.grinchBoss
        assertNotNull(boss10)
        assertEquals(400, boss10!!.hp)
        assertEquals(400, boss10.maxHp)
        assertTrue(boss10.alive)

        val g10 = engine.lvlData.goal
        engine.heroObj.x = g10.x - engine.heroObj.w / 2f
        engine.heroObj.y = g10.y - engine.heroObj.h
        engine.stepSimulation(800f, 480f, 1f)
        assertTrue(!engine.isWon)

        // 4 head stomps x 100 HP = 400 HP defeats Level 10 Boss
        repeat(4) {
            boss10.hurtCooldown = 0f
            engine.heroObj.x = boss10.x + boss10.w / 2f - engine.heroObj.w / 2f
            engine.heroObj.y = boss10.y - engine.heroObj.h + 4f
            engine.heroObj.vy = 5f
            engine.stepSimulation(800f, 480f, 1f)
        }
        assertEquals(0, boss10.hp)
        assertTrue(!boss10.alive)

        engine.heroObj.x = g10.x - engine.heroObj.w / 2f
        engine.heroObj.y = g10.y - engine.heroObj.h
        engine.heroObj.vy = 0f
        repeat(45) {
            if (!engine.isWon) engine.stepSimulation(800f, 480f, 1f)
        }
        assertTrue(engine.isWon)
        assertEquals(10, wonLevel)
    }

    @Test
    fun perLevelThemes_andFlyingSantaDropsGiftsAcrossSky() {
        val levels = (1..8).map { buildLevel(it) }
        val distinctStyles = levels.map { it.theme.facadeStyle }.toSet()
        val distinctTitles = levels.map { it.theme.title }.toSet()
        assertEquals(8, distinctStyles.size)
        assertEquals(8, distinctTitles.size)

        for (lvlData in levels) {
            assertTrue("Expected no background buildings in level ${lvlData.lvl}", lvlData.buildings.isEmpty())
            assertTrue("Expected light palette in level ${lvlData.lvl}", lvlData.theme.lightPalette.isNotEmpty())
        }

        val audio = CosmicAudioEngine().apply { muted = true }
        val callbacks = object : EngineCallbacks {
            override fun onCoinsCollected(coinsDelta: Int, xpDelta: Int) {}
            override fun onDiamondCollected(diamondsDelta: Int, xpDelta: Int) {}
            override fun onIntruderStomped(coinsDelta: Int) {}
            override fun onStatIncrement(key: String, amount: Int) {}
            override fun onLevelCompleted(lvl: Int, runCoins: Int, runDiamonds: Int) {}
            override fun onToast(message: String) {}
            override fun onOpenSantaQuests() {}
        }
        val engine = ParkourEngine(audio, callbacks)
        engine.loadLevel(1, "om_de_zapada")
        val initialSantaX = engine.skySantaX
        repeat(110) {
            engine.stepSimulation(800f, 480f, 1f)
        }
        assertTrue(engine.skySantaX > initialSantaX)
        assertTrue(engine.skyGiftDrops.isNotEmpty())
    }
}
