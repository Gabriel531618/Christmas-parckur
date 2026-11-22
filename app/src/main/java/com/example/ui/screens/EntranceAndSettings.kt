package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PersistentProgress
import com.example.model.findHeroMeta
import com.example.ui.HudUiState
import com.example.ui.components.ChristmasTouchButton
import com.example.ui.components.CoinBadgeDot
import com.example.ui.components.LiveHeroPreviewScaled
import com.example.ui.components.NeoSurfaceBox
import com.example.ui.components.formatRoNumber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicGold
import com.example.ui.theme.CosmicGoldLight
import com.example.ui.theme.CosmicGreen
import com.example.ui.theme.CosmicInk
import com.example.ui.theme.CosmicPink
import com.example.ui.theme.WinterPineDark
import kotlin.math.sin

@Composable
fun EntranceMenuOverlay(
    visible: Boolean,
    progress: PersistentProgress,
    effectsEnabled: Boolean,
    onPlayClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(240)),
        exit = fadeOut(tween(260))
    ) {
        val inf = rememberInfiniteTransition(label = "entrance_snow")
        val fallShift by inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
            label = "fallShift"
        )
        val heroMeta = findHeroMeta(progress.equippedHero)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF071426), Color(0xFF0E2A47), WinterPineDark)
                    )
                )
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0 until 65) {
                    val speed = 0.7f + (i % 4) * 0.4f
                    val rawY = (i * 97f) + fallShift * size.height * speed
                    val sy = rawY % size.height
                    val sx = ((i * 173f) + sin(fallShift * 6.28f + i) * 24f + size.width) % size.width
                    val r = (1.5f + (i % 3)) * density
                    drawCircle(Color.White.copy(alpha = 0.62f), r, Offset(sx, sy))
                }
            }

            NeoSurfaceBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 350.dp),
                cornerRadius = 26.dp,
                borderWidth = 3.5.dp,
                shadowOffsetY = 8.dp,
                backgroundBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFF0F9FF)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoSurfaceBox(
                        cornerRadius = 999.dp,
                        borderWidth = 2.5.dp,
                        shadowOffsetY = 2.5.dp,
                        backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFE03131), CosmicGold, Color(0xFF2B8A3E)))
                    ) {
                        Text(
                            text = "🎄 CHRISTMAS PARKOUR 🎄",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    LiveHeroPreviewScaled(
                        heroId = progress.equippedHero,
                        canvasSize = 68.dp,
                        heroVirtualSize = 36f
                    )

                    Text(
                        text = "Christmas Parkour",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = CosmicInk,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE7F5FF))
                                .border(2.dp, CosmicInk, RoundedCornerShape(10.dp))
                                .clickable(onClick = onProfileClick)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("btn_menu_profile")
                        ) {
                            Text(
                                text = "👤 Nivel ${progress.currentLevel} • ${heroMeta.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CosmicGoldLight)
                                .border(2.dp, CosmicInk, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinBadgeDot(12.dp)
                            Text(
                                text = formatRoNumber(progress.coins),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                        }
                    }

                    // Compact Play, Settings & Exit buttons with festive touch burst effects!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        ChristmasTouchButton(
                            modifier = Modifier
                                .fillMaxWidth(0.76f)
                                .widthIn(max = 225.dp)
                                .height(48.dp)
                                .testTag("btn_menu_play"),
                            cornerRadius = 14.dp,
                            borderWidth = 3.dp,
                            shadowOffsetY = 4.dp,
                            backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFF69DB7C), Color(0xFF2B8A3E))),
                            glowColor = Color(0xFF8CE99A),
                            effectsEnabled = effectsEnabled,
                            onClick = onPlayClick
                        ) {
                            Text(
                                text = "▶ PLAY",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        ChristmasTouchButton(
                            modifier = Modifier
                                .fillMaxWidth(0.76f)
                                .widthIn(max = 225.dp)
                                .height(48.dp)
                                .testTag("btn_menu_settings"),
                            cornerRadius = 14.dp,
                            borderWidth = 3.dp,
                            shadowOffsetY = 4.dp,
                            backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFA5D8FF), CosmicCyan)),
                            glowColor = Color(0xFFD0EBFF),
                            effectsEnabled = effectsEnabled,
                            onClick = onSettingsClick
                        ) {
                            Text(
                                text = "⚙️ SETTINGS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                        }

                        ChristmasTouchButton(
                            modifier = Modifier
                                .fillMaxWidth(0.76f)
                                .widthIn(max = 225.dp)
                                .height(48.dp)
                                .testTag("btn_menu_exit"),
                            cornerRadius = 14.dp,
                            borderWidth = 3.dp,
                            shadowOffsetY = 4.dp,
                            backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFFF8787), Color(0xFFE03131))),
                            glowColor = CosmicGoldLight,
                            effectsEnabled = effectsEnabled,
                            onClick = onExitClick
                        ) {
                            Text(
                                text = "🚪 EXIT",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsModal(
    uiState: HudUiState,
    onToggleSound: () -> Unit,
    onToggleLandscape: () -> Unit,
    onToggleTouchEffects: () -> Unit,
    onReturnToMenu: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    ModalBackdrop(onDismiss = onDismiss, maxWidth = 390.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF1C7ED6), Color(0xFF339AF0), Color(0xFF2B8A3E))))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️", fontSize = 18.sp)
                    Text(
                        text = "Setări Crăciun",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                ModalCloseButton(onClick = onDismiss, tag = "close_settings")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(CosmicInk)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChristmasTouchButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_settings_sound"),
                    cornerRadius = 14.dp,
                    borderWidth = 3.dp,
                    shadowOffsetY = 3.5.dp,
                    backgroundBrush = if (uiState.isMuted) {
                        Brush.horizontalGradient(listOf(Color(0xFFE9ECEF), Color(0xFFCED4DA)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF69DB7C), CosmicGreen))
                    },
                    glowColor = Color(0xFF8CE99A),
                    effectsEnabled = uiState.touchEffectsEnabled,
                    onClick = onToggleSound
                ) {
                    Text(
                        text = if (uiState.isMuted) "🔇 Sunet: OPRIT" else "🔊 Sunet: PORNIT",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicInk
                    )
                }

                ChristmasTouchButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_settings_landscape"),
                    cornerRadius = 14.dp,
                    borderWidth = 3.dp,
                    shadowOffsetY = 3.5.dp,
                    backgroundBrush = if (uiState.isLandscapeLocked) {
                        Brush.horizontalGradient(listOf(CosmicGoldLight, CosmicGold))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFE7F5FF), Color(0xFFA5D8FF)))
                    },
                    glowColor = CosmicGoldLight,
                    effectsEnabled = uiState.touchEffectsEnabled,
                    onClick = onToggleLandscape
                ) {
                    Text(
                        text = if (uiState.isLandscapeLocked) "🔄 Ecran: LANDSCAPE" else "📱 Ecran: AUTOMAT",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicInk
                    )
                }

                ChristmasTouchButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_settings_effects"),
                    cornerRadius = 14.dp,
                    borderWidth = 3.dp,
                    shadowOffsetY = 3.5.dp,
                    backgroundBrush = if (uiState.touchEffectsEnabled) {
                        Brush.horizontalGradient(listOf(Color(0xFFFFD8A8), CosmicPink))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFE9ECEF), Color(0xFFCED4DA)))
                    },
                    glowColor = CosmicGoldLight,
                    effectsEnabled = uiState.touchEffectsEnabled,
                    onClick = onToggleTouchEffects
                ) {
                    Text(
                        text = if (uiState.touchEffectsEnabled) "❄️ Efecte Atingere: PORNITE" else "❄️ Efecte Atingere: OPRITE",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicInk
                    )
                }

                if (onReturnToMenu != null) {
                    ChristmasTouchButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_settings_menu"),
                        cornerRadius = 14.dp,
                        borderWidth = 3.dp,
                        shadowOffsetY = 3.5.dp,
                        backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFFF8787), Color(0xFFE03131))),
                        glowColor = CosmicGoldLight,
                        effectsEnabled = uiState.touchEffectsEnabled,
                        onClick = onReturnToMenu
                    ) {
                        Text(
                            text = "🏠 Înapoi la Meniu (Play / Exit)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
