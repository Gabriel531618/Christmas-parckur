package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.audio.CosmicAudioEngine
import com.example.model.AbilityType
import com.example.model.HeroRarity
import com.example.model.findHeroMeta
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class FxParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    var life: Float,
    val decay: Float
)

data class BubbleParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val wobblePhase: Float,
    val tint: Color,
    var life: Float,
    val decay: Float
)

data class SnowflakeParticle(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedY: Float,
    val driftPhase: Float,
    val alpha: Float
)

data class JumpSnowflake(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    var rot: Float,
    val rotSpeed: Float,
    val color: Color,
    var life: Float,
    val decay: Float
)

data class ShockRing(
    val x: Float,
    val y: Float,
    var r: Float,
    val maxR: Float,
    val color: Color,
    var life: Float
)

data class PopText(
    val x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var life: Float
)

data class ShootingStar(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var life: Float
)

data class SkyGiftDrop(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rot: Float,
    val rotSpeed: Float,
    val boxColor: Color,
    val ribbonColor: Color,
    val hasParachute: Boolean,
    var life: Float
)

data class SkyTrailSpark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    var life: Float
)

data class CosmicStar(
    val x: Float,
    val y: Float,
    val r: Float,
    val phase: Float,
    val speed: Float,
    val color: Color
)

data class PowerState(
    var cdLeft: Float = 0f,
    var maxCd: Float = 4f,
    var activeLeft: Float = 0f,
    var kind: AbilityType? = null,
    var invuln: Float = 0f,
    var solarShield: Boolean = false
)

data class HeroBody(
    var x: Float = 65f,
    var y: Float = 260f,
    val w: Float = 38f,
    val h: Float = 38f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val speed: Float = 5.3f,
    val jumpVel: Float = -12.7f,
    val grav: Float = 0.58f,
    var grounded: Boolean = false,
    var coyote: Int = 0,
    var jumpBuf: Int = 0,
    var jumpsLeft: Int = 2,
    var dir: Int = 1,
    var sx: Float = 1f,
    var sy: Float = 1f,
    var walkTiltDeg: Float = 0f,
    var walkBobY: Float = 0f,
    var ridePlat: Platform? = null
)

data class GameInputState(
    var left: Boolean = false,
    var right: Boolean = false,
    var jumpHeld: Boolean = false
)

interface EngineCallbacks {
    fun onCoinsCollected(coinsDelta: Int, xpDelta: Int)
    fun onDiamondCollected(diamondsDelta: Int, xpDelta: Int)
    fun onIntruderStomped(coinsDelta: Int)
    fun onStatIncrement(key: String, amount: Int)
    fun onLevelCompleted(lvl: Int, runCoins: Int, runDiamonds: Int)
    fun onCheckpointActivated(lvl: Int, spawnX: Float, spawnY: Float) {}
    fun onToast(message: String)
    fun onOpenSantaQuests()
}

class ParkourEngine(
    private val audio: CosmicAudioEngine,
    private val callbacks: EngineCallbacks
) {
    val cosmicStars: List<CosmicStar> = List(75) { i ->
        CosmicStar(
            x = ((i * 193) % 2600).toFloat(),
            y = ((i * 79) % 900).toFloat(),
            r = if (i % 3 == 0) 2.2f else 1.3f,
            phase = i * 0.9f,
            speed = 0.06f + (i % 4) * 0.04f,
            color = when {
                i % 4 == 0 -> Color(0xFFFFE066)
                i % 3 == 0 -> Color(0xFFD0EBFF)
                else -> Color.White
            }
        )
    }

    val snowflakes: List<SnowflakeParticle> = List(70) { i ->
        SnowflakeParticle(
            x = ((i * 137) % 1200).toFloat(),
            y = ((i * 83) % 900).toFloat(),
            radius = 1.8f + (i % 4) * 1.1f,
            speedY = 0.8f + (i % 5) * 0.38f,
            driftPhase = i * 0.7f,
            alpha = 0.45f + (i % 4) * 0.14f
        )
    }

    var lvlData: LevelData = buildLevel(1)
        private set
    val heroObj = HeroBody()
    val powerState = PowerState()
    val input = GameInputState()

    val fxParticles = mutableListOf<FxParticle>()
    val jumpBubbles = mutableListOf<BubbleParticle>()
    val jumpSnowflakes = mutableListOf<JumpSnowflake>()
    val shockRings = mutableListOf<ShockRing>()
    val popTexts = mutableListOf<PopText>()
    val shootingStars = mutableListOf<ShootingStar>()
    val skyGiftDrops = mutableListOf<SkyGiftDrop>()
    val skyTrailSparks = mutableListOf<SkyTrailSpark>()

    var skySantaX: Float = -60f
        private set
    var skySantaY: Float = 110f
        private set
    var skySantaDir: Int = 1
        private set
    var skySantaThrowAnim: Float = 0f
        private set
    private var skyGiftSpawnTimer: Float = 0.6f

    var spawnX: Float = 65f
    var spawnY: Float = 260f
    var camX: Float = 0f
    var camY: Float = 0f
    var shakeTimer: Int = 0
    var clock: Float = 0f
    var isWon: Boolean = false
    var runCoins: Int = 0
    var runDiamonds: Int = 0
    var equippedHeroId: String = "om_de_zapada"

    // Tree Door & Santa's Village Dimension state
    var doorOpenProgress: Float = 0f
        private set
    private var doorOpeningActive: Boolean = false
    var nearTreeDoor: Boolean = false
        private set
    var nearReturnDoor: Boolean = false
        private set
    var nearSanta: Boolean = false
        private set

    // Finish Door opening animation state (replaces flag on all levels!)
    var finishDoorOpenProgress: Float = 0f
        private set
    private var finishDoorOpeningActive: Boolean = false
    var nearFinishDoor: Boolean = false
        private set
    private var bossLockToastCooldown: Float = 0f

    private var savedMainLevelData: LevelData? = null
    private var savedMainHeroX: Float = 65f
    private var savedMainHeroY: Float = 260f
    private var savedMainSpawnX: Float = 65f
    private var savedMainSpawnY: Float = 260f
    private var savedMainRunCoins: Int = 0
    private var savedMainRunDiamonds: Int = 0

    fun loadLevel(
        lvlNum: Int,
        heroId: String,
        savedCheckpointX: Float? = null,
        savedCheckpointY: Float? = null
    ) {
        equippedHeroId = heroId
        lvlData = buildLevel(lvlNum)
        savedMainLevelData = null
        doorOpenProgress = 0f
        doorOpeningActive = false
        finishDoorOpenProgress = 0f
        finishDoorOpeningActive = false
        nearFinishDoor = false
        bossLockToastCooldown = 0f
        nearTreeDoor = false
        nearReturnDoor = false
        nearSanta = false
        isWon = false
        runCoins = 0
        runDiamonds = 0
        fxParticles.clear()
        jumpBubbles.clear()
        jumpSnowflakes.clear()
        shockRings.clear()
        popTexts.clear()
        skyGiftDrops.clear()
        skyTrailSparks.clear()
        skySantaX = -40f
        skySantaY = 110f
        skySantaDir = 1
        skySantaThrowAnim = 0f
        skyGiftSpawnTimer = 0.55f
        shakeTimer = 0

        val hasSavedCp = savedCheckpointX != null && savedCheckpointY != null && savedCheckpointX > 70f
        spawnX = if (hasSavedCp) savedCheckpointX!! else 65f
        spawnY = if (hasSavedCp) savedCheckpointY!! else 260f

        if (hasSavedCp) {
            lvlData.checkpoints.forEach { cp ->
                if (cp.x - 19f <= spawnX + 14f) {
                    cp.activated = true
                }
            }
        }

        val meta = findHeroMeta(heroId)
        powerState.cdLeft = 0f
        powerState.activeLeft = 0f
        powerState.kind = null
        powerState.invuln = 0f
        powerState.solarShield = (meta.id == "phoenix_festiv")

        heroObj.x = spawnX
        heroObj.y = spawnY
        heroObj.vx = 0f
        heroObj.vy = 0f
        heroObj.grounded = false
        heroObj.coyote = 0
        heroObj.jumpBuf = 0
        heroObj.jumpsLeft = if (meta.id == "ingerul_zapezii") 3 else 2
        heroObj.dir = 1
        heroObj.sx = 1f
        heroObj.sy = 1f
        heroObj.walkTiltDeg = 0f
        heroObj.walkBobY = 0f
        heroObj.ridePlat = null
        camX = max(0f, spawnX - 180f)
        camY = 0f
    }

    fun captureCheckpointPosition(): Pair<Float, Float> {
        if (lvlData.dimension == WorldDimension.MAIN_LEVEL && heroObj.grounded && heroObj.x > 65f) {
            spawnX = heroObj.x
            spawnY = heroObj.y
        }
        return spawnX to spawnY
    }

    fun triggerTreeDoor() {
        if (lvlData.dimension != WorldDimension.MAIN_LEVEL || doorOpeningActive) return
        doorOpeningActive = true
        doorOpenProgress = 0.05f
        audio.playDoorPortal()
        val tree = lvlData.startTree
        emitGreenSplash(tree.x, tree.y - 36f, count = 26)
        emitRing(tree.x, tree.y - 34f, Color(0xFF51CF66), 88f)
        emitPopup(tree.x, tree.y - 90f, "✨ Dimensiunea lui Moș Crăciun!", Color(0xFF8CE99A))
    }

    fun returnToMainLevel() {
        if (lvlData.dimension != WorldDimension.SANTAS_VILLAGE) return
        val restored = savedMainLevelData ?: buildLevel(lvlData.lvl)
        lvlData = restored
        savedMainLevelData = null
        doorOpenProgress = 0f
        doorOpeningActive = false
        finishDoorOpenProgress = 0f
        finishDoorOpeningActive = false
        nearFinishDoor = false
        spawnX = savedMainSpawnX
        spawnY = savedMainSpawnY
        heroObj.x = savedMainHeroX
        heroObj.y = savedMainHeroY
        heroObj.vx = 0f
        heroObj.vy = 0f
        heroObj.ridePlat = null
        runCoins = savedMainRunCoins
        runDiamonds = savedMainRunDiamonds
        fxParticles.clear()
        jumpBubbles.clear()
        jumpSnowflakes.clear()
        shockRings.clear()
        popTexts.clear()
        audio.playDoorPortal()
        emitGreenSplash(lvlData.startTree.x, lvlData.startTree.y - 36f, count = 24)
        callbacks.onToast("🎄 Te-ai întors la Nivelul ${lvlData.lvl}!")
    }

    private fun enterSantaVillageDimension() {
        savedMainLevelData = lvlData
        savedMainHeroX = heroObj.x
        savedMainHeroY = heroObj.y
        savedMainSpawnX = spawnX
        savedMainSpawnY = spawnY
        savedMainRunCoins = runCoins
        savedMainRunDiamonds = runDiamonds

        lvlData = buildSantaVillageLevel(lvlData.lvl)
        doorOpenProgress = 0f
        doorOpeningActive = false
        spawnX = 155f
        spawnY = 260f
        heroObj.x = spawnX
        heroObj.y = spawnY
        heroObj.vx = 0f
        heroObj.vy = 0f
        heroObj.ridePlat = null
        camX = 0f
        camY = 0f
        fxParticles.clear()
        jumpBubbles.clear()
        jumpSnowflakes.clear()
        shockRings.clear()
        popTexts.clear()
        emitBurst(heroObj.x + 19f, heroObj.y + 19f, Color(0xFFFCC419), 24, 6f)
        emitRing(heroObj.x + 19f, heroObj.y + 19f, Color(0xFF51CF66), 72f)
        callbacks.onToast("🎅 Bun venit în Satul lui Moș Crăciun! Vizitează căsuțele și sania!")
    }

    fun requestJump() {
        heroObj.jumpBuf = 8
    }

    fun useDivinePower() {
        val meta = findHeroMeta(equippedHeroId)
        if (meta.rarity != HeroRarity.DIVIN) return
        if (powerState.cdLeft > 0f) {
            callbacks.onToast("Abilitatea se reîncarcă (${String.format("%.1f", powerState.cdLeft)}s)")
            return
        }
        val abType = meta.abilityType ?: return
        powerState.maxCd = if (meta.cooldownSec > 0f) meta.cooldownSec else 4f
        powerState.cdLeft = powerState.maxCd
        powerState.kind = abType
        audio.playAbility(abType)

        when (abType) {
            AbilityType.DASH -> {
                powerState.activeLeft = 0.25f
                powerState.invuln = 1.4f
                val oldX = heroObj.x
                heroObj.x = max(0f, heroObj.x + heroObj.dir * 165f)
                heroObj.vy = -3f
                val lo = min(oldX, heroObj.x) - 30f
                val hi = max(oldX, heroObj.x) + heroObj.w + 30f

                lvlData.lasers.forEach { laser ->
                    if (!laser.broken && laser.x + laser.w >= lo && laser.x <= hi && abs(laser.y - (heroObj.y + heroObj.h)) < 95f) {
                        laser.broken = true
                        emitBurst(laser.x + laser.w / 2f, laser.y - laser.h * 0.5f, Color(0xFFFFE066), 14, 5f)
                    }
                }
                lvlData.elfGoblins.forEach { en ->
                    if (en.alive && en.x + en.w >= lo && en.x <= hi && abs(en.y - heroObj.y) < 90f) {
                        en.alive = false
                        callbacks.onStatIncrement("intrudersKilled", 1)
                        emitBurst(en.x + en.w / 2f, en.y + en.h / 2f, Color(0xFF51CF66), 16, 6f)
                    }
                }
                emitBurst(oldX + 19f, heroObj.y + 19f, Color(0xFFFA5252), 18, 6f)
                emitBurst(heroObj.x + 19f, heroObj.y + 19f, Color(0xFF51CF66), 22, 7f)
                emitPopup(heroObj.x + 20f, heroObj.y - 14f, "🎅 SLEIGH DASH!", Color(0xFFFFE066))
            }

            AbilityType.SLOWMO -> {
                powerState.activeLeft = 4.0f
                heroObj.jumpsLeft = 3
                emitBurst(heroObj.x + 19f, heroObj.y + 19f, Color(0xFF74C0FC), 24, 5f)
                emitPopup(heroObj.x + 20f, heroObj.y - 14f, "❄️ FROST-SLOW + 3x SALT!", Color(0xFFD0EBFF))
            }

            AbilityType.SOLAR -> {
                powerState.activeLeft = 4.0f
                powerState.invuln = 4.0f
                powerState.solarShield = true
                heroObj.vy = -14.5f
                heroObj.jumpsLeft = 2
                emitBurst(heroObj.x + 19f, heroObj.y + 38f, Color(0xFFFF6B6B), 28, 7f)
                emitPopup(heroObj.x + 20f, heroObj.y - 14f, "🔥 FESTIVE FLIGHT!", Color(0xFFFFD43B))
            }

            AbilityType.ZEROG -> {
                powerState.activeLeft = 5.0f
                heroObj.vy = -5.5f
                emitBurst(heroObj.x + 19f, heroObj.y + 19f, Color(0xFF51CF66), 25, 6f)
                emitPopup(heroObj.x + 20f, heroObj.y - 14f, "🎄 ZERO-G + MAGNET!", Color(0xFF8CE99A))
            }

            AbilityType.BLIZZARD -> {
                powerState.activeLeft = 4.5f
                powerState.invuln = 1.6f
                val cx = heroObj.x + heroObj.w / 2f
                val cy = heroObj.y + heroObj.h / 2f
                lvlData.lasers.forEach { laser ->
                    if (!laser.broken && hypot(laser.x + laser.w / 2f - cx, laser.y - laser.h * 0.5f - cy) < 250f) {
                        laser.broken = true
                        emitBurst(laser.x + laser.w / 2f, laser.y - laser.h * 0.5f, Color(0xFF99E9F2), 14, 5.5f)
                    }
                }
                lvlData.elfGoblins.forEach { en ->
                    if (en.alive && hypot(en.x + en.w / 2f - cx, en.y + en.h / 2f - cy) < 250f) {
                        en.alive = false
                        runCoins += 2
                        callbacks.onIntruderStomped(2)
                        emitBurst(en.x + en.w / 2f, en.y + en.h / 2f, Color(0xFF74C0FC), 18, 6f)
                    }
                }
                emitRing(cx, cy, Color(0xFF3BC9DB), 120f)
                emitBurst(cx, cy, Color(0xFFD0EBFF), 32, 7.5f)
                emitPopup(cx, heroObj.y - 16f, "🌨️ VISCOL POLAR!", Color(0xFF99E9F2))
            }

            AbilityType.AURORA_NOVA -> {
                powerState.activeLeft = 4.5f
                powerState.invuln = 2.2f
                heroObj.jumpsLeft = 3
                heroObj.vy = -9.5f
                val cx = heroObj.x + heroObj.w / 2f
                val cy = heroObj.y + heroObj.h / 2f
                emitRing(cx, cy, Color(0xFF38D9A9), 115f)
                emitRing(cx, cy, Color(0xFF9775FA), 82f)
                emitBurst(cx, cy, Color(0xFF38D9A9), 22, 6.5f)
                emitBurst(cx, cy, Color(0xFF9775FA), 18, 6.0f)
                emitPopup(cx, heroObj.y - 16f, "🌌 EXPLOZIE AURORALĂ!", Color(0xFF63E6BE))
            }

            AbilityType.PRESENT_BARRAGE -> {
                powerState.activeLeft = 3.5f
                powerState.invuln = 2.0f
                val cx = heroObj.x + heroObj.w / 2f
                val cy = heroObj.y + heroObj.h / 2f
                val lo = min(cx, cx + heroObj.dir * 270f) - 40f
                val hi = max(cx, cx + heroObj.dir * 270f) + 40f
                lvlData.lasers.forEach { laser ->
                    if (!laser.broken && laser.x + laser.w >= lo && laser.x <= hi && abs(laser.y - (heroObj.y + heroObj.h)) < 120f) {
                        laser.broken = true
                        emitBurst(laser.x + laser.w / 2f, laser.y - laser.h * 0.5f, Color(0xFFFA5252), 16, 6f)
                    }
                }
                lvlData.elfGoblins.forEach { en ->
                    if (en.alive && en.x + en.w >= lo && en.x <= hi && abs(en.y - heroObj.y) < 120f) {
                        en.alive = false
                        runCoins += 2
                        callbacks.onIntruderStomped(2)
                        emitBurst(en.x + en.w / 2f, en.y + en.h / 2f, Color(0xFFFCC419), 20, 6.5f)
                    }
                }
                runCoins += 3
                callbacks.onCoinsCollected(3, 15)
                emitBurst(cx + heroObj.dir * 75f, cy, Color(0xFFFA5252), 20, 7f)
                emitBurst(cx + heroObj.dir * 150f, cy - 12f, Color(0xFF51CF66), 20, 7f)
                emitBurst(cx + heroObj.dir * 220f, cy, Color(0xFFFCC419), 20, 7f)
                emitRing(cx + heroObj.dir * 90f, cy, Color(0xFFFFD43B), 95f)
                emitPopup(cx, heroObj.y - 16f, "🎁 PLOAIE CADOURI (+3 🪙)!", Color(0xFFFFD43B))
            }

            AbilityType.CANDY_TURBO -> {
                powerState.activeLeft = 4.0f
                powerState.invuln = 4.0f
                val cx = heroObj.x + heroObj.w / 2f
                val cy = heroObj.y + heroObj.h / 2f
                emitRing(cx, cy, Color(0xFFE03131), 90f)
                emitBurst(cx, cy, Color(0xFFE03131), 16, 6.5f)
                emitBurst(cx, cy, Color.White, 16, 6.5f)
                emitBurst(cx, cy, Color(0xFFFCC419), 14, 6f)
                emitPopup(cx, heroObj.y - 16f, "🍬 TURBO ACADEA!", Color(0xFFFFE066))
            }
        }

        // Offensive Divine powers also chip the active Boss if nearby
        val boss = lvlData.grinchBoss
        if (boss != null && boss.alive &&
            (abType == AbilityType.DASH || abType == AbilityType.BLIZZARD || abType == AbilityType.PRESENT_BARRAGE || abType == AbilityType.AURORA_NOVA)
        ) {
            val distToBoss = hypot((boss.x + boss.w / 2f) - (heroObj.x + heroObj.w / 2f), (boss.y + boss.h / 2f) - (heroObj.y + heroObj.h / 2f))
            if (distToBoss < 280f && boss.hurtCooldown <= 0f) {
                val dmg = if (boss.kind == BossKind.ELF_COMMANDER_L10) 100 else 250
                damageGrinchBoss(boss, dmg, "⚡")
            }
        }
    }

    fun stepSimulation(viewW: Float, viewH: Float, dtScale: Float = 1f) {
        if (isWon) return
        val dt = 0.016f * dtScale
        clock += dt
        if (shakeTimer > 0) shakeTimer--

        // Update snowfall in wind
        val windX = sin(clock * 0.7f) * 0.45f + 0.55f
        for (sf in snowflakes) {
            sf.y += sf.speedY * dtScale
            sf.x += (windX + sin(clock * 1.8f + sf.driftPhase) * 0.4f) * dtScale
            if (sf.y > viewH + 20f) sf.y = -15f
            if (sf.x > viewW + 40f) sf.x = -20f
            if (sf.x < -30f) sf.x = viewW + 20f
        }

        updateSkySantaAndGifts(viewW, viewH, dt, dtScale)

        // Handle Tree Door opening animation & green splash emitter
        if (lvlData.dimension == WorldDimension.MAIN_LEVEL) {
            if (Random.nextFloat() < 0.22f * dtScale) {
                val tree = lvlData.startTree
                val angle = Random.nextFloat() * 2f * PI.toFloat()
                val rad = 18f + Random.nextFloat() * 58f
                fxParticles.add(
                    FxParticle(
                        x = tree.x + cos(angle) * rad,
                        y = tree.y - 70f - Random.nextFloat() * 140f,
                        vx = cos(angle) * 0.75f,
                        vy = -0.6f - Random.nextFloat() * 0.8f,
                        size = 4f + Random.nextFloat() * 4f,
                        color = if (Random.nextFloat() < 0.55f) Color(0xFF51CF66) else Color(0xFF8CE99A),
                        life = 0.85f,
                        decay = 0.024f
                    )
                )
            }
            if (doorOpeningActive) {
                doorOpenProgress = min(1f, doorOpenProgress + dt * 1.9f)
                if (doorOpenProgress >= 1f) {
                    enterSantaVillageDimension()
                    return
                }
            }
            if (finishDoorOpeningActive) {
                finishDoorOpenProgress = min(1f, finishDoorOpenProgress + dt * 2.1f)
                val g = lvlData.goal
                heroObj.vx = 0f
                heroObj.vy = 0f
                heroObj.x += (g.x - heroObj.w / 2f - heroObj.x) * 0.14f
                if (Random.nextFloat() < 0.75f * dtScale) {
                    emitBurst(
                        g.x + (Random.nextFloat() - 0.5f) * 28f,
                        g.y - Random.nextFloat() * g.height,
                        if (Random.nextFloat() < 0.5f) Color(0xFFFFD43B) else Color(0xFF51CF66),
                        2,
                        4.5f
                    )
                }
                updateTransientFx(dtScale)
                if (finishDoorOpenProgress >= 1f) {
                    finishDoorOpeningActive = false
                    finishLevel()
                }
                return
            }
        }

        if (Random.nextFloat() < 0.015f * dtScale) {
            shootingStars.add(
                ShootingStar(
                    x = camX + viewW * (0.3f + Random.nextFloat() * 0.8f),
                    y = Random.nextFloat() * viewH * 0.35f,
                    vx = -10f - Random.nextFloat() * 4f,
                    vy = 3.5f + Random.nextFloat() * 2.5f,
                    life = 1f
                )
            )
        }

        if (powerState.cdLeft > 0f) powerState.cdLeft = max(0f, powerState.cdLeft - dt)
        if (powerState.invuln > 0f) powerState.invuln = max(0f, powerState.invuln - dt)
        if (powerState.activeLeft > 0f) {
            powerState.activeLeft = max(0f, powerState.activeLeft - dt)
            if (powerState.activeLeft == 0f) powerState.kind = null
        }

        val tScale = when (powerState.kind) {
            AbilityType.SLOWMO -> 0.4f
            AbilityType.BLIZZARD -> 0.35f
            else -> 1.0f
        } * dtScale

        for (p in lvlData.platforms) {
            p.dx = 0f
            p.dy = 0f
            when (p.kind) {
                PlatformKind.CYBER_MOVE_H -> {
                    val old = p.x
                    p.x = p.baseX + (sin(clock * p.speed * tScale + p.phase) + 1f) * 0.5f * p.range
                    p.dx = p.x - old
                }
                PlatformKind.CYBER_MOVE_V -> {
                    val old = p.y
                    p.y = p.baseY - (sin(clock * p.speed * tScale + p.phase) + 1f) * 0.5f * p.range
                    p.dy = p.y - old
                }
                PlatformKind.CYBER_FALL -> {
                    if (p.triggered) {
                        p.timer += tScale
                        if (p.timer > 22f) {
                            p.vy += 0.45f * tScale
                            p.y += p.vy * tScale
                            p.dy = p.vy * tScale
                        }
                    }
                }
                else -> Unit
            }
        }

        if (heroObj.grounded && heroObj.ridePlat != null) {
            heroObj.x += heroObj.ridePlat?.dx ?: 0f
            heroObj.y += heroObj.ridePlat?.dy ?: 0f
        }

        for (en in lvlData.elfGoblins) {
            if (!en.alive) continue
            en.x += en.vx * tScale
            if (en.x <= en.minX) {
                en.x = en.minX
                en.vx = abs(en.vx)
            } else if (en.x >= en.maxX) {
                en.x = en.maxX
                en.vx = -abs(en.vx)
            }

            en.y = when (en.kind) {
                ElfGoblinKind.SCOUT_ELF_GOBLIN -> en.baseY - abs(sin(clock * 6f + en.phase)) * 3.5f
                ElfGoblinKind.JINGLE_FLYER_GOBLIN -> en.baseY - 12f + sin(clock * 4f + en.phase) * 12f
                ElfGoblinKind.BOUNCY_ELF_GOBLIN -> en.baseY - abs(sin(clock * 4.5f * tScale + en.phase)) * 22f
                ElfGoblinKind.TINKER_ELF_GOBLIN -> en.baseY
            }
        }

        // Update Moving Lasers!
        for (laser in lvlData.lasers) {
            if (laser.broken) continue
            val wave = (sin(clock * laser.speed * tScale + laser.phase) + 1f) * 0.5f
            if (laser.axis == LaserMotionAxis.HORIZONTAL) {
                laser.x = laser.baseX + wave * laser.range
                laser.y = laser.baseY
            } else {
                laser.x = laser.baseX + laser.range * 0.35f
                laser.y = laser.baseY - wave * laser.range
            }
        }

        // Update Level 10 (400 HP) & Level 20 (2,000 HP) Bosses
        lvlData.grinchBoss?.let { boss ->
            if (boss.alive) {
                if (boss.hitFlashTimer > 0f) boss.hitFlashTimer = max(0f, boss.hitFlashTimer - dt)
                if (boss.hurtCooldown > 0f) boss.hurtCooldown = max(0f, boss.hurtCooldown - dt)
                if (boss.jumpCooldown > 0f) boss.jumpCooldown = max(0f, boss.jumpCooldown - dt)

                val enraged = boss.hp <= boss.maxHp / 2
                val spdMul = (if (enraged) 1.48f else 1.0f) * tScale
                boss.x += boss.vx * spdMul
                if (boss.x <= boss.minX) {
                    boss.x = boss.minX
                    boss.vx = abs(boss.vx)
                } else if (boss.x >= boss.maxX) {
                    boss.x = boss.maxX
                    boss.vx = -abs(boss.vx)
                }

                if (boss.y >= boss.baseY - 1f && boss.jumpCooldown <= 0f) {
                    boss.vy = if (enraged) -10.5f else -8.8f
                    boss.jumpCooldown = if (enraged) 1.55f else 2.25f
                    emitBurst(boss.x + boss.w / 2f, boss.baseY + boss.h, Color(0xFF82C91E), 8, 3.6f)
                }
                boss.vy = min(14f, boss.vy + 0.45f * tScale)
                boss.y += boss.vy * tScale
                if (boss.y >= boss.baseY) {
                    if (boss.vy > 4f) {
                        emitRing(boss.x + boss.w / 2f, boss.baseY + boss.h, Color(0xFF51CF66), 56f)
                        emitBurst(boss.x + boss.w / 2f, boss.baseY + boss.h, Color(0xFF94D82D), 10, 4.2f)
                    }
                    boss.y = boss.baseY
                    boss.vy = 0f
                }

                // Evil Grinch green aura sparks
                if (Random.nextFloat() < (if (enraged) 0.55f else 0.30f) * dtScale) {
                    fxParticles.add(
                        FxParticle(
                            x = boss.x + Random.nextFloat() * boss.w,
                            y = boss.y + Random.nextFloat() * boss.h,
                            vx = (Random.nextFloat() - 0.5f) * 1.4f,
                            vy = -1.2f - Random.nextFloat() * 1.2f,
                            size = 4.5f + Random.nextFloat() * 4f,
                            color = if (enraged && Random.nextFloat() < 0.4f) Color(0xFFFA5252) else Color(0xFF82C91E),
                            life = 0.85f,
                            decay = 0.032f
                        )
                    )
                }
            }
        }

        val moveSpeedMul = when (powerState.kind) {
            AbilityType.BLIZZARD -> 1.35f
            AbilityType.CANDY_TURBO -> 1.55f
            AbilityType.AURORA_NOVA -> 1.2f
            else -> 1f
        }
        if (input.left) {
            heroObj.vx = -heroObj.speed * moveSpeedMul * dtScale
            heroObj.dir = -1
        } else if (input.right) {
            heroObj.vx = heroObj.speed * moveSpeedMul * dtScale
            heroObj.dir = 1
        } else {
            heroObj.vx *= 0.72f
            if (abs(heroObj.vx) < 0.2f) heroObj.vx = 0f
        }

        // Smooth expressive movement lean & walk bounce
        val targetTilt = if (abs(heroObj.vx) > 0.5f) heroObj.dir * 8.5f + sin(clock * 18f) * 3f else 0f
        heroObj.walkTiltDeg += (targetTilt - heroObj.walkTiltDeg) * 0.22f
        heroObj.walkBobY = if (heroObj.grounded && abs(heroObj.vx) > 0.6f) {
            -abs(sin(clock * 14f)) * 4.5f
        } else {
            sin(clock * 3.8f) * 1.2f
        }

        val meta = findHeroMeta(equippedHeroId)
        if (heroObj.grounded && abs(heroObj.vx) > 2.2f && Random.nextFloat() < 0.65f) {
            fxParticles.add(
                FxParticle(
                    x = heroObj.x + heroObj.w / 2f - heroObj.dir * 10f,
                    y = heroObj.y + heroObj.h - 2f,
                    vx = -heroObj.dir * (1.4f + Random.nextFloat() * 1.8f),
                    vy = -0.9f - Random.nextFloat() * 1.4f,
                    size = 4f + Random.nextFloat() * 3f,
                    color = meta.trailColor,
                    life = 0.9f,
                    decay = 0.045f
                )
            )
        }
        if (meta.id == "om_de_zapada" && Random.nextFloat() < 0.35f * dtScale) {
            jumpSnowflakes.add(
                JumpSnowflake(
                    x = heroObj.x + Random.nextFloat() * heroObj.w,
                    y = heroObj.y + Random.nextFloat() * heroObj.h * 0.7f,
                    vx = (Random.nextFloat() - 0.5f) * 1.1f - heroObj.vx * 0.15f,
                    vy = -0.7f - Random.nextFloat() * 0.9f,
                    radius = 3.6f + Random.nextFloat() * 2.8f,
                    rot = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 6f,
                    color = if (Random.nextFloat() < 0.5f) Color(0xFFD0EBFF) else Color(0xFF63E6BE),
                    life = 0.78f,
                    decay = 0.030f
                )
            )
        }

        // Continuous particle emission for active new Divine abilities
        when (powerState.kind) {
            AbilityType.BLIZZARD -> if (Random.nextFloat() < 0.55f) {
                emitBurst(heroObj.x + 19f, heroObj.y + 19f, Color(0xFFBAE6FD), 2, 3.8f)
            }
            AbilityType.AURORA_NOVA -> if (Random.nextFloat() < 0.55f) {
                val col = if (Random.nextFloat() < 0.5f) Color(0xFF38D9A9) else Color(0xFF9775FA)
                emitBurst(heroObj.x + 19f, heroObj.y + 19f, col, 2, 3.4f)
            }
            AbilityType.CANDY_TURBO -> if (Random.nextFloat() < 0.65f) {
                val col = if (Random.nextFloat() < 0.5f) Color(0xFFE03131) else Color(0xFFFCC419)
                emitBurst(heroObj.x + 19f - heroObj.dir * 14f, heroObj.y + 20f, col, 2, 4.2f)
            }
            AbilityType.PRESENT_BARRAGE -> if (Random.nextFloat() < 0.45f) {
                val col = if (Random.nextFloat() < 0.5f) Color(0xFFFFD43B) else Color(0xFF51CF66)
                emitBurst(heroObj.x + 19f, heroObj.y + 16f, col, 2, 4.0f)
            }
            else -> Unit
        }

        heroObj.x += heroObj.vx
        if (heroObj.x < 0f) heroObj.x = 0f

        val maxJumps = if (meta.id == "ingerul_zapezii" || powerState.kind == AbilityType.AURORA_NOVA) 3 else 2
        if (heroObj.grounded) {
            heroObj.coyote = 7
            heroObj.jumpsLeft = maxJumps
        } else if (heroObj.coyote > 0) {
            heroObj.coyote--
        }

        val jumpPowerMul = if (powerState.kind == AbilityType.AURORA_NOVA) 1.16f else 1f
        if (heroObj.jumpBuf > 0) {
            heroObj.jumpBuf--
            if (heroObj.coyote > 0) {
                heroObj.vy = heroObj.jumpVel * jumpPowerMul
                heroObj.grounded = false
                heroObj.coyote = 0
                heroObj.jumpBuf = 0
                heroObj.jumpsLeft = maxJumps - 1
                heroObj.sx = 0.72f
                heroObj.sy = 1.3f
                callbacks.onStatIncrement("jumpsMade", 1)
                callbacks.onStatIncrement("bubbleJumps", 1)
                audio.playJump(false)
                emitJumpBubbles(heroObj.x + 19f, heroObj.y + 34f, isDouble = false, accentColor = meta.trailColor)
                emitJumpSnowEffect(heroObj.x + 19f, heroObj.y + 36f, isDouble = false)
                emitRing(heroObj.x + 19f, heroObj.y + 38f, Color(0xFFBAE6FD), 44f)
            } else if (heroObj.jumpsLeft > 0) {
                heroObj.vy = heroObj.jumpVel * 0.92f * jumpPowerMul
                heroObj.jumpsLeft--
                heroObj.jumpBuf = 0
                heroObj.sx = 0.75f
                heroObj.sy = 1.28f
                callbacks.onStatIncrement("jumpsMade", 1)
                callbacks.onStatIncrement("bubbleJumps", 1)
                audio.playJump(true)
                emitJumpBubbles(heroObj.x + 19f, heroObj.y + 34f, isDouble = true, accentColor = meta.trailColor)
                emitJumpSnowEffect(heroObj.x + 19f, heroObj.y + 36f, isDouble = true)
                emitRing(heroObj.x + 19f, heroObj.y + 38f, Color(0xFF4DABF7), 52f)
            }
        }

        var gForce = heroObj.grav
        if (powerState.kind == AbilityType.ZEROG || powerState.kind == AbilityType.SOLAR) {
            gForce = 0.06f
            if (input.jumpHeld) heroObj.vy = max(heroObj.vy - 0.65f * dtScale, -6.0f)
            if (Random.nextFloat() < 0.5f) {
                emitBurst(
                    heroObj.x + 19f,
                    heroObj.y + 38f,
                    if (powerState.kind == AbilityType.SOLAR) Color(0xFFFF922B) else Color(0xFF51CF66),
                    2,
                    2.2f
                )
            }
        }

        heroObj.vy = min(15f, heroObj.vy + gForce * dtScale)
        val prevGrounded = heroObj.grounded
        heroObj.grounded = false
        heroObj.ridePlat = null
        heroObj.y += heroObj.vy * dtScale

        for (p in lvlData.platforms) {
            if (p.kind == PlatformKind.CYBER_FALL && p.timer > 55f) continue
            if (overlap(heroObj.x, heroObj.y, heroObj.w, heroObj.h, p.x, p.y, p.w, p.h)) {
                if (heroObj.vy >= 0f) {
                    heroObj.y = p.y - heroObj.h
                    if (!prevGrounded && heroObj.vy > 3.5f) {
                        heroObj.sx = 1.28f
                        heroObj.sy = 0.74f
                        emitBurst(heroObj.x + 19f, heroObj.y + 38f, Color(0xFFF8FCFF), 9, 2.8f)
                    }
                    heroObj.vy = 0f
                    heroObj.grounded = true
                    heroObj.ridePlat = p
                    if (p.kind == PlatformKind.CYBER_FALL) p.triggered = true
                } else {
                    heroObj.y = p.y + p.h
                    heroObj.vy = 0f
                }
            }
        }

        heroObj.sx += (1f - heroObj.sx) * 0.2f
        heroObj.sy += (1f - heroObj.sy) * 0.2f

        // Christmas Checkpoints activation!
        for (cp in lvlData.checkpoints) {
            if (!cp.activated && overlap(heroObj.x, heroObj.y, heroObj.w, heroObj.h, cp.x - 24f, cp.y - 64f, 48f, 64f)) {
                cp.activated = true
                spawnX = cp.x - 19f
                spawnY = cp.y - 46f
                audio.playCheckpoint()
                emitBurst(cp.x, cp.y - 34f, Color(0xFFFA5252), 14, 5.2f)
                emitBurst(cp.x, cp.y - 34f, Color(0xFF51CF66), 14, 5.2f)
                emitBurst(cp.x, cp.y - 34f, Color(0xFFFFD43B), 12, 4.8f)
                emitRing(cp.x, cp.y - 24f, Color(0xFF51CF66), 58f)
                emitRing(cp.x, cp.y - 24f, Color(0xFFFFD43B), 42f)
                emitPopup(cp.x, cp.y - 68f, "🎄 CHECKPOINT!", Color(0xFF51CF66))
                callbacks.onCheckpointActivated(lvlData.lvl, spawnX, spawnY)
                callbacks.onToast("🎄 Checkpoint de Crăciun activat și salvat!")
            }
        }

        for (en in lvlData.elfGoblins) {
            if (!en.alive) continue
            if (overlap(heroObj.x + 3f, heroObj.y + 3f, heroObj.w - 6f, heroObj.h - 3f, en.x, en.y, en.w, en.h)) {
                val prevBot = (heroObj.y - heroObj.vy * dtScale) + heroObj.h
                if (heroObj.vy > 0f && prevBot <= en.y + 16f) {
                    en.alive = false
                    heroObj.vy = -11.2f
                    heroObj.jumpsLeft = maxJumps - 1
                    runCoins += 2
                    callbacks.onIntruderStomped(2)
                    audio.playStomp()
                    emitBurst(en.x + 17f, en.y + 17f, Color(0xFF51CF66), 20, 5.5f)
                    emitRing(en.x + 17f, en.y + 17f, Color(0xFFFFD43B), 46f)
                    emitPopup(en.x + 17f, en.y - 10f, "+2 🪙", Color(0xFFFFD43B))
                } else if (powerState.kind == AbilityType.CANDY_TURBO) {
                    en.alive = false
                    runCoins += 2
                    callbacks.onIntruderStomped(2)
                    audio.playStomp()
                    emitBurst(en.x + 17f, en.y + 17f, Color(0xFFE03131), 20, 6f)
                    emitPopup(en.x + 17f, en.y - 10f, "🍬 +2 🪙", Color(0xFFFFE066))
                } else {
                    if (powerState.invuln > 0f) continue
                    if (powerState.solarShield) {
                        powerState.solarShield = false
                        powerState.invuln = 1.3f
                        en.alive = false
                        heroObj.vy = -12f
                        audio.playAbility(AbilityType.SOLAR)
                        emitBurst(en.x + 17f, en.y + 17f, Color(0xFFFF6B6B), 22, 6f)
                        callbacks.onToast("🔥 Scutul Festiv a pulverizat goblinul elf!")
                        continue
                    }
                    respawnPlayer("Ai fost lovit de un goblin elf!")
                    return
                }
            }
        }

        // Level 10 (400 HP) & Level 20 (2,000 HP) Boss Head-Stomp Combat!
        lvlData.grinchBoss?.let { boss ->
            if (boss.alive && overlap(
                    heroObj.x + 2f, heroObj.y + 2f, heroObj.w - 4f, heroObj.h - 2f,
                    boss.x + 2f, boss.y + 4f, boss.w - 4f, boss.h - 4f
                )
            ) {
                val prevBot = (heroObj.y - heroObj.vy * dtScale) + heroObj.h
                val isHeadStomp = (heroObj.vy >= -0.6f && prevBot <= boss.y + boss.h * 0.46f) ||
                    (heroObj.y + heroObj.h <= boss.y + boss.h * 0.40f)
                val stompDamage = if (boss.kind == BossKind.ELF_COMMANDER_L10) 100 else 250
                if (isHeadStomp) {
                    heroObj.vy = -13.6f
                    heroObj.grounded = false
                    heroObj.jumpsLeft = maxJumps - 1
                    heroObj.sx = 0.78f
                    heroObj.sy = 1.24f
                    if (boss.hurtCooldown <= 0f) {
                        damageGrinchBoss(boss, stompDamage, "👟")
                    }
                } else if (powerState.invuln <= 0f) {
                    if (powerState.solarShield) {
                        powerState.solarShield = false
                        powerState.invuln = 1.5f
                        heroObj.vy = -12.6f
                        damageGrinchBoss(boss, stompDamage, "🔥")
                        callbacks.onToast("🔥 Scutul Festiv l-a lovit pe ${boss.name}!")
                    } else {
                        respawnPlayer("${boss.name} te-a prins! Sari pe capul lui!")
                        return
                    }
                }
            }
        }

        for (bp in lvlData.bouncePads) {
            if (bp.squash > 0) bp.squash--
            if (overlap(heroObj.x, heroObj.y, heroObj.w, heroObj.h, bp.x, bp.y - bp.h, bp.w, bp.h)) {
                heroObj.vy = -17.5f
                heroObj.grounded = false
                heroObj.jumpsLeft = maxJumps - 1
                bp.squash = 10
                heroObj.sx = 0.72f
                heroObj.sy = 1.32f
                audio.playBounce()
                emitJumpBubbles(bp.x + bp.w / 2f, bp.y - bp.h, isDouble = true, accentColor = Color(0xFFFCC419))
                emitJumpSnowEffect(bp.x + bp.w / 2f, bp.y - bp.h, isDouble = true)
            }
        }

        for (laser in lvlData.lasers) {
            if (laser.broken) continue
            if (overlap(
                    heroObj.x + 5f, heroObj.y + 4f, heroObj.w - 10f, heroObj.h - 4f,
                    laser.x + 2f, laser.y - laser.h + 4f, laser.w - 4f, laser.h - 4f
                )
            ) {
                if (powerState.invuln > 0f) continue
                if (powerState.kind == AbilityType.CANDY_TURBO) {
                    laser.broken = true
                    emitBurst(laser.x + laser.w / 2f, laser.y - laser.h * 0.5f, Color(0xFFFFD43B), 16, 5.5f)
                    continue
                }
                if (powerState.solarShield) {
                    powerState.solarShield = false
                    powerState.invuln = 1.2f
                    laser.broken = true
                    heroObj.vy = -13.5f
                    audio.playAbility(AbilityType.SOLAR)
                    emitBurst(heroObj.x + 19f, heroObj.y + 19f, Color(0xFFFF6B6B), 25, 6f)
                    callbacks.onToast("🔥 Scutul Festiv a oprit laserul mobil!")
                    continue
                }
                respawnPlayer("Ai atins laserul mobil!")
                return
            }
        }

        val magnetRange = when (powerState.kind) {
            AbilityType.ZEROG -> 280f
            AbilityType.AURORA_NOVA -> 230f
            AbilityType.CANDY_TURBO -> 320f
            else -> 0f
        }
        for (c in lvlData.coins) {
            if (c.taken) continue
            val d = hypot((heroObj.x + 19f) - c.x, (heroObj.y + 19f) - c.y)
            if (magnetRange > 0f && d < magnetRange) {
                c.x += ((heroObj.x + 19f) - c.x) * 0.18f
                c.y += ((heroObj.y + 19f) - c.y) * 0.18f
            }
            if (d < 30f) {
                c.taken = true
                runCoins += 1
                callbacks.onCoinsCollected(1, 5)
                audio.playCoin()
                emitBurst(c.x, c.y, Color(0xFFFFD43B), 8, 3f)
                emitPopup(c.x, c.y - 10f, "+1", Color(0xFFFFD43B))
            }
        }

        for (dm in lvlData.diamonds) {
            if (dm.taken) continue
            val d = hypot((heroObj.x + 19f) - dm.x, (heroObj.y + 19f) - dm.y)
            if (magnetRange > 0f && d < magnetRange) {
                dm.x += ((heroObj.x + 19f) - dm.x) * 0.14f
                dm.y += ((heroObj.y + 19f) - dm.y) * 0.14f
            }
            if (d < 32f) {
                dm.taken = true
                runDiamonds += 1
                callbacks.onDiamondCollected(1, 25)
                audio.playDiamond()
                emitBurst(dm.x, dm.y, Color(0xFF4DABF7), 14, 4f)
                emitPopup(dm.x, dm.y - 12f, "+1 💎", Color(0xFFD0EBFF))
            }
        }

        if (bossLockToastCooldown > 0f) bossLockToastCooldown = max(0f, bossLockToastCooldown - dt)

        val g = lvlData.goal
        val heroCenterX = heroObj.x + heroObj.w / 2f
        val heroCenterY = heroObj.y + heroObj.h / 2f
        nearFinishDoor = lvlData.dimension == WorldDimension.MAIN_LEVEL &&
            abs(heroCenterX - g.x) < 130f &&
            abs((heroObj.y + heroObj.h) - g.y) < 110f

        if (lvlData.dimension == WorldDimension.MAIN_LEVEL &&
            overlap(
                heroObj.x, heroObj.y, heroObj.w, heroObj.h,
                g.x - g.width * 0.5f, g.y - g.height, g.width, g.height
            )
        ) {
            val activeBoss = lvlData.grinchBoss
            if (activeBoss != null && activeBoss.alive) {
                if (bossLockToastCooldown <= 0f) {
                    bossLockToastCooldown = 2.0f
                    audio.playHurt()
                    emitRing(g.x, g.y - g.height * 0.5f, Color(0xFFFA5252), 58f)
                    emitPopup(g.x, g.y - g.height - 16f, "🔒 Învinge ${activeBoss.name} (${activeBoss.hp} HP)!", Color(0xFFFF8787))
                    callbacks.onToast("🔒 Ușa este blocată! Învinge-l pe ${activeBoss.name} sărind pe capul lui!")
                }
            } else if (!finishDoorOpeningActive) {
                finishDoorOpeningActive = true
                finishDoorOpenProgress = 0.05f
                audio.playDoorPortal()
                emitGreenSplash(g.x, g.y - g.height * 0.5f, count = 28)
                emitBurst(g.x, g.y - g.height * 0.5f, Color(0xFFFFD43B), 22, 6.2f)
                emitRing(g.x, g.y - g.height * 0.5f, Color(0xFF51CF66), 94f)
                emitPopup(g.x, g.y - g.height - 20f, "🚪✨ FINISH!", Color(0xFFFFE066))
                return
            }
        }

        // Tree Door / Return Door / Santa Village interactions
        if (lvlData.dimension == WorldDimension.MAIN_LEVEL) {
            val tree = lvlData.startTree
            nearTreeDoor = tree.hasDoor &&
                abs(heroCenterX - tree.x) < 96f &&
                abs((heroObj.y + heroObj.h) - tree.y) < 90f
            nearReturnDoor = false
            nearSanta = false
        } else {
            nearFinishDoor = false
            nearTreeDoor = false
            val rd = lvlData.returnDoor
            nearReturnDoor = rd != null &&
                abs(heroCenterX - rd.x) < 82f &&
                abs((heroObj.y + heroObj.h) - rd.y) < 90f

            val sleigh = lvlData.santaSleigh
            nearSanta = sleigh != null &&
                abs(heroCenterX - (sleigh.x - 15f)) < 160f &&
                abs((heroObj.y + heroObj.h) - sleigh.y) < 110f

            // Light up Village Houses with glowing lights effect when hero visits them!
            for (house in lvlData.houses) {
                if (!house.litBoosted &&
                    heroCenterX >= house.x - 10f &&
                    heroCenterX <= house.x + house.w + 10f
                ) {
                    house.litBoosted = true
                    audio.playCheckpoint()
                    callbacks.onStatIncrement("housesVisited", 1)
                    emitBurst(house.x + house.w / 2f, house.y - house.h * 0.6f, Color(0xFFFCC419), 18, 5.2f)
                    emitBurst(house.x + house.w / 2f, house.y - house.h * 0.6f, Color(0xFF51CF66), 14, 4.8f)
                    emitRing(house.x + house.w / 2f, house.y - 24f, Color(0xFFFCC419), 68f)
                    emitPopup(house.x + house.w / 2f, house.y - house.h - 22f, "🏠 Lumini Aprinse!", Color(0xFFFFE066))
                }
            }

            // Collect Village Gift Boxes for Santa's Sleigh Quest!
            for (gb in lvlData.giftBoxes) {
                if (!gb.taken && hypot(heroCenterX - gb.x, heroCenterY - gb.y) < 34f) {
                    gb.taken = true
                    runCoins += 3
                    callbacks.onCoinsCollected(3, 10)
                    callbacks.onStatIncrement("giftsDelivered", 1)
                    audio.playGiftCollect()
                    emitBurst(gb.x, gb.y, Color(gb.boxColor), 16, 4.8f)
                    emitBurst(gb.x, gb.y, Color(gb.ribbonColor), 10, 3.8f)
                    emitPopup(gb.x, gb.y - 14f, "+1 🎁 (+3 🪙)", Color(0xFF69DB7C))
                }
            }
        }

        if (heroObj.y > lvlData.deathY) {
            respawnPlayer("Ai căzut în prăpastia de zăpadă!")
            return
        }

        updateTransientFx(dtScale)

        val targetCamX = heroObj.x - viewW * 0.34f
        val targetCamY = heroObj.y - viewH * 0.56f
        camX += (max(0f, targetCamX) - camX) * 0.12f
        camY += (max(-220f, min(200f, targetCamY)) - camY) * 0.1f
    }

    private fun updateTransientFx(dtScale: Float) {
        for (i in shootingStars.indices.reversed()) {
            val ss = shootingStars[i]
            ss.x += ss.vx * dtScale
            ss.y += ss.vy * dtScale
            ss.life -= 0.025f * dtScale
            if (ss.life <= 0f) shootingStars.removeAt(i)
        }
        for (i in shockRings.indices.reversed()) {
            val sr = shockRings[i]
            sr.r += (sr.maxR - sr.r) * 0.22f
            sr.life -= 0.06f * dtScale
            if (sr.life <= 0f) shockRings.removeAt(i)
        }
        for (i in jumpBubbles.indices.reversed()) {
            val b = jumpBubbles[i]
            b.y += b.vy * dtScale
            b.x += (b.vx + sin(clock * 6f + b.wobblePhase) * 0.45f) * dtScale
            b.radius += 0.06f * dtScale
            b.life -= b.decay * dtScale
            if (b.life <= 0f) jumpBubbles.removeAt(i)
        }
        for (i in jumpSnowflakes.indices.reversed()) {
            val js = jumpSnowflakes[i]
            js.x += (js.vx + sin(clock * 7f + js.rot * 0.03f) * 0.55f) * dtScale
            js.y += js.vy * dtScale
            js.vy += 0.06f * dtScale
            js.rot += js.rotSpeed * dtScale
            js.life -= js.decay * dtScale
            if (js.life <= 0f) jumpSnowflakes.removeAt(i)
        }
        for (i in fxParticles.indices.reversed()) {
            val pt = fxParticles[i]
            pt.x += pt.vx * dtScale
            pt.y += pt.vy * dtScale
            pt.vy += 0.14f * dtScale
            pt.life -= pt.decay * dtScale
            if (pt.life <= 0f) fxParticles.removeAt(i)
        }
        for (i in popTexts.indices.reversed()) {
            val ft = popTexts[i]
            ft.y -= 1.1f * dtScale
            ft.life -= 0.025f * dtScale
            if (ft.life <= 0f) popTexts.removeAt(i)
        }
    }

    private fun respawnPlayer(msg: String) {
        audio.playHurt()
        shakeTimer = 14
        val meta = findHeroMeta(equippedHeroId)
        emitBurst(heroObj.x + 19f, heroObj.y + 19f, meta.trailColor, 24, 6.5f)
        callbacks.onToast("$msg Reîncearcă! ❄️")

        powerState.activeLeft = 0f
        powerState.kind = null
        powerState.invuln = 0.8f
        powerState.solarShield = (meta.id == "phoenix_festiv")

        heroObj.x = spawnX
        heroObj.y = spawnY
        heroObj.vx = 0f
        heroObj.vy = 0f
        heroObj.grounded = false
        heroObj.jumpsLeft = if (meta.id == "ingerul_zapezii") 3 else 2
        heroObj.ridePlat = null

        lvlData.platforms.forEach { p ->
            if (p.kind == PlatformKind.CYBER_FALL) {
                p.y = p.origY
                p.triggered = false
                p.timer = 0f
                p.vy = 0f
            }
        }
        lvlData.lasers.forEach { it.broken = false }
        lvlData.elfGoblins.forEach {
            if (it.x >= spawnX - 40f) it.alive = true
        }
    }

    private fun finishLevel() {
        if (isWon) return
        isWon = true
        audio.playWin()

        val lvlCoins = 10 + min(15, (lvlData.lvl * 0.5f).toInt())
        runCoins += lvlCoins
        val xmasColors = listOf(
            Color(0xFFFA5252),
            Color(0xFF51CF66),
            Color(0xFFFCC419),
            Color(0xFF4DABF7),
            Color(0xFFF8FCFF)
        )
        for (i in 0 until 45) {
            emitBurst(heroObj.x + 19f, heroObj.y, xmasColors[i % xmasColors.size], 1, 7f)
        }
        callbacks.onLevelCompleted(lvlData.lvl, runCoins, runDiamonds)
    }

    private fun damageGrinchBoss(boss: GrinchBoss, amount: Int, icon: String) {
        if (!boss.alive) return
        boss.hp = max(0, boss.hp - amount)
        boss.hurtCooldown = 0.26f
        boss.hitFlashTimer = 0.24f
        shakeTimer = 8
        audio.playStomp()

        val bx = boss.x + boss.w / 2f
        val by = boss.y + boss.h / 2f
        emitBurst(bx, boss.y + 12f, Color(0xFF94D82D), 18, 6.2f)
        emitBurst(bx, boss.y + 12f, Color(0xFFFFD43B), 16, 5.8f)
        emitRing(bx, by, Color(0xFFFFD43B), 76f)
        emitPopup(bx, boss.y - 20f, "$icon -$amount HP (${boss.hp})", Color(0xFFFFE066))

        if (boss.hp <= 0) {
            boss.alive = false
            shakeTimer = 18
            audio.playWin()
            val rewardCoins = if (boss.kind == BossKind.ELF_COMMANDER_L10) 25 else 50
            val rewardXp = if (boss.kind == BossKind.ELF_COMMANDER_L10) 120 else 250
            runCoins += rewardCoins
            callbacks.onCoinsCollected(rewardCoins, rewardXp)
            callbacks.onIntruderStomped(rewardCoins)
            emitGreenSplash(bx, by, count = 34)
            emitBurst(bx, by, Color(0xFFFA5252), 26, 7.5f)
            emitBurst(bx, by, Color(0xFFFFD43B), 26, 7.5f)
            emitRing(bx, by, Color(0xFF51CF66), 125f)
            emitRing(bx, by, Color(0xFFFFD43B), 92f)
            emitPopup(bx, boss.y - 34f, "🏆 ${boss.name} ÎNVINS! +$rewardCoins 🪙", Color(0xFF69DB7C))
            callbacks.onToast("🎉 L-ai învins pe ${boss.name}! Ușa de Finish s-a deblocat!")
        }
    }

    private fun emitJumpBubbles(x: Float, y: Float, isDouble: Boolean, accentColor: Color) {
        val count = if (isDouble) 11 else 8
        val bubbleTints = listOf(Color(0xFFBAE6FD), Color(0xFF74C0FC), Color(0xFFFFE066), accentColor)
        repeat(count) { i ->
            jumpBubbles.add(
                BubbleParticle(
                    x = x + (Random.nextFloat() - 0.5f) * 28f,
                    y = y + (Random.nextFloat() - 0.5f) * 10f,
                    vx = (Random.nextFloat() - 0.5f) * 1.9f,
                    vy = -1.2f - Random.nextFloat() * 1.9f,
                    radius = (4.5f + Random.nextFloat() * 6.5f) * if (isDouble) 1.18f else 1f,
                    wobblePhase = Random.nextFloat() * 6.28f,
                    tint = bubbleTints[i % bubbleTints.size],
                    life = 1f,
                    decay = 0.022f + Random.nextFloat() * 0.015f
                )
            )
        }
        emitBurst(x, y, Color(0xFFD0EBFF), if (isDouble) 10 else 6, 3.2f)
    }

    private fun emitJumpSnowEffect(x: Float, y: Float, isDouble: Boolean) {
        val count = if (isDouble) 18 else 13
        val snowPalette = listOf(Color.White, Color(0xFFE7F5FF), Color(0xFFBAE6FD), Color(0xFF74C0FC))
        repeat(count) { i ->
            val spreadAngle = -PI.toFloat() + (Random.nextFloat() - 0.5f) * 2.5f
            val spd = (1.1f + Random.nextFloat() * 2.8f) * (if (isDouble) 1.18f else 1f)
            jumpSnowflakes.add(
                JumpSnowflake(
                    x = x + (Random.nextFloat() - 0.5f) * 30f,
                    y = y + (Random.nextFloat() - 0.5f) * 8f,
                    vx = cos(spreadAngle) * spd + (Random.nextFloat() - 0.5f) * 1.6f,
                    vy = -0.9f - Random.nextFloat() * 2.2f,
                    radius = (4.8f + Random.nextFloat() * 5.0f) * (if (isDouble) 1.12f else 1f),
                    rot = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 8f,
                    color = snowPalette[i % snowPalette.size],
                    life = 1.0f,
                    decay = 0.019f + Random.nextFloat() * 0.012f
                )
            )
        }
    }

    fun emitGreenSplash(x: Float, y: Float, count: Int = 22) {
        val greens = listOf(Color(0xFF51CF66), Color(0xFF2FB344), Color(0xFF8CE99A), Color(0xFFD8F5A2))
        repeat(count) { i ->
            val a = Random.nextFloat() * 2f * PI.toFloat()
            val v = 1.8f + Random.nextFloat() * 5.2f
            fxParticles.add(
                FxParticle(
                    x = x + (Random.nextFloat() - 0.5f) * 26f,
                    y = y + (Random.nextFloat() - 0.5f) * 32f,
                    vx = cos(a) * v,
                    vy = sin(a) * v - 2.2f,
                    size = 5f + Random.nextFloat() * 5f,
                    color = greens[i % greens.size],
                    life = 1.05f,
                    decay = 0.022f + Random.nextFloat() * 0.015f
                )
            )
        }
    }

    fun emitBurst(x: Float, y: Float, color: Color, count: Int = 10, spd: Float = 4f) {
        repeat(count) {
            val a = Random.nextFloat() * 2f * PI.toFloat()
            val v = (0.3f + Random.nextFloat() * 0.7f) * spd
            fxParticles.add(
                FxParticle(
                    x = x,
                    y = y,
                    vx = cos(a) * v,
                    vy = sin(a) * v - 1.5f,
                    size = 4f + Random.nextFloat() * 4.5f,
                    color = color,
                    life = 1f,
                    decay = 0.026f + Random.nextFloat() * 0.02f
                )
            )
        }
    }

    fun emitRing(x: Float, y: Float, color: Color = Color(0xFFBAE6FD), maxR: Float = 44f) {
        shockRings.add(ShockRing(x, y, 6f, maxR, color, 1f))
    }

    fun emitPopup(x: Float, y: Float, text: String, color: Color = Color.White) {
        popTexts.add(PopText(x, y, text, color, 1f))
    }

    private fun updateSkySantaAndGifts(viewW: Float, viewH: Float, dt: Float, dtScale: Float) {
        skySantaX += skySantaDir * 1.95f * dtScale
        if (skySantaDir == 1 && skySantaX > viewW + 250f) {
            skySantaDir = -1
            skySantaX = viewW + 230f
        } else if (skySantaDir == -1 && skySantaX < -250f) {
            skySantaDir = 1
            skySantaX = -230f
        }

        val baseSkyY = max(88f, min(140f, viewH * 0.18f))
        skySantaY = baseSkyY + sin(clock * 1.85f) * 14f

        if (skySantaThrowAnim > 0f) {
            skySantaThrowAnim = max(0f, skySantaThrowAnim - dt * 2.4f)
        }

        // Magic golden sleigh stardust trail
        if (skySantaX in -180f..(viewW + 180f) && Random.nextFloat() < 0.75f * dtScale) {
            val trailColors = listOf(Color(0xFFFCC419), Color(0xFFFFE066), Color.White, Color(0xFFFA5252))
            skyTrailSparks.add(
                SkyTrailSpark(
                    x = skySantaX - skySantaDir * (26f + Random.nextFloat() * 14f),
                    y = skySantaY + 6f + (Random.nextFloat() - 0.5f) * 10f,
                    vx = -skySantaDir * (0.4f + Random.nextFloat() * 0.6f),
                    vy = 0.25f + (Random.nextFloat() - 0.5f) * 0.5f,
                    size = 2.2f + Random.nextFloat() * 2.6f,
                    color = trailColors[Random.nextInt(trailColors.size)],
                    life = 1.0f
                )
            )
            if (skyTrailSparks.size > 48) skyTrailSparks.removeAt(0)
        }

        // Santa throws wrapped gifts down from his overflowing sack!
        skyGiftSpawnTimer -= dt
        if (skyGiftSpawnTimer <= 0f) {
            skyGiftSpawnTimer = 0.58f + Random.nextFloat() * 0.44f
            if (skySantaX in 18f..(viewW - 18f)) {
                skySantaThrowAnim = 1.0f
                val boxPalette = listOf(
                    Color(0xFFE03131) to Color(0xFFFCC419),
                    Color(0xFF2FB344) to Color(0xFFFFE066),
                    Color(0xFF339AF0) to Color.White,
                    Color(0xFF9C36B5) to Color(0xFFFCC419),
                    Color(0xFFF76707) to Color(0xFF51CF66)
                )
                val pair = boxPalette[Random.nextInt(boxPalette.size)]
                skyGiftDrops.add(
                    SkyGiftDrop(
                        x = skySantaX - skySantaDir * 18f,
                        y = skySantaY - 8f,
                        vx = -skySantaDir * (0.45f + Random.nextFloat() * 0.85f),
                        vy = -1.6f - Random.nextFloat() * 0.8f,
                        rot = (Random.nextFloat() - 0.5f) * 20f,
                        rotSpeed = (Random.nextFloat() - 0.5f) * 2.6f,
                        boxColor = pair.first,
                        ribbonColor = pair.second,
                        hasParachute = Random.nextFloat() < 0.55f,
                        life = 1.0f
                    )
                )
                if (skyGiftDrops.size > 26) skyGiftDrops.removeAt(0)
            }
        }

        val sparkIt = skyTrailSparks.iterator()
        while (sparkIt.hasNext()) {
            val sp = sparkIt.next()
            sp.x += sp.vx * dtScale
            sp.y += sp.vy * dtScale
            sp.life -= 0.032f * dtScale
            if (sp.life <= 0f) sparkIt.remove()
        }

        val giftIt = skyGiftDrops.iterator()
        while (giftIt.hasNext()) {
            val g = giftIt.next()
            val maxFall = if (g.hasParachute) 2.0f else 3.1f
            g.vy = min(maxFall, g.vy + 0.062f * dtScale)
            g.vx *= 0.992f
            g.x += (g.vx + sin(clock * 2.8f + g.rot * 0.06f) * 0.35f) * dtScale
            g.y += g.vy * dtScale
            g.rot += g.rotSpeed * dtScale
            if (g.y > viewH - 35f) g.life -= 0.055f * dtScale
            if (g.life <= 0f || g.y > viewH + 45f) giftIt.remove()
        }
    }

    private fun overlap(
        ax: Float, ay: Float, aw: Float, ah: Float,
        bx: Float, by: Float, bw: Float, bh: Float
    ): Boolean = ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by
}
