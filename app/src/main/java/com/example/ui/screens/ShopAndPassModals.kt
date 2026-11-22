package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HEROES_CATALOG
import com.example.model.HeroCurrency
import com.example.model.HeroMeta
import com.example.model.HeroRarity
import com.example.model.PREMIUM_COST_COINS
import com.example.model.PassReward
import com.example.model.PersistentProgress
import com.example.model.SEASON_TIERS
import com.example.model.SeasonTier
import com.example.ui.ParkourViewModel
import com.example.ui.components.CoinBadgeDot
import com.example.ui.components.LiveHeroPreviewScaled
import com.example.ui.components.NeoSurfaceBox
import com.example.ui.components.formatRoNumber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicGold
import com.example.ui.theme.CosmicGoldLight
import com.example.ui.theme.CosmicGreen
import com.example.ui.theme.CosmicInk
import com.example.ui.theme.CosmicOrange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShopModal(
    progress: PersistentProgress,
    selectedFilter: HeroRarity?,
    dailyRemainingMs: Long,
    onSelectFilter: (HeroRarity?) -> Unit,
    onClaimDaily: () -> Unit,
    onEquipHero: (HeroMeta) -> Unit,
    onBuyWithCoins: (HeroMeta) -> Unit,
    onBuyWithDiamonds: (HeroMeta) -> Unit,
    onOpenPassFromHero: () -> Unit,
    onDismiss: () -> Unit
) {
    val filteredHeroes = HEROES_CATALOG.filter { selectedFilter == null || it.rarity == selectedFilter }

    ModalBackdrop(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF14532D), Color(0xFFB91C1C), Color(0xFFF59F00))))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎅", fontSize = 18.sp)
                        Text(
                            text = "Magazin Eroi de Crăciun (${HEROES_CATALOG.size} Eroi)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    ModalCloseButton(onClick = onDismiss, tag = "close_shop")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dailyLocked = dailyRemainingMs > 0L
                    NeoSurfaceBox(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_shop_daily"),
                        cornerRadius = 11.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = if (dailyLocked) {
                            Brush.verticalGradient(listOf(Color(0xFFDEE2E6), Color(0xFFCED4DA)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFFFF8787), Color(0xFFFA5252)))
                        },
                        onClick = onClaimDaily
                    ) {
                        Text(
                            text = if (dailyLocked) {
                                "⏳ Următorul câștig în ${ParkourViewModel.formatTimer(dailyRemainingMs)}"
                            } else {
                                "🎁 Câștig Zilnic: +50 🪙"
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (dailyLocked) Color(0xFF495057) else Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NeoSurfaceBox(
                            cornerRadius = 11.dp,
                            shadowOffsetY = 3.dp,
                            backgroundBrush = Brush.verticalGradient(listOf(CosmicGoldLight, CosmicGold))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoinBadgeDot(14.dp)
                                Text(formatRoNumber(progress.coins), fontSize = 12.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                            }
                        }

                        NeoSurfaceBox(
                            cornerRadius = 11.dp,
                            shadowOffsetY = 3.dp,
                            backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF99E9F2), CosmicCyan))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💎", fontSize = 11.sp)
                                Text(formatRoNumber(progress.diamonds), fontSize = 12.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(CosmicInk)
            )

            // Filter Pills Bar (100% visible, wraps cleanly)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFFF3F0FF), Color(0xFFF8F9FA))))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                FilterPill(
                    label = "Toate (${HEROES_CATALOG.size})",
                    selected = selectedFilter == null,
                    brush = Brush.verticalGradient(listOf(Color.White, Color.White)),
                    onClick = { onSelectFilter(null) },
                    tag = "filter_all"
                )
                HeroRarity.entries.forEach { rarity ->
                    val pillBrush = when (rarity) {
                        HeroRarity.COMUN -> Brush.verticalGradient(listOf(Color.White, Color.White))
                        HeroRarity.RAR -> Brush.verticalGradient(listOf(Color(0xFFB2F2BB), Color(0xFFB2F2BB)))
                        HeroRarity.EPIC -> Brush.verticalGradient(listOf(Color(0xFFE5DBFF), Color(0xFFE5DBFF)))
                        HeroRarity.MITIC -> Brush.verticalGradient(listOf(Color(0xFFFFC9C9), Color(0xFFFFC9C9)))
                        HeroRarity.LEGENDAR -> Brush.linearGradient(listOf(Color(0xFFFFD8A8), Color(0xFFFFA94D)))
                        HeroRarity.DIVIN -> Brush.linearGradient(listOf(Color(0xFFFFF3BF), CosmicGold, Color(0xFFFF8787)))
                    }
                    val label = when (rarity) {
                        HeroRarity.LEGENDAR -> "🔥 Legendar"
                        HeroRarity.DIVIN -> "✨ Divin (${HEROES_CATALOG.count { it.rarity == HeroRarity.DIVIN }})"
                        else -> rarity.label
                    }
                    FilterPill(
                        label = label,
                        selected = selectedFilter == rarity,
                        brush = pillBrush,
                        onClick = { onSelectFilter(rarity) },
                        tag = "filter_${rarity.id}"
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(CosmicInk)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 365.dp)
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredHeroes, key = { it.id }) { hero ->
                    ShopHeroCard(
                        hero = hero,
                        owned = hero.id in progress.ownedHeroes,
                        equipped = progress.equippedHero == hero.id,
                        onEquip = { onEquipHero(hero) },
                        onBuyCoins = { onBuyWithCoins(hero) },
                        onBuyDiamonds = { onBuyWithDiamonds(hero) },
                        onGoToPass = onOpenPassFromHero
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    brush: Brush,
    onClick: () -> Unit,
    tag: String
) {
    NeoSurfaceBox(
        modifier = Modifier
            .height(34.dp)
            .testTag(tag),
        cornerRadius = 11.dp,
        borderWidth = 2.5.dp,
        shadowOffsetY = 2.5.dp,
        backgroundBrush = if (selected) Brush.verticalGradient(listOf(CosmicInk, CosmicInk)) else brush,
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) Color.White else CosmicInk
        )
    }
}

@Composable
private fun ShopHeroCard(
    hero: HeroMeta,
    owned: Boolean,
    equipped: Boolean,
    onEquip: () -> Unit,
    onBuyCoins: () -> Unit,
    onBuyDiamonds: () -> Unit,
    onGoToPass: () -> Unit
) {
    val cardBg = when {
        equipped -> Brush.verticalGradient(listOf(Color(0xFFF3FFF9), Color(0xFFE6FCF5)))
        hero.rarity == HeroRarity.LEGENDAR -> Brush.verticalGradient(listOf(Color(0xFFFFF9F2), Color(0xFFFFF4E6)))
        hero.rarity == HeroRarity.DIVIN -> Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF9DB)))
        else -> Brush.verticalGradient(listOf(Color.White, Color.White))
    }

    val chipBrush = when (hero.rarity) {
        HeroRarity.COMUN -> Brush.horizontalGradient(listOf(Color(0xFFE9ECEF), Color(0xFFE9ECEF)))
        HeroRarity.RAR -> Brush.horizontalGradient(listOf(Color(0xFF8CE99A), Color(0xFF8CE99A)))
        HeroRarity.EPIC -> Brush.horizontalGradient(listOf(Color(0xFFD0BFFF), Color(0xFFD0BFFF)))
        HeroRarity.MITIC -> Brush.horizontalGradient(listOf(Color(0xFFFFA8A8), Color(0xFFFFA8A8)))
        HeroRarity.LEGENDAR -> Brush.horizontalGradient(listOf(Color(0xFFFFC078), CosmicOrange))
        HeroRarity.DIVIN -> Brush.horizontalGradient(listOf(CosmicGoldLight, Color(0xFFFFA94D), Color(0xFFFF8787)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(if (equipped) 3.5.dp else 3.dp, hero.rarity.borderColor, RoundedCornerShape(18.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBrush)
                        .border(2.dp, CosmicInk, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(hero.rarity.chipTag, fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                }

                if (hero.effectLabel != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE7F5FF))
                            .border(1.5.dp, Color(0xFF339AF0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(hero.effectLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF1864AB))
                    }
                }
            }

            if (owned) {
                Text("DEBLOCAT", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF2B8A3E))
            } else {
                when (hero.currency) {
                    HeroCurrency.FREE -> Text("GRATUIT", fontSize = 12.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                    HeroCurrency.PASS -> Text("👑 PASS TIER ${hero.passTier}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFD9480F))
                    HeroCurrency.COINS -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinBadgeDot(14.dp)
                            Text(
                                text = if (hero.gemPrice != null) "${hero.price} / 💎 ${hero.gemPrice}" else "${hero.price}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.radialGradient(listOf(Color(0xFF23196B), Color(0xFF0B0A24))))
                    .border(3.dp, CosmicInk, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                LiveHeroPreviewScaled(
                    heroId = hero.id,
                    canvasSize = 62.dp,
                    heroVirtualSize = 35f
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(hero.name, fontSize = 15.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                Text(hero.desc, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF495057), lineHeight = 14.sp)
            }
        }

        if (hero.abilityDesc != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFFFFF3BF))
                    .border(2.dp, Color(0xFFF59F00), RoundedCornerShape(9.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(hero.abilityIcon ?: "⚡", fontSize = 14.sp)
                Text(hero.abilityDesc, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5C3C00), lineHeight = 14.sp)
            }
        }

        when {
            equipped -> {
                NeoSurfaceBox(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    cornerRadius = 12.dp,
                    shadowOffsetY = 3.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF63E6BE), Color(0xFF38D9A9)))
                ) {
                    Text("✓ ECHIPAT", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                }
            }
            owned -> {
                NeoSurfaceBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_equip_${hero.id}"),
                    cornerRadius = 12.dp,
                    shadowOffsetY = 3.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFA5D8FF), Color(0xFF74C0FC))),
                    onClick = onEquip
                ) {
                    Text("Echipează", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                }
            }
            hero.currency == HeroCurrency.COINS && hero.gemPrice != null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoSurfaceBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_buy_coins_${hero.id}"),
                        cornerRadius = 12.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = Brush.verticalGradient(listOf(CosmicGoldLight, CosmicGold)),
                        onClick = onBuyCoins
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            CoinBadgeDot(14.dp)
                            Text("${hero.price}", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                        }
                    }
                    NeoSurfaceBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_buy_gems_${hero.id}"),
                        cornerRadius = 12.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF99E9F2), Color(0xFF66D9E8))),
                        onClick = onBuyDiamonds
                    ) {
                        Text("💎 ${hero.gemPrice}", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                    }
                }
            }
            hero.currency == HeroCurrency.COINS -> {
                NeoSurfaceBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_buy_coins_${hero.id}"),
                    cornerRadius = 12.dp,
                    shadowOffsetY = 3.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(CosmicGoldLight, CosmicGold)),
                    onClick = onBuyCoins
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Cumpără (", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                        CoinBadgeDot(14.dp)
                        Text("${hero.price})", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                    }
                }
            }
            else -> {
                NeoSurfaceBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_pass_hero_${hero.id}"),
                    cornerRadius = 12.dp,
                    shadowOffsetY = 3.dp,
                    backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFFFFD43B), CosmicOrange)),
                    onClick = onGoToPass
                ) {
                    Text(
                        text = "👑 Revendică din Premium Pass (Tier ${hero.passTier})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicInk
                    )
                }
            }
        }
    }
}

@Composable
fun PassModal(
    progress: PersistentProgress,
    onUnlockPremium: () -> Unit,
    onClaimSlot: (SeasonTier, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val tierReached = minOf(15, progress.passXP / 100 + 1)
    val remXP = progress.passXP % 100
    val xpFraction = if (tierReached >= 15) 1f else remXP / 100f

    ModalBackdrop(onDismiss = onDismiss, maxWidth = 560.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF14532D), Color(0xFFB91C1C), Color(0xFFF59F00))))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎄", fontSize = 18.sp)
                        Text(
                            text = "Parkour Pass de Crăciun (5 Eroi Exclusivi!)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    ModalCloseButton(onClick = onDismiss, tag = "close_pass")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(175.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(CosmicInk.copy(alpha = 0.65f))
                            .border(2.5.dp, CosmicInk, RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .fillMaxWidth(xpFraction)
                                .background(Brush.horizontalGradient(listOf(CosmicGold, CosmicGreen)))
                        )
                        Text(
                            text = if (tierReached >= 15) "Tier MAXIM (15) • ${progress.passXP} XP" else "Tier $tierReached / 15 • $remXP / 100 XP",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    NeoSurfaceBox(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_unlock_prem"),
                        cornerRadius = 10.dp,
                        shadowOffsetY = 3.dp,
                        backgroundBrush = if (progress.premiumUnlocked) {
                            Brush.verticalGradient(listOf(Color(0xFF69DB7C), CosmicGreen))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFF241759), Color(0xFF845EF7), Color(0xFFF06595)))
                        },
                        enabled = !progress.premiumUnlocked,
                        onClick = onUnlockPremium
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (progress.premiumUnlocked) {
                                Text("👑 Premium Crăciun Activ", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = CosmicInk)
                            } else {
                                Text("👑 Deblochează Premium ($PREMIUM_COST_COINS", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                                CoinBadgeDot(14.dp)
                                Text(")", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(CosmicInk)
            )

            // Sticky 2-Column Vertical Track Headers: FREE PASS on the LEFT, PREMIUM PASS on the RIGHT!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE9ECEF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeoSurfaceBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    cornerRadius = 11.dp,
                    borderWidth = 2.5.dp,
                    shadowOffsetY = 2.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF74C0FC), Color(0xFF339AF0)))
                ) {
                    Text(
                        text = "🎁 FREE PASS",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicInk
                    )
                }

                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(CosmicInk),
                    contentAlignment = Alignment.Center
                ) {
                    Text("TIER", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                NeoSurfaceBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    cornerRadius = 11.dp,
                    borderWidth = 2.5.dp,
                    shadowOffsetY = 2.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFFD43B), Color(0xFFF59F00)))
                ) {
                    Text(
                        text = "👑 PREMIUM PASS",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicInk
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(CosmicInk)
            )

            // Vertical Scrolling Pass Track (Free on LEFT, Premium on RIGHT — entire reward cards are clickable!)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 380.dp)
                    .background(Color(0xFFF1F3F5)),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SEASON_TIERS, key = { it.tier }) { item ->
                    val unlocked = item.tier <= tierReached
                    val fTaken = item.tier in progress.claimedFree
                    val pTaken = item.tier in progress.claimedPrem
                    val premHero = item.premReward as? PassReward.Hero
                    val isHeroEquipped = premHero != null && progress.equippedHero == premHero.heroId

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT COLUMN: FREE PASS
                        PassRewardSlot(
                            modifier = Modifier.weight(1f),
                            tier = item.tier,
                            reward = item.freeReward,
                            isPremium = false,
                            unlocked = unlocked,
                            taken = fTaken,
                            isHeroEquipped = false,
                            premUnlocked = true,
                            onClick = { onClaimSlot(item, false) }
                        )

                        // CENTER TIER BADGE
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (unlocked) {
                                        Brush.verticalGradient(listOf(Color(0xFF69DB7C), CosmicGreen))
                                    } else {
                                        Brush.verticalGradient(listOf(Color(0xFFDEE2E6), Color(0xFFCED4DA)))
                                    }
                                )
                                .border(2.5.dp, CosmicInk, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "T${item.tier}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CosmicInk
                            )
                        }

                        // RIGHT COLUMN: PREMIUM PASS
                        PassRewardSlot(
                            modifier = Modifier.weight(1f),
                            tier = item.tier,
                            reward = item.premReward,
                            isPremium = true,
                            unlocked = unlocked && progress.premiumUnlocked,
                            taken = pTaken,
                            isHeroEquipped = isHeroEquipped,
                            premUnlocked = progress.premiumUnlocked,
                            onClick = { onClaimSlot(item, true) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PassRewardSlot(
    modifier: Modifier = Modifier,
    tier: Int,
    reward: PassReward,
    isPremium: Boolean,
    unlocked: Boolean,
    taken: Boolean,
    isHeroEquipped: Boolean,
    premUnlocked: Boolean,
    onClick: () -> Unit
) {
    val isHero = reward is PassReward.Hero
    val dimmed = !unlocked && !taken
    val bgBrush = when {
        isPremium && isHero -> Brush.verticalGradient(listOf(Color(0xFF211552), Color(0xFF4C1D95)))
        isPremium -> Brush.verticalGradient(listOf(Color(0xFFFFF9DB), Color(0xFFFFE8CC)))
        else -> Brush.verticalGradient(listOf(Color(0xFFF8F9FA), Color(0xFFE7F5FF)))
    }

    val btnText = when {
        taken && isHero -> if (isHeroEquipped) "✓ Echipat" else "Echipează"
        taken -> "✓ Luat"
        isPremium && !premUnlocked -> "🔒 $PREMIUM_COST_COINS 🪙"
        !unlocked -> "Tier $tier"
        isHero -> "Revendică Eroul!"
        else -> "Revendică"
    }

    val btnBrush = when {
        taken && isHero && !isHeroEquipped -> Brush.verticalGradient(listOf(Color(0xFF66D9E8), CosmicCyan))
        taken -> Brush.verticalGradient(listOf(Color(0xFFDEE2E6), Color(0xFFDEE2E6)))
        !unlocked -> Brush.verticalGradient(listOf(Color(0xFFF1F3F5), Color(0xFFF1F3F5)))
        else -> Brush.verticalGradient(listOf(Color(0xFF69DB7C), CosmicGreen))
    }

    NeoSurfaceBox(
        modifier = modifier
            .height(138.dp)
            .alpha(if (dimmed) 0.80f else 1f)
            .testTag("btn_pass_${if (isPremium) "prem" else "free"}_$tier"),
        cornerRadius = 15.dp,
        borderWidth = if (isPremium && isHero) 3.5.dp else 3.dp,
        shadowOffsetY = 3.5.dp,
        backgroundBrush = bgBrush,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (reward is PassReward.Hero) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CosmicInk.copy(alpha = 0.55f))
                        .border(2.dp, CosmicGold, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    LiveHeroPreviewScaled(
                        heroId = reward.heroId,
                        canvasSize = 44.dp,
                        heroVirtualSize = 27f,
                        phaseOffset = tier * 0.5f
                    )
                }
            } else {
                Text(reward.icon, fontSize = 25.sp)
            }

            Text(
                text = reward.text,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                color = if (isPremium && isHero) Color.White else CosmicInk,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                maxLines = 2
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(btnBrush)
                    .border(2.dp, CosmicInk, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = btnText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (!unlocked && !taken) Color(0xFF868E96) else CosmicInk
                )
            }
        }
    }
}
