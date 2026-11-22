package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.findHeroMeta
import com.example.ui.HudUiState
import com.example.ui.components.ChristmasTouchButton
import com.example.ui.components.CoinBadgeDot
import com.example.ui.components.LiveHeroPreviewScaled
import com.example.ui.components.NeoSurfaceBox
import com.example.ui.components.formatRoNumber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicCyanLight
import com.example.ui.theme.CosmicGold
import com.example.ui.theme.CosmicGoldLight
import com.example.ui.theme.CosmicGreen
import com.example.ui.theme.CosmicInk
import com.example.ui.theme.CosmicPink
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.WinterPineDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileModal(
    uiState: HudUiState,
    onSaveCheckpointNow: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onCloudPushClick: () -> Unit,
    onCloudPullClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onOpenShopClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val p = uiState.progress
    val heroMeta = findHeroMeta(p.equippedHero)
    val passTier = min(15, p.passXP / 100 + 1)
    val hasActiveLevelCp = p.checkpointLevel == p.currentLevel && p.checkpointX > 70f

    ModalBackdrop(onDismiss = onDismiss, maxWidth = 460.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Festive Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0E2A47), Color(0xFF1C7ED6), Color(0xFF2B8A3E))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👤", fontSize = 20.sp)
                    Text(
                        text = "Profilul Meu & Checkpoint",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                ModalCloseButton(onClick = onDismiss, tag = "close_profile")
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
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Player Level, Hero & Progress Card
                NeoSurfaceBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    borderWidth = 3.dp,
                    shadowOffsetY = 4.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFE7F5FF)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.radialGradient(listOf(WinterPineDark, Color(0xFF071426))))
                                    .border(2.5.dp, CosmicInk, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                LiveHeroPreviewScaled(
                                    heroId = p.equippedHero,
                                    canvasSize = 54.dp,
                                    heroVirtualSize = 34f
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = uiState.userDisplayName ?: "Explorator Polul Nord",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CosmicInk,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Erou: ${heroMeta.name} (${heroMeta.rarity.label})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.horizontalGradient(listOf(CosmicGoldLight, CosmicGold)))
                                        .border(2.dp, CosmicInk, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("profile_level_badge")
                                ) {
                                    Text(
                                        text = "🎄 NIVELUL CURENT: ${p.currentLevel}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CosmicInk
                                    )
                                }
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProfileStatChip("🔓 Deblocat: Lvl ${p.unlockedLevel}", Color(0xFFD0EBFF))
                            ProfileStatChip("🏁 Terminate: ${p.completedLevels.size}", Color(0xFFD3F9D8))
                            ProfileStatChip("🏆 Pass Tier $passTier (${p.passXP} XP)", Color(0xFFFFF3BF))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CosmicGoldLight)
                                    .border(2.dp, CosmicInk, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoinBadgeDot(13.dp)
                                Text(
                                    text = formatRoNumber(p.coins),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CosmicInk
                                )
                            }
                            ProfileStatChip("💎 ${formatRoNumber(p.diamonds)}", CosmicCyanLight)
                        }

                        ChristmasTouchButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_profile_open_shop"),
                            cornerRadius = 13.dp,
                            borderWidth = 2.5.dp,
                            shadowOffsetY = 3.dp,
                            backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFFF8787), CosmicPink)),
                            glowColor = CosmicGoldLight,
                            effectsEnabled = uiState.touchEffectsEnabled,
                            onClick = onOpenShopClick
                        ) {
                            Text(
                                text = "🛒 Schimbă Eroul (Deschide Shop)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // 2. Persistent Checkpoint Card
                NeoSurfaceBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    borderWidth = 3.dp,
                    shadowOffsetY = 4.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFF3FFF5), Color(0xFFE6FCF5)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📍 Checkpoint Progres (Fără Pierderi)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = CosmicInk
                        )
                        Text(
                            text = if (hasActiveLevelCp) {
                                "✅ Checkpoint activ salvat la Nivelul ${p.checkpointLevel} (coord. ${p.checkpointX.toInt()}). Dacă ieși din joc, vei continua exact de aici!"
                            } else {
                                "💾 Nivelul curent (${p.currentLevel}), monedele, eroii și misiunile tale sunt salvate automat local. Apasă mai jos pentru a salva poziția exactă ca Checkpoint!"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        uiState.lastCloudSyncMs?.let { ts ->
                            val formatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ts))
                            Text(
                                text = "☁️ Ultimul Checkpoint Cloud sincronizat la: $formatted",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B7285)
                            )
                        }

                        ChristmasTouchButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_save_checkpoint"),
                            cornerRadius = 13.dp,
                            borderWidth = 2.5.dp,
                            shadowOffsetY = 3.dp,
                            backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFF69DB7C), CosmicGreen)),
                            glowColor = Color(0xFF8CE99A),
                            effectsEnabled = uiState.touchEffectsEnabled,
                            onClick = onSaveCheckpointNow
                        ) {
                            Text(
                                text = if (uiState.isSignedIn) {
                                    "💾 Salvează Checkpoint (Local + Cloud)"
                                } else {
                                    "💾 Salvează Checkpoint Acum"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                        }
                    }
                }

                // 3. Google Sign-In & Cloud Backup Card (+ honest Facebook status)
                NeoSurfaceBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    borderWidth = 3.dp,
                    shadowOffsetY = 4.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFF3F0FF)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isSignedIn) CosmicGreen else Color(0xFFADB5BD))
                                    .border(1.5.dp, CosmicInk, CircleShape)
                            )
                            Text(
                                text = if (uiState.isSignedIn) {
                                    "☁️ Conectat cu Contul Google"
                                } else {
                                    "🔐 Conectare Cont & Salvare Cloud"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                        }

                        if (uiState.isSignedIn) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE7F5FF))
                                    .border(2.dp, CosmicInk, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = uiState.userDisplayName ?: "Jucător Google",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CosmicInk
                                )
                                if (!uiState.userEmail.isNullOrBlank()) {
                                    Text(
                                        text = uiState.userEmail,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChristmasTouchButton(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_cloud_push"),
                                    cornerRadius = 13.dp,
                                    borderWidth = 2.5.dp,
                                    shadowOffsetY = 3.dp,
                                    backgroundBrush = Brush.horizontalGradient(listOf(CosmicCyanLight, CosmicCyan)),
                                    glowColor = Color.White,
                                    effectsEnabled = uiState.touchEffectsEnabled,
                                    enabled = !uiState.isCloudSyncing,
                                    onClick = onCloudPushClick
                                ) {
                                    Text(
                                        text = if (uiState.isCloudSyncing) "⏳..." else "☁️ Trimite",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CosmicInk
                                    )
                                }

                                ChristmasTouchButton(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_cloud_pull"),
                                    cornerRadius = 13.dp,
                                    borderWidth = 2.5.dp,
                                    shadowOffsetY = 3.dp,
                                    backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFD0BFFF), CosmicPurple)),
                                    glowColor = Color.White,
                                    effectsEnabled = uiState.touchEffectsEnabled,
                                    enabled = !uiState.isCloudSyncing,
                                    onClick = onCloudPullClick
                                ) {
                                    Text(
                                        text = if (uiState.isCloudSyncing) "⏳..." else "🔄 Restaurează",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CosmicInk
                                    )
                                }
                            }

                            ChristmasTouchButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_google_sign_out"),
                                cornerRadius = 13.dp,
                                borderWidth = 2.5.dp,
                                shadowOffsetY = 3.dp,
                                backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFFF8787), Color(0xFFE03131))),
                                glowColor = CosmicGoldLight,
                                effectsEnabled = uiState.touchEffectsEnabled,
                                onClick = onSignOutClick
                            ) {
                                Text(
                                    text = "🚪 Deconectează-te din Google",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                text = "Conectează-te cu contul Google pentru a-ți salva Checkpoint-ul, nivelul și eroii în Cloud și a nu pierde niciodată progresul.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )

                            if (!uiState.authErrorMessage.isNullOrBlank()) {
                                Text(
                                    text = "⚠️ ${uiState.authErrorMessage}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC92A2A)
                                )
                            }

                            ChristmasTouchButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_google_sign_in"),
                                cornerRadius = 14.dp,
                                borderWidth = 3.dp,
                                shadowOffsetY = 3.5.dp,
                                backgroundBrush = Brush.horizontalGradient(listOf(Color.White, Color(0xFFE7F5FF))),
                                glowColor = CosmicCyanLight,
                                effectsEnabled = uiState.touchEffectsEnabled,
                                enabled = !uiState.isAuthLoading,
                                onClick = onGoogleSignInClick
                            ) {
                                if (uiState.isAuthLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.5.dp,
                                        color = CosmicInk
                                    )
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "G",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1971C2)
                                        )
                                        Text(
                                            text = "Conectează-te cu Google",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CosmicInk
                                        )
                                    }
                                }
                            }

                            // Honest notice about Facebook login requirement (no non-functional dead button)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEDF2FF))
                                    .border(1.5.dp, Color(0xFF748FFC), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "📘 Despre conectarea cu Facebook",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "Autentificarea Facebook necesită configurare manuală în Firebase Console (Authentication → Facebook) cu un App ID și App Secret din Meta for Developers. Folosește conectarea cu Google de mai sus pentru salvarea automată în Cloud.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatChip(text: String, bg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(2.dp, CosmicInk, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            color = CosmicInk
        )
    }
}
