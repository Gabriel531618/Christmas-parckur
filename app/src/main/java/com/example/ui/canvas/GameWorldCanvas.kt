package com.example.ui.canvas

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.BigTreeProp
import com.example.game.BossKind
import com.example.game.CheckpointItem
import com.example.game.ElfGoblin
import com.example.game.ElfGoblinKind
import com.example.game.FinishDoor
import com.example.game.GiftBoxItem
import com.example.game.GrinchBoss
import com.example.game.LaserMotionAxis
import com.example.game.LevelTheme
import com.example.game.MovingLaser
import com.example.game.ParkourEngine
import com.example.game.Platform
import com.example.game.PlatformKind
import com.example.game.ReturnDoorProp
import com.example.game.SantaSleighProp
import com.example.game.VillageHouse
import com.example.game.WorldDimension
import com.example.model.AbilityType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

private val WorldInk = Color(0xFF0F172A)

@Composable
fun GameWorldCanvas(
    engine: ParkourEngine,
    frameTick: Long,
    onTreeDoorTap: () -> Unit = {},
    onReturnDoorTap: () -> Unit = {},
    onSantaTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coinTextPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#E67700")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = 13f
        }
    }
    val popShadowPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = 15f
        }
    }
    val popFillPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = 15f
        }
    }
    val worldLabelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = 13.5f
            setShadowLayer(4f, 0f, 1.5f, android.graphics.Color.parseColor("#0F172A"))
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(engine) {
                detectTapGestures { tapOffset ->
                    val baseDensity = density
                    val heightDp = size.height / baseDensity
                    val worldScale = if (heightDp < 440f) {
                        baseDensity * max(0.74f, heightDp / 480f)
                    } else {
                        baseDensity
                    }
                    val worldX = tapOffset.x / worldScale + engine.camX
                    val worldY = tapOffset.y / worldScale + engine.camY
                    val lvl = engine.lvlData
                    if (lvl.dimension == WorldDimension.MAIN_LEVEL) {
                        val t = lvl.startTree
                        if (t.hasDoor && abs(worldX - t.x) < 62f && worldY in (t.y - 110f)..(t.y + 20f)) {
                            onTreeDoorTap()
                        }
                    } else {
                        lvl.returnDoor?.let { rd ->
                            if (abs(worldX - rd.x) < 62f && worldY in (rd.y - 110f)..(rd.y + 20f)) {
                                onReturnDoorTap()
                            }
                        }
                        lvl.santaSleigh?.let { sleigh ->
                            if (worldX in (sleigh.x - 165f)..(sleigh.x + 155f) &&
                                worldY in (sleigh.y - 140f)..(sleigh.y + 20f)
                            ) {
                                onSantaTap()
                            }
                        }
                    }
                }
            }
    ) {
        @Suppress("UNUSED_VARIABLE")
        val tick = frameTick

        val lvl = engine.lvlData
        val clock = engine.clock
        val isVillage = lvl.dimension == WorldDimension.SANTAS_VILLAGE

        // Per-level distinct winter night sky (or cozy aurora twilight in Santa's Village)
        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isVillage) {
                    listOf(Color(0xFF061529), Color(0xFF0E2C47), Color(0xFF1A435E))
                } else {
                    listOf(
                        Color(lvl.theme.skyTop),
                        Color(lvl.theme.skyMid),
                        Color(lvl.theme.skyBot)
                    )
                }
            )
        )

        val baseDensity = density
        val heightDp = size.height / baseDensity
        val worldScale = if (heightDp < 440f) {
            baseDensity * max(0.74f, heightDp / 480f)
        } else {
            baseDensity
        }
        val viewW = size.width / worldScale
        val viewH = size.height / worldScale

        withTransform({
            scale(worldScale, worldScale, Offset.Zero)
        }) {
            renderChristmasBackdrop(engine, viewW, viewH, isVillage, lvl.theme)
            renderFlyingSkySantaAndGifts(engine, clock)

            when (engine.powerState.kind) {
                AbilityType.SLOWMO -> drawRect(Color(0xFF74C0FC).copy(alpha = 0.16f), Offset.Zero, Size(viewW, viewH))
                AbilityType.BLIZZARD -> drawRect(Color(0xFF99E9F2).copy(alpha = 0.14f), Offset.Zero, Size(viewW, viewH))
                AbilityType.AURORA_NOVA -> drawRect(Color(0xFF38D9A9).copy(alpha = 0.12f), Offset.Zero, Size(viewW, viewH))
                AbilityType.CANDY_TURBO -> drawRect(Color(0xFFFCC419).copy(alpha = 0.10f), Offset.Zero, Size(viewW, viewH))
                else -> Unit
            }

            val sxOff = if (engine.shakeTimer > 0) (Random.nextFloat() - 0.5f) * 7f else 0f
            val syOff = if (engine.shakeTimer > 0) (Random.nextFloat() - 0.5f) * 7f else 0f

            withTransform({
                translate(-round(engine.camX) + sxOff, -round(engine.camY) + syOff)
            }) {

                // 1. GIANT SWAYING DECORATED TREE WITH GREEN SPLASH EFFECT & PORTAL DOOR (in MAIN_LEVEL)
                if (!isVillage) {
                    renderBigSwayingChristmasTree(
                        tree = lvl.startTree,
                        clock = clock,
                        doorOpenProgress = engine.doorOpenProgress,
                        nearDoor = engine.nearTreeDoor
                    )
                } else {
                    // Return Portal Door Arch in Santa's Village
                    lvl.returnDoor?.let { rd ->
                        renderReturnTreeDoor(rd, clock, engine.nearReturnDoor)
                    }

                    // Snow-covered Houses with glowing lights & chimney smoke!
                    for (house in lvl.houses) {
                        renderSnowyVillageHouse(house, clock)
                    }

                    // Santa Claus & His Big Reindeer Sleigh!
                    lvl.santaSleigh?.let { sleigh ->
                        renderSantaAndBigSleigh(sleigh, clock, engine.nearSanta)
                    }
                }

                // Beacons / Festive Lampposts
                for (b in lvl.beacons) {
                    withTransform({ translate(b.x, b.y) }) {
                        drawRect(Color(0xFF5C3A21), Offset(-4f, -38f), Size(8f, 38f))
                        drawRect(WorldInk, Offset(-4f, -38f), Size(8f, 38f), style = Stroke(2.5f))
                        val r = 7f + sin(clock * 5f + b.x * 0.05f) * 1.6f
                        drawCircle(Color(b.colorHex).copy(alpha = 0.35f), r * 2.1f, Offset(0f, -42f))
                        drawCircle(Color(b.colorHex), r, Offset(0f, -42f))
                        drawCircle(WorldInk, r, Offset(0f, -42f), style = Stroke(2.5f))
                    }
                }

                // 2. PLATFORMS AS THEMED MULTI-STORY BLOCKS / SHORES WITH CHRISTMAS LIGHTS
                lvl.platforms.forEachIndexed { pIdx, p ->
                    if (p.kind == PlatformKind.CYBER_FALL && p.timer > 55f) return@forEachIndexed
                    if (p.x + p.w < engine.camX - 140f || p.x > engine.camX + viewW + 140f) return@forEachIndexed
                    renderThemedPlatform(p, pIdx, lvl.theme, clock, isVillage)
                }

                // 3. CHRISTMAS CHECKPOINTS
                for (cp in lvl.checkpoints) {
                    renderChristmasCheckpoint(cp, clock)
                }

                // Moving Neon Lasers (replacing spikes)
                for (laser in lvl.lasers) {
                    if (!laser.broken) renderMovingLaser(laser, clock)
                }

                // Goblins dressed as Christmas Elves
                for (en in lvl.elfGoblins) {
                    if (en.alive) renderElfGoblin(en, clock)
                }

                // Level 10 Boss (400 HP) & Level 20 Grinch Boss (2,000 HP!)
                lvl.grinchBoss?.let { boss ->
                    if (boss.alive) {
                        renderGrinchBoss(boss, clock, worldLabelPaint)
                    }
                }

                // Bounce Pads
                for (bp in lvl.bouncePads) {
                    val sq = if (bp.squash > 0) 5f else 0f
                    drawRoundRect(
                        Color(0xFFFCC419),
                        Offset(bp.x, bp.y - bp.h + sq),
                        Size(bp.w, bp.h - sq),
                        CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        WorldInk,
                        Offset(bp.x, bp.y - bp.h + sq),
                        Size(bp.w, bp.h - sq),
                        CornerRadius(6f, 6f),
                        style = Stroke(3f)
                    )
                }

                // Collectible Gift Boxes in Santa's Village Dimension!
                for (gb in lvl.giftBoxes) {
                    if (!gb.taken) renderGiftBoxItem(gb, clock)
                }

                // Coins
                for (c in lvl.coins) {
                    if (c.taken) continue
                    val bob = sin(clock * 5f + c.x * 0.05f) * 4f
                    drawCircle(Color(0xFFFFD43B), 12f, Offset(c.x, c.y + bob))
                    drawCircle(WorldInk, 12f, Offset(c.x, c.y + bob), style = Stroke(2.8f))
                    drawContext.canvas.nativeCanvas.drawText("1", c.x, c.y + bob + 4.5f, coinTextPaint)
                }

                // Ice Diamonds
                for (dm in lvl.diamonds) {
                    if (dm.taken) continue
                    val bob = sin(clock * 4.5f + dm.x * 0.05f) * 5f
                    val cy = dm.y + bob
                    val p = Path().apply {
                        moveTo(dm.x, cy - 14f)
                        lineTo(dm.x + 13f, cy - 3f)
                        lineTo(dm.x, cy + 14f)
                        lineTo(dm.x - 13f, cy - 3f)
                        close()
                    }
                    drawPath(p, Color(0xFF4DABF7))
                    drawPath(p, WorldInk, style = Stroke(2.8f))
                }

                // Christmas Finish Door with Opening Portal Effect (all levels!)
                if (!isVillage) {
                    renderFinishChristmasDoor(
                        door = lvl.goal,
                        clock = clock,
                        openProgress = engine.finishDoorOpenProgress,
                        nearDoor = engine.nearFinishDoor,
                        lockedByBoss = (lvl.grinchBoss?.alive == true),
                        labelPaint = worldLabelPaint
                    )
                }

                // Shockwave rings
                for (sr in engine.shockRings) {
                    drawOval(
                        color = sr.color.copy(alpha = sr.life.coerceIn(0f, 1f)),
                        topLeft = Offset(sr.x - sr.r, sr.y - sr.r * 0.35f),
                        size = Size(sr.r * 2f, sr.r * 0.7f),
                        style = Stroke(3f)
                    )
                }

                // 4. BUBBLE JUMP EFFECT PARTICLES
                for (b in engine.jumpBubbles) {
                    val alpha = b.life.coerceIn(0f, 1f)
                    drawCircle(
                        color = b.tint.copy(alpha = alpha * 0.35f),
                        radius = b.radius,
                        center = Offset(b.x, b.y)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.85f),
                        radius = b.radius,
                        center = Offset(b.x, b.y),
                        style = Stroke(1.8f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.9f),
                        radius = b.radius * 0.28f,
                        center = Offset(b.x - b.radius * 0.35f, b.y - b.radius * 0.35f)
                    )
                }

                // 4b. COOL SNOWFLAKE BURST EFFECT ON JUMP
                for (js in engine.jumpSnowflakes) {
                    val alpha = js.life.coerceIn(0f, 1f)
                    val col = js.color.copy(alpha = alpha)
                    drawCircle(
                        color = Color(0xFFBAE6FD).copy(alpha = alpha * 0.28f),
                        radius = js.radius * 1.35f,
                        center = Offset(js.x, js.y)
                    )
                    for (arm in 0 until 3) {
                        val a = (js.rot + arm * 60f) * PI.toFloat() / 180f
                        val dx = cos(a) * js.radius
                        val dy = sin(a) * js.radius
                        drawLine(
                            color = col,
                            start = Offset(js.x - dx, js.y - dy),
                            end = Offset(js.x + dx, js.y + dy),
                            strokeWidth = 2.0f,
                            cap = StrokeCap.Round
                        )
                        // Small crystal branch tips
                        val tipScale = 0.62f
                        val branchLen = js.radius * 0.36f
                        for (sSign in listOf(-1f, 1f)) {
                            val bx = js.x + dx * tipScale * sSign
                            val by = js.y + dy * tipScale * sSign
                            val ba1 = a + 0.7f
                            val ba2 = a - 0.7f
                            drawLine(
                                color = col,
                                start = Offset(bx, by),
                                end = Offset(bx + cos(ba1) * branchLen * sSign, by + sin(ba1) * branchLen * sSign),
                                strokeWidth = 1.4f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = col,
                                start = Offset(bx, by),
                                end = Offset(bx + cos(ba2) * branchLen * sSign, by + sin(ba2) * branchLen * sSign),
                                strokeWidth = 1.4f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    drawCircle(Color.White.copy(alpha = alpha), js.radius * 0.24f, Offset(js.x, js.y))
                }

                // Hero with Squash & Stretch + Smooth Walk Bob & Tilt + Shield
                val hero = engine.heroObj
                withTransform({
                    translate(hero.x + hero.w / 2f, hero.y + hero.h + hero.walkBobY)
                    scale(hero.sx, hero.sy, Offset.Zero)
                }) {
                    if (abs(hero.vx) > 3.2f) {
                        val lineCol = Color(0xFFD0EBFF).copy(alpha = 0.85f)
                        drawLine(lineCol, Offset(-hero.dir * 24f, -hero.h * 0.75f), Offset(-hero.dir * 40f, -hero.h * 0.75f), strokeWidth = 2.5f)
                        drawLine(lineCol, Offset(-hero.dir * 22f, -hero.h * 0.35f), Offset(-hero.dir * 36f, -hero.h * 0.35f), strokeWidth = 2.5f)
                    }

                    if (engine.powerState.solarShield) {
                        rotate(degrees = (clock * 2f * 180f / PI.toFloat()), pivot = Offset(0f, -hero.h / 2f)) {
                            drawCircle(
                                color = Color(0xFF51CF66),
                                radius = hero.w * 0.82f,
                                center = Offset(0f, -hero.h / 2f),
                                style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
                            )
                        }
                    }

                    when (engine.powerState.kind) {
                        AbilityType.BLIZZARD -> {
                            rotate(degrees = clock * 140f, pivot = Offset(0f, -hero.h / 2f)) {
                                drawCircle(
                                    color = Color(0xFF99E9F2).copy(alpha = 0.85f),
                                    radius = hero.w * 1.05f,
                                    center = Offset(0f, -hero.h / 2f),
                                    style = Stroke(3.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                                )
                            }
                        }
                        AbilityType.AURORA_NOVA -> {
                            drawCircle(
                                color = Color(0xFF38D9A9).copy(alpha = 0.55f),
                                radius = hero.w * (0.98f + 0.12f * sin(clock * 7f)),
                                center = Offset(0f, -hero.h / 2f),
                                style = Stroke(3.5f)
                            )
                        }
                        AbilityType.CANDY_TURBO -> {
                            rotate(degrees = -clock * 180f, pivot = Offset(0f, -hero.h / 2f)) {
                                drawCircle(
                                    color = Color(0xFFE03131).copy(alpha = 0.85f),
                                    radius = hero.w * 0.96f,
                                    center = Offset(0f, -hero.h / 2f),
                                    style = Stroke(3.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f)))
                                )
                            }
                        }
                        else -> Unit
                    }

                    drawHeroWithEffects(
                        heroId = engine.equippedHeroId,
                        x = -hero.w / 2f,
                        y = -hero.h,
                        w = hero.w,
                        h = hero.h,
                        dir = hero.dir,
                        t = clock,
                        vy = hero.vy,
                        isShopPreview = false,
                        tiltDeg = hero.walkTiltDeg
                    )
                }

                // Particles
                for (pt in engine.fxParticles) {
                    val alpha = pt.life.coerceIn(0f, 1f)
                    val tl = Offset(pt.x - pt.size / 2f, pt.y - pt.size / 2f)
                    val sz = Size(pt.size, pt.size)
                    drawRect(pt.color.copy(alpha = alpha), tl, sz)
                    drawRect(WorldInk.copy(alpha = alpha), tl, sz, style = Stroke(1.5f))
                }

                // Popups
                for (ft in engine.popTexts) {
                    val a = (ft.life.coerceIn(0f, 1f) * 255).toInt()
                    popShadowPaint.alpha = a
                    popFillPaint.color = android.graphics.Color.argb(
                        a,
                        (ft.color.red * 255).toInt(),
                        (ft.color.green * 255).toInt(),
                        (ft.color.blue * 255).toInt()
                    )
                    drawContext.canvas.nativeCanvas.drawText(ft.text, ft.x + 1.5f, ft.y + 1.5f, popShadowPaint)
                    drawContext.canvas.nativeCanvas.drawText(ft.text, ft.x, ft.y, popFillPaint)
                }
            }

            // 5. FOREGROUND CHRISTMAS SNOWFALL EFFECT
            for (sf in engine.snowflakes) {
                drawCircle(
                    color = Color.White.copy(alpha = sf.alpha),
                    radius = sf.radius,
                    center = Offset(sf.x, sf.y)
                )
            }
        }
    }
}

private fun DrawScope.renderChristmasBackdrop(
    engine: ParkourEngine,
    viewW: Float,
    viewH: Float,
    isVillage: Boolean,
    theme: LevelTheme
) {
    // Soft Aurora Borealis glow themed per level
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                (if (isVillage) Color(0xFF40C057) else Color(theme.auroraPrimary)).copy(alpha = if (isVillage) 0.28f else 0.24f),
                Color.Transparent
            ),
            center = Offset(viewW * 0.3f, viewH * 0.22f),
            radius = max(20f, viewW * 0.55f)
        ),
        radius = viewW * 0.55f,
        center = Offset(viewW * 0.3f, viewH * 0.22f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                (if (isVillage) Color(0xFFFCC419) else Color(theme.auroraSecondary)).copy(alpha = if (isVillage) 0.16f else 0.22f),
                Color.Transparent
            ),
            center = Offset(viewW * 0.78f, viewH * 0.18f),
            radius = max(20f, viewW * 0.42f)
        ),
        radius = viewW * 0.42f,
        center = Offset(viewW * 0.78f, viewH * 0.18f)
    )

    // Moon (Warm full moon in Santa's Village, themed crescent moon in Main Level)
    val mx = viewW * 0.83f - ((engine.camX * 0.02f) % max(1f, viewW * 0.4f))
    val my = viewH * 0.19f
    if (isVillage) {
        drawCircle(Color(0xFFFFF3BF).copy(alpha = 0.26f), 46f, Offset(mx, my))
        drawCircle(Color(0xFFFFF3BF), 28f, Offset(mx, my))
        drawCircle(Color(0xFFFFE066).copy(alpha = 0.45f), 7f, Offset(mx - 8f, my - 6f))
        drawCircle(Color(0xFFFFE066).copy(alpha = 0.35f), 5f, Offset(mx + 9f, my + 7f))
    } else {
        drawCircle(Color(0xFFFFF3BF), 26f, Offset(mx, my))
        drawCircle(Color(theme.skyMid), 22f, Offset(mx + 9f, my - 5f))
    }

    // Twinkling Christmas Night Stars
    val wrapW = viewW + 80f
    for (st in engine.cosmicStars) {
        val sx = (((st.x - engine.camX * st.speed) % wrapW) + wrapW) % wrapW
        val sy = st.y % max(1f, viewH * 0.74f)
        val alpha = 0.4f + 0.6f * abs(sin(engine.clock * 2.4f + st.phase))
        drawCircle(st.color.copy(alpha = alpha), st.r, Offset(sx, sy))
    }

    // Distant snowy pine hills silhouette tinted to current district
    val hillBaseY = viewH * 0.88f
    val shift = (engine.camX * 0.12f) % 240f
    val hillFill = if (isVillage) Color(0xFF0D2847).copy(alpha = 0.65f) else Color(theme.hillColor).copy(alpha = 0.68f)
    for (k in -1..6) {
        val hx = k * 240f - shift
        val hill = Path().apply {
            moveTo(hx, viewH)
            lineTo(hx, hillBaseY)
            lineTo(hx + 120f, hillBaseY - 75f)
            lineTo(hx + 240f, hillBaseY)
            lineTo(hx + 240f, viewH)
            close()
        }
        drawPath(hill, hillFill)
    }

    for (ss in engine.shootingStars) {
        val sx = ss.x - engine.camX * 0.3f
        drawLine(
            color = Color(0xFFFFF3BF).copy(alpha = ss.life.coerceIn(0f, 1f)),
            start = Offset(sx, ss.y),
            end = Offset(sx - ss.vx * 3f, ss.y - ss.vy * 3f),
            strokeWidth = 2.5f
        )
    }
}

private fun DrawScope.renderStringLightsGarland(
    xStart: Float,
    xEnd: Float,
    yBase: Float,
    palette: List<Long>,
    clock: Float,
    seedOffset: Int,
    bulbSpacing: Float = 18f,
    sagDepth: Float = 5.5f,
    bulbRadius: Float = 3.2f
) {
    val span = xEnd - xStart
    if (span < 12f || palette.isEmpty()) return
    var bx = xStart + 6f
    var idx = 0
    var prevX = xStart
    var prevY = yBase
    while (bx <= xEnd - 6f) {
        val wave = if (idx % 2 == 0) sagDepth else sagDepth * 0.35f
        val by = yBase + wave
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(prevX, prevY),
            end = Offset(bx, by),
            strokeWidth = 1.6f
        )
        val col = Color(palette[(idx + seedOffset) % palette.size])
        val blink = 0.45f + 0.55f * abs(sin(clock * 4.8f + idx * 0.78f + seedOffset * 0.31f))
        drawCircle(col.copy(alpha = blink * 0.45f), bulbRadius * 2.1f, Offset(bx, by + 2.2f))
        drawCircle(col.copy(alpha = blink), bulbRadius, Offset(bx, by + 2.2f))
        prevX = bx
        prevY = by
        bx += bulbSpacing
        idx++
    }
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(prevX, prevY),
        end = Offset(xEnd, yBase),
        strokeWidth = 1.6f
    )
}

private fun DrawScope.renderFlyingSkySantaAndGifts(
    engine: ParkourEngine,
    clock: Float
) {
    // 1. Magic golden stardust trail drifting behind Santa's flying sleigh
    for (sp in engine.skyTrailSparks) {
        val alpha = sp.life.coerceIn(0f, 1f)
        drawCircle(sp.color.copy(alpha = alpha * 0.42f), sp.size * 2.1f, Offset(sp.x, sp.y))
        drawCircle(sp.color.copy(alpha = alpha), sp.size, Offset(sp.x, sp.y))
    }

    // 2. Falling wrapped Christmas gifts tossed down from Santa's full sack
    for (gb in engine.skyGiftDrops) {
        val alpha = gb.life.coerceIn(0f, 1f)
        withTransform({
            translate(gb.x, gb.y)
            rotate(degrees = gb.rot, pivot = Offset.Zero)
        }) {
            if (gb.hasParachute) {
                // Cute festive red-and-white Christmas parachute above the falling gift
                drawLine(Color.White.copy(alpha = alpha * 0.85f), Offset(-6f, -7f), Offset(-11f, -20f), 1.3f)
                drawLine(Color.White.copy(alpha = alpha * 0.85f), Offset(6f, -7f), Offset(11f, -20f), 1.3f)
                drawArc(
                    color = Color(0xFFFA5252).copy(alpha = alpha),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(-13f, -30f),
                    size = Size(26f, 20f)
                )
                drawArc(
                    color = Color.White.copy(alpha = alpha),
                    startAngle = 215f,
                    sweepAngle = 110f,
                    useCenter = true,
                    topLeft = Offset(-13f, -30f),
                    size = Size(26f, 20f)
                )
                drawArc(
                    color = WorldInk.copy(alpha = alpha * 0.85f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(-13f, -30f),
                    size = Size(26f, 20f),
                    style = Stroke(1.6f)
                )
            }

            // Golden sparkle glow + wrapped gift box with cross ribbon & bow
            drawCircle(gb.ribbonColor.copy(alpha = alpha * 0.25f), 14f, Offset.Zero)
            drawRoundRect(gb.boxColor.copy(alpha = alpha), Offset(-8f, -8f), Size(16f, 16f), CornerRadius(3f, 3f))
            drawRect(gb.ribbonColor.copy(alpha = alpha), Offset(-1.8f, -8f), Size(3.6f, 16f))
            drawRect(gb.ribbonColor.copy(alpha = alpha), Offset(-8f, -1.8f), Size(16f, 3.6f))
            drawRoundRect(WorldInk.copy(alpha = alpha), Offset(-8f, -8f), Size(16f, 16f), CornerRadius(3f, 3f), style = Stroke(1.8f))
            drawCircle(gb.ribbonColor.copy(alpha = alpha), 2.8f, Offset(-3f, -9.5f))
            drawCircle(gb.ribbonColor.copy(alpha = alpha), 2.8f, Offset(3f, -9.5f))
        }
    }

    // 3. Flying Santa Claus with Crimson Sleigh, Overflowing Sack & 3 Galloping Reindeer!
    withTransform({
        translate(engine.skySantaX, engine.skySantaY)
        scale(engine.skySantaDir.toFloat(), 1f, Offset.Zero)
    }) {
        // Soft golden Christmas flight halo behind the sleigh
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFCC419).copy(alpha = 0.24f), Color.Transparent),
                center = Offset(20f, -4f),
                radius = 95f
            ),
            radius = 95f,
            center = Offset(20f, -4f)
        )

        // Golden magic reins from sleigh to all 3 flying reindeer
        drawLine(Color(0xFFFCC419).copy(alpha = 0.9f), Offset(18f, -6f), Offset(120f, -4f), 1.6f)
        drawLine(Color(0xFFFCC419).copy(alpha = 0.85f), Offset(18f, -3f), Offset(86f, -2f), 1.4f)

        // 3 Animated Flying Reindeer pulling forward (+X)
        val reindeerXs = listOf(50f, 84f, 118f)
        reindeerXs.forEachIndexed { idx, rx ->
            val gallop = sin(clock * 10.5f + idx * 1.15f)
            val bobY = gallop * 3.5f
            val legSwing = gallop * 6f
            withTransform({ translate(rx, -2f + bobY) }) {
                // Galloping legs
                drawLine(Color(0xFF5C3A21), Offset(-6f, 5f), Offset(-11f - legSwing, 13f), 2.6f, StrokeCap.Round)
                drawLine(Color(0xFF5C3A21), Offset(6f, 5f), Offset(11f + legSwing, 13f), 2.6f, StrokeCap.Round)
                // Reindeer body
                drawRoundRect(Color(0xFF8B5A2B), Offset(-11f, -5f), Size(22f, 12f), CornerRadius(6f, 6f))
                drawRoundRect(WorldInk, Offset(-11f, -5f), Size(22f, 12f), CornerRadius(6f, 6f), style = Stroke(1.8f))
                // Festive red harness collar
                drawLine(Color(0xFFE03131), Offset(4f, -5f), Offset(4f, 6f), 3f)
                // Head & antlers facing forward (+X)
                drawCircle(Color(0xFF9C6635), 6.2f, Offset(11f, -8f))
                drawCircle(WorldInk, 6.2f, Offset(11f, -8f), style = Stroke(1.6f))
                drawLine(Color(0xFFD4A373), Offset(9f, -13f), Offset(5f, -21f), 1.9f, StrokeCap.Round)
                drawLine(Color(0xFFD4A373), Offset(11f, -13f), Offset(14f, -21f), 1.9f, StrokeCap.Round)
                drawCircle(WorldInk, 1.3f, Offset(13f, -9f))
                // Lead reindeer Rudolph's glowing red nose!
                if (idx == 2) {
                    val glow = 0.65f + 0.35f * sin(clock * 7f)
                    drawCircle(Color(0xFFFA5252).copy(alpha = glow * 0.55f), 6.5f, Offset(17.5f, -7.5f))
                    drawCircle(Color(0xFFFA5252), 2.9f, Offset(17.5f, -7.5f))
                } else {
                    drawCircle(Color(0xFF3E2412), 2.0f, Offset(17f, -7.5f))
                }
            }
        }

        // Big Overflowing Gift Sack ("sacul plin") in the back of the sleigh (-X)
        drawCircle(Color(0xFF9C5221), 16f, Offset(-22f, -10f))
        drawCircle(WorldInk, 16f, Offset(-22f, -10f), style = Stroke(2.2f))
        // Presents peeking out of the top of the full sack
        drawRect(Color(0xFF2FB344), Offset(-32f, -29f), Size(11f, 11f))
        drawRect(Color(0xFFFCC419), Offset(-28f, -29f), Size(3f, 11f))
        drawRect(WorldInk, Offset(-32f, -29f), Size(11f, 11f), style = Stroke(1.6f))
        drawRect(Color(0xFF339AF0), Offset(-20f, -27f), Size(10f, 10f))
        drawRect(Color(0xFFFA5252), Offset(-16f, -27f), Size(2.8f, 10f))
        drawRect(WorldInk, Offset(-20f, -27f), Size(10f, 10f), style = Stroke(1.6f))

        // Jolly Santa Claus sitting in the sleigh & tossing gifts from his sack
        withTransform({ translate(-4f, -11f) }) {
            // Red coat torso
            drawRoundRect(Color(0xFFE03131), Offset(-9f, -10f), Size(18f, 16f), CornerRadius(6f, 6f))
            drawRoundRect(WorldInk, Offset(-9f, -10f), Size(18f, 16f), CornerRadius(6f, 6f), style = Stroke(1.9f))
            // Face + fluffy white beard
            drawCircle(Color(0xFFFFD8B5), 6.8f, Offset(1f, -15f))
            drawCircle(WorldInk, 6.8f, Offset(1f, -15f), style = Stroke(1.7f))
            drawOval(Color.White, Offset(-4.5f, -14f), Size(12f, 8f))
            drawCircle(WorldInk, 1.2f, Offset(3.8f, -16.5f))
            // Red Santa hat + white trim + bobbing pom-pom
            val hatPath = Path().apply {
                moveTo(-6f, -19f)
                lineTo(-9f, -29f)
                lineTo(7f, -19f)
                close()
            }
            drawPath(hatPath, Color(0xFFE03131))
            drawPath(hatPath, WorldInk, style = Stroke(1.6f))
            drawRoundRect(Color.White, Offset(-7f, -21f), Size(15f, 3.6f), CornerRadius(2f, 2f))
            drawCircle(Color.White, 3.0f, Offset(-9.5f, -29f))

            // Animated Santa throwing arm dipping toward sack & tossing gifts downward
            val armAngle = -20f - engine.skySantaThrowAnim * 75f + sin(clock * 4f) * 10f
            rotate(degrees = armAngle, pivot = Offset(-3f, -6f)) {
                drawLine(Color(0xFFE03131), Offset(-3f, -6f), Offset(-13f, -12f), 4.6f, StrokeCap.Round)
                drawCircle(Color.White, 2.8f, Offset(-14f, -12.5f))
            }
        }

        // Crimson & Gold Curved Sleigh Body & Runners
        val runner = Path().apply {
            moveTo(-36f, 10f)
            lineTo(24f, 10f)
            quadraticTo(36f, 10f, 38f, 1f)
        }
        drawPath(runner, Color(0xFFFCC419), style = Stroke(3.6f, cap = StrokeCap.Round))
        drawLine(Color(0xFFFCC419), Offset(-20f, 10f), Offset(-20f, 3f), 2.5f)
        drawLine(Color(0xFFFCC419), Offset(14f, 10f), Offset(14f, 3f), 2.5f)

        val hull = Path().apply {
            moveTo(-34f, -12f)
            lineTo(-34f, 4f)
            lineTo(22f, 4f)
            quadraticTo(34f, 4f, 32f, -14f)
            lineTo(18f, -14f)
            quadraticTo(10f, -4f, -6f, -4f)
            lineTo(-22f, -12f)
            close()
        }
        drawPath(hull, Color(0xFFE03131))
        drawPath(hull, WorldInk, style = Stroke(2.2f))
        drawLine(Color(0xFFFCC419), Offset(-30f, -1f), Offset(26f, -1f), 2.2f)
        // Front glowing golden sleigh lantern
        drawCircle(Color(0xFFFFE066).copy(alpha = 0.55f), 8f, Offset(32f, -14f))
        drawCircle(Color(0xFFFCC419), 3.4f, Offset(32f, -14f))
    }
}

private fun DrawScope.renderBigSwayingChristmasTree(
    tree: BigTreeProp,
    clock: Float,
    doorOpenProgress: Float,
    nearDoor: Boolean
) {
    val baseX = tree.x
    val baseY = tree.y

    // Slow, smooth multi-harmonic winter wind sway for the tree and each branch!
    val windBase = sin(clock * 0.85f) * 0.7f + sin(clock * 1.45f + 0.4f) * 0.3f
    val sway1 = windBase * 5.0f
    val sway2 = sin(clock * 0.85f - 0.35f) * 8.5f
    val sway3 = sin(clock * 0.85f - 0.65f) * 12.5f
    val sway4 = sin(clock * 0.85f - 0.95f) * 16.5f
    val sway5 = sin(clock * 0.85f - 1.25f) * 21.0f

    // 1. RICH GREEN SPLASH EFFECT (emerald aura + pulsing splash rings + green splash droplets)
    val splashPulse = 0.32f + 0.14f * sin(clock * 2.2f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF69DB7C).copy(alpha = splashPulse + 0.16f),
                Color(0xFF2FB344).copy(alpha = splashPulse),
                Color(0xFF15803D).copy(alpha = splashPulse * 0.45f),
                Color.Transparent
            ),
            center = Offset(baseX + sway3 * 0.5f, baseY - 138f),
            radius = 195f
        ),
        radius = 195f,
        center = Offset(baseX + sway3 * 0.5f, baseY - 138f)
    )

    // Animated Green Splash Wave Rings expanding gently from the tree
    for (ringIdx in 0 until 2) {
        val phase = (clock * 0.48f + ringIdx * 0.5f) % 1f
        val r = 48f + phase * 125f
        val alpha = (1f - phase) * 0.42f
        drawCircle(
            color = Color(0xFF69DB7C).copy(alpha = alpha),
            radius = r,
            center = Offset(baseX + sway2 * 0.4f, baseY - 132f),
            style = Stroke(width = 4.5f * (1f - phase * 0.6f))
        )
    }

    // Splashing green droplets & teardrop emerald splash lobes around the canopy & base!
    for (i in 0 until 18) {
        val cycle = (clock * 0.55f + i * 0.17f) % 1f
        val angle = -PI.toFloat() * 0.95f + (i / 17f) * PI.toFloat() * 0.9f
        val dist = 54f + cycle * 74f
        val px = baseX + sway2 * 0.5f + cos(angle) * dist * 1.18f
        val py = baseY - 135f + sin(angle) * dist * 0.95f - cycle * 22f
        val a = (1f - cycle) * 0.88f
        val sz = 3.8f + (i % 3) * 1.6f
        drawCircle(Color(0xFF8CE99A).copy(alpha = a), sz, Offset(px, py))
        drawCircle(Color(0xFF40C057).copy(alpha = a * 0.65f), sz * 2.0f, Offset(px, py))
        // Emerald splash teardrop tail
        drawLine(
            color = Color(0xFF69DB7C).copy(alpha = a * 0.75f),
            start = Offset(px, py),
            end = Offset(px - cos(angle) * 10f, py - sin(angle) * 10f),
            strokeWidth = 2.6f,
            cap = StrokeCap.Round
        )
    }

    // Lush emerald green splash lobes fanning out at tree base
    for (side in listOf(-1f, 1f)) {
        val splashBob = sin(clock * 2.5f + side) * 4f
        val splashPath = Path().apply {
            moveTo(baseX + side * 32f, baseY - 6f)
            quadraticTo(baseX + side * 66f, baseY - 26f + splashBob, baseX + side * 86f, baseY - 12f + splashBob)
            quadraticTo(baseX + side * 62f, baseY - 2f, baseX + side * 32f, baseY - 2f)
            close()
        }
        drawPath(splashPath, Color(0xFF51CF66).copy(alpha = 0.65f))
    }

    // 2. Sturdy Wide Trunk housing the Magical Christmas Tree Door!
    val trunkW = 70f
    val trunkH = 74f
    val trunkTopY = baseY - trunkH
    val trunkPath = Path().apply {
        moveTo(baseX - trunkW * 0.46f + sway1 * 0.15f, trunkTopY)
        lineTo(baseX + trunkW * 0.46f + sway1 * 0.15f, trunkTopY)
        lineTo(baseX + trunkW * 0.54f, baseY)
        lineTo(baseX - trunkW * 0.54f, baseY)
        close()
    }
    drawPath(trunkPath, Color(0xFF5C3A21))
    drawPath(trunkPath, WorldInk, style = Stroke(3.2f))

    // Bark texture lines
    drawLine(Color(0xFF3E2412), Offset(baseX - 24f, baseY - 56f), Offset(baseX - 26f, baseY - 8f), 2.2f)
    drawLine(Color(0xFF3E2412), Offset(baseX + 25f, baseY - 52f), Offset(baseX + 27f, baseY - 10f), 2.2f)

    // 3. Gift boxes flanking the grand tree trunk
    drawRect(Color(0xFFE03131), Offset(baseX - 78f, baseY - 24f), Size(24f, 24f))
    drawRect(Color(0xFFFCC419), Offset(baseX - 68f, baseY - 24f), Size(5f, 24f))
    drawRect(Color(0xFFFCC419), Offset(baseX - 78f, baseY - 14f), Size(24f, 5f))
    drawRect(WorldInk, Offset(baseX - 78f, baseY - 24f), Size(24f, 24f), style = Stroke(2.5f))

    drawRect(Color(0xFF4DABF7), Offset(baseX + 52f, baseY - 22f), Size(22f, 22f))
    drawRect(Color(0xFFFCC419), Offset(baseX + 61f, baseY - 22f), Size(4.5f, 22f))
    drawRect(WorldInk, Offset(baseX + 52f, baseY - 22f), Size(22f, 22f), style = Stroke(2.5f))

    // 4. MAGIC PORTAL DOOR IN THE TREE TRUNK!
    if (tree.hasDoor) {
        val doorW = tree.doorWidth
        val doorH = tree.doorHeight
        val doorLeft = baseX - doorW / 2f
        val doorTop = baseY - doorH

        // Glowing golden-emerald doorway frame
        val frameGlow = if (nearDoor || doorOpenProgress > 0f) 0.55f + 0.35f * sin(clock * 6f) else 0.25f
        drawRoundRect(
            color = Color(0xFF69DB7C).copy(alpha = frameGlow),
            topLeft = Offset(doorLeft - 7f, doorTop - 7f),
            size = Size(doorW + 14f, doorH + 9f),
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = Color(0xFFFCC419),
            topLeft = Offset(doorLeft - 4f, doorTop - 4f),
            size = Size(doorW + 8f, doorH + 4f),
            cornerRadius = CornerRadius(22f, 22f)
        )
        drawRoundRect(
            color = WorldInk,
            topLeft = Offset(doorLeft - 4f, doorTop - 4f),
            size = Size(doorW + 8f, doorH + 4f),
            cornerRadius = CornerRadius(22f, 22f),
            style = Stroke(2.6f)
        )

        // Glowing Magic Dimension Portal behind the door!
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFD8F5A2), Color(0xFF51CF66), Color(0xFF0B7285)),
                center = Offset(baseX, doorTop + doorH * 0.48f),
                radius = 38f
            ),
            topLeft = Offset(doorLeft, doorTop),
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(20f, 20f)
        )

        // Swinging wooden door leaf (animates open smoothly via doorOpenProgress)
        val leafScale = (1f - doorOpenProgress * 0.92f).coerceAtLeast(0.08f)
        withTransform({
            translate(doorLeft, doorTop)
            scale(leafScale, 1f, Offset.Zero)
        }) {
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF9C5221), Color(0xFF6B3713))),
                topLeft = Offset.Zero,
                size = Size(doorW, doorH),
                cornerRadius = CornerRadius(20f, 20f)
            )
            // Plank lines
            drawLine(Color(0xFF4A250B), Offset(doorW * 0.34f, 6f), Offset(doorW * 0.34f, doorH - 2f), 1.8f)
            drawLine(Color(0xFF4A250B), Offset(doorW * 0.68f, 6f), Offset(doorW * 0.68f, doorH - 2f), 1.8f)

            // Mini green wreath on the door
            drawCircle(Color(0xFF2FB344), 9f, Offset(doorW * 0.5f, doorH * 0.36f), style = Stroke(4.2f))
            drawCircle(Color(0xFFFA5252), 2.8f, Offset(doorW * 0.5f, doorH * 0.25f))

            // Golden doorknob & emerald keyhole glow
            drawCircle(Color(0xFF51CF66).copy(alpha = 0.7f), 5f, Offset(doorW * 0.8f, doorH * 0.58f))
            drawCircle(Color(0xFFFCC419), 3.6f, Offset(doorW * 0.8f, doorH * 0.58f))
            drawCircle(WorldInk, 3.6f, Offset(doorW * 0.8f, doorH * 0.58f), style = Stroke(1.4f))

            drawRoundRect(
                color = WorldInk,
                topLeft = Offset.Zero,
                size = Size(doorW, doorH),
                cornerRadius = CornerRadius(20f, 20f),
                style = Stroke(2.6f)
            )
        }
    }

    // 5. FIVE LARGE SWAYING BRANCH TIERS WITH WIND-CURVED BOUGHS & SNOW CAPS!
    data class TreeTier(
        val topY: Float,
        val botY: Float,
        val halfW: Float,
        val topSway: Float,
        val botSway: Float,
        val color: Color
    )

    val tiers = listOf(
        TreeTier(baseY - 122f, baseY - 58f, 94f, sway2, sway1 * 0.55f, Color(0xFF126B33)),
        TreeTier(baseY - 166f, baseY - 102f, 78f, sway3, sway2 * 0.8f, Color(0xFF15803D)),
        TreeTier(baseY - 204f, baseY - 144f, 62f, sway4, sway3 * 0.85f, Color(0xFF1E9E49)),
        TreeTier(baseY - 238f, baseY - 184f, 45f, sway4 * 1.05f, sway4 * 0.8f, Color(0xFF2FB344)),
        TreeTier(baseY - 268f, baseY - 220f, 28f, sway5, sway4, Color(0xFF40C057))
    )

    tiers.forEachIndexed { idx, tier ->
        val leftFlutter = sin(clock * 1.4f + idx * 0.65f) * (3.2f + idx * 0.8f)
        val rightFlutter = sin(clock * 1.4f + idx * 0.65f + 0.9f) * (3.2f + idx * 0.8f)

        val topPt = Offset(baseX + tier.topSway, tier.topY)
        val rightTip = Offset(baseX + tier.halfW + tier.botSway + rightFlutter, tier.botY)
        val midDip = Offset(baseX + (tier.botSway + tier.topSway) * 0.5f, tier.botY - 8f)
        val leftTip = Offset(baseX - tier.halfW + tier.botSway + leftFlutter, tier.botY)

        // Swaying curved branch silhouette
        val branchPath = Path().apply {
            moveTo(topPt.x, topPt.y)
            quadraticTo(
                baseX + tier.halfW * 0.68f + tier.topSway * 0.7f,
                (tier.topY + tier.botY) * 0.54f,
                rightTip.x,
                rightTip.y
            )
            // Scalloped swaying pine bough bottom
            quadraticTo(
                (rightTip.x + midDip.x) * 0.5f,
                tier.botY + 7f,
                midDip.x,
                midDip.y
            )
            quadraticTo(
                (leftTip.x + midDip.x) * 0.5f,
                tier.botY + 7f,
                leftTip.x,
                leftTip.y
            )
            quadraticTo(
                baseX - tier.halfW * 0.68f + tier.topSway * 0.7f,
                (tier.topY + tier.botY) * 0.54f,
                topPt.x,
                topPt.y
            )
            close()
        }
        drawPath(branchPath, tier.color)
        drawPath(branchPath, WorldInk, style = Stroke(3.0f))

        // Individual wind-swaying projecting pine boughs on left and right sides!
        for (side in listOf(-1f, 1f)) {
            val sideTip = if (side < 0f) leftTip else rightTip
            val boughWave = sin(clock * 1.7f + idx * 0.8f + side) * 4.8f
            val subBough = Path().apply {
                moveTo(sideTip.x - side * 14f, sideTip.y - 10f)
                quadraticTo(
                    sideTip.x + side * 8f,
                    sideTip.y - 4f + boughWave * 0.6f,
                    sideTip.x + side * 16f,
                    sideTip.y + 4f + boughWave
                )
                quadraticTo(
                    sideTip.x + side * 2f,
                    sideTip.y + 7f + boughWave * 0.4f,
                    sideTip.x - side * 12f,
                    sideTip.y + 1f
                )
                close()
            }
            drawPath(subBough, tier.color)
            drawPath(subBough, WorldInk, style = Stroke(2.2f))
        }

        // Soft snow highlights resting on the swaying branch tips!
        drawCircle(Color(0xFFF8FCFF).copy(alpha = 0.9f), 6.2f - idx * 0.5f, Offset(leftTip.x + 6f, leftTip.y - 3f))
        drawCircle(Color(0xFFF8FCFF).copy(alpha = 0.9f), 6.2f - idx * 0.5f, Offset(rightTip.x - 6f, rightTip.y - 3f))
    }

    // 6. Twinkling Garlands & Colorful Christmas Lights swaying with the branches!
    val lightColors = listOf(
        Color(0xFFFA5252),
        Color(0xFFFCC419),
        Color(0xFF4DABF7),
        Color(0xFFF06595),
        Color(0xFF69DB7C),
        Color(0xFFFF922B)
    )
    val bulbs = listOf(
        Offset(baseX - 58f + sway1 * 0.7f, baseY - 72f),
        Offset(baseX - 24f + sway1 * 0.8f, baseY - 68f),
        Offset(baseX + 20f + sway1 * 0.8f, baseY - 68f),
        Offset(baseX + 56f + sway1 * 0.7f, baseY - 74f),

        Offset(baseX - 46f + sway2 * 0.85f, baseY - 116f),
        Offset(baseX - 8f + sway2 * 0.9f, baseY - 112f),
        Offset(baseX + 34f + sway2 * 0.85f, baseY - 116f),

        Offset(baseX - 34f + sway3 * 0.95f, baseY - 156f),
        Offset(baseX + 4f + sway3 * 0.95f, baseY - 152f),
        Offset(baseX + 32f + sway3 * 0.95f, baseY - 158f),

        Offset(baseX - 22f + sway4, baseY - 196f),
        Offset(baseX + 18f + sway4, baseY - 196f),

        Offset(baseX - 10f + sway5 * 0.9f, baseY - 232f),
        Offset(baseX + 12f + sway5 * 0.9f, baseY - 234f)
    )
    bulbs.forEachIndexed { idx, pos ->
        val blink = 0.45f + 0.55f * abs(sin(clock * 4.2f + idx * 0.85f))
        val c = lightColors[idx % lightColors.size]
        drawCircle(c.copy(alpha = blink * 0.5f), 10.5f, pos)
        drawCircle(c.copy(alpha = blink), 4.6f, pos)
        drawCircle(WorldInk, 4.6f, pos, style = Stroke(1.5f))
    }

    // 7. Radiant Golden Star Topper swaying at the crown!
    val starCenter = Offset(baseX + sway5, baseY - 276f)
    drawCircle(Color(0xFFFFE066).copy(alpha = 0.45f), 22f, starCenter)
    val starPath = Path().apply {
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) 14f else 6f
            val a = -PI.toFloat() / 2f + i * (PI.toFloat() / 5f)
            val px = starCenter.x + cos(a) * r
            val py = starCenter.y + sin(a) * r
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(starPath, Color(0xFFFCC419))
    drawPath(starPath, WorldInk, style = Stroke(2.4f))
}

private fun DrawScope.renderReturnTreeDoor(
    rd: ReturnDoorProp,
    clock: Float,
    nearDoor: Boolean
) {
    val x = rd.x
    val y = rd.y
    val w = rd.width
    val h = rd.height

    val pulse = if (nearDoor) 0.58f + 0.3f * sin(clock * 5f) else 0.34f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF51CF66).copy(alpha = pulse), Color.Transparent),
            center = Offset(x, y - h * 0.55f),
            radius = 75f
        ),
        radius = 75f,
        center = Offset(x, y - h * 0.55f)
    )

    // Snow-topped cozy timber portal frame
    drawRoundRect(
        color = Color(0xFF5C3A21),
        topLeft = Offset(x - w / 2f - 8f, y - h - 10f),
        size = Size(w + 16f, h + 10f),
        cornerRadius = CornerRadius(28f, 28f)
    )
    drawRoundRect(
        color = WorldInk,
        topLeft = Offset(x - w / 2f - 8f, y - h - 10f),
        size = Size(w + 16f, h + 10f),
        cornerRadius = CornerRadius(28f, 28f),
        style = Stroke(3f)
    )

    // Swirling green-gold return portal vortex
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFF8CE99A), Color(0xFF15803D)),
            center = Offset(x, y - h * 0.5f),
            radius = 36f
        ),
        topLeft = Offset(x - w / 2f, y - h),
        size = Size(w, h),
        cornerRadius = CornerRadius(22f, 22f)
    )
    rotate(degrees = clock * 75f, pivot = Offset(x, y - h * 0.5f)) {
        drawCircle(
            color = Color(0xFFFFE066),
            radius = 15f,
            center = Offset(x, y - h * 0.5f),
            style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)))
        )
    }
    // Snow cap on arch
    drawOval(Color(0xFFF8FCFF), Offset(x - w / 2f - 10f, y - h - 15f), Size(w + 20f, 12f))
    drawOval(WorldInk, Offset(x - w / 2f - 10f, y - h - 15f), Size(w + 20f, 12f), style = Stroke(2.2f))
}

private fun DrawScope.renderSnowyVillageHouse(house: VillageHouse, clock: Float) {
    val x = house.x
    val y = house.y
    val w = house.w
    val h = house.h
    val wallTop = y - h * 0.58f
    val wallH = h * 0.58f
    val roofPeakY = y - h

    // Extra festive halo when the house lights have been activated!
    if (house.litBoosted) {
        val boost = 0.25f + 0.14f * sin(clock * 4f + house.id)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFCC419).copy(alpha = boost), Color(0xFF51CF66).copy(alpha = boost * 0.6f), Color.Transparent),
                center = Offset(x + w / 2f, y - h * 0.52f),
                radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = Offset(x + w / 2f, y - h * 0.52f)
        )
    }

    // Chimney with gently rising smoke puffs
    if (house.hasChimney) {
        val chimX = x + w * 0.72f
        val chimTop = roofPeakY + 12f
        drawRect(Color(0xFF7C2D12), Offset(chimX, chimTop), Size(18f, 34f))
        drawRect(WorldInk, Offset(chimX, chimTop), Size(18f, 34f), style = Stroke(2.4f))
        // Snow rim on chimney
        drawRoundRect(Color(0xFFF8FCFF), Offset(chimX - 3f, chimTop - 4f), Size(24f, 7f), CornerRadius(3f, 3f))
        // Animated smoke puffs
        for (s in 0 until 3) {
            val cycle = (clock * 0.45f + s * 0.33f + house.id * 0.2f) % 1f
            val sx = chimX + 9f + sin(clock * 2f + s + house.id) * 7f + cycle * 12f
            val sy = chimTop - 8f - cycle * 42f
            drawCircle(
                color = Color(0xFFE2E8F0).copy(alpha = (1f - cycle) * 0.55f),
                radius = 5f + cycle * 7f,
                center = Offset(sx, sy)
            )
        }
    }

    // House Walls
    drawRect(Color(house.wallColor), Offset(x, wallTop), Size(w, wallH))
    drawRect(WorldInk, Offset(x, wallTop), Size(w, wallH), style = Stroke(3f))

    // Timber corner posts & trim
    drawRect(Color(0xFF4A250B), Offset(x, wallTop), Size(7f, wallH))
    drawRect(Color(0xFF4A250B), Offset(x + w - 7f, wallTop), Size(7f, wallH))

    // Cozy Arched Door in center or side
    val doorW = 24f
    val doorH = 38f
    val doorX = x + w * 0.5f - doorW / 2f
    val doorY = y - doorH
    drawRoundRect(Color(0xFF5C3A21), Offset(doorX, doorY), Size(doorW, doorH), CornerRadius(10f, 10f))
    drawRoundRect(WorldInk, Offset(doorX, doorY), Size(doorW, doorH), CornerRadius(10f, 10f), style = Stroke(2.2f))
    drawCircle(Color(0xFF2FB344), 5.5f, Offset(doorX + doorW / 2f, doorY + 14f), style = Stroke(2.6f))
    drawCircle(Color(0xFFFCC419), 2.3f, Offset(doorX + doorW - 5f, doorY + 22f))

    // Warm Glowing Windows (with candlelight shimmer & golden spill glow!)
    val winGlow = 0.78f + 0.22f * sin(clock * 5.2f + house.id * 1.4f)
    val leftWin = Offset(x + 16f, wallTop + 14f)
    val rightWin = Offset(x + w - 40f, wallTop + 14f)
    listOf(leftWin, rightWin).forEach { wp ->
        drawCircle(Color(0xFFFCC419).copy(alpha = 0.25f * winGlow), 22f, Offset(wp.x + 12f, wp.y + 11f))
        drawRoundRect(Color(0xFFFFE066).copy(alpha = winGlow), wp, Size(24f, 22f), CornerRadius(4f, 4f))
        drawRoundRect(WorldInk, wp, Size(24f, 22f), CornerRadius(4f, 4f), style = Stroke(2.2f))
        drawLine(WorldInk, Offset(wp.x + 12f, wp.y), Offset(wp.x + 12f, wp.y + 22f), 1.8f)
        drawLine(WorldInk, Offset(wp.x, wp.y + 11f), Offset(wp.x + 24f, wp.y + 11f), 1.8f)
        // Snow sill under window
        drawRoundRect(Color(0xFFF8FCFF), Offset(wp.x - 2f, wp.y + 21f), Size(28f, 5f), CornerRadius(2.5f, 2.5f))
    }

    // Steep Pitched Roof
    val roofPath = Path().apply {
        moveTo(x - 10f, wallTop + 4f)
        lineTo(x + w / 2f, roofPeakY)
        lineTo(x + w + 10f, wallTop + 4f)
        close()
    }
    drawPath(roofPath, Color(house.roofColor))
    drawPath(roofPath, WorldInk, style = Stroke(3f))

    // Attic round glowing window if second floor!
    if (house.hasSecondFloor) {
        val atticC = Offset(x + w / 2f, (wallTop + roofPeakY) * 0.55f)
        drawCircle(Color(0xFFFCC419).copy(alpha = 0.32f), 17f, atticC)
        drawCircle(Color(0xFFFFE066), 10f, atticC)
        drawCircle(WorldInk, 10f, atticC, style = Stroke(2.2f))
    }

    // Thick Fluffy Snow Blanket on the Roof + Scalloped Snow Eaves + Icicles!
    val snowRoof = Path().apply {
        moveTo(x - 12f, wallTop + 4f)
        lineTo(x + w / 2f, roofPeakY - 6f)
        lineTo(x + w + 12f, wallTop + 4f)
        lineTo(x + w + 4f, wallTop + 10f)
        lineTo(x + w / 2f, roofPeakY + 7f)
        lineTo(x - 4f, wallTop + 10f)
        close()
    }
    drawPath(snowRoof, Color(0xFFF8FCFF))
    drawPath(snowRoof, WorldInk, style = Stroke(2.2f))

    // Hanging snow puffs & glistening icicles along the eave
    var sx = x - 2f
    var icIdx = 0
    while (sx <= x + w + 2f) {
        drawCircle(Color(0xFFF8FCFF), 5.5f, Offset(sx, wallTop + 6f))
        if (icIdx % 2 == 0) {
            val icLen = 9f + (icIdx % 3) * 4f
            val icPath = Path().apply {
                moveTo(sx - 3f, wallTop + 9f)
                lineTo(sx, wallTop + 9f + icLen)
                lineTo(sx + 3f, wallTop + 9f)
                close()
            }
            drawPath(icPath, Color(0xFFBAE6FD).copy(alpha = 0.88f))
            drawPath(icPath, WorldInk, style = Stroke(1.3f))
        }
        sx += 15f
        icIdx++
    }

    // Animated Festive Christmas Lights Strung Across the Roof Eaves with Glow Effect!
    val bulbCount = 8
    for (i in 0 until bulbCount) {
        val frac = i / (bulbCount - 1).toFloat()
        val bx = (x - 4f) + frac * (w + 8f)
        val sag = sin(frac * PI.toFloat()) * 5f
        val by = wallTop + 9f + sag
        val col = Color(house.lightColors[i % house.lightColors.size])
        val speed = if (house.litBoosted) 7.5f else 4.2f
        val blink = 0.45f + 0.55f * abs(sin(clock * speed + i * 0.9f + house.id))
        val haloR = if (house.litBoosted) 13.5f else 9.5f

        drawCircle(col.copy(alpha = blink * 0.48f), haloR, Offset(bx, by))
        drawCircle(col.copy(alpha = blink), 4.2f, Offset(bx, by))
        drawCircle(WorldInk, 4.2f, Offset(bx, by), style = Stroke(1.3f))
    }
}

private fun DrawScope.renderSantaAndBigSleigh(
    sleigh: SantaSleighProp,
    clock: Float,
    nearSanta: Boolean
) {
    val sx = sleigh.x
    val sy = sleigh.y
    val hoverBob = sin(clock * 2.4f) * 2.5f

    // Warm golden magic aura around Santa & His Big Sleigh
    val auraAlpha = if (nearSanta) 0.36f + 0.14f * sin(clock * 4f) else 0.22f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFCC419).copy(alpha = auraAlpha), Color(0xFFFA5252).copy(alpha = auraAlpha * 0.4f), Color.Transparent),
            center = Offset(sx - 15f, sy - 46f),
            radius = 155f
        ),
        radius = 155f,
        center = Offset(sx - 15f, sy - 46f)
    )

    // 1. TWO ANIMATED REINDEER HARNESSED ON THE LEFT (facing left)
    val reindeerXs = listOf(sx - 130f, sx - 78f)
    reindeerXs.forEachIndexed { idx, rx ->
        val gallopY = sin(clock * 4.5f + idx * 1.1f) * 3.5f
        withTransform({ translate(rx, sy - 24f + gallopY) }) {
            // Legs
            drawLine(Color(0xFF6B3E26), Offset(-10f, 8f), Offset(-12f, 24f - gallopY * 0.5f), 4f, StrokeCap.Round)
            drawLine(Color(0xFF6B3E26), Offset(8f, 8f), Offset(10f, 24f - gallopY * 0.5f), 4f, StrokeCap.Round)
            // Body
            drawRoundRect(Color(0xFF8B5A2B), Offset(-16f, -6f), Size(32f, 18f), CornerRadius(9f, 9f))
            drawRoundRect(WorldInk, Offset(-16f, -6f), Size(32f, 18f), CornerRadius(9f, 9f), style = Stroke(2.4f))
            // Festive red harness collar
            drawLine(Color(0xFFE03131), Offset(-8f, -6f), Offset(-8f, 11f), 4f)
            // Head
            drawCircle(Color(0xFF9C6635), 9f, Offset(-16f, -12f))
            drawCircle(WorldInk, 9f, Offset(-16f, -12f), style = Stroke(2.2f))
            // Antlers
            drawLine(Color(0xFFD4A373), Offset(-14f, -20f), Offset(-19f, -31f), 2.5f)
            drawLine(Color(0xFFD4A373), Offset(-17f, -26f), Offset(-12f, -29f), 2.2f)
            drawLine(Color(0xFFD4A373), Offset(-11f, -20f), Offset(-8f, -30f), 2.5f)
            // Eye
            drawCircle(WorldInk, 1.8f, Offset(-19f, -13f))
            // Rudolph glowing red nose on lead reindeer!
            if (idx == 0) {
                val noseGlow = 0.6f + 0.4f * sin(clock * 6f)
                drawCircle(Color(0xFFFA5252).copy(alpha = noseGlow * 0.55f), 8f, Offset(-25f, -11f))
                drawCircle(Color(0xFFFA5252), 3.8f, Offset(-25f, -11f))
            } else {
                drawCircle(Color(0xFF3E2412), 2.8f, Offset(-24f, -11f))
            }
        }
    }

    // Golden magic reins connecting reindeer to the Big Sleigh!
    drawLine(Color(0xFFFCC419), Offset(sx - 128f, sy - 24f), Offset(sx - 35f, sy - 30f + hoverBob), 2.2f)
    drawLine(Color(0xFFFCC419), Offset(sx - 76f, sy - 24f), Offset(sx - 35f, sy - 30f + hoverBob), 2.2f)

    // 2. SANTA'S BIG ORNATE SLEIGH!
    withTransform({ translate(sx + 38f, sy + hoverBob) }) {
        // Big overflowing Gift Sack in the back of the sleigh
        drawCircle(Color(0xFF9C5221), 24f, Offset(24f, -46f))
        drawCircle(WorldInk, 24f, Offset(24f, -46f), style = Stroke(2.8f))
        // Colorful wrapped presents peeking out of the sack
        drawRect(Color(0xFF2FB344), Offset(10f, -72f), Size(16f, 16f))
        drawRect(Color(0xFFFCC419), Offset(16f, -72f), Size(4f, 16f))
        drawRect(WorldInk, Offset(10f, -72f), Size(16f, 16f), style = Stroke(2f))

        drawRect(Color(0xFF339AF0), Offset(26f, -68f), Size(15f, 15f))
        drawRect(Color(0xFFFA5252), Offset(32f, -68f), Size(3.5f, 15f))
        drawRect(WorldInk, Offset(26f, -68f), Size(15f, 15f), style = Stroke(2f))

        // Sleigh Curved Golden Runners
        val runner = Path().apply {
            moveTo(-76f, -16f)
            quadraticTo(-82f, -4f, -62f, -2f)
            lineTo(54f, -2f)
            quadraticTo(68f, -2f, 70f, -12f)
        }
        drawPath(runner, Color(0xFFFCC419), style = Stroke(5.5f, cap = StrokeCap.Round))
        drawLine(Color(0xFFFCC419), Offset(-42f, -2f), Offset(-42f, -16f), 4f)
        drawLine(Color(0xFFFCC419), Offset(36f, -2f), Offset(36f, -16f), 4f)

        // Grand Crimson & Gold Sleigh Body
        val hull = Path().apply {
            moveTo(-64f, -38f)
            quadraticTo(-48f, -10f, -18f, -12f)
            lineTo(48f, -12f)
            quadraticTo(64f, -12f, 64f, -44f)
            lineTo(42f, -44f)
            quadraticTo(28f, -28f, 0f, -28f)
            lineTo(-34f, -28f)
            quadraticTo(-54f, -28f, -64f, -38f)
            close()
        }
        drawPath(hull, Color(0xFFE03131))
        drawPath(hull, WorldInk, style = Stroke(3.2f))

        // Gold decorative swirl & trim on sleigh side
        drawLine(Color(0xFFFCC419), Offset(-50f, -22f), Offset(52f, -22f), 3.2f)
        // Glowing golden front sleigh lantern
        drawCircle(Color(0xFFFFE066).copy(alpha = 0.55f), 11f, Offset(-64f, -40f))
        drawCircle(Color(0xFFFCC419), 5f, Offset(-64f, -40f))
        drawCircle(WorldInk, 5f, Offset(-64f, -40f), style = Stroke(1.8f))
    }

    // 3. CHEERFUL SANTA CLAUS STANDING BESIDE HIS BIG SLEIGH!
    val santaX = sx - 18f
    val santaY = sy
    val wave = sin(clock * 4.5f) * 16f
    withTransform({ translate(santaX, santaY) }) {
        // Boots
        drawRoundRect(WorldInk, Offset(-11f, -12f), Size(9f, 12f), CornerRadius(3f, 3f))
        drawRoundRect(WorldInk, Offset(2f, -12f), Size(9f, 12f), CornerRadius(3f, 3f))

        // Jolly Red Coat
        drawRoundRect(Color(0xFFE03131), Offset(-14f, -38f), Size(28f, 28f), CornerRadius(10f, 10f))
        drawRoundRect(WorldInk, Offset(-14f, -38f), Size(28f, 28f), CornerRadius(10f, 10f), style = Stroke(2.6f))
        // White fur hem & center stripe
        drawRect(Color.White, Offset(-14f, -15f), Size(28f, 5f))
        drawRect(Color.White, Offset(-3f, -38f), Size(6f, 24f))
        // Black belt & golden buckle
        drawRect(WorldInk, Offset(-14f, -24f), Size(28f, 5f))
        drawRect(Color(0xFFFCC419), Offset(-4f, -25f), Size(8f, 7f))

        // Waving arm!
        rotate(degrees = -25f + wave, pivot = Offset(-13f, -33f)) {
            drawRoundRect(Color(0xFFE03131), Offset(-24f, -36f), Size(13f, 7f), CornerRadius(3.5f, 3.5f))
            drawCircle(Color.White, 4.2f, Offset(-24f, -32.5f))
        }

        // Santa's Head & Fluffy White Beard
        drawCircle(Color(0xFFFFD8A8), 11f, Offset(0f, -48f))
        drawCircle(WorldInk, 11f, Offset(0f, -48f), style = Stroke(2.4f))
        // Beard
        val beard = Path().apply {
            moveTo(-10f, -45f)
            quadraticTo(0f, -30f, 10f, -45f)
            quadraticTo(0f, -40f, -10f, -45f)
            close()
        }
        drawPath(beard, Color.White)
        drawPath(beard, WorldInk, style = Stroke(1.8f))
        // Eyes & rosy nose
        drawCircle(WorldInk, 1.8f, Offset(-3.8f, -50f))
        drawCircle(WorldInk, 1.8f, Offset(3.8f, -50f))
        drawCircle(Color(0xFFFF8787), 2.5f, Offset(0f, -47f))

        // Red Santa Hat + White Trim + Pom-pom
        val hat = Path().apply {
            moveTo(-12f, -55f)
            lineTo(2f, -72f)
            lineTo(13f, -55f)
            close()
        }
        drawPath(hat, Color(0xFFE03131))
        drawPath(hat, WorldInk, style = Stroke(2.2f))
        drawRoundRect(Color.White, Offset(-13f, -58f), Size(26f, 6f), CornerRadius(3f, 3f))
        drawCircle(Color.White, 4.5f, Offset(3f, -73f))
        drawCircle(WorldInk, 4.5f, Offset(3f, -73f), style = Stroke(1.6f))

        // Floating golden quest badge above Santa Claus
        val badgeBob = sin(clock * 3.5f) * 3f
        drawRoundRect(
            color = Color(0xFFFCC419),
            topLeft = Offset(-36f, -104f + badgeBob),
            size = Size(72f, 20f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = WorldInk,
            topLeft = Offset(-36f, -104f + badgeBob),
            size = Size(72f, 20f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(2.2f)
        )
        val labelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textSize = 11f
        }
        drawContext.canvas.nativeCanvas.drawText("🎅 QUESTURI", 0f, -90f + badgeBob, labelPaint)
    }
}

private fun DrawScope.renderGiftBoxItem(gb: GiftBoxItem, clock: Float) {
    val bob = sin(clock * 4.5f + gb.x * 0.04f) * 4f
    val cx = gb.x
    val cy = gb.y + bob

    drawCircle(Color(gb.ribbonColor).copy(alpha = 0.28f), 19f, Offset(cx, cy))
    drawRoundRect(Color(gb.boxColor), Offset(cx - 11f, cy - 11f), Size(22f, 22f), CornerRadius(4f, 4f))
    drawRect(Color(gb.ribbonColor), Offset(cx - 2.5f, cy - 11f), Size(5f, 22f))
    drawRect(Color(gb.ribbonColor), Offset(cx - 11f, cy - 2.5f), Size(22f, 5f))
    drawRoundRect(WorldInk, Offset(cx - 11f, cy - 11f), Size(22f, 22f), CornerRadius(4f, 4f), style = Stroke(2.4f))
    // Ribbon bow
    drawCircle(Color(gb.ribbonColor), 3.8f, Offset(cx - 4f, cy - 13f))
    drawCircle(Color(gb.ribbonColor), 3.8f, Offset(cx + 4f, cy - 13f))
    drawCircle(WorldInk, 3.8f, Offset(cx - 4f, cy - 13f), style = Stroke(1.6f))
    drawCircle(WorldInk, 3.8f, Offset(cx + 4f, cy - 13f), style = Stroke(1.6f))
}

private fun DrawScope.renderThemedPlatform(
    p: Platform,
    blockIndex: Int,
    theme: LevelTheme,
    clock: Float,
    isVillage: Boolean
) {
    val jitterX = if (p.kind == PlatformKind.CYBER_FALL && p.triggered) {
        (Random.nextFloat() - 0.5f) * 4f
    } else 0f

    withTransform({ translate(jitterX, 0f) }) {
        when (p.kind) {
            PlatformKind.CYBER_SOLID, PlatformKind.FINISH_ZONE -> {
                // Restored Earthy Soil & Cliff Shores ("malurile pământului") with Snow Cap & Christmas String Lights!
                val earthBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6B4423), Color(0xFF4A2E1B), Color(0xFF2E1B0E)),
                    startY = p.y,
                    endY = p.y + min(p.h, 240f)
                )
                drawRect(earthBrush, Offset(p.x, p.y), Size(p.w, p.h))

                if (p.h > 40f) {
                    // Beveled darker cliff banks on left and right shores
                    drawRect(Color(0xFF3A2212).copy(alpha = 0.7f), Offset(p.x, p.y), Size(10f, p.h))
                    drawRect(Color(0xFF3A2212).copy(alpha = 0.7f), Offset(p.x + p.w - 10f, p.y), Size(10f, p.h))

                    // Embedded earthy stones & soil texture pebbles
                    val pebbleStep = 44f
                    var px = p.x + 18f
                    var idx = 0
                    while (px < p.x + p.w - 18f) {
                        val py1 = p.y + 32f + (idx % 3) * 18f
                        drawOval(Color(0xFF3B2314), Offset(px, py1), Size(14f, 8f))
                        drawOval(Color(0xFF8B5E34).copy(alpha = 0.45f), Offset(px + 2f, py1 + 1f), Size(8f, 4f))
                        px += pebbleStep
                        idx++
                    }
                }

                // Lush white snow (or emerald green finish snow) top cap with hanging snow scallops
                val capColor = if (p.kind == PlatformKind.FINISH_ZONE) Color(0xFF51CF66) else Color(0xFFF8FCFF)
                val rimColor = if (p.kind == PlatformKind.FINISH_ZONE) Color(0xFF2B8A3E) else Color(0xFFBAE6FD)
                drawRect(rimColor, Offset(p.x, p.y + 8f), Size(p.w, 6f))
                drawRect(capColor, Offset(p.x, p.y), Size(p.w, 10f))
                var sx = p.x + 10f
                while (sx < p.x + p.w - 10f) {
                    drawCircle(capColor, 6.5f, Offset(sx, p.y + 11f))
                    sx += 18f
                }
                drawRect(WorldInk, Offset(p.x, p.y), Size(p.w, p.h), style = Stroke(3.5f))

                // Festive twinkling Christmas string lights ("instalații") draped right beneath the snow rim!
                renderStringLightsGarland(
                    xStart = p.x + 6f,
                    xEnd = p.x + p.w - 6f,
                    yBase = if (p.h <= 30f) p.y + p.h - 1f else p.y + 16f,
                    palette = theme.lightPalette,
                    clock = clock,
                    seedOffset = blockIndex * 7 + if (isVillage) 0 else theme.id,
                    bulbSpacing = 20f,
                    sagDepth = if (p.h <= 30f) 4.0f else 6.0f,
                    bulbRadius = 3.1f
                )
            }

            PlatformKind.CYBER_MOVE_H, PlatformKind.CYBER_MOVE_V -> {
                drawRect(Color(0xFF5C3A21), Offset(p.x, p.y), Size(p.w, p.h))
                drawRect(Color(0xFFF8FCFF), Offset(p.x, p.y), Size(p.w, 8f))
                drawRect(Color(0xFFBAE6FD), Offset(p.x, p.y + 7f), Size(p.w, 3f))
                drawRect(WorldInk, Offset(p.x, p.y), Size(p.w, p.h), style = Stroke(3.2f))
                renderStringLightsGarland(
                    xStart = p.x + 6f,
                    xEnd = p.x + p.w - 6f,
                    yBase = p.y + p.h - 2f,
                    palette = theme.lightPalette,
                    clock = clock,
                    seedOffset = blockIndex * 13,
                    bulbSpacing = 18f,
                    sagDepth = 4.5f,
                    bulbRadius = 2.9f
                )
            }

            PlatformKind.CYBER_FALL -> {
                drawRect(Color(0xFF74C0FC), Offset(p.x, p.y), Size(p.w, p.h))
                drawRect(Color(0xFFF8FCFF), Offset(p.x, p.y), Size(p.w, 7f))
                drawLine(Color(0xFF1C7ED6), Offset(p.x + 20f, p.y + 6f), Offset(p.x + 36f, p.y + p.h - 3f), 2f)
                drawLine(Color(0xFF1C7ED6), Offset(p.x + p.w - 24f, p.y + 5f), Offset(p.x + p.w - 38f, p.y + p.h - 2f), 2f)
                drawRect(WorldInk, Offset(p.x, p.y), Size(p.w, p.h), style = Stroke(3.2f))
                renderStringLightsGarland(
                    xStart = p.x + 8f,
                    xEnd = p.x + p.w - 8f,
                    yBase = p.y + p.h - 1f,
                    palette = theme.lightPalette,
                    clock = clock,
                    seedOffset = blockIndex * 19,
                    bulbSpacing = 22f,
                    sagDepth = 3.5f,
                    bulbRadius = 2.5f
                )
            }
        }
    }
}

private fun DrawScope.renderChristmasCheckpoint(cp: CheckpointItem, clock: Float) {
    withTransform({ translate(cp.x, cp.y) }) {
        if (cp.activated) {
            val haloAlpha = 0.28f + 0.14f * sin(clock * 5f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF51CF66).copy(alpha = haloAlpha), Color(0xFFFCC419).copy(alpha = haloAlpha * 0.6f), Color.Transparent),
                    center = Offset(0f, -34f),
                    radius = 48f
                ),
                radius = 48f,
                center = Offset(0f, -34f)
            )
        }

        drawRect(Color.White, Offset(-4f, -56f), Size(8f, 56f))
        for (yOff in -52..-8 step 12) {
            drawRect(Color(0xFFE03131), Offset(-4f, yOff.toFloat()), Size(8f, 6f))
        }
        drawRect(WorldInk, Offset(-4f, -56f), Size(8f, 56f), style = Stroke(2.6f))

        val wreathColor = if (cp.activated) Color(0xFF2FB344) else Color(0xFF6B7280)
        drawCircle(wreathColor, 13f, Offset(0f, -38f), style = Stroke(5.5f))
        drawCircle(WorldInk, 15.5f, Offset(0f, -38f), style = Stroke(2f))
        drawCircle(if (cp.activated) Color(0xFFFA5252) else Color(0xFF9CA3AF), 4f, Offset(0f, -26f))

        val starCol = if (cp.activated) Color(0xFFFCC419) else Color(0xFFCBD5E1)
        val star = Path().apply {
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) 10f else 4.5f
                val a = -PI.toFloat() / 2f + i * (PI.toFloat() / 5f)
                val px = cos(a) * r
                val py = -62f + sin(a) * r
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(star, starCol)
        drawPath(star, WorldInk, style = Stroke(2.2f))
    }
}

private fun DrawScope.renderElfGoblin(goblin: ElfGoblin, clock: Float) {
    val cx = goblin.x + goblin.w / 2f
    val cy = goblin.y + goblin.h / 2f
    val dir = if (goblin.vx >= 0f) 1f else -1f
    val stepBob = abs(sin(clock * 8f + goblin.phase)) * 2.2f

    withTransform({ translate(cx, cy - stepBob) }) {
        val tunicPrimary = when (goblin.kind) {
            ElfGoblinKind.SCOUT_ELF_GOBLIN -> Color(0xFFE03131)
            ElfGoblinKind.TINKER_ELF_GOBLIN -> Color(0xFF15803D)
            ElfGoblinKind.JINGLE_FLYER_GOBLIN -> Color(0xFFBE123C)
            ElfGoblinKind.BOUNCY_ELF_GOBLIN -> Color(0xFF2FB344)
        }
        val hatPrimary = when (goblin.kind) {
            ElfGoblinKind.SCOUT_ELF_GOBLIN -> Color(0xFF2FB344)
            ElfGoblinKind.TINKER_ELF_GOBLIN -> Color(0xFFE03131)
            ElfGoblinKind.JINGLE_FLYER_GOBLIN -> Color(0xFF16A34A)
            ElfGoblinKind.BOUNCY_ELF_GOBLIN -> Color(0xFFC92A2A)
        }

        // Special kind-specific back accessories
        when (goblin.kind) {
            ElfGoblinKind.JINGLE_FLYER_GOBLIN -> {
                val flap = sin(clock * 14f + goblin.phase) * 5f
                drawCircle(Color(0xFFFCC419).copy(alpha = 0.25f), goblin.w * 0.72f, Offset.Zero)
                val wings = Path().apply {
                    moveTo(-8f, -2f); lineTo(-24f, -12f + flap); lineTo(-12f, 6f); close()
                    moveTo(8f, -2f); lineTo(24f, -12f + flap); lineTo(12f, 6f); close()
                }
                drawPath(wings, Color(0xFFD0EBFF).copy(alpha = 0.9f))
                drawPath(wings, WorldInk, style = Stroke(2f))
            }
            ElfGoblinKind.BOUNCY_ELF_GOBLIN -> {
                val spring = Path().apply {
                    moveTo(-6f, goblin.h / 2f - 5f)
                    lineTo(6f, goblin.h / 2f - 2f)
                    lineTo(-6f, goblin.h / 2f + 1f)
                }
                drawPath(spring, Color(0xFFFCC419), style = Stroke(3f, cap = StrokeCap.Round))
            }
            ElfGoblinKind.SCOUT_ELF_GOBLIN -> {
                // Candy cane lantern held in hand
                drawLine(Color.White, Offset(dir * 14f, 6f), Offset(dir * 14f, -10f), 3.4f, StrokeCap.Round)
                drawLine(Color(0xFFFA5252), Offset(dir * 14f, 4f), Offset(dir * 14f, -6f), 2.2f)
                drawCircle(Color(0xFFFFE066), 3.2f, Offset(dir * 14f, -11f))
            }
            ElfGoblinKind.TINKER_ELF_GOBLIN -> {
                // Mini stolen gift pack on back
                drawRoundRect(Color(0xFFB45309), Offset(-dir * 16f - 5f, -4f), Size(10f, 13f), CornerRadius(3f, 3f))
                drawRoundRect(WorldInk, Offset(-dir * 16f - 5f, -4f), Size(10f, 13f), CornerRadius(3f, 3f), style = Stroke(2f))
            }
        }

        // Curly-toed pointy Elf Boots
        val leftShoe = Path().apply {
            moveTo(-11f, goblin.h / 2f - 5f)
            lineTo(-3f, goblin.h / 2f - 5f)
            lineTo(-3f, goblin.h / 2f)
            lineTo(-14f, goblin.h / 2f)
            quadraticTo(-16f, goblin.h / 2f - 5f, -11f, goblin.h / 2f - 5f)
            close()
        }
        val rightShoe = Path().apply {
            moveTo(3f, goblin.h / 2f - 5f)
            lineTo(11f, goblin.h / 2f - 5f)
            quadraticTo(16f, goblin.h / 2f - 5f, 14f, goblin.h / 2f)
            lineTo(3f, goblin.h / 2f)
            close()
        }
        drawPath(leftShoe, hatPrimary)
        drawPath(leftShoe, WorldInk, style = Stroke(2f))
        drawPath(rightShoe, hatPrimary)
        drawPath(rightShoe, WorldInk, style = Stroke(2f))

        // Festive Elf Tunic + Gold Belt Buckle
        drawRoundRect(tunicPrimary, Offset(-11f, -1f), Size(22f, 16f), CornerRadius(6f, 6f))
        drawRoundRect(WorldInk, Offset(-11f, -1f), Size(22f, 16f), CornerRadius(6f, 6f), style = Stroke(2.4f))
        drawRect(WorldInk, Offset(-10f, 7f), Size(20f, 3.5f))
        drawRect(Color(0xFFFCC419), Offset(-3f, 6.5f), Size(6f, 4.5f))

        // Jagged white/gold Elf collar
        val collar = Path().apply {
            moveTo(-10f, -1f)
            lineTo(-5f, 4f)
            lineTo(0f, -1f)
            lineTo(5f, 4f)
            lineTo(10f, -1f)
            close()
        }
        drawPath(collar, Color(0xFFF8FCFF))
        drawPath(collar, WorldInk, style = Stroke(1.6f))

        // Long pointed green Goblin Ears!
        val ears = Path().apply {
            moveTo(-9f, -9f); lineTo(-20f, -14f); lineTo(-10f, -3f); close()
            moveTo(9f, -9f); lineTo(20f, -14f); lineTo(10f, -3f); close()
        }
        drawPath(ears, Color(0xFF74B816))
        drawPath(ears, WorldInk, style = Stroke(2.2f))

        // Goblin Head
        drawCircle(Color(0xFF82C91E), 10.5f, Offset(0f, -8f))
        drawCircle(WorldInk, 10.5f, Offset(0f, -8f), style = Stroke(2.5f))

        // Angry/cheeky yellow Goblin Eyes + dark brows + fang smirk
        drawCircle(Color(0xFFFFE066), 2.8f, Offset(-3.8f + dir * 1.4f, -8.5f))
        drawCircle(Color(0xFFFFE066), 2.8f, Offset(3.8f + dir * 1.4f, -8.5f))
        drawCircle(WorldInk, 1.3f, Offset(-3.4f + dir * 1.8f, -8.5f))
        drawCircle(WorldInk, 1.3f, Offset(4.2f + dir * 1.8f, -8.5f))
        drawLine(WorldInk, Offset(-6.8f + dir, -12f), Offset(-1.5f + dir, -10.5f), 2f, StrokeCap.Round)
        drawLine(WorldInk, Offset(6.8f + dir, -12f), Offset(1.5f + dir, -10.5f), 2f, StrokeCap.Round)
        drawLine(WorldInk, Offset(-4.5f + dir, -3.5f), Offset(4.5f + dir, -4f), 2f, StrokeCap.Round)
        // Tiny goblin fang
        drawCircle(Color.White, 1.3f, Offset(-2.2f + dir, -3.4f))

        // Pointed Christmas Elf Hat with fluffy trim & swinging golden bell!
        val tipSway = sin(clock * 6.5f + goblin.phase) * 3f - dir * 4f
        val hat = Path().apply {
            moveTo(-11f, -14f)
            quadraticTo(tipSway * 0.4f, -24f, tipSway, -31f)
            quadraticTo(5f, -22f, 11f, -14f)
            close()
        }
        drawPath(hat, hatPrimary)
        drawPath(hat, WorldInk, style = Stroke(2.4f))
        drawRoundRect(Color.White, Offset(-12f, -16f), Size(24f, 4.8f), CornerRadius(2.4f, 2.4f))
        drawRoundRect(WorldInk, Offset(-12f, -16f), Size(24f, 4.8f), CornerRadius(2.4f, 2.4f), style = Stroke(1.8f))
        drawCircle(Color(0xFFFCC419), 3.4f, Offset(tipSway, -31f))
        drawCircle(WorldInk, 3.4f, Offset(tipSway, -31f), style = Stroke(1.6f))
    }
}

private fun DrawScope.renderFinishChristmasDoor(
    door: FinishDoor,
    clock: Float,
    openProgress: Float,
    nearDoor: Boolean,
    lockedByBoss: Boolean,
    labelPaint: Paint
) {
    val cx = door.x
    val baseY = door.y
    val doorW = door.width
    val doorH = door.height
    val doorLeft = cx - doorW / 2f
    val doorTop = baseY - doorH

    // Radiant Christmas portal aura around the finish door
    val glowAlpha = if (openProgress > 0f) 0.72f else if (nearDoor && !lockedByBoss) 0.52f + 0.25f * sin(clock * 6f) else 0.30f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                if (lockedByBoss) Color(0xFFFA5252).copy(alpha = glowAlpha) else Color(0xFF69DB7C).copy(alpha = glowAlpha),
                if (lockedByBoss) Color(0xFF82C91E).copy(alpha = glowAlpha * 0.4f) else Color(0xFFFCC419).copy(alpha = glowAlpha * 0.5f),
                Color.Transparent
            ),
            center = Offset(cx, doorTop + doorH * 0.48f),
            radius = 78f + openProgress * 28f
        ),
        radius = 78f + openProgress * 28f,
        center = Offset(cx, doorTop + doorH * 0.48f)
    )

    // Festive garland arch frame
    drawArc(
        color = if (lockedByBoss) Color(0xFFE03131) else Color(0xFF2FB344),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - 46f, doorTop - 22f),
        size = Size(92f, 64f),
        style = Stroke(8f, cap = StrokeCap.Round)
    )
    // Twinkling arch fairy lights
    val bulbColors = listOf(Color(0xFFFA5252), Color(0xFFFCC419), Color(0xFF51CF66), Color(0xFF4DABF7))
    for (i in 0..6) {
        val frac = i / 6f
        val angle = PI.toFloat() * (1f - frac)
        val bx = cx + cos(angle) * 46f
        val by = (doorTop + 10f) - sin(angle) * 32f
        val bCol = if (lockedByBoss) Color(0xFFFA5252) else bulbColors[i % bulbColors.size]
        val pulse = 3.2f + sin(clock * 6f + i * 1.1f) * 1.1f
        drawCircle(bCol.copy(alpha = 0.45f), pulse * 1.8f, Offset(bx, by))
        drawCircle(bCol, pulse, Offset(bx, by))
    }

    // Golden-emerald stone doorway frame
    drawRoundRect(
        color = if (lockedByBoss) Color(0xFFFF8787) else Color(0xFFFCC419),
        topLeft = Offset(doorLeft - 6f, doorTop - 6f),
        size = Size(doorW + 12f, doorH + 6f),
        cornerRadius = CornerRadius(24f, 24f)
    )
    drawRoundRect(
        color = WorldInk,
        topLeft = Offset(doorLeft - 6f, doorTop - 6f),
        size = Size(doorW + 12f, doorH + 6f),
        cornerRadius = CornerRadius(24f, 24f),
        style = Stroke(3f)
    )

    // Glowing swirling portal behind the door leaf!
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = if (lockedByBoss) {
                listOf(Color(0xFF2B0B0E), Color(0xFF7F1D1D), Color(0xFF14532D))
            } else {
                listOf(Color(0xFFFFFFF0), Color(0xFF69DB7C), Color(0xFF1098AD))
            },
            center = Offset(cx, doorTop + doorH * 0.48f),
            radius = 46f
        ),
        topLeft = Offset(doorLeft, doorTop),
        size = Size(doorW, doorH),
        cornerRadius = CornerRadius(20f, 20f)
    )

    // Portal vortex rings visible as door swings open
    if (openProgress > 0.02f && !lockedByBoss) {
        val swirlRot = clock * 260f
        for (ring in 0..2) {
            val rr = (12f + ring * 9f) * (0.45f + openProgress * 0.7f)
            drawCircle(
                color = if (ring % 2 == 0) Color(0xFFFFE066).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f),
                radius = rr,
                center = Offset(cx, doorTop + doorH * 0.5f),
                style = Stroke(2.4f)
            )
            val rx = cx + cos((swirlRot + ring * 120f) * PI.toFloat() / 180f) * rr
            val ry = (doorTop + doorH * 0.5f) + sin((swirlRot + ring * 120f) * PI.toFloat() / 180f) * rr
            drawCircle(Color(0xFFD8F5A2), 3.2f, Offset(rx, ry))
        }
    }

    // Swinging wooden door leaf (animates open smoothly via finishDoorOpenProgress, same as start tree door!)
    val leafScale = if (lockedByBoss) 1f else (1f - openProgress * 0.92f).coerceAtLeast(0.08f)
    withTransform({
        translate(doorLeft, doorTop)
        scale(leafScale, 1f, Offset.Zero)
    }) {
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFB45309), Color(0xFF78350F))),
            topLeft = Offset.Zero,
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(20f, 20f)
        )
        drawLine(Color(0xFF451A03), Offset(doorW * 0.33f, 8f), Offset(doorW * 0.33f, doorH - 2f), 2f)
        drawLine(Color(0xFF451A03), Offset(doorW * 0.67f, 8f), Offset(doorW * 0.67f, doorH - 2f), 2f)

        // Wreath + Golden Star on Finish Door
        drawCircle(Color(0xFF2FB344), 10f, Offset(doorW * 0.5f, doorH * 0.36f), style = Stroke(4.5f))
        drawCircle(Color(0xFFFA5252), 3f, Offset(doorW * 0.5f, doorH * 0.24f))
        drawCircle(Color(0xFFFCC419), 4.5f, Offset(doorW * 0.8f, doorH * 0.58f))
        drawCircle(WorldInk, 4.5f, Offset(doorW * 0.8f, doorH * 0.58f), style = Stroke(1.6f))

        drawRoundRect(
            color = WorldInk,
            topLeft = Offset.Zero,
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(20f, 20f),
            style = Stroke(2.8f)
        )
    }

    // Villain padlock chains if Level 20 Grinch Boss is still alive
    if (lockedByBoss) {
        drawLine(Color(0xFF343A40), Offset(doorLeft, doorTop + 18f), Offset(doorLeft + doorW, doorTop + doorH - 16f), 5.5f)
        drawLine(Color(0xFF343A40), Offset(doorLeft + doorW, doorTop + 18f), Offset(doorLeft, doorTop + doorH - 16f), 5.5f)
        drawRoundRect(Color(0xFFE03131), Offset(cx - 12f, doorTop + doorH * 0.44f), Size(24f, 20f), CornerRadius(5f, 5f))
        drawRoundRect(WorldInk, Offset(cx - 12f, doorTop + doorH * 0.44f), Size(24f, 20f), CornerRadius(5f, 5f), style = Stroke(2.2f))
        drawContext.canvas.nativeCanvas.drawText("🔒 BOSS", cx, doorTop - 26f, labelPaint)
    } else {
        drawContext.canvas.nativeCanvas.drawText("🚪 FINISH", cx, doorTop - 26f, labelPaint)
    }
}

private fun DrawScope.renderMovingLaser(laser: MovingLaser, clock: Float) {
    val beamCx = laser.x + laser.w / 2f
    val beamTop = laser.y - laser.h
    val beamBot = laser.y
    val pulse = 0.72f + 0.28f * sin(clock * 22f + laser.phase)

    // Motion track rail showing sweep path
    if (laser.axis == LaserMotionAxis.HORIZONTAL) {
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(laser.baseX - 2f, laser.baseY - 5f),
            size = Size(laser.range + laser.w + 4f, 6f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = WorldInk,
            topLeft = Offset(laser.baseX - 2f, laser.baseY - 5f),
            size = Size(laser.range + laser.w + 4f, 6f),
            cornerRadius = CornerRadius(3f, 3f),
            style = Stroke(1.8f)
        )
    } else {
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(beamCx - 2.5f, laser.baseY - laser.range - laser.h),
            size = Size(5f, laser.range + laser.h),
            cornerRadius = CornerRadius(2.5f, 2.5f)
        )
    }

    // Outer laser glow beam
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFFFF2A6D).copy(alpha = 0.65f * pulse),
                Color(0xFFFA5252).copy(alpha = 0.90f * pulse),
                Color(0xFFFF2A6D).copy(alpha = 0.65f * pulse),
                Color.Transparent
            ),
            startX = beamCx - 14f,
            endX = beamCx + 14f
        ),
        topLeft = Offset(beamCx - 14f, beamTop + 6f),
        size = Size(28f, laser.h - 10f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Intense pulsing hot-white/crimson core beam
    drawRect(
        color = Color(0xFFFF2A6D),
        topLeft = Offset(beamCx - 3.6f, beamTop + 6f),
        size = Size(7.2f, laser.h - 12f)
    )
    drawRect(
        color = Color.White.copy(alpha = 0.95f),
        topLeft = Offset(beamCx - 1.5f, beamTop + 6f),
        size = Size(3.0f, laser.h - 12f)
    )

    // Bottom & Top laser emitter pods
    drawRoundRect(Color(0xFF1E293B), Offset(beamCx - 9f, beamBot - 8f), Size(18f, 8f), CornerRadius(3f, 3f))
    drawRoundRect(WorldInk, Offset(beamCx - 9f, beamBot - 8f), Size(18f, 8f), CornerRadius(3f, 3f), style = Stroke(2f))
    drawRoundRect(Color(0xFF1E293B), Offset(beamCx - 8f, beamTop), Size(16f, 8f), CornerRadius(3f, 3f))
    drawRoundRect(WorldInk, Offset(beamCx - 8f, beamTop), Size(16f, 8f), CornerRadius(3f, 3f), style = Stroke(2f))

    // Crackling emitter spark orbs
    val sparkR = 4.2f + sin(clock * 28f + laser.phase) * 1.1f
    drawCircle(Color(0xFFFF6B6B).copy(alpha = 0.7f), sparkR * 1.6f, Offset(beamCx, beamTop + 8f))
    drawCircle(Color.White, sparkR * 0.7f, Offset(beamCx, beamTop + 8f))
    drawCircle(Color(0xFFFF6B6B).copy(alpha = 0.7f), sparkR * 1.6f, Offset(beamCx, beamBot - 8f))
    drawCircle(Color.White, sparkR * 0.7f, Offset(beamCx, beamBot - 8f))
}

private fun DrawScope.renderGrinchBoss(boss: GrinchBoss, clock: Float, labelPaint: Paint) {
    val cx = boss.x + boss.w / 2f
    val cy = boss.y + boss.h / 2f
    val dir = if (boss.vx >= 0f) 1f else -1f
    val enraged = boss.hp <= boss.maxHp / 2
    val flash = boss.hitFlashTimer > 0f
    val stompPulse = abs(sin(clock * (if (enraged) 9f else 5.5f))) * 3f
    val isElfCommander = boss.kind == BossKind.ELF_COMMANDER_L10

    // Villain shadow & toxic/golden aura ring
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                (if (enraged) Color(0xFFFA5252) else if (isElfCommander) Color(0xFFFCC419) else Color(0xFF82C91E)).copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = Offset(cx, boss.baseY + boss.h),
            radius = boss.w * 0.88f
        ),
        topLeft = Offset(cx - boss.w * 0.72f, boss.baseY + boss.h - 16f),
        size = Size(boss.w * 1.44f, 28f)
    )

    withTransform({
        translate(cx, cy - stompPulse)
    }) {
        if (isElfCommander) {
            // Level 10 Boss: Goblin-Elf Commander (400 HP)!
            // Flowing crimson royal cape behind
            val cape = Path().apply {
                moveTo(-dir * 10f, -10f)
                lineTo(-dir * 32f, 24f)
                lineTo(-dir * 8f, 28f)
                close()
            }
            drawPath(cape, Color(0xFFC92A2A))
            drawPath(cape, WorldInk, style = Stroke(2.6f))

            val bodyCol = if (flash) Color.White else if (enraged) Color(0xFF2B8A3E) else Color(0xFF15803D)
            // Armored green & red Elf Commander torso
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(bodyCol, Color(0xFF0F5132))),
                topLeft = Offset(-boss.w * 0.38f, -4f),
                size = Size(boss.w * 0.76f, boss.h * 0.52f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = WorldInk,
                topLeft = Offset(-boss.w * 0.38f, -4f),
                size = Size(boss.w * 0.76f, boss.h * 0.52f),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(3.2f)
            )
            // Golden belt & red chest trim
            drawRect(WorldInk, Offset(-boss.w * 0.36f, 14f), Size(boss.w * 0.72f, 6f))
            drawRect(Color(0xFFFCC419), Offset(-6f, 12.5f), Size(12f, 9f))

            // Curly Elf Commander boots
            drawOval(Color(0xFFE03131), Offset(-20f, boss.h * 0.40f), Size(17f, 11f))
            drawOval(WorldInk, Offset(-20f, boss.h * 0.40f), Size(17f, 11f), style = Stroke(2.4f))
            drawOval(Color(0xFFE03131), Offset(3f, boss.h * 0.40f), Size(17f, 11f))
            drawOval(WorldInk, Offset(3f, boss.h * 0.40f), Size(17f, 11f), style = Stroke(2.4f))

            // Big Goblin Ears
            val ears = Path().apply {
                moveTo(-18f, -18f); lineTo(-34f, -26f); lineTo(-18f, -8f); close()
                moveTo(18f, -18f); lineTo(34f, -26f); lineTo(18f, -8f); close()
            }
            drawPath(ears, if (flash) Color.White else Color(0xFF74B816))
            drawPath(ears, WorldInk, style = Stroke(2.6f))

            // Goblin Head
            val headCol = if (flash) Color.White else Color(0xFF82C91E)
            drawCircle(headCol, 21f, Offset(0f, -16f))
            drawCircle(WorldInk, 21f, Offset(0f, -16f), style = Stroke(3.2f))

            // Tall Red Elf Commander Hat + Golden Royal Crown!
            val hat = Path().apply {
                moveTo(-19f, -31f)
                lineTo(0f, -56f)
                lineTo(19f, -31f)
                close()
            }
            drawPath(hat, Color(0xFFE03131))
            drawPath(hat, WorldInk, style = Stroke(2.6f))
            drawRoundRect(Color.White, Offset(-20f, -34f), Size(40f, 7f), CornerRadius(3.5f, 3.5f))
            drawRoundRect(WorldInk, Offset(-20f, -34f), Size(40f, 7f), CornerRadius(3.5f, 3.5f), style = Stroke(2f))
            // Golden crown band
            val crown = Path().apply {
                moveTo(-14f, -34f); lineTo(-14f, -43f); lineTo(-6f, -37f)
                lineTo(0f, -46f); lineTo(6f, -37f); lineTo(14f, -43f); lineTo(14f, -34f); close()
            }
            drawPath(crown, Color(0xFFFCC419))
            drawPath(crown, WorldInk, style = Stroke(2f))
            drawCircle(Color(0xFFFCC419), 5f, Offset(0f, -57f))
            drawCircle(WorldInk, 5f, Offset(0f, -57f), style = Stroke(2f))

            // Fierce yellow/red eyes & fang smirk
            val eyeCol = if (enraged) Color(0xFFFF6B6B) else Color(0xFFFFE066)
            drawOval(eyeCol, Offset(-11f + dir * 3f, -21f), Size(8f, 6.5f))
            drawOval(eyeCol, Offset(3f + dir * 3f, -21f), Size(8f, 6.5f))
            drawCircle(Color(0xFF991B1B), 2.2f, Offset(-7f + dir * 4f, -18f))
            drawCircle(Color(0xFF991B1B), 2.2f, Offset(7f + dir * 4f, -18f))
            drawLine(WorldInk, Offset(-13f + dir * 2f, -24f), Offset(-3f + dir * 2f, -21f), 2.8f, StrokeCap.Round)
            drawLine(WorldInk, Offset(13f + dir * 2f, -24f), Offset(3f + dir * 2f, -21f), 2.8f, StrokeCap.Round)
            drawLine(WorldInk, Offset(-10f + dir * 2f, -9f), Offset(10f + dir * 2f, -10f), 2.8f, StrokeCap.Round)
        } else {
            // Level 20 Boss: Grinch Boss (2,000 HP)!
            // Stolen red Santa sack on his back
            drawCircle(Color(0xFFB91C1C), 19f, Offset(-dir * 24f, 8f))
            drawCircle(WorldInk, 19f, Offset(-dir * 24f, 8f), style = Stroke(3f))

            // Furry emerald-lime Grinch body
            val bodyCol = if (flash) Color.White else if (enraged) Color(0xFF66A80F) else Color(0xFF82C91E)
            val bellyCol = if (flash) Color(0xFFFFF3BF) else Color(0xFFA9E34B)
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(bodyCol, Color(0xFF5C940D))),
                topLeft = Offset(-boss.w * 0.40f, -6f),
                size = Size(boss.w * 0.80f, boss.h * 0.56f),
                cornerRadius = CornerRadius(22f, 22f)
            )
            drawRoundRect(
                color = WorldInk,
                topLeft = Offset(-boss.w * 0.40f, -6f),
                size = Size(boss.w * 0.80f, boss.h * 0.56f),
                cornerRadius = CornerRadius(22f, 22f),
                style = Stroke(3.4f)
            )
            // Fluffy tummy patch
            drawOval(bellyCol, Offset(-16f, 2f), Size(32f, 28f))

            // Feet
            drawOval(bodyCol, Offset(-22f, boss.h * 0.42f), Size(18f, 11f))
            drawOval(WorldInk, Offset(-22f, boss.h * 0.42f), Size(18f, 11f), style = Stroke(2.6f))
            drawOval(bodyCol, Offset(4f, boss.h * 0.42f), Size(18f, 11f))
            drawOval(WorldInk, Offset(4f, boss.h * 0.42f), Size(18f, 11f), style = Stroke(2.6f))

            // Grinch Head (target to stomp!)
            drawCircle(bodyCol, 24f, Offset(0f, -18f))
            drawCircle(WorldInk, 24f, Offset(0f, -18f), style = Stroke(3.4f))

            // Stolen Santa Hat perched on his head
            val hat = Path().apply {
                moveTo(-20f, -34f)
                lineTo(6f, -60f)
                lineTo(20f, -34f)
                close()
            }
            drawPath(hat, Color(0xFFE03131))
            drawPath(hat, WorldInk, style = Stroke(2.8f))
            drawRoundRect(Color.White, Offset(-22f, -38f), Size(44f, 8f), CornerRadius(4f, 4f))
            drawRoundRect(WorldInk, Offset(-22f, -38f), Size(44f, 8f), CornerRadius(4f, 4f), style = Stroke(2.2f))
            drawCircle(Color.White, 6f, Offset(8f, -60f))
            drawCircle(WorldInk, 6f, Offset(8f, -60f), style = Stroke(2.2f))

            // Angry furrowed eyebrows + glowing yellow/red Grinch eyes
            val eyeCol = if (enraged) Color(0xFFFF6B6B) else Color(0xFFFFE066)
            drawOval(eyeCol, Offset(-13f + dir * 3f, -24f), Size(9f, 7f))
            drawOval(eyeCol, Offset(4f + dir * 3f, -24f), Size(9f, 7f))
            drawCircle(Color(0xFF991B1B), 2.4f, Offset(-8.5f + dir * 4f, -20.5f))
            drawCircle(Color(0xFF991B1B), 2.4f, Offset(8.5f + dir * 4f, -20.5f))
            drawLine(WorldInk, Offset(-15f + dir * 2f, -28f), Offset(-4f + dir * 2f, -24f), 3.2f, StrokeCap.Round)
            drawLine(WorldInk, Offset(15f + dir * 2f, -28f), Offset(4f + dir * 2f, -24f), 3.2f, StrokeCap.Round)

            // Wide mischievous Grinch smirk
            val grin = Path().apply {
                moveTo(-13f + dir * 2f, -11f)
                quadraticTo(dir * 2f, -4f, 13f + dir * 2f, -12f)
            }
            drawPath(grin, WorldInk, style = Stroke(3f, cap = StrokeCap.Round))
        }
    }

    // World-space Boss HP Bar & "SARI PE CAP!" indicator above Grinch's head
    val barW = 108f
    val barH = 12f
    val barX = cx - barW / 2f
    val barY = boss.y - 42f - stompPulse
    val hpFrac = (boss.hp.toFloat() / boss.maxHp.toFloat()).coerceIn(0f, 1f)

    drawContext.canvas.nativeCanvas.drawText(
        "⬇️ SARI PE CAP! (${boss.hp} HP)",
        cx,
        barY - 8f,
        labelPaint
    )
    drawRoundRect(Color(0xFF1E293B), Offset(barX, barY), Size(barW, barH), CornerRadius(6f, 6f))
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFFFA5252), Color(0xFFFCC419), Color(0xFF51CF66))),
        topLeft = Offset(barX + 1.5f, barY + 1.5f),
        size = Size((barW - 3f) * hpFrac, barH - 3f),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawRoundRect(WorldInk, Offset(barX, barY), Size(barW, barH), CornerRadius(6f, 6f), style = Stroke(2.4f))
}
