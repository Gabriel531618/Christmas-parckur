package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AbilityType
import com.example.model.HeroRarity
import com.example.model.findHeroMeta
import com.example.ui.HudUiState
import com.example.ui.canvas.drawHeroWithEffects
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicCyanLight
import com.example.ui.theme.CosmicGold
import com.example.ui.theme.CosmicGoldLight
import com.example.ui.theme.CosmicGreen
import com.example.ui.theme.CosmicInk
import com.example.ui.theme.CosmicPink
import com.example.ui.theme.WinterPineDark
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NeoSurfaceBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    borderWidth: Dp = 3.dp,
    shadowOffsetY: Dp = 4.dp,
    backgroundBrush: Brush,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shift = if (isPressed && enabled && onClick != null) shadowOffsetY * 0.6f else 0.dp
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .drawBehind {
                val radiusPx = cornerRadius.toPx()
                val shadowPx = shadowOffsetY.toPx()
                if (!isPressed || onClick == null) {
                    drawRoundRect(
                        color = CosmicInk,
                        topLeft = Offset(0f, shadowPx),
                        size = size,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                }
            }
            .offset(y = shift)
            .clip(shape)
            .background(backgroundBrush)
            .border(borderWidth, CosmicInk, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = Color.White),
                        enabled = enabled,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun ChristmasTouchButton(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    borderWidth: Dp = 3.dp,
    shadowOffsetY: Dp = 4.dp,
    backgroundBrush: Brush,
    glowColor: Color = CosmicGoldLight,
    effectsEnabled: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var burstSeed by remember { mutableIntStateOf(0) }
    val burstProgress = remember { Animatable(1f) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press && effectsEnabled) {
                burstSeed++
                burstProgress.snapTo(0f)
                burstProgress.animateTo(1f, tween(440, easing = FastOutSlowInEasing))
            }
        }
    }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 520f),
        label = "xmas_btn_scale"
    )
    val shift = if (isPressed && enabled) shadowOffsetY * 0.65f else 0.dp
    val shape = RoundedCornerShape(cornerRadius)
    val burstVal = burstProgress.value

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val radiusPx = cornerRadius.toPx()
                    val shadowPx = shadowOffsetY.toPx()
                    if (effectsEnabled && (isPressed || burstVal < 0.98f)) {
                        val auraAlpha = if (isPressed) 0.55f else (1f - burstVal) * 0.5f
                        val pad = (6.dp.toPx() + burstVal * 10.dp.toPx())
                        drawRoundRect(
                            color = glowColor.copy(alpha = auraAlpha),
                            topLeft = Offset(-pad * 0.5f, -pad * 0.5f),
                            size = Size(size.width + pad, size.height + pad),
                            cornerRadius = CornerRadius(radiusPx + pad * 0.4f, radiusPx + pad * 0.4f)
                        )
                    }
                    if (!isPressed) {
                        drawRoundRect(
                            color = CosmicInk,
                            topLeft = Offset(0f, shadowPx),
                            size = size,
                            cornerRadius = CornerRadius(radiusPx, radiusPx)
                        )
                    }
                }
                .offset(y = shift)
                .clip(shape)
                .background(backgroundBrush)
                .border(
                    width = borderWidth,
                    color = if (effectsEnabled && isPressed) glowColor else CosmicInk,
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = Color.White),
                    enabled = enabled,
                    onClick = {
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center,
            content = content
        )

        if (effectsEnabled && burstVal < 0.99f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val alpha = (1f - burstVal).coerceIn(0f, 1f)
                val palette = listOf(Color.White, CosmicGoldLight, Color(0xFF74C0FC), Color(0xFF69DB7C), Color(0xFFFF8787))

                // Expanding festive ring
                drawRoundRect(
                    color = glowColor.copy(alpha = alpha * 0.85f),
                    topLeft = Offset(-burstVal * 12f, -burstVal * 10f),
                    size = Size(size.width + burstVal * 24f, size.height + burstVal * 20f),
                    cornerRadius = CornerRadius(cornerRadius.toPx() + burstVal * 10f),
                    style = Stroke(width = 3.2f * alpha)
                )

                // Radial Christmas snowflake & star sparks around the touched button
                val count = 14
                for (i in 0 until count) {
                    val angle = ((i.toFloat() / count) * 2f * PI.toFloat()) + (burstSeed * 0.47f)
                    val distX = (size.width * 0.42f + burstVal * 34.dp.toPx()) * cos(angle)
                    val distY = (size.height * 0.42f + burstVal * 24.dp.toPx()) * sin(angle)
                    val px = cx + distX
                    val py = cy + distY
                    val color = palette[(i + burstSeed) % palette.size].copy(alpha = alpha)
                    val r = (3.8f + (i % 3) * 1.4f) * (1f - burstVal * 0.35f)
                    if (i % 2 == 0) {
                        // Mini crystal snowflake cross
                        for (k in 0 until 3) {
                            val a = angle + k * (PI.toFloat() / 3f)
                            drawLine(
                                color = color,
                                start = Offset(px - cos(a) * r, py - sin(a) * r),
                                end = Offset(px + cos(a) * r, py + sin(a) * r),
                                strokeWidth = 2.2f,
                                cap = StrokeCap.Round
                            )
                        }
                    } else {
                        drawCircle(color = color, radius = r * 0.72f, center = Offset(px, py))
                    }
                }
            }
        }
    }
}

@Composable
fun GalacticGemIcon(size: Dp = 18.dp, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "xmas_star_pulse")
    val pulse by inf.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val outerR = this.size.minDimension * 0.46f * pulse
        val innerR = outerR * 0.46f
        val starPath = Path().apply {
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val a = -PI.toFloat() / 2f + i * (PI.toFloat() / 5f)
                val px = cx + cos(a) * r
                val py = cy + sin(a) * r
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(
            starPath,
            Brush.linearGradient(
                listOf(CosmicGoldLight, CosmicGold, Color(0xFFFF922B)),
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            )
        )
        drawPath(starPath, CosmicInk, style = Stroke(2.0f))
        drawCircle(Color.White, outerR * 0.14f, Offset(cx - outerR * 0.12f, cy - outerR * 0.12f))
    }
}

@Composable
fun CoinBadgeDot(size: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFFFFF3BF), Color(0xFFFAB005))))
            .border(1.8.dp, Color(0xFF7C4D00), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "1",
            fontSize = (size.value * 0.58f).sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF5C3800),
            lineHeight = (size.value * 0.6f).sp
        )
    }
}

@Composable
fun LiveHeroPreviewScaled(
    heroId: String,
    canvasSize: Dp,
    heroVirtualSize: Float,
    phaseOffset: Float = 0f,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "hero_preview_scaled")
    val t by inf.animateFloat(
        initialValue = 0f,
        targetValue = 62.8318f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    Canvas(modifier = modifier.size(canvasSize)) {
        val virtualBox = canvasSize.value
        val scaleFactor = size.minDimension / virtualBox
        val pad = (virtualBox - heroVirtualSize) / 2f
        val bob = sin(t * 4.2f + phaseOffset) * 2.6f
        scale(scaleFactor, scaleFactor, Offset.Zero) {
            drawHeroWithEffects(
                heroId = heroId,
                x = pad,
                y = pad + bob,
                w = heroVirtualSize,
                h = heroVirtualSize,
                dir = 1,
                t = t + phaseOffset,
                vy = bob,
                isShopPreview = true
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopHudOverlay(
    uiState: HudUiState,
    onHeroBadgeClick: () -> Unit,
    onLevelsClick: () -> Unit,
    onQuestsClick: () -> Unit,
    onShopClick: () -> Unit,
    onPassClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRotateClick: () -> Unit,
    onSoundClick: () -> Unit,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = uiState.progress
    // Use FlowRow at root so portrait screens wrap the two clusters cleanly across 2 rows without ever crushing the left cluster!
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Cluster 1: hero badge, level selector, coins, diamonds, magic stars
        FlowRow(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(CosmicInk.copy(alpha = 0.52f))
                .border(1.5.dp, CosmicGreen.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NeoSurfaceBox(
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_hud_hero")
                    .semantics {
                        contentDescription = "Profilul meu, Nivel ${p.currentLevel} și conectare Google"
                    },
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.radialGradient(listOf(WinterPineDark, Color(0xFF071426))),
                onClick = onHeroBadgeClick
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LiveHeroPreviewScaled(
                        heroId = p.equippedHero,
                        canvasSize = 32.dp,
                        heroVirtualSize = 21f
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isSignedIn) CosmicGreen else CosmicGoldLight)
                            .border(1.5.dp, CosmicInk, CircleShape)
                    )
                }
            }

            NeoSurfaceBox(
                modifier = Modifier
                    .height(36.dp)
                    .testTag("btn_levels"),
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFE7F5FF))),
                onClick = onLevelsClick
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🎄", fontSize = 12.sp)
                    Text(
                        text = "Nivel ${p.currentLevel} / ????",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 12.sp,
                        color = CosmicInk,
                        maxLines = 1
                    )
                    Text("▼", fontSize = 9.sp, color = CosmicInk)
                }
            }

            NeoSurfaceBox(
                modifier = Modifier.height(36.dp),
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(CosmicGoldLight, CosmicGold))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CoinBadgeDot(14.dp)
                    Text(
                        text = formatRoNumber(p.coins),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 12.sp,
                        color = CosmicInk,
                        maxLines = 1
                    )
                }
            }

            NeoSurfaceBox(
                modifier = Modifier.height(36.dp),
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(CosmicCyanLight, CosmicCyan))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("💎", fontSize = 11.sp)
                    Text(
                        text = formatRoNumber(p.diamonds),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 12.sp,
                        color = CosmicInk,
                        maxLines = 1
                    )
                }
            }
        }

        // Cluster 2: Quests, Shop, Pass, Rotate, Sound, Restart
        FlowRow(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(CosmicInk.copy(alpha = 0.52f))
                .border(1.5.dp, Color(0xFFFA5252).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NeoSurfaceBox(
                modifier = Modifier
                    .height(36.dp)
                    .testTag("btn_quests"),
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF69DB7C), Color(0xFF2B8A3E))),
                onClick = onQuestsClick
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("📜", fontSize = 12.sp)
                    Text("Questuri", style = MaterialTheme.typography.labelLarge, fontSize = 12.sp, color = Color.White)
                }
            }

            NeoSurfaceBox(
                modifier = Modifier
                    .height(36.dp)
                    .testTag("btn_shop"),
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFF8787), CosmicPink)),
                onClick = onShopClick
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🛒", fontSize = 12.sp)
                    Text("Shop", style = MaterialTheme.typography.labelLarge, fontSize = 12.sp, color = Color.White)
                }
            }

            NeoSurfaceBox(
                modifier = Modifier
                    .height(36.dp)
                    .testTag("btn_pass"),
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(CosmicGoldLight, Color(0xFFF59F00))),
                onClick = onPassClick
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🏆", fontSize = 12.sp)
                    Text("Pass", style = MaterialTheme.typography.labelLarge, fontSize = 12.sp, color = CosmicInk)
                }
            }

            NeoSurfaceBox(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("btn_hud_settings")
                    .semantics { contentDescription = "Setări" },
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFD0EBFF), Color(0xFF74C0FC))),
                onClick = onSettingsClick
            ) {
                Text("⚙️", fontSize = 14.sp)
            }

            NeoSurfaceBox(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("btn_rotate")
                    .semantics { contentDescription = "Mod Orizontal" },
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFB2F2BB), Color(0xFF51CF66))),
                onClick = onRotateClick
            ) {
                Text("🔄", fontSize = 14.sp)
            }

            NeoSurfaceBox(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("btn_sound")
                    .semantics { contentDescription = "Sunet" },
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F3F5))),
                onClick = onSoundClick
            ) {
                Text(if (uiState.isMuted) "🔇" else "🔊", fontSize = 14.sp)
            }

            NeoSurfaceBox(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("btn_restart")
                    .semantics { contentDescription = "Reîncepe Nivelul" },
                cornerRadius = 11.dp,
                shadowOffsetY = 3.dp,
                backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFA5D8FF), Color(0xFF4DABF7))),
                onClick = onRestartClick
            ) {
                Text("↻", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CosmicInk)
            }
        }
    }
}

@Composable
fun EffectsStatusStrip(uiState: HudUiState, modifier: Modifier = Modifier) {
    val badges = buildList {
        if (uiState.inSantaVillage) add("🏠 Satul lui Moș Crăciun")
        if (uiState.solarShieldActive) add("🔥 Scut Festiv Activ")
        when (uiState.activeAbilityKind) {
            AbilityType.SLOWMO -> add("❄️ Frost-Slow (${String.format("%.1f", uiState.activeAbilityTimeLeft)}s)")
            AbilityType.ZEROG -> add("🎄 Zero-G + Magnet (${String.format("%.1f", uiState.activeAbilityTimeLeft)}s)")
            AbilityType.SOLAR -> add("🔥 Festive Flight (${String.format("%.1f", uiState.activeAbilityTimeLeft)}s)")
            AbilityType.BLIZZARD -> add("🌨️ Viscol Polar (${String.format("%.1f", uiState.activeAbilityTimeLeft)}s)")
            AbilityType.AURORA_NOVA -> add("🌌 Explozie Aurorală (${String.format("%.1f", uiState.activeAbilityTimeLeft)}s)")
            AbilityType.PRESENT_BARRAGE -> add("🎁 Ploaie Cadouri (${String.format("%.1f", uiState.activeAbilityTimeLeft)}s)")
            AbilityType.CANDY_TURBO -> add("🍬 Turbo Acadea (${String.format("%.1f", uiState.activeAbilityTimeLeft)}s)")
            else -> Unit
        }
    }
    val bossHp = if (!uiState.inSantaVillage) uiState.grinchBossHp else null
    if (badges.isNotEmpty() || bossHp != null) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (bossHp != null) {
                val maxHp = uiState.grinchBossMaxHp.coerceAtLeast(1)
                val frac = (bossHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .widthIn(max = 320.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CosmicInk.copy(alpha = 0.90f))
                        .border(2.5.dp, Color(0xFF82C91E), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("boss_hp_bar"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "👑 ${uiState.grinchBossName} • ${formatRoNumber(bossHp)} / ${formatRoNumber(maxHp)} HP (Sari pe cap!)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFE066),
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(frac)
                                .background(Brush.horizontalGradient(listOf(Color(0xFFFA5252), Color(0xFFFCC419), Color(0xFF82C91E))))
                        )
                    }
                }
            }

            if (badges.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badges.forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CosmicInk.copy(alpha = 0.92f))
                                .border(2.dp, if (label.startsWith("🏠")) CosmicGreen else CosmicCyan, RoundedCornerShape(12.dp))
                                .padding(horizontal = 11.dp, vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DimensionActionPrompt(
    uiState: HudUiState,
    onOpenTreeDoor: () -> Unit,
    onReturnFromVillage: () -> Unit,
    onOpenSantaQuests: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.nearTreeDoor && !uiState.inSantaVillage) {
            NeoSurfaceBox(
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_open_tree_door"),
                cornerRadius = 16.dp,
                borderWidth = 3.dp,
                shadowOffsetY = 4.dp,
                backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFF69DB7C), Color(0xFF2FB344))),
                onClick = onOpenTreeDoor
            ) {
                Text(
                    text = "🚪 Deschide Ușa Bradului!",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = CosmicInk
                )
            }
        }

        if (uiState.nearReturnDoor && uiState.inSantaVillage) {
            NeoSurfaceBox(
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_return_from_village"),
                cornerRadius = 16.dp,
                borderWidth = 3.dp,
                shadowOffsetY = 4.dp,
                backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFF8CE99A), Color(0xFF2B8A3E))),
                onClick = onReturnFromVillage
            ) {
                Text(
                    text = "🎄 Înapoi la Nivel",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        if (uiState.nearSanta && uiState.inSantaVillage) {
            NeoSurfaceBox(
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_talk_santa"),
                cornerRadius = 16.dp,
                borderWidth = 3.dp,
                shadowOffsetY = 4.dp,
                backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFFA5252), Color(0xFFF59F00))),
                onClick = onOpenSantaQuests
            ) {
                Text(
                    text = "🎅 Questurile lui Moș Crăciun!",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TouchControlsOverlay(
    equippedHeroId: String,
    abilityCdLeft: Float,
    abilityMaxCd: Float,
    effectsEnabled: Boolean = true,
    onLeftChanged: (Boolean) -> Unit,
    onRightChanged: (Boolean) -> Unit,
    onJumpPressed: () -> Unit,
    onJumpReleased: () -> Unit,
    onAbilityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroMeta = findHeroMeta(equippedHeroId)
    val isDivine = heroMeta.rarity == HeroRarity.DIVIN
    var jumpHeld by remember { mutableStateOf(false) }
    val jumpBurst = remember { Animatable(1f) }
    val jumpScope = rememberCoroutineScope()
    val jumpScale by animateFloatAsState(
        targetValue = if (jumpHeld) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
        label = "jump_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Compact Left & Right buttons (66.dp) that morph into glowing Crystal Snowflakes with frost effect on touch!
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SnowflakePadButton(
                label = "<",
                contentDesc = "Stânga",
                tag = "btn_left",
                size = 66.dp,
                cornerRadius = 20.dp,
                onHoldChanged = onLeftChanged
            )
            SnowflakePadButton(
                label = ">",
                contentDesc = "Dreapta",
                tag = "btn_right",
                size = 66.dp,
                cornerRadius = 20.dp,
                onHoldChanged = onRightChanged
            )
        }

        // Action Pad (Divine Ability + Jump)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            if (isDivine) {
                val onCooldown = abilityCdLeft > 0f
                val cdFraction = if (abilityMaxCd > 0f) (abilityCdLeft / abilityMaxCd).coerceIn(0f, 1f) else 0f
                ChristmasTouchButton(
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("btn_ability")
                        .graphicsLayer { alpha = if (onCooldown) 0.72f else 1f }
                        .semantics { contentDescription = heroMeta.abilityName ?: "Abilitate Specială" },
                    cornerRadius = 18.dp,
                    borderWidth = 3.dp,
                    shadowOffsetY = 4.dp,
                    backgroundBrush = Brush.linearGradient(
                        listOf(CosmicGoldLight, Color(0xFFFF922B), CosmicPink)
                    ),
                    glowColor = CosmicGoldLight,
                    effectsEnabled = effectsEnabled,
                    onClick = onAbilityClick
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (onCooldown) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(62.dp * cdFraction)
                                    .background(CosmicInk.copy(alpha = 0.65f))
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(heroMeta.abilityIcon ?: "🎅", fontSize = 18.sp)
                            Text(
                                text = (heroMeta.abilityName ?: "ABILITATE").uppercase(),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk,
                                maxLines = 1
                            )
                        }
                        if (onCooldown) {
                            Text(
                                text = "${String.format("%.1f", abilityCdLeft)}s",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Compact Bubble Jump button (68.dp) with frost/bubble touch burst
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .graphicsLayer {
                        scaleX = jumpScale
                        scaleY = jumpScale
                    }
                    .testTag("btn_jump")
                    .semantics { contentDescription = "Sari cu bule" },
                contentAlignment = Alignment.Center
            ) {
                NeoSurfaceBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(effectsEnabled) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    when (event.type) {
                                        PointerEventType.Press -> {
                                            jumpHeld = true
                                            onJumpPressed()
                                            if (effectsEnabled) {
                                                jumpScope.launch {
                                                    jumpBurst.snapTo(0f)
                                                    jumpBurst.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
                                                }
                                            }
                                        }
                                        PointerEventType.Release -> {
                                            jumpHeld = false
                                            onJumpReleased()
                                        }
                                    }
                                }
                            }
                        },
                    cornerRadius = 20.dp,
                    borderWidth = 3.dp,
                    shadowOffsetY = 4.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(CosmicGoldLight, CosmicGold))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🫧", fontSize = 18.sp)
                        Text("SARI", fontSize = 12.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                    }
                }

                if (effectsEnabled && jumpBurst.value < 0.99f) {
                    val p = jumpBurst.value
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val alpha = (1f - p).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color(0xFFD0EBFF).copy(alpha = alpha * 0.85f),
                            radius = size.minDimension * (0.48f + p * 0.35f),
                            center = Offset(cx, cy),
                            style = Stroke(width = 3.5f * alpha)
                        )
                        for (i in 0 until 8) {
                            val angle = i * (PI.toFloat() / 4f)
                            val dist = size.minDimension * (0.35f + p * 0.42f)
                            drawCircle(
                                color = if (i % 2 == 0) Color.White.copy(alpha = alpha) else Color(0xFF74C0FC).copy(alpha = alpha),
                                radius = 4.5f * (1f - p * 0.4f),
                                center = Offset(cx + cos(angle) * dist, cy + sin(angle) * dist)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnowflakePadButton(
    label: String,
    contentDesc: String,
    tag: String,
    size: Dp,
    cornerRadius: Dp,
    onHoldChanged: (Boolean) -> Unit
) {
    var isHeld by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isHeld) 1.14f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 420f),
        label = "snowflake_scale"
    )
    val inf = rememberInfiniteTransition(label = "snowflake_spin")
    val spinDeg by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing)),
        label = "spin"
    )
    val pulseVal by inf.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val normalBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFE7F5FF)))

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
            .testTag(tag)
            .semantics { contentDescription = contentDesc }
            .then(
                if (!isHeld) {
                    Modifier
                        .drawBehind {
                            val r = cornerRadius.toPx()
                            drawRoundRect(
                                color = CosmicInk,
                                topLeft = Offset(0f, 5.dp.toPx()),
                                size = this.size,
                                cornerRadius = CornerRadius(r, r)
                            )
                        }
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(normalBrush)
                        .border(3.5.dp, CosmicInk, RoundedCornerShape(cornerRadius))
                } else {
                    Modifier
                }
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val anyPressed = event.changes.any { it.pressed }
                        isHeld = anyPressed
                        onHoldChanged(anyPressed)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isHeld) {
            // Completely transforms into a spinning radiant Crystal Snowflake with frost touch burst!
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val r = this.size.minDimension * 0.42f

                // Radiant icy halo behind the snowflake
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFF74C0FC).copy(alpha = 0.65f),
                            Color(0xFF1C7ED6).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = r * 1.45f * pulseVal
                    ),
                    radius = r * 1.45f * pulseVal,
                    center = Offset(cx, cy)
                )

                // Outer frost touch ring
                drawCircle(
                    color = Color(0xFFD0EBFF).copy(alpha = 0.8f),
                    radius = r * 1.05f * pulseVal,
                    center = Offset(cx, cy),
                    style = Stroke(3.2f)
                )

                // 6-branch rotating crystal snowflake + sparkling frost dots
                for (i in 0 until 6) {
                    val angle = (spinDeg * 0.35f + i * 60f) * PI.toFloat() / 180f
                    val ex = cx + cos(angle) * r
                    val ey = cy + sin(angle) * r
                    // Outer dark-ice outline for high contrast
                    drawLine(
                        color = Color(0xFF0B7285),
                        start = Offset(cx, cy),
                        end = Offset(ex, ey),
                        strokeWidth = 6.5f,
                        cap = StrokeCap.Round
                    )
                    // Bright crystal core
                    drawLine(
                        color = Color.White,
                        start = Offset(cx, cy),
                        end = Offset(ex, ey),
                        strokeWidth = 3.2f,
                        cap = StrokeCap.Round
                    )
                    // Double V-twigs along each arm
                    for (frac in listOf(0.45f, 0.72f)) {
                        val midX = cx + cos(angle) * (r * frac)
                        val midY = cy + sin(angle) * (r * frac)
                        val twigLen = r * (if (frac < 0.6f) 0.32f else 0.22f)
                        for (delta in listOf(0.62f, -0.62f)) {
                            val bAngle = angle + delta
                            val tx = midX + cos(bAngle) * twigLen
                            val ty = midY + sin(bAngle) * twigLen
                            drawLine(Color(0xFF0B7285), Offset(midX, midY), Offset(tx, ty), 4.6f, StrokeCap.Round)
                            drawLine(Color.White, Offset(midX, midY), Offset(tx, ty), 2.2f, StrokeCap.Round)
                        }
                    }
                    // Tip frost sparkle
                    drawCircle(Color(0xFFBAE6FD), 3.6f, Offset(ex, ey))
                }
                drawCircle(Color(0xFF0B7285), 7f, Offset(cx, cy))
                drawCircle(Color.White, 4.5f, Offset(cx, cy))
            }
        } else {
            Text(label, fontSize = 34.sp, fontWeight = FontWeight.Black, color = CosmicInk)
        }
    }
}

fun formatRoNumber(value: Int): String {
    return value.toString().reversed().chunked(3).joinToString(".").reversed()
}
