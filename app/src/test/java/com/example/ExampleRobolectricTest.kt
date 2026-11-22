package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProgressRepository
import com.example.model.BonusQuestDef
import com.example.model.BonusStats
import com.example.model.GameStats
import com.example.model.PREMIUM_COST_COINS
import com.example.model.PersistentProgress
import com.example.model.SANTA_QUESTS
import com.example.model.STANDARD_QUESTS
import com.example.model.SantaGrandSleighBonus
import com.example.model.SantaQuestStats
import com.example.ui.ParkourViewModel
import com.example.ui.screens.PassModal
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Christmas Parkour", appName)
  }

  @Test
  fun `vertical pass modal places free pass on left and premium pass on right and slots are clickable`() {
    var clickedTier = -1
    var clickedIsPremium: Boolean? = null

    composeTestRule.setContent {
      MyApplicationTheme {
        PassModal(
          progress = PersistentProgress(coins = 500, passXP = 250),
          onUnlockPremium = {},
          onClaimSlot = { tier, isPrem ->
            clickedTier = tier.tier
            clickedIsPremium = isPrem
          },
          onDismiss = {}
        )
      }
    }

    val freeHeaderBounds = composeTestRule.onNodeWithText("🎁 FREE PASS", useUnmergedTree = true)
      .assertIsDisplayed()
      .fetchSemanticsNode().boundsInRoot
    val premHeaderBounds = composeTestRule.onNodeWithText("👑 PREMIUM PASS", useUnmergedTree = true)
      .assertIsDisplayed()
      .fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Free Pass ($freeHeaderBounds) should be on the left of Premium Pass ($premHeaderBounds)",
      freeHeaderBounds.left < premHeaderBounds.left
    )

    val freeSlotBounds = composeTestRule.onNodeWithTag("btn_pass_free_1")
      .assertIsDisplayed()
      .fetchSemanticsNode().boundsInRoot
    val premSlotBounds = composeTestRule.onNodeWithTag("btn_pass_prem_1")
      .assertIsDisplayed()
      .fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Free reward slot ($freeSlotBounds) should be on the left of Premium reward slot ($premSlotBounds)",
      freeSlotBounds.left < premSlotBounds.left
    )

    composeTestRule.onNodeWithTag("btn_pass_prem_1").performClick()
    assertEquals(1, clickedTier)
    assertEquals(true, clickedIsPremium)

    composeTestRule.onNodeWithTag("btn_pass_free_1").performClick()
    assertEquals(1, clickedTier)
    assertEquals(false, clickedIsPremium)
  }

  @Test
  fun `quests award coins only and unlock premium pass with coins`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val repo = ProgressRepository(app)
    val initialDiamonds = 7
    val initialGalactic = 12
    val initialCoins = 100

    runBlocking {
      repo.saveProgress(
        PersistentProgress(
          coins = initialCoins,
          diamonds = initialDiamonds,
          galactic = initialGalactic,
          stats = GameStats(
            levelsDone = 50,
            intrudersKilled = 50,
            coinsGot = 500,
            jumpsMade = 200
          ),
          bonusStats = BonusStats(
            levelsDone = BonusQuestDef.TARGET_LEVELS,
            intrudersKilled = BonusQuestDef.TARGET_INTRUDERS,
            coinsGot = BonusQuestDef.TARGET_COINS
          ),
          santaStats = SantaQuestStats(
            giftsDelivered = 25,
            housesVisited = 25,
            bubbleJumps = 25,
            sleighCoins = 150
          )
        )
      )
    }

    val vm = ParkourViewModel.provideFactory(app).create(ParkourViewModel::class.java)
    vm.audio.muted = true
    // Allow init coroutine to load saved progress from DataStore
    repeat(10) {
      ShadowLooper.idleMainLooper()
      if (vm.uiState.value.progress.diamonds == initialDiamonds) return@repeat
      Thread.sleep(25)
    }
    assertEquals(initialDiamonds, vm.uiState.value.progress.diamonds)

    val firstStandard = STANDARD_QUESTS.first()
    val coinsBeforeStandard = vm.uiState.value.progress.coins
    vm.claimStandardQuest(firstStandard)
    assertEquals(coinsBeforeStandard + firstStandard.rewardCoins, vm.uiState.value.progress.coins)
    assertEquals(initialDiamonds, vm.uiState.value.progress.diamonds)
    assertEquals(initialGalactic, vm.uiState.value.progress.galactic)

    SANTA_QUESTS.forEach { q ->
      val before = vm.uiState.value.progress.coins
      vm.claimSantaQuest(q)
      assertEquals(before + q.rewardCoins, vm.uiState.value.progress.coins)
      assertEquals(initialDiamonds, vm.uiState.value.progress.diamonds)
      assertEquals(initialGalactic, vm.uiState.value.progress.galactic)
    }

    val beforeGrand = vm.uiState.value.progress.coins
    vm.claimSantaGrandSleighBonus()
    assertEquals(beforeGrand + SantaGrandSleighBonus.REWARD_COINS, vm.uiState.value.progress.coins)
    assertEquals(initialDiamonds, vm.uiState.value.progress.diamonds)
    assertEquals(initialGalactic, vm.uiState.value.progress.galactic)

    val beforeBonus = vm.uiState.value.progress.coins
    vm.claimBonusQuest()
    assertEquals(beforeBonus + BonusQuestDef.REWARD_COINS, vm.uiState.value.progress.coins)
    assertEquals(initialDiamonds, vm.uiState.value.progress.diamonds)
    assertEquals(initialGalactic, vm.uiState.value.progress.galactic)

    val preUnlockCoins = vm.uiState.value.progress.coins
    assertTrue("Coins ($preUnlockCoins) should cover $PREMIUM_COST_COINS", preUnlockCoins >= PREMIUM_COST_COINS)
    vm.unlockPremiumPass()
    assertTrue(vm.uiState.value.progress.premiumUnlocked)
    assertEquals(preUnlockCoins - PREMIUM_COST_COINS, vm.uiState.value.progress.coins)

    assertTrue(vm.uiState.value.showEntranceMenu)
    vm.startGameFromEntrance()
    assertTrue(!vm.uiState.value.showEntranceMenu)
    vm.returnToEntranceMenu()
    assertTrue(vm.uiState.value.showEntranceMenu)
  }

  @Test
  fun `entrance menu has no loading bar and play settings exit buttons work`() {
    var played = false
    var openedSettings = false
    var exited = false

    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.screens.EntranceMenuOverlay(
          visible = true,
          progress = PersistentProgress(coins = 120),
          effectsEnabled = true,
          onPlayClick = { played = true },
          onProfileClick = {},
          onSettingsClick = { openedSettings = true },
          onExitClick = { exited = true }
        )
      }
    }

    composeTestRule.onNodeWithText("Încărcare Christmas Edition...", substring = true).assertDoesNotExist()
    composeTestRule.onNodeWithTag("btn_menu_play").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("btn_menu_settings").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("btn_menu_exit").assertIsDisplayed().performClick()

    assertTrue(played)
    assertTrue(openedSettings)
    assertTrue(exited)
  }
}

