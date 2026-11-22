package com.example.game

import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class PlatformKind {
    CYBER_SOLID,
    CYBER_MOVE_H,
    CYBER_MOVE_V,
    CYBER_FALL,
    FINISH_ZONE
}

enum class ElfGoblinKind {
    SCOUT_ELF_GOBLIN,
    TINKER_ELF_GOBLIN,
    JINGLE_FLYER_GOBLIN,
    BOUNCY_ELF_GOBLIN
}

data class Platform(
    var x: Float,
    var y: Float,
    val w: Float,
    val h: Float,
    val kind: PlatformKind,
    val baseX: Float = x,
    val baseY: Float = y,
    val origY: Float = y,
    val range: Float = 0f,
    val speed: Float = 0f,
    val phase: Float = 0f,
    var dx: Float = 0f,
    var dy: Float = 0f,
    var triggered: Boolean = false,
    var timer: Float = 0f,
    var vy: Float = 0f
)

data class CoinItem(var x: Float, var y: Float, var taken: Boolean = false)
data class DiamondItem(var x: Float, var y: Float, var taken: Boolean = false)
enum class LaserMotionAxis {
    HORIZONTAL,
    VERTICAL
}

data class MovingLaser(
    var x: Float,
    var y: Float,
    val baseX: Float,
    val baseY: Float,
    val w: Float = 14f,
    val h: Float = 52f,
    val axis: LaserMotionAxis = LaserMotionAxis.HORIZONTAL,
    val range: Float = 90f,
    val speed: Float = 2.0f,
    val phase: Float = 0f,
    var broken: Boolean = false
)
data class BouncePad(val x: Float, val y: Float, val w: Float, val h: Float, var squash: Int = 0)
enum class WorldDimension {
    MAIN_LEVEL,
    SANTAS_VILLAGE
}

data class Beacon(val x: Float, val y: Float, val colorHex: Long)
data class CheckpointItem(val x: Float, val y: Float, var activated: Boolean = false)
data class BigTreeProp(
    val x: Float,
    val y: Float,
    val hasDoor: Boolean = true,
    val doorWidth: Float = 44f,
    val doorHeight: Float = 66f
)

data class ReturnDoorProp(val x: Float, val y: Float, val width: Float = 46f, val height: Float = 70f)

data class VillageHouse(
    val id: Int,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val wallColor: Long,
    val roofColor: Long,
    val lightColors: List<Long>,
    val hasChimney: Boolean = true,
    val hasSecondFloor: Boolean = false,
    var litBoosted: Boolean = false
)

data class GiftBoxItem(
    var x: Float,
    var y: Float,
    var taken: Boolean = false,
    val boxColor: Long = 0xFFE03131,
    val ribbonColor: Long = 0xFFFCC419
)

data class SantaSleighProp(val x: Float, val y: Float)

data class ElfGoblin(
    val kind: ElfGoblinKind,
    var x: Float,
    var y: Float,
    val baseY: Float,
    val w: Float,
    val h: Float,
    val minX: Float,
    val maxX: Float,
    var vx: Float,
    var vy: Float = 0f,
    var alive: Boolean = true,
    val phase: Float = 0f
)

enum class BossKind {
    ELF_COMMANDER_L10,
    GRINCH_L20
}

data class GrinchBoss(
    val kind: BossKind = BossKind.GRINCH_L20,
    val name: String = "BOSS GRINCH",
    var x: Float,
    var y: Float,
    val baseY: Float,
    val w: Float = 72f,
    val h: Float = 84f,
    val minX: Float,
    val maxX: Float,
    var vx: Float = 2.2f,
    var vy: Float = 0f,
    var hp: Int = 2000,
    val maxHp: Int = 2000,
    var alive: Boolean = true,
    var hitFlashTimer: Float = 0f,
    var jumpCooldown: Float = 1.6f,
    var hurtCooldown: Float = 0f,
    val phase: Float = 0f
)

enum class BlockFacadeStyle {
    SNOWY_EARTH,
    CITY_APARTMENTS,
    CANDY_TOWERS,
    ICE_CITADEL,
    ROYAL_BOULEVARD,
    NEON_SKYSCRAPERS,
    EMERALD_MANOR,
    STARLIGHT_CASTLE
}

data class LevelTheme(
    val id: Int,
    val title: String,
    val skyTop: Long,
    val skyMid: Long,
    val skyBot: Long,
    val auroraPrimary: Long,
    val auroraSecondary: Long,
    val hillColor: Long,
    val facadeStyle: BlockFacadeStyle,
    val blockColors: List<Long>,
    val trimColor: Long,
    val windowLitColor: Long,
    val roofCapColor: Long,
    val roofRimColor: Long,
    val lightPalette: List<Long>
)

data class DecoratedBuildingProp(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val floors: Int,
    val windowsPerFloor: Int,
    val wallColor: Long,
    val trimColor: Long,
    val roofColor: Long,
    val lightColors: List<Long>,
    val style: BlockFacadeStyle,
    val hasBalconies: Boolean,
    val hasRooftopTree: Boolean,
    val seed: Int
)

data class FinishDoor(
    val x: Float,
    val y: Float,
    val width: Float = 56f,
    val height: Float = 86f
)

data class LevelData(
    val lvl: Int,
    val dimension: WorldDimension = WorldDimension.MAIN_LEVEL,
    val theme: LevelTheme = themeForLevel(lvl),
    val startTree: BigTreeProp,
    val platforms: List<Platform>,
    val buildings: List<DecoratedBuildingProp> = emptyList(),
    val coins: List<CoinItem>,
    val diamonds: List<DiamondItem>,
    val lasers: List<MovingLaser>,
    val bouncePads: List<BouncePad>,
    val elfGoblins: List<ElfGoblin>,
    val beacons: List<Beacon>,
    val checkpoints: List<CheckpointItem>,
    val goal: FinishDoor,
    val grinchBoss: GrinchBoss? = null,
    val houses: List<VillageHouse> = emptyList(),
    val giftBoxes: List<GiftBoxItem> = emptyList(),
    val santaSleigh: SantaSleighProp? = null,
    val returnDoor: ReturnDoorProp? = null,
    val deathY: Float = 760f
)

fun themeForLevel(lvl: Int): LevelTheme {
    val safe = max(1, lvl)
    if (safe == 1) {
        return LevelTheme(
            id = 0,
            title = "Poiana Bradului Fermecat",
            skyTop = 0xFF071426,
            skyMid = 0xFF0E223D,
            skyBot = 0xFF173659,
            auroraPrimary = 0xFF2FB344,
            auroraSecondary = 0xFF4DABF7,
            hillColor = 0xFF0D2847,
            facadeStyle = BlockFacadeStyle.SNOWY_EARTH,
            blockColors = listOf(0xFF6B4423, 0xFF5C3A21, 0xFF4A2E1B),
            trimColor = 0xFF3A2212,
            windowLitColor = 0xFFFFE066,
            roofCapColor = 0xFFF8FCFF,
            roofRimColor = 0xFFBAE6FD,
            lightPalette = listOf(0xFFFA5252, 0xFFFCC419, 0xFF51CF66, 0xFF4DABF7)
        )
    }

    val themes = listOf(
        LevelTheme(
            id = 1,
            title = "Cerul Aurorei de Aur",
            skyTop = 0xFF0B102B,
            skyMid = 0xFF1A1F4B,
            skyBot = 0xFF2B2D6E,
            auroraPrimary = 0xFFFCC419,
            auroraSecondary = 0xFF4DABF7,
            hillColor = 0xFF14183D,
            facadeStyle = BlockFacadeStyle.CITY_APARTMENTS,
            blockColors = listOf(0xFF334155, 0xFF475569, 0xFF3B4D6B, 0xFF52527A),
            trimColor = 0xFF1E293B,
            windowLitColor = 0xFFFFE066,
            roofCapColor = 0xFFF8FCFF,
            roofRimColor = 0xFF93C5FD,
            lightPalette = listOf(0xFFFA5252, 0xFFFCC419, 0xFF51CF66, 0xFF4DABF7, 0xFFF06595)
        ),
        LevelTheme(
            id = 2,
            title = "Noaptea de Turtă Dulce",
            skyTop = 0xFF1D0B28,
            skyMid = 0xFF3B1347,
            skyBot = 0xFF5C1D5E,
            auroraPrimary = 0xFFF06595,
            auroraSecondary = 0xFFFFD43B,
            hillColor = 0xFF2B0E36,
            facadeStyle = BlockFacadeStyle.CANDY_TOWERS,
            blockColors = listOf(0xFFB45309, 0xFFBE185D, 0xFF9D4EDD, 0xFFD97706),
            trimColor = 0xFF78350F,
            windowLitColor = 0xFFFFF3BF,
            roofCapColor = 0xFFFFF0F6,
            roofRimColor = 0xFFFF8787,
            lightPalette = listOf(0xFFFF8787, 0xFFFFE066, 0xFF63E6BE, 0xFFF06595, 0xFFFFFFFF)
        ),
        LevelTheme(
            id = 3,
            title = "Ghețarul de Cristal Polar",
            skyTop = 0xFF031926,
            skyMid = 0xFF083344,
            skyBot = 0xFF0E4F66,
            auroraPrimary = 0xFF3BC9DB,
            auroraSecondary = 0xFF69DB7C,
            hillColor = 0xFF062838,
            facadeStyle = BlockFacadeStyle.ICE_CITADEL,
            blockColors = listOf(0xFF0C4A6E, 0xFF0369A1, 0xFF155E75, 0xFF1E3A8A),
            trimColor = 0xFF082F49,
            windowLitColor = 0xFFBAE6FD,
            roofCapColor = 0xFFE0F2FE,
            roofRimColor = 0xFF38BDF8,
            lightPalette = listOf(0xFF74C0FC, 0xFFFFFFFF, 0xFF63E6BE, 0xFFFCC419)
        ),
        LevelTheme(
            id = 4,
            title = "Cerul Regal al Moșului",
            skyTop = 0xFF210912,
            skyMid = 0xFF450A1F,
            skyBot = 0xFF6B142F,
            auroraPrimary = 0xFFFA5252,
            auroraSecondary = 0xFFFCC419,
            hillColor = 0xFF2E0816,
            facadeStyle = BlockFacadeStyle.ROYAL_BOULEVARD,
            blockColors = listOf(0xFF7F1D1D, 0xFF991B1B, 0xFF7C2D12, 0xFF881337),
            trimColor = 0xFF450A0A,
            windowLitColor = 0xFFFFD43B,
            roofCapColor = 0xFFF8FCFF,
            roofRimColor = 0xFFFCC419,
            lightPalette = listOf(0xFFFCC419, 0xFF51CF66, 0xFFFFFFFF, 0xFFFA5252)
        ),
        LevelTheme(
            id = 5,
            title = "Noaptea Stelelor Neon",
            skyTop = 0xFF050816,
            skyMid = 0xFF101935,
            skyBot = 0xFF1E2952,
            auroraPrimary = 0xFF22D3EE,
            auroraSecondary = 0xFFF43F5E,
            hillColor = 0xFF090E24,
            facadeStyle = BlockFacadeStyle.NEON_SKYSCRAPERS,
            blockColors = listOf(0xFF1E1B4B, 0xFF172554, 0xFF312E81, 0xFF0F172A),
            trimColor = 0xFF020617,
            windowLitColor = 0xFF67E8F9,
            roofCapColor = 0xFFE0F2FE,
            roofRimColor = 0xFFF472B6,
            lightPalette = listOf(0xFF22D3EE, 0xFFF43F5E, 0xFFA3E635, 0xFFFACC15)
        ),
        LevelTheme(
            id = 6,
            title = "Pădurea de Smarald & Brad",
            skyTop = 0xFF051F17,
            skyMid = 0xFF0B3B2B,
            skyBot = 0xFF135740,
            auroraPrimary = 0xFF51CF66,
            auroraSecondary = 0xFFFFD43B,
            hillColor = 0xFF072B20,
            facadeStyle = BlockFacadeStyle.EMERALD_MANOR,
            blockColors = listOf(0xFF14532D, 0xFF166534, 0xFF065F46, 0xFF115E59),
            trimColor = 0xFF052E16,
            windowLitColor = 0xFFFFE066,
            roofCapColor = 0xFFF8FCFF,
            roofRimColor = 0xFF69DB7C,
            lightPalette = listOf(0xFFFCC419, 0xFFFA5252, 0xFF69DB7C, 0xFFFFFFFF)
        ),
        LevelTheme(
            id = 7,
            title = "Cerul Stelelor de Crăciun",
            skyTop = 0xFF120C2E,
            skyMid = 0xFF241B54,
            skyBot = 0xFF38297A,
            auroraPrimary = 0xFFB197FC,
            auroraSecondary = 0xFFFFE066,
            hillColor = 0xFF19123D,
            facadeStyle = BlockFacadeStyle.STARLIGHT_CASTLE,
            blockColors = listOf(0xFF3B0764, 0xFF4C1D95, 0xFF3730A3, 0xFF581C87),
            trimColor = 0xFF1E1B4B,
            windowLitColor = 0xFFFFE066,
            roofCapColor = 0xFFF8FCFF,
            roofRimColor = 0xFFC084FC,
            lightPalette = listOf(0xFFFFE066, 0xFF4DABF7, 0xFFF06595, 0xFF69DB7C, 0xFFFF922B)
        )
    )
    return themes[(safe - 2) % themes.size]
}

fun hashRng(seed: Int): Float {
    val v = sin(seed * 12.9898 + 78.233) * 43758.5453
    return (v - floor(v)).toFloat()
}

private val ELF_GOBLIN_CYCLE = listOf(
    ElfGoblinKind.SCOUT_ELF_GOBLIN,
    ElfGoblinKind.TINKER_ELF_GOBLIN,
    ElfGoblinKind.JINGLE_FLYER_GOBLIN,
    ElfGoblinKind.BOUNCY_ELF_GOBLIN
)

fun buildLevel(lvl: Int): LevelData {
    val theme = themeForLevel(lvl)
    val platforms = mutableListOf<Platform>()
    val coins = mutableListOf<CoinItem>()
    val diamonds = mutableListOf<DiamondItem>()
    val lasers = mutableListOf<MovingLaser>()
    val bouncePads = mutableListOf<BouncePad>()
    val elfGoblins = mutableListOf<ElfGoblin>()
    val beacons = mutableListOf<Beacon>()
    val checkpoints = mutableListOf<CheckpointItem>()

    val diff = min(1.0f, (lvl - 1) / 35f)
    val segments = 6 + min(14, floor(ln((lvl + 1).toDouble()) / ln(2.0) * 2.2).toInt())

    // Starting snow/block plaza platform with the Giant Swaying Christmas Tree + Magic Door!
    platforms.add(Platform(x = 0f, y = 360f, w = 340f, h = 440f, kind = PlatformKind.CYBER_SOLID))
    val startTree = BigTreeProp(x = 212f, y = 360f, hasDoor = true)

    var cx = 340f
    var cy = 360f

    val checkpointSeg = segments / 2
    val secondCheckpointSeg = if (segments >= 12) (segments * 3) / 4 else -1

    for (i in 0 until segments) {
        val r1 = hashRng(lvl * 107 + i * 23)
        val r2 = hashRng(lvl * 229 + i * 41)
        val pat = floor(r1 * 8f).toInt()
        val enemyKind = ELF_GOBLIN_CYCLE[(lvl + i) % ELF_GOBLIN_CYCLE.size]

        val extraGap = floor(diff * 44f)
        val shrinkW = floor(diff * 42f)
        val spdMult = 1f + diff * 0.95f

        var checkpointPlatformX: Float? = null
        var checkpointPlatformY: Float? = null

        if (lvl == 1 && i == 0) {
            coins.add(CoinItem(cx + 45f, cy - 55f))
            cx += 105f
            val w = 240f
            platforms.add(Platform(cx, cy, w, 440f, PlatformKind.CYBER_SOLID))
            coins.add(CoinItem(cx + 80f, cy - 35f))
            coins.add(CoinItem(cx + 135f, cy - 35f))
            elfGoblins.add(
                ElfGoblin(
                    kind = ElfGoblinKind.SCOUT_ELF_GOBLIN,
                    x = cx + 135f,
                    y = cy - 36f,
                    baseY = cy - 36f,
                    w = 34f,
                    h = 36f,
                    minX = cx + 18f,
                    maxX = cx + w - 52f,
                    vx = 1.4f,
                    phase = 0f
                )
            )
            checkpointPlatformX = cx + 65f
            checkpointPlatformY = cy
            cx += w
        } else if (pat == 0) {
            val gap = min(165f, 95f + extraGap + floor(r2 * 24f))
            cy = max(170f, min(440f, cy + if (i % 2 == 0) -46f else 36f))
            coins.add(CoinItem(cx + gap / 2f, cy - 45f))
            cx += gap
            val w = max(135f, 225f - shrinkW)
            platforms.add(Platform(cx, cy, w, 440f, PlatformKind.CYBER_SOLID))
            if (i % 2 == 0) beacons.add(Beacon(cx + 24f, cy, 0xFFFA5252))

            if (w >= 165f && (lvl >= 2 || i > 1)) {
                if (r2 > 0.42f) {
                    elfGoblins.add(
                        ElfGoblin(
                            kind = enemyKind,
                            x = cx + w * 0.5f,
                            y = cy - 36f,
                            baseY = cy - 36f,
                            w = 34f,
                            h = 36f,
                            minX = cx + 12f,
                            maxX = cx + w - 46f,
                            vx = (1.45f + diff * 1.4f) * if (i % 2 == 0) 1f else -1f,
                            phase = i * 1.1f
                        )
                    )
                } else {
                    lasers.add(
                        MovingLaser(
                            x = cx + 24f,
                            y = cy,
                            baseX = cx + 24f,
                            baseY = cy,
                            w = 14f,
                            h = 52f,
                            axis = LaserMotionAxis.HORIZONTAL,
                            range = max(44f, w - 62f),
                            speed = 1.9f + diff * 1.1f,
                            phase = i * 0.9f
                        )
                    )
                }
                coins.add(CoinItem(cx + w / 2f, cy - 70f))
            }
            checkpointPlatformX = cx + 38f
            checkpointPlatformY = cy
            if (lvl >= 2 && w >= 170f && i % 2 == 1) {
                val terraceW = min(110f, w - 55f)
                val terraceX = cx + (w - terraceW) * 0.5f
                val terraceY = cy - 94f
                platforms.add(Platform(terraceX, terraceY, terraceW, 20f, PlatformKind.CYBER_SOLID))
                coins.add(CoinItem(terraceX + terraceW * 0.35f, terraceY - 30f))
                coins.add(CoinItem(terraceX + terraceW * 0.65f, terraceY - 30f))
            }
            cx += w
        } else if (pat == 1) {
            bouncePads.add(BouncePad(cx - 44f, cy, 36f, 14f))
            val highY = max(135f, cy - 140f)
            coins.add(CoinItem(cx + 35f, highY - 30f))
            coins.add(CoinItem(cx + 75f, highY - 45f))
            cx += 120f
            cy = highY
            val w = max(135f, 195f - shrinkW)
            platforms.add(Platform(cx, cy, w, 500f, PlatformKind.CYBER_SOLID))
            if (lvl >= 3 && w >= 150f) {
                elfGoblins.add(
                    ElfGoblin(
                        kind = ElfGoblinKind.JINGLE_FLYER_GOBLIN,
                        x = cx + 40f,
                        y = cy - 38f,
                        baseY = cy - 38f,
                        w = 34f,
                        h = 36f,
                        minX = cx + 12f,
                        maxX = cx + w - 46f,
                        vx = 1.55f * spdMult,
                        phase = i.toFloat()
                    )
                )
            }
            checkpointPlatformX = cx + 45f
            checkpointPlatformY = cy
            cx += w
        } else if (pat == 2) {
            cx += 75f
            platforms.add(
                Platform(
                    x = cx,
                    y = cy,
                    w = max(88f, 118f - floor(diff * 24f)),
                    h = 26f,
                    kind = PlatformKind.CYBER_MOVE_H,
                    baseX = cx,
                    range = 115f + floor(diff * 35f),
                    speed = (1.4f + r2 * 0.6f) * spdMult,
                    phase = i.toFloat()
                )
            )
            coins.add(CoinItem(cx + 55f, cy - 40f))
            cx += 245f
            platforms.add(Platform(cx, cy, 165f, 440f, PlatformKind.CYBER_SOLID))
            checkpointPlatformX = cx + 75f
            checkpointPlatformY = cy
            cx += 165f
        } else if (pat == 3 && lvl >= 2) {
            val cnt = if (lvl >= 8) 3 else 2
            for (f in 0 until cnt) {
                cx += 85f
                platforms.add(
                    Platform(
                        x = cx,
                        y = cy,
                        w = 92f,
                        h = 24f,
                        kind = PlatformKind.CYBER_FALL,
                        origY = cy
                    )
                )
                coins.add(CoinItem(cx + 46f, cy - 35f))
                cx += 92f
            }
            cx += 85f
            platforms.add(Platform(cx, cy, 165f, 440f, PlatformKind.CYBER_SOLID))
            checkpointPlatformX = cx + 75f
            checkpointPlatformY = cy
            cx += 165f
        } else if (pat == 4 && lvl >= 3) {
            cx += 80f
            val liftY = min(410f, cy + 30f)
            platforms.add(
                Platform(
                    x = cx,
                    y = liftY,
                    w = 105f,
                    h = 26f,
                    kind = PlatformKind.CYBER_MOVE_V,
                    baseY = liftY,
                    range = 120f,
                    speed = 1.5f * spdMult,
                    phase = i * 1.2f
                )
            )
            cy = liftY - 105f
            cx += 155f
            platforms.add(Platform(cx, cy, 175f, 500f, PlatformKind.CYBER_SOLID))
            checkpointPlatformX = cx + 80f
            checkpointPlatformY = cy
            cx += 175f
        } else {
            cx += 95f + floor(diff * 24f)
            val w = 250f
            platforms.add(Platform(cx, cy, w, 440f, PlatformKind.CYBER_SOLID))
            lasers.add(
                MovingLaser(
                    x = cx + 32f,
                    y = cy,
                    baseX = cx + 32f,
                    baseY = cy,
                    w = 14f,
                    h = 50f,
                    axis = if (i % 2 == 0) LaserMotionAxis.HORIZONTAL else LaserMotionAxis.VERTICAL,
                    range = if (i % 2 == 0) 172f else 54f,
                    speed = 2.0f + diff * 1.15f,
                    phase = i * 1.3f
                )
            )
            platforms.add(Platform(cx + 75f, cy - 112f, 100f, 22f, PlatformKind.CYBER_SOLID))
            elfGoblins.add(
                ElfGoblin(
                    kind = ElfGoblinKind.BOUNCY_ELF_GOBLIN,
                    x = cx + 95f,
                    y = cy - 148f,
                    baseY = cy - 148f,
                    w = 32f,
                    h = 36f,
                    minX = cx + 78f,
                    maxX = cx + 142f,
                    vx = 1.3f * spdMult,
                    phase = i.toFloat()
                )
            )
            coins.add(CoinItem(cx + 105f, cy - 156f))
            coins.add(CoinItem(cx + 145f, cy - 156f))
            checkpointPlatformX = cx + 45f
            checkpointPlatformY = cy
            cx += w
        }

        if ((i == checkpointSeg || i == secondCheckpointSeg) && checkpointPlatformX != null && checkpointPlatformY != null) {
            checkpoints.add(CheckpointItem(checkpointPlatformX, checkpointPlatformY))
        }

        if (i == segments / 2) {
            diamonds.add(DiamondItem(cx - 55f, cy - 95f))
        }
    }

    var grinchBoss: GrinchBoss? = null
    if (lvl == 10) {
        // Level 10 Boss Arena (400 HP — defeat by jumping on his head!)
        cx += 85f
        val arenaW = 500f
        platforms.add(Platform(cx, cy, arenaW, 480f, PlatformKind.CYBER_SOLID))
        checkpoints.add(CheckpointItem(cx + 36f, cy))
        bouncePads.add(BouncePad(cx + 72f, cy, 36f, 14f))
        bouncePads.add(BouncePad(cx + arenaW - 92f, cy, 36f, 14f))
        platforms.add(Platform(cx + 100f, cy - 132f, 82f, 20f, PlatformKind.CYBER_SOLID))
        platforms.add(Platform(cx + arenaW - 182f, cy - 132f, 82f, 20f, PlatformKind.CYBER_SOLID))
        coins.add(CoinItem(cx + 141f, cy - 166f))
        coins.add(CoinItem(cx + arenaW - 141f, cy - 166f))
        beacons.add(Beacon(cx + 22f, cy, 0xFFFA5252))
        beacons.add(Beacon(cx + arenaW - 22f, cy, 0xFFFCC419))

        grinchBoss = GrinchBoss(
            kind = BossKind.ELF_COMMANDER_L10,
            name = "BOSS ELF COMANDANT",
            x = cx + arenaW * 0.5f - 32f,
            y = cy - 76f,
            baseY = cy - 76f,
            w = 64f,
            h = 76f,
            minX = cx + 118f,
            maxX = cx + arenaW - 182f,
            vx = 1.95f,
            hp = 400,
            maxHp = 400
        )
        cx += arenaW
    } else if (lvl == 20) {
        // Level 20 Grinch Boss Arena (2,000 HP — defeat by jumping on his head!)
        cx += 90f
        val arenaW = 540f
        platforms.add(Platform(cx, cy, arenaW, 480f, PlatformKind.CYBER_SOLID))
        checkpoints.add(CheckpointItem(cx + 38f, cy))
        bouncePads.add(BouncePad(cx + 76f, cy, 38f, 14f))
        bouncePads.add(BouncePad(cx + arenaW - 96f, cy, 38f, 14f))
        // Elevated wooden ledges for aerial head-stomp setups
        platforms.add(Platform(cx + 125f, cy - 115f, 95f, 20f, PlatformKind.CYBER_SOLID))
        platforms.add(Platform(cx + arenaW - 220f, cy - 115f, 95f, 20f, PlatformKind.CYBER_SOLID))
        coins.add(CoinItem(cx + 172f, cy - 150f))
        coins.add(CoinItem(cx + arenaW - 172f, cy - 150f))
        beacons.add(Beacon(cx + 22f, cy, 0xFFFA5252))
        beacons.add(Beacon(cx + arenaW - 22f, cy, 0xFF51CF66))

        grinchBoss = GrinchBoss(
            kind = BossKind.GRINCH_L20,
            name = "BOSS GRINCH",
            x = cx + arenaW * 0.5f - 36f,
            y = cy - 84f,
            baseY = cy - 84f,
            w = 72f,
            h = 84f,
            minX = cx + 122f,
            maxX = cx + arenaW - 188f,
            vx = 2.25f,
            hp = 2000,
            maxHp = 2000
        )
        cx += arenaW
    }

    // Extended 340px finish block/shore platform with Magic Christmas Finish Door!
    cx += 95f
    platforms.add(Platform(cx, cy, 340f, 480f, PlatformKind.FINISH_ZONE))
    beacons.add(Beacon(cx + 65f, cy, 0xFF51CF66))
    beacons.add(Beacon(cx + 135f, cy, 0xFFFCC419))

    return LevelData(
        lvl = lvl,
        dimension = WorldDimension.MAIN_LEVEL,
        theme = theme,
        startTree = startTree,
        platforms = platforms,
        buildings = emptyList(),
        coins = coins,
        diamonds = diamonds,
        lasers = lasers,
        bouncePads = bouncePads,
        elfGoblins = elfGoblins,
        beacons = beacons,
        checkpoints = checkpoints,
        goal = FinishDoor(cx + 235f, cy),
        grinchBoss = grinchBoss
    )
}

fun buildSantaVillageLevel(lvl: Int): LevelData {
    // Cozy snowy Christmas dimension behind the Tree Door!
    val platforms = mutableListOf(
        // Main snowy village avenue shore
        Platform(x = 0f, y = 360f, w = 1880f, h = 440f, kind = PlatformKind.CYBER_SOLID),
        // Snowy rooftop / balcony parkour ledges above houses
        Platform(x = 240f, y = 246f, w = 135f, h = 20f, kind = PlatformKind.CYBER_SOLID),
        Platform(x = 455f, y = 224f, w = 155f, h = 20f, kind = PlatformKind.CYBER_SOLID),
        Platform(x = 1230f, y = 238f, w = 145f, h = 20f, kind = PlatformKind.CYBER_SOLID),
        Platform(x = 1470f, y = 216f, w = 160f, h = 20f, kind = PlatformKind.CYBER_SOLID),
        // Walkable floating gift bridge above Santa's Sleigh!
        Platform(x = 805f, y = 240f, w = 170f, h = 20f, kind = PlatformKind.CYBER_MOVE_V, baseY = 240f, range = 34f, speed = 1.1f, phase = 0f)
    )

    val houses = listOf(
        VillageHouse(
            id = 1, x = 220f, y = 360f, w = 150f, h = 114f,
            wallColor = 0xFFD97706, roofColor = 0xFFB91C1C,
            lightColors = listOf(0xFFFA5252, 0xFFFCC419, 0xFF51CF66, 0xFF4DABF7),
            hasChimney = true, hasSecondFloor = false
        ),
        VillageHouse(
            id = 2, x = 410f, y = 360f, w = 175f, h = 138f,
            wallColor = 0xFF9F1239, roofColor = 0xFF15803D,
            lightColors = listOf(0xFFFCC419, 0xFF4DABF7, 0xFFF06595, 0xFF69DB7C),
            hasChimney = true, hasSecondFloor = true
        ),
        VillageHouse(
            id = 3, x = 615f, y = 360f, w = 135f, h = 108f,
            wallColor = 0xFF1D4ED8, roofColor = 0xFF991B1B,
            lightColors = listOf(0xFF74C0FC, 0xFFFFE066, 0xFFFA5252, 0xFF8CE99A),
            hasChimney = true, hasSecondFloor = false
        ),
        VillageHouse(
            id = 4, x = 1045f, y = 360f, w = 145f, h = 116f,
            wallColor = 0xFF047857, roofColor = 0xFFC92A2A,
            lightColors = listOf(0xFF51CF66, 0xFFFCC419, 0xFFFA5252, 0xFF4DABF7),
            hasChimney = true, hasSecondFloor = false
        ),
        VillageHouse(
            id = 5, x = 1220f, y = 360f, w = 165f, h = 132f,
            wallColor = 0xFF7C2D12, roofColor = 0xFF1E40AF,
            lightColors = listOf(0xFFFFE066, 0xFFFA5252, 0xFF69DB7C, 0xFFF06595),
            hasChimney = true, hasSecondFloor = true
        ),
        VillageHouse(
            id = 6, x = 1425f, y = 360f, w = 180f, h = 144f,
            wallColor = 0xFF6D28D9, roofColor = 0xFFDC2626,
            lightColors = listOf(0xFF4DABF7, 0xFFFCC419, 0xFF51CF66, 0xFFFF8787),
            hasChimney = true, hasSecondFloor = true
        ),
        VillageHouse(
            id = 7, x = 1645f, y = 360f, w = 155f, h = 118f,
            wallColor = 0xFFB45309, roofColor = 0xFF166534,
            lightColors = listOf(0xFFFA5252, 0xFFFFE066, 0xFF74C0FC, 0xFF69DB7C),
            hasChimney = true, hasSecondFloor = false
        )
    )

    val bouncePads = listOf(
        BouncePad(185f, 360f, 34f, 14f),
        BouncePad(1185f, 360f, 34f, 14f)
    )

    val giftBoxes = listOf(
        GiftBoxItem(260f, 342f, boxColor = 0xFFE03131, ribbonColor = 0xFFFCC419),
        GiftBoxItem(305f, 224f, boxColor = 0xFF2FB344, ribbonColor = 0xFFFFE066),
        GiftBoxItem(485f, 342f, boxColor = 0xFF339AF0, ribbonColor = 0xFFFFFFFF),
        GiftBoxItem(535f, 202f, boxColor = 0xFFF06595, ribbonColor = 0xFFFCC419),
        GiftBoxItem(670f, 342f, boxColor = 0xFFFCC419, ribbonColor = 0xFFE03131),
        GiftBoxItem(890f, 200f, boxColor = 0xFFE03131, ribbonColor = 0xFF51CF66),
        GiftBoxItem(1095f, 342f, boxColor = 0xFF845EF7, ribbonColor = 0xFFFFE066),
        GiftBoxItem(1295f, 216f, boxColor = 0xFF2FB344, ribbonColor = 0xFFE03131),
        GiftBoxItem(1340f, 342f, boxColor = 0xFFE03131, ribbonColor = 0xFFFCC419),
        GiftBoxItem(1545f, 194f, boxColor = 0xFF339AF0, ribbonColor = 0xFFFCC419),
        GiftBoxItem(1615f, 342f, boxColor = 0xFFF76707, ribbonColor = 0xFFFFFFFF),
        GiftBoxItem(1720f, 342f, boxColor = 0xFF2FB344, ribbonColor = 0xFFFFE066)
    )

    val coins = listOf(
        CoinItem(270f, 220f),
        CoinItem(340f, 220f),
        CoinItem(500f, 196f),
        CoinItem(570f, 196f),
        CoinItem(845f, 196f),
        CoinItem(935f, 196f),
        CoinItem(1265f, 210f),
        CoinItem(1510f, 188f),
        CoinItem(1590f, 188f),
        CoinItem(1775f, 315f)
    )

    val diamonds = listOf(
        DiamondItem(535f, 165f),
        DiamondItem(890f, 158f),
        DiamondItem(1550f, 155f)
    )

    val beacons = listOf(
        Beacon(392f, 360f, 0xFFFA5252),
        Beacon(598f, 360f, 0xFF51CF66),
        Beacon(1025f, 360f, 0xFFFCC419),
        Beacon(1406f, 360f, 0xFF4DABF7),
        Beacon(1628f, 360f, 0xFFFA5252)
    )

    return LevelData(
        lvl = lvl,
        dimension = WorldDimension.SANTAS_VILLAGE,
        startTree = BigTreeProp(x = 92f, y = 360f, hasDoor = false),
        platforms = platforms,
        coins = coins,
        diamonds = diamonds,
        lasers = emptyList(),
        bouncePads = bouncePads,
        elfGoblins = emptyList(),
        beacons = beacons,
        checkpoints = emptyList(),
        goal = FinishDoor(x = 99999f, y = 360f),
        houses = houses,
        giftBoxes = giftBoxes,
        santaSleigh = SantaSleighProp(x = 870f, y = 360f),
        returnDoor = ReturnDoorProp(x = 92f, y = 360f),
        deathY = 760f
    )
}
