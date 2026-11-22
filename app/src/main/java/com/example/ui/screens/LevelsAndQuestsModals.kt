package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.themeForLevel
import com.example.model.BonusQuestDef
import com.example.model.PersistentProgress
import com.example.model.QuestDef
import com.example.model.SANTA_QUESTS
import com.example.model.STANDARD_QUESTS
import com.example.model.SantaGrandSleighBonus
import com.example.model.SantaQuestDef
import com.example.ui.ParkourViewModel
import com.example.ui.VictorySummary
import com.example.ui.components.CoinBadgeDot
import com.example.ui.components.LiveHeroPreviewScaled
import com.example.ui.components.NeoSurfaceBox
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicGold
import com.example.ui.theme.CosmicGoldLight
import com.example.ui.theme.CosmicGreen
import com.example.ui.theme.CosmicInk
import com.example.ui.theme.CosmicPink
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicRed
import com.example.ui.theme.CosmicViolet
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ModalBackdrop(
    onDismiss: () -> Unit,
    maxWidth: Dp = 540.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050714).copy(alpha = 0.82f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        NeoSurfaceBox(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            cornerRadius = 26.dp,
            borderWidth = 3.5.dp,
            shadowOffsetY = 8.dp,
            backgroundBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFF8F9FA))),
            content = content
        )
    }
}

@Composable
fun ModalCloseButton(onClick: () -> Unit, tag: String) {
    NeoSurfaceBox(
        modifier = Modifier
            .size(36.dp)
            .testTag(tag),
        cornerRadius = 11.dp,
        borderWidth = 3.dp,
        shadowOffsetY = 3.dp,
        backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFF8787), CosmicRed)),
        onClick = onClick
    ) {
        Text("✕", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CosmicInk)
    }
}

@Composable
fun VictoryModal(
    summary: VictorySummary,
    onNextLevel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050714).copy(alpha = 0.72f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        NeoSurfaceBox(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 375.dp),
            cornerRadius = 24.dp,
            borderWidth = 3.5.dp,
            shadowOffsetY = 8.dp,
            backgroundBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFF8F9FA)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆", fontSize = 42.sp)
                Text(
                    text = "Nivelul ${summary.level} Completat! • Nivel ${summary.level} / ????",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2B8A3E),
                    textAlign = TextAlign.Center
                )
                val nextTheme = remember(summary.level) { themeForLevel(summary.level + 1) }
                Text(
                    text = "Ai cucerit: ${themeForLevel(summary.level).title}!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF495057),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "🏙️ Urmează Nivel ${summary.level + 1}: ${nextTheme.title}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1971C2),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoSurfaceBox(
                        cornerRadius = 12.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = Brush.verticalGradient(listOf(CosmicGoldLight, CosmicGold))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinBadgeDot(15.dp)
                            Text("+${summary.coinsEarned}", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                        }
                    }

                    NeoSurfaceBox(
                        cornerRadius = 12.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFE5DBFF), Color(0xFFD0BFFF)))
                    ) {
                        Text(
                            text = "🏆 +${summary.xpEarned} XP",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = CosmicInk
                        )
                    }

                    if (summary.diamondsEarned > 0) {
                        NeoSurfaceBox(
                            cornerRadius = 12.dp,
                            shadowOffsetY = 3.dp,
                            backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF99E9F2), CosmicCyan))
                        ) {
                            Text(
                                text = "💎 +${summary.diamondsEarned}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                        }
                    }
                }

                NeoSurfaceBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_next_lvl"),
                    cornerRadius = 15.dp,
                    borderWidth = 3.5.dp,
                    shadowOffsetY = 4.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF69DB7C), Color(0xFF40C057))),
                    onClick = onNextLevel
                ) {
                    Text(
                        text = "Nivelul ${summary.level + 1} / ???? ➔",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicInk
                    )
                }
            }
        }
    }
}

@Composable
fun LevelsModal(
    progress: PersistentProgress,
    onSelectLevel: (Int) -> Unit,
    onMysteryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val topLvl = max(progress.unlockedLevel, progress.currentLevel)
    val startLvl = max(1, topLvl - 47)
    val levelList = (startLvl..topLvl).toList()

    ModalBackdrop(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF66D9E8), CosmicPurple)))
                    .border(0.dp, Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏁", fontSize = 18.sp)
                    Text(
                        text = "Alege Nivelul • Nivel ${progress.currentLevel} / ????",
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicInk
                    )
                }
                ModalCloseButton(onClick = onDismiss, tag = "close_levels")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(CosmicInk)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 68.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(levelList) { lvl ->
                    val isCurrent = progress.currentLevel == lvl
                    val isDone = lvl in progress.completedLevels
                    val bg = when {
                        isCurrent -> Brush.verticalGradient(listOf(CosmicGoldLight, CosmicGold))
                        isDone -> Brush.verticalGradient(listOf(Color(0xFFEBFBEE), Color(0xFFD3F9D8)))
                        else -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF8F9FA)))
                    }
                    NeoSurfaceBox(
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("lvl_cell_$lvl"),
                        cornerRadius = 13.dp,
                        shadowOffsetY = 4.dp,
                        backgroundBrush = bg,
                        onClick = { onSelectLevel(lvl) }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "$lvl",
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                            if (isDone) {
                                Text(
                                    text = "★",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 5.dp),
                                    fontSize = 10.sp,
                                    color = Color(0xFFF59F00)
                                )
                            }
                        }
                    }
                }

                item {
                    NeoSurfaceBox(
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("lvl_cell_mystery"),
                        cornerRadius = 13.dp,
                        shadowOffsetY = 4.dp,
                        backgroundBrush = Brush.verticalGradient(listOf(CosmicInk, Color(0xFF1E1B4B))),
                        onClick = onMysteryClick
                    ) {
                        Text(
                            text = "????",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF66D9E8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestsModal(
    progress: PersistentProgress,
    bonusRemainingMs: Long,
    onClaimBonus: () -> Unit,
    onClaimStandard: (QuestDef) -> Unit,
    onOpenSantaQuests: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val bL = min(BonusQuestDef.TARGET_LEVELS, progress.bonusStats.levelsDone)
    val bI = min(BonusQuestDef.TARGET_INTRUDERS, progress.bonusStats.intrudersKilled)
    val bC = min(BonusQuestDef.TARGET_COINS, progress.bonusStats.coinsGot)
    val bDone = bL >= 15 && bI >= 15 && bC >= 250
    val bPct = (((bL / 15f + bI / 15f + bC / 250f) / 3f) * 100f).roundToInt().coerceIn(0, 100)

    ModalBackdrop(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF9C36B5), Color(0xFF0CA678))))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎄", fontSize = 18.sp)
                    Text(
                        text = "Questuri de Crăciun & Bonus",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                ModalCloseButton(onClick = onDismiss, tag = "close_quests")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(CosmicInk)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3BF))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "🪙 Questurile oferă acum exclusiv Monede de Aur pentru a debloca Premium Pass! (Fără nestemate din questuri)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5C3C00)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(CosmicInk)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(410.dp)
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Shortcut Banner to Santa Claus & Big Sleigh Quests
                item {
                    NeoSurfaceBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_santa_quests_from_modal"),
                        cornerRadius = 15.dp,
                        borderWidth = 3.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFC92A2A), Color(0xFFE03131), Color(0xFFF59F00))),
                        onClick = onOpenSantaQuests
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "🎅 Questurile lui Moș Crăciun & Sania Mare",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Deschide ușa bradului pentru a intra în Satul lui Moș Crăciun!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFF3BF)
                                )
                            }
                            Text("🛷 ➔", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }

                // Bonus Quest Card
                item {
                    val canClaimBonus = bDone && bonusRemainingMs == 0L
                    NeoSurfaceBox(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        borderWidth = 3.5.dp,
                        shadowOffsetY = 4.dp,
                        backgroundBrush = if (canClaimBonus) {
                            Brush.verticalGradient(listOf(Color(0xFFFFF9DB), Color(0xFFF3F0FF)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFFF3F0FF), Color(0xFFFFF0F6), Color(0xFFE3FAFC)))
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(BonusQuestDef.NAME, fontSize = 14.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF5F3DC4))
                                    Text(BonusQuestDef.SUB, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF495057))
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.linearGradient(listOf(CosmicGoldLight, CosmicGold)))
                                        .border(2.5.dp, CosmicInk, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("+${BonusQuestDef.REWARD_COINS}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                                    CoinBadgeDot(15.dp)
                                }
                            }

                            QuestProgressBar(
                                pct = bPct / 100f,
                                label = "$bPct% Completat",
                                fillBrush = Brush.horizontalGradient(listOf(CosmicCyan, CosmicPurple, CosmicPink))
                            )

                            val btnLabel = when {
                                bonusRemainingMs > 0L -> "⏳ Revino în ${ParkourViewModel.formatTimer(bonusRemainingMs)}"
                                bDone -> "Revendică +${BonusQuestDef.REWARD_COINS} Monede 🪙"
                                else -> "În progres: $bL/15 Nivele • $bI/15 Intruși • $bC/250 Monede"
                            }

                            NeoSurfaceBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_claim_bonus_quest"),
                                cornerRadius = 12.dp,
                                shadowOffsetY = if (canClaimBonus) 3.dp else 0.dp,
                                backgroundBrush = if (canClaimBonus) {
                                    Brush.verticalGradient(listOf(Color(0xFF69DB7C), CosmicGreen))
                                } else {
                                    Brush.verticalGradient(listOf(Color(0xFFDEE2E6), Color(0xFFCED4DA)))
                                },
                                enabled = canClaimBonus,
                                onClick = onClaimBonus
                            ) {
                                Text(
                                    text = btnLabel,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (canClaimBonus) CosmicInk else Color(0xFF6C757D)
                                )
                            }
                        }
                    }
                }

                // 4 Standard Quests
                items(STANDARD_QUESTS, key = { it.id }) { q ->
                    val rawVal = when (q.statKey) {
                        "levelsDone" -> progress.stats.levelsDone
                        "intrudersKilled" -> progress.stats.intrudersKilled
                        "coinsGot" -> progress.stats.coinsGot
                        "jumpsMade" -> progress.stats.jumpsMade
                        else -> 0
                    }
                    val valClamped = min(q.target, rawVal)
                    val done = valClamped >= q.target
                    val fraction = (valClamped.toFloat() / q.target.toFloat()).coerceIn(0f, 1f)

                    NeoSurfaceBox(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = if (done) {
                            Brush.verticalGradient(listOf(Color(0xFFFFF9DB), Color(0xFFF3F0FF)))
                        } else {
                            Brush.verticalGradient(listOf(Color.White, Color.White))
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(q.name, fontSize = 14.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                                    Text(q.sub, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF495057))
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.linearGradient(listOf(CosmicGoldLight, CosmicGold)))
                                        .border(2.5.dp, CosmicInk, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("+${q.rewardCoins}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                                    CoinBadgeDot(15.dp)
                                }
                            }

                            QuestProgressBar(
                                pct = fraction,
                                label = "$valClamped / ${q.target}",
                                fillBrush = Brush.horizontalGradient(listOf(Color(0xFF845EF7), CosmicPink))
                            )

                            NeoSurfaceBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_claim_${q.id}"),
                                cornerRadius = 12.dp,
                                shadowOffsetY = if (done) 3.dp else 0.dp,
                                backgroundBrush = if (done) {
                                    Brush.verticalGradient(listOf(Color(0xFF69DB7C), CosmicGreen))
                                } else {
                                    Brush.verticalGradient(listOf(Color(0xFFDEE2E6), Color(0xFFCED4DA)))
                                },
                                enabled = done,
                                onClick = { onClaimStandard(q) }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (done) "Revendică +${q.rewardCoins} Monede" else "În progres ($valClamped / ${q.target})",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (done) CosmicInk else Color(0xFF6C757D)
                                    )
                                    if (done) CoinBadgeDot(15.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestProgressBar(pct: Float, label: String, fillBrush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFFE9ECEF))
            .border(2.5.dp, CosmicInk, RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(pct.coerceIn(0f, 1f))
                .background(fillBrush)
        )
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            color = CosmicInk
        )
    }
}

@Composable
fun SantaQuestsModal(
    progress: PersistentProgress,
    onClaimSantaQuest: (SantaQuestDef) -> Unit,
    onClaimGrandBonus: () -> Unit,
    onDismiss: () -> Unit
) {
    val sStats = progress.santaStats
    val claimedCount = SANTA_QUESTS.count { it.id in sStats.claimedSantaQuests }
    val allClaimed = claimedCount == SANTA_QUESTS.size
    val canClaimGrand = allClaimed && !sStats.grandSleighClaimed

    ModalBackdrop(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF991B1B), Color(0xFFE03131), Color(0xFFD97706))))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveHeroPreviewScaled(
                        heroId = "mos_craciun_turbo",
                        canvasSize = 38.dp,
                        heroVirtualSize = 24f
                    )
                    Column {
                        Text(
                            text = "Questurile lui Moș Crăciun",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "🛷 Sania Mare • $claimedCount / ${SANTA_QUESTS.size} complete",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFF3BF)
                        )
                    }
                }
                ModalCloseButton(onClick = onDismiss, tag = "close_santa_quests")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(CosmicInk)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEBFBEE))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "🎅 Ho Ho Ho! Vizitează căsuțele cu zăpadă, aprinde luminile și adună cadouri pentru Sania Mare!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF14532D)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(CosmicInk)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(415.dp)
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Grand Sleigh Completion Chest
                item {
                    NeoSurfaceBox(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        borderWidth = 3.5.dp,
                        shadowOffsetY = 4.dp,
                        backgroundBrush = if (canClaimGrand) {
                            Brush.verticalGradient(listOf(Color(0xFFFFF3BF), Color(0xFFFFD43B)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFFFFF5F5), Color(0xFFFFF9DB)))
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = SantaGrandSleighBonus.NAME,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = SantaGrandSleighBonus.SUB,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF495057)
                            )
                            Text(
                                text = "Premiu: +${SantaGrandSleighBonus.REWARD_COINS} 🪙 pentru Premium Pass • +${SantaGrandSleighBonus.REWARD_XP} XP",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2B8A3E)
                            )

                            QuestProgressBar(
                                pct = claimedCount.toFloat() / SANTA_QUESTS.size.toFloat(),
                                label = "$claimedCount / ${SANTA_QUESTS.size} Questuri ale Moșului",
                                fillBrush = Brush.horizontalGradient(listOf(Color(0xFFE03131), CosmicGold, CosmicGreen))
                            )

                            NeoSurfaceBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_claim_santa_grand"),
                                cornerRadius = 12.dp,
                                shadowOffsetY = if (canClaimGrand) 3.dp else 0.dp,
                                backgroundBrush = when {
                                    sStats.grandSleighClaimed -> Brush.verticalGradient(listOf(Color(0xFFD3F9D8), Color(0xFFB2F2BB)))
                                    canClaimGrand -> Brush.verticalGradient(listOf(Color(0xFF69DB7C), CosmicGreen))
                                    else -> Brush.verticalGradient(listOf(Color(0xFFDEE2E6), Color(0xFFCED4DA)))
                                },
                                enabled = canClaimGrand,
                                onClick = onClaimGrandBonus
                            ) {
                                Text(
                                    text = when {
                                        sStats.grandSleighClaimed -> "✅ Sania Mare Binecuvântată!"
                                        canClaimGrand -> "🛷 Revendică +${SantaGrandSleighBonus.REWARD_COINS} 🪙 Marele Premiu!"
                                        else -> "Completează toate cele ${SANTA_QUESTS.size} questuri ($claimedCount/${SANTA_QUESTS.size})"
                                    },
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (canClaimGrand || sStats.grandSleighClaimed) CosmicInk else Color(0xFF6C757D)
                                )
                            }
                        }
                    }
                }

                // 4 Santa Quests
                items(SANTA_QUESTS, key = { it.id }) { q ->
                    val rawVal = when (q.statKey) {
                        "giftsDelivered" -> sStats.giftsDelivered
                        "housesVisited" -> sStats.housesVisited
                        "bubbleJumps" -> sStats.bubbleJumps
                        "sleighCoins" -> sStats.sleighCoins
                        else -> 0
                    }
                    val clamped = min(q.target, rawVal)
                    val claimed = q.id in sStats.claimedSantaQuests
                    val ready = clamped >= q.target && !claimed
                    val frac = (clamped.toFloat() / q.target.toFloat()).coerceIn(0f, 1f)

                    NeoSurfaceBox(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = when {
                            claimed -> Brush.verticalGradient(listOf(Color(0xFFEBFBEE), Color(0xFFD3F9D8)))
                            ready -> Brush.verticalGradient(listOf(Color(0xFFFFF9DB), Color(0xFFFFF0F6)))
                            else -> Brush.verticalGradient(listOf(Color.White, Color.White))
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("${q.icon} ${q.name}", fontSize = 14.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                                    Text(q.sub, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF495057))
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.linearGradient(listOf(CosmicGoldLight, CosmicGold)))
                                        .border(2.dp, CosmicInk, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("+${q.rewardCoins}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                                    CoinBadgeDot(14.dp)
                                }
                            }

                            QuestProgressBar(
                                pct = frac,
                                label = "$clamped / ${q.target}",
                                fillBrush = Brush.horizontalGradient(listOf(Color(0xFFE03131), Color(0xFF2FB344)))
                            )

                            NeoSurfaceBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("btn_claim_${q.id}"),
                                cornerRadius = 12.dp,
                                shadowOffsetY = if (ready) 3.dp else 0.dp,
                                backgroundBrush = when {
                                    claimed -> Brush.verticalGradient(listOf(Color(0xFFD3F9D8), Color(0xFFB2F2BB)))
                                    ready -> Brush.verticalGradient(listOf(Color(0xFF69DB7C), CosmicGreen))
                                    else -> Brush.verticalGradient(listOf(Color(0xFFDEE2E6), Color(0xFFCED4DA)))
                                },
                                enabled = ready,
                                onClick = { onClaimSantaQuest(q) }
                            ) {
                                Text(
                                    text = when {
                                        claimed -> "✅ Revendicat de la Moș Crăciun!"
                                        ready -> "🎁 Revendică Răsplata lui Moș Crăciun!"
                                        else -> "În progres ($clamped / ${q.target})"
                                    },
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (ready || claimed) CosmicInk else Color(0xFF6C757D)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
