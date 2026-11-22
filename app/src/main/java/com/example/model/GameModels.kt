package com.example.model

import androidx.compose.ui.graphics.Color

enum class HeroRarity(
    val id: String,
    val label: String,
    val chipTag: String,
    val borderColor: Color
) {
    COMUN("comun", "Comun", "COMUN", Color(0xFFADB5BD)),
    RAR("rar", "Rar", "RAR", Color(0xFF20C997)),
    EPIC("epic", "Epic", "EPIC", Color(0xFF4DABF7)),
    MITIC("mitic", "Mitic", "MITIC", Color(0xFFFF6B6B)),
    LEGENDAR("legendar", "Legendar", "🔥 LEGENDAR", Color(0xFFF76707)),
    DIVIN("divin", "Divin", "✨ DIVIN", Color(0xFFF59F00))
}

enum class HeroCurrency {
    FREE,
    COINS,
    PASS
}

enum class AbilityType {
    DASH,
    SLOWMO,
    SOLAR,
    ZEROG,
    BLIZZARD,
    AURORA_NOVA,
    PRESENT_BARRAGE,
    CANDY_TURBO
}

data class HeroMeta(
    val id: String,
    val name: String,
    val rarity: HeroRarity,
    val price: Int = 0,
    val gemPrice: Int? = null,
    val currency: HeroCurrency = HeroCurrency.COINS,
    val passTier: Int? = null,
    val desc: String,
    val trailColor: Color,
    val effectLabel: String? = null,
    val abilityName: String? = null,
    val abilityIcon: String? = null,
    val abilityType: AbilityType? = null,
    val cooldownSec: Float = 0f,
    val abilityDesc: String? = null
)

val HEROES_CATALOG: List<HeroMeta> = listOf(
    // COMUN (4)
    HeroMeta(
        id = "om_de_zapada",
        name = "Om de Zăpadă",
        rarity = HeroRarity.COMUN,
        price = 0,
        currency = HeroCurrency.FREE,
        desc = "Omuleț vesel de zăpadă cu fular fluturând, halou polar și cristale orbitale.",
        trailColor = Color(0xFF74C0FC),
        effectLabel = "❄️ Halou Polar & Cristale de Zăpadă"
    ),
    HeroMeta(
        id = "turta_dulce",
        name = "Turtă Dulce",
        rarity = HeroRarity.COMUN,
        price = 15,
        desc = "Prăjitură crocantă de Crăciun cu glazură dulce și scântei de scorțișoară.",
        trailColor = Color(0xFFFF922B)
    ),
    HeroMeta(
        id = "cadou_magic",
        name = "Cadou Magic",
        rarity = HeroRarity.COMUN,
        price = 20,
        desc = "Cutie de cadou roșu-aurie cu fundă săltăreață și sclipici festiv.",
        trailColor = Color(0xFFFA5252)
    ),
    HeroMeta(
        id = "bradut_vesel",
        name = "Brăduț Vesel",
        rarity = HeroRarity.COMUN,
        price = 25,
        desc = "Brăduț verde plin de beculețe colorate și aură de cetină proaspătă.",
        trailColor = Color(0xFF2FB344)
    ),
    // RAR (3)
    HeroMeta(
        id = "renul_rudolf",
        name = "Renul Rudolf",
        rarity = HeroRarity.RAR,
        price = 35,
        desc = "Ren rapid cu coarne aurii, nas roșu luminos și praf de stele.",
        trailColor = Color(0xFFFF6B6B)
    ),
    HeroMeta(
        id = "spiridus_jucaus",
        name = "Spiriduș Jucăuș",
        rarity = HeroRarity.RAR,
        currency = HeroCurrency.PASS,
        passTier = 3,
        desc = "Ajutorul lui Moș Crăciun cu clopoțel de aur (Exclusiv Premium Pass Tier 3).",
        trailColor = Color(0xFF51CF66)
    ),
    HeroMeta(
        id = "acadea_ninja",
        name = "Acadea Ninja",
        rarity = HeroRarity.RAR,
        price = 50,
        desc = "Ninja dulce cu stele de mentă rotative și dâră de zahăr.",
        trailColor = Color(0xFFFF8787)
    ),
    // EPIC (4)
    HeroMeta(
        id = "pinguin_polar",
        name = "Pinguin Polar",
        rarity = HeroRarity.EPIC,
        price = 75,
        gemPrice = 4,
        desc = "Pinguin alunecos cu căciulă de iarnă și unde de gheață albastră.",
        trailColor = Color(0xFF4DABF7)
    ),
    HeroMeta(
        id = "clopotel_magic",
        name = "Clopoțelul Magic",
        rarity = HeroRarity.EPIC,
        price = 85,
        gemPrice = 5,
        desc = "Clopoțel auriu de sanie cu fundă de catifea, unde sonore aurii și note muzicale.",
        trailColor = Color(0xFFFCC419),
        effectLabel = "🔔 Unde Sonore Aurii & Note"
    ),
    HeroMeta(
        id = "vrajitoarea_ghetii",
        name = "Vrăjitoarea Gheții",
        rarity = HeroRarity.EPIC,
        currency = HeroCurrency.PASS,
        passTier = 6,
        desc = "Magiciană polară cu rune de cristal (Exclusiv Premium Pass Tier 6).",
        trailColor = Color(0xFF74C0FC)
    ),
    HeroMeta(
        id = "urs_polar_festiv",
        name = "Urs Polar Festiv",
        rarity = HeroRarity.EPIC,
        price = 95,
        gemPrice = 6,
        desc = "Ursuleț blănos cu căciulă de Moș și halou de auroră boreală.",
        trailColor = Color(0xFFA5D8FF)
    ),
    // MITIC (2)
    HeroMeta(
        id = "regele_ghetii",
        name = "Regele Gheții",
        rarity = HeroRarity.MITIC,
        currency = HeroCurrency.PASS,
        passTier = 9,
        desc = "Monarh polar cu coroană de safir și cristale orbitale (Exclusiv Premium Pass Tier 9).",
        trailColor = Color(0xFF66D9E8)
    ),
    HeroMeta(
        id = "steaua_craciunului",
        name = "Steaua Crăciunului",
        rarity = HeroRarity.MITIC,
        price = 160,
        gemPrice = 12,
        desc = "Stea aurie strălucitoare cu inel festiv și praf magic de Crăciun.",
        trailColor = Color(0xFFFFD43B)
    ),
    // LEGENDAR (3)
    HeroMeta(
        id = "dragon_de_gheata",
        name = "Dragonul de Gheață",
        rarity = HeroRarity.LEGENDAR,
        price = 220,
        gemPrice = 18,
        desc = "Dragon legendar al iernii învăluit în cristale de gheață și foc polar.",
        trailColor = Color(0xFF3BC9DB)
    ),
    HeroMeta(
        id = "vulpea_polara",
        name = "Vulpea Polară",
        rarity = HeroRarity.LEGENDAR,
        price = 235,
        gemPrice = 19,
        desc = "Vulpe albă a nordului cu coadă pufoasă, petale de gheață și sclipiri de auroră.",
        trailColor = Color(0xFF99E9F2),
        effectLabel = "🦊 Petale de Gheață & Auroră"
    ),
    HeroMeta(
        id = "spargatorul_de_nuci",
        name = "Spărgătorul de Nuci",
        rarity = HeroRarity.LEGENDAR,
        price = 250,
        gemPrice = 20,
        desc = "Soldat regal de Crăciun cu săbii aurii rotative și armură festivă.",
        trailColor = Color(0xFFFA5252)
    ),
    // DIVIN (8)
    HeroMeta(
        id = "mos_craciun_turbo",
        name = "Moș Crăciun Turbo",
        rarity = HeroRarity.DIVIN,
        price = 290,
        gemPrice = 24,
        desc = "Moș Crăciun cu barbă pufoasă și scântei de cadouri de mare viteză.",
        trailColor = Color(0xFFE03131),
        effectLabel = "✨ Scântei Festive Orbite",
        abilityName = "Sleigh Dash",
        abilityIcon = "🎅",
        abilityType = AbilityType.DASH,
        cooldownSec = 4.0f,
        abilityDesc = "Sleigh Dash: Te teleportează cu sania înainte distrugând laserele și goblinii elfi (CD: 4s)."
    ),
    HeroMeta(
        id = "jack_frost_divin",
        name = "Jack Frost Divin",
        rarity = HeroRarity.DIVIN,
        price = 310,
        gemPrice = 25,
        desc = "Stăpânul viscolului cu coroană de gheață și vârtej de fulgi cristalini.",
        trailColor = Color(0xFF3BC9DB),
        effectLabel = "🌨️ Vârtej de Fulgi & Halou Polar",
        abilityName = "Viscol Polar",
        abilityIcon = "🌨️",
        abilityType = AbilityType.BLIZZARD,
        cooldownSec = 5.0f,
        abilityDesc = "Viscol Polar: Furtună de gheață 4.5s ce oprește laserele, îngheață goblinii și dă +35% viteză (CD: 5s)."
    ),
    HeroMeta(
        id = "aurora_boreala_divin",
        name = "Aurora Boreală",
        rarity = HeroRarity.DIVIN,
        price = 330,
        gemPrice = 27,
        desc = "Spirit celest al luminilor nordice cu panglici prismatice și praf de stele.",
        trailColor = Color(0xFF38D9A9),
        effectLabel = "🌌 Panglici Aurorale & Stele Prismatice",
        abilityName = "Explozie Aurorală",
        abilityIcon = "🌌",
        abilityType = AbilityType.AURORA_NOVA,
        cooldownSec = 4.5f,
        abilityDesc = "Explozie Aurorală: Undă prismatică 4.5s cu salt înalt, invulnerabilitate 2.2s și magnet de monede (CD: 4.5s)."
    ),
    HeroMeta(
        id = "ingerul_zapezii",
        name = "Îngerul Zăpezii",
        rarity = HeroRarity.DIVIN,
        price = 340,
        gemPrice = 28,
        desc = "Înger luminos cu aripi albe-aurii și inel de gheață temporală.",
        trailColor = Color(0xFF74C0FC),
        effectLabel = "🪽 Aripi Luminoase & Aură Sacră",
        abilityName = "Frost-Slow",
        abilityIcon = "❄️",
        abilityType = AbilityType.SLOWMO,
        cooldownSec = 5.0f,
        abilityDesc = "Frost-Slow: Încetinește goblinii elfi, laserele și platformele la 40% timp de 4s + Triplu-Salt (CD: 5s)."
    ),
    HeroMeta(
        id = "craciunita_stelara",
        name = "Crăciunița Stelară",
        rarity = HeroRarity.DIVIN,
        price = 360,
        gemPrice = 30,
        desc = "Crăciunița regală în mantie stacojie cu cadouri orbitale și confetti aurii.",
        trailColor = Color(0xFFF06595),
        effectLabel = "🎁 Cadouri Orbitale & Confetti",
        abilityName = "Ploaie Cadouri",
        abilityIcon = "🎁",
        abilityType = AbilityType.PRESENT_BARRAGE,
        cooldownSec = 5.0f,
        abilityDesc = "Ploaie de Cadouri: Lansează cadouri explozive ce sparg lasere/goblini și îți oferă +3 🪙 bonus instant (CD: 5s)."
    ),
    HeroMeta(
        id = "mos_nicolae_astral",
        name = "Moș Nicolae Astral",
        rarity = HeroRarity.DIVIN,
        price = 390,
        gemPrice = 32,
        desc = "Legendă divină cu mitră aurie, barbă argintie și comete de acadea.",
        trailColor = Color(0xFFFFD43B),
        effectLabel = "🍬 Comete Acadea & Stele Regale",
        abilityName = "Turbo Acadea",
        abilityIcon = "🍬",
        abilityType = AbilityType.CANDY_TURBO,
        cooldownSec = 4.0f,
        abilityDesc = "Turbo Acadea: 4s hyper-viteză (+55%), scut invincibil ce spulberă goblinii și laserele (CD: 4s)."
    ),
    HeroMeta(
        id = "phoenix_festiv",
        name = "Phoenix Festiv",
        rarity = HeroRarity.DIVIN,
        currency = HeroCurrency.PASS,
        passTier = 12,
        desc = "Pasăre magică roșu-aurie cu aripi de Crăciun (Exclusiv Premium Pass Tier 12).",
        trailColor = Color(0xFFFF6B6B),
        effectLabel = "🔥 Aripi de Foc & Scut Festiv",
        abilityName = "Festive Flight",
        abilityIcon = "🔥",
        abilityType = AbilityType.SOLAR,
        cooldownSec = 5.0f,
        abilityDesc = "Festive Flight: Zbor magic 4s + Scut Festiv care te salvează de goblini elfi sau lasere (CD: 5s)."
    ),
    HeroMeta(
        id = "spiritul_craciunului",
        name = "Spiritul Crăciunului",
        rarity = HeroRarity.DIVIN,
        currency = HeroCurrency.PASS,
        passTier = 15,
        desc = "Spiritul suprem al Crăciunului (Exclusiv Premium Pass Tier 15).",
        trailColor = Color(0xFF51CF66),
        effectLabel = "🎄 Orbe Magice Tri-Color",
        abilityName = "Zero-G + Magnet",
        abilityIcon = "🎄",
        abilityType = AbilityType.ZEROG,
        cooldownSec = 4.5f,
        abilityDesc = "Zero Gravity + Magnet: Plutești 5s fără gravitație și atragi toate monedele (CD: 4.5s)."
    )
)

fun findHeroMeta(id: String): HeroMeta = HEROES_CATALOG.firstOrNull { it.id == id } ?: HEROES_CATALOG.first()

data class QuestDef(
    val id: String,
    val name: String,
    val sub: String,
    val statKey: String,
    val target: Int,
    val rewardCoins: Int
)

val STANDARD_QUESTS = listOf(
    QuestDef("q_lvl5", "Cuceritorul Polului Nord", "Completează 5 niveluri de Crăciun.", "levelsDone", 5, 50),
    QuestDef("q_int6", "Apărătorul Atelierului", "Elimină 6 goblini elfi sărind pe capul lor.", "intrudersKilled", 6, 55),
    QuestDef("q_coin100", "Sacul cu Monede", "Colectează 100 de monede de aur.", "coinsGot", 100, 60),
    QuestDef("q_jump60", "Salturi cu Zăpadă", "Fă 60 de sărituri cu bule și zăpadă în niveluri.", "jumpsMade", 60, 45)
)

object BonusQuestDef {
    const val NAME = "🌟 QUEST BONUS DE CRĂCIUN (24h)"
    const val SUB = "Completează 15 niveluri, elimină 15 goblini elfi și adună 250 monede!"
    const val TARGET_LEVELS = 15
    const val TARGET_INTRUDERS = 15
    const val TARGET_COINS = 250
    const val REWARD_COINS = 200
}

data class SantaQuestDef(
    val id: String,
    val icon: String,
    val name: String,
    val sub: String,
    val statKey: String,
    val target: Int,
    val rewardCoins: Int
)

val SANTA_QUESTS = listOf(
    SantaQuestDef(
        id = "sq_gifts",
        icon = "🎁",
        name = "Cadourile Săniei Mari",
        sub = "Adună 10 cutii de cadouri din Satul lui Moș Crăciun (pe lângă căsuțele cu zăpadă).",
        statKey = "giftsDelivered",
        target = 10,
        rewardCoins = 95
    ),
    SantaQuestDef(
        id = "sq_houses",
        icon = "🏠",
        name = "Luminile Căsuțelor",
        sub = "Aprinde luminile festive la 5 căsuțe acoperite cu zăpadă din noua dimensiune.",
        statKey = "housesVisited",
        target = 5,
        rewardCoins = 80
    ),
    SantaQuestDef(
        id = "sq_bubbles",
        icon = "🫧",
        name = "Salturi cu Bule de Iarnă",
        sub = "Execută 25 de sărituri cu bule pentru a încărca magia saniei.",
        statKey = "bubbleJumps",
        target = 25,
        rewardCoins = 70
    ),
    SantaQuestDef(
        id = "sq_coins",
        icon = "🛷",
        name = "Combustibil Aurit pentru Renii Moșului",
        sub = "Colectează 45 de monede în aventură pentru Sania lui Moș Crăciun.",
        statKey = "sleighCoins",
        target = 45,
        rewardCoins = 110
    )
)

object SantaGrandSleighBonus {
    const val NAME = "🎅 Marea Binecuvântare a Săniei"
    const val SUB = "Revendică toate cele 4 Questuri ale lui Moș Crăciun pentru marele premiu!"
    const val REWARD_COINS = 250
    const val REWARD_XP = 200
}

sealed class PassReward(val icon: String, val text: String) {
    class Coins(val amount: Int, icon: String = "🪙") : PassReward(icon, "+$amount Monede")
    class Diamonds(val amount: Int) : PassReward("💎", "+$amount Diamante")
    class Galactic(val amount: Int) : PassReward("🎄", "+$amount Stele Magice")
    class Hero(val heroId: String, icon: String, label: String) : PassReward(icon, label)
}

data class SeasonTier(
    val tier: Int,
    val freeReward: PassReward,
    val premReward: PassReward
)

val SEASON_TIERS: List<SeasonTier> = (1..15).map { t ->
    val freeRew: PassReward = when (t) {
        4, 11 -> PassReward.Coins(35 + t * 2, "🪙")
        7 -> PassReward.Coins(55, "💰")
        10 -> PassReward.Coins(85, "💰")
        15 -> PassReward.Coins(120, "💰")
        else -> PassReward.Coins(15 + t * 3)
    }

    val premRew: PassReward = when {
        t == 3 -> PassReward.Hero("spiridus_jucaus", "🧝", "Spiriduș Jucăuș (RAR)")
        t == 6 -> PassReward.Hero("vrajitoarea_ghetii", "❄️", "Vrăjitoarea Gheții (EPIC)")
        t == 9 -> PassReward.Hero("regele_ghetii", "👑", "Regele Gheții (MITIC)")
        t == 12 -> PassReward.Hero("phoenix_festiv", "🔥", "Phoenix Festiv (DIVIN)")
        t == 15 -> PassReward.Hero("spiritul_craciunului", "🎄", "Spiritul Crăciunului (DIVIN)")
        t % 2 == 0 -> PassReward.Diamonds(5)
        else -> PassReward.Coins(45 + t * 6, "💰")
    }
    SeasonTier(t, freeRew, premRew)
}

const val PREMIUM_COST_COINS = 350
const val DAY_MILLIS = 24L * 60L * 60L * 1000L

data class GameStats(
    val levelsDone: Int = 0,
    val intrudersKilled: Int = 0,
    val coinsGot: Int = 0,
    val jumpsMade: Int = 0
)

data class BonusStats(
    val levelsDone: Int = 0,
    val intrudersKilled: Int = 0,
    val coinsGot: Int = 0
)

data class SantaQuestStats(
    val giftsDelivered: Int = 0,
    val housesVisited: Int = 0,
    val bubbleJumps: Int = 0,
    val sleighCoins: Int = 0,
    val claimedSantaQuests: Set<String> = emptySet(),
    val grandSleighClaimed: Boolean = false
)

data class PersistentProgress(
    val coins: Int = 0,
    val diamonds: Int = 0,
    val galactic: Int = 0,
    val currentLevel: Int = 1,
    val unlockedLevel: Int = 1,
    val completedLevels: Set<Int> = emptySet(),
    val ownedHeroes: Set<String> = setOf("om_de_zapada"),
    val equippedHero: String = "om_de_zapada",
    val passXP: Int = 0,
    val premiumUnlocked: Boolean = false,
    val claimedFree: Set<Int> = emptySet(),
    val claimedPrem: Set<Int> = emptySet(),
    val lastDailyTs: Long = 0L,
    val lastBonusQuestTs: Long = 0L,
    val checkpointLevel: Int = 0,
    val checkpointX: Float = 0f,
    val checkpointY: Float = 0f,
    val stats: GameStats = GameStats(),
    val bonusStats: BonusStats = BonusStats(),
    val santaStats: SantaQuestStats = SantaQuestStats()
)
