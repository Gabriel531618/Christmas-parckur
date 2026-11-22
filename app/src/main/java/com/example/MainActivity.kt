package com.example

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ActiveModal
import com.example.ui.ParkourViewModel
import com.example.ui.canvas.GameWorldCanvas
import com.example.ui.components.DimensionActionPrompt
import com.example.ui.components.EffectsStatusStrip
import com.example.ui.components.TopHudOverlay
import com.example.ui.components.TouchControlsOverlay
import com.example.ui.screens.EntranceMenuOverlay
import com.example.ui.screens.LevelsModal
import com.example.ui.screens.PassModal
import com.example.ui.screens.ProfileModal
import com.example.ui.screens.QuestsModal
import com.example.ui.screens.SantaQuestsModal
import com.example.ui.screens.SettingsModal
import com.example.ui.screens.ShopModal
import com.example.ui.screens.VictoryModal
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicInk
import com.example.ui.theme.MyApplicationTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ParkourAdventureScreen()
            }
        }
    }
}

@Composable
fun ParkourAdventureScreen() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val viewModel: ParkourViewModel = viewModel(
        factory = remember(appContext) { ParkourViewModel.provideFactory(appContext) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = context as? Activity
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val webClientId = remember(context) { context.getString(R.string.default_web_client_id) }
val webClientId = ""
    fun launchGoogleSignIn() {
        coroutineScope.launch {
            viewModel.setAuthLoading(true)
            try {
                val googleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                val cred = result.credential
                if (cred is CustomCredential &&
                    cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val tokenCred = GoogleIdTokenCredential.createFrom(cred.data)
                    viewModel.signInWithGoogleToken(tokenCred.idToken)
                } else {
                    viewModel.setAuthError("Tip de autentificare nerecunoscut.")
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "User cancelled Google sign-in", e)
                viewModel.setAuthLoading(false)
            } catch (e: NoCredentialException) {
                Log.w("Auth", "No Google credentials available", e)
                viewModel.setAuthError("Nu există niciun cont Google configurat pe acest dispozitiv.")
            } catch (e: GetCredentialException) {
                Log.e("Auth", "Credential Manager error", e)
                viewModel.setAuthError(e.localizedMessage ?: "Conectarea Google a eșuat.")
            } catch (e: Exception) {
                Log.e("Auth", "Unexpected Google sign-in error", e)
                viewModel.setAuthError(e.localizedMessage ?: "Eroare la conectarea Google.")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            try {
                val silentOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(silentOption)
                    .build()
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                val cred = result.credential
                if (cred is CustomCredential &&
                    cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val tokenCred = GoogleIdTokenCredential.createFrom(cred.data)
                    viewModel.signInWithGoogleToken(tokenCred.idToken)
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.d("Auth", "Silent Google sign-in skipped: ${e.javaClass.simpleName}")
            }
        }
    }

    LaunchedEffect(uiState.isLandscapeLocked) {
        activity?.requestedOrientation = if (uiState.isLandscapeLocked) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    if (uiState.activeModal != ActiveModal.NONE) {
        BackHandler { viewModel.closeModal() }
    } else if (!uiState.showEntranceMenu) {
        BackHandler { viewModel.returnToEntranceMenu() }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050714))
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                val isDown = event.type == KeyEventType.KeyDown
                val isUp = event.type == KeyEventType.KeyUp
                when (event.key) {
                    Key.A, Key.DirectionLeft -> {
                        if (isDown) viewModel.engine.input.left = true
                        if (isUp) viewModel.engine.input.left = false
                        true
                    }
                    Key.D, Key.DirectionRight -> {
                        if (isDown) viewModel.engine.input.right = true
                        if (isUp) viewModel.engine.input.right = false
                        true
                    }
                    Key.Spacebar, Key.W, Key.DirectionUp -> {
                        if (isDown && !viewModel.engine.input.jumpHeld) {
                            viewModel.engine.input.jumpHeld = true
                            viewModel.engine.requestJump()
                        }
                        if (isUp) viewModel.engine.input.jumpHeld = false
                        true
                    }
                    Key.E, Key.ShiftLeft, Key.ShiftRight -> {
                        if (isDown) viewModel.engine.useDivinePower()
                        true
                    }
                    Key.F, Key.Enter -> {
                        if (isDown) {
                            when {
                                uiState.nearTreeDoor -> viewModel.openTreeDoor()
                                uiState.nearReturnDoor -> viewModel.returnFromVillage()
                                uiState.nearSanta -> viewModel.openSantaQuests()
                            }
                        }
                        true
                    }
                    Key.R -> {
                        if (isDown) viewModel.restartCurrentLevel()
                        true
                    }
                    else -> false
                }
            }
    ) {
        val density = LocalDensity.current.density
        val widthDp = maxWidth.value
        val heightDp = maxHeight.value
        val worldScale = if (heightDp < 440f) {
            density * max(0.74f, heightDp / 480f)
        } else {
            density
        }
        val viewW = (widthDp * density) / worldScale
        val viewH = (heightDp * density) / worldScale

        LaunchedEffect(viewW, viewH) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { frameNanos ->
                    if (lastNanos == 0L) lastNanos = frameNanos
                    val dtScale = ((frameNanos - lastNanos) / 16_666_667f).coerceIn(0.5f, 2.0f)
                    lastNanos = frameNanos
                    if (!uiState.showEntranceMenu && uiState.activeModal == ActiveModal.NONE) {
                        viewModel.stepFrame(viewW, viewH, dtScale)
                    }
                }
            }
        }

        // 1. Full-bleed Christmas Parkour World Canvas
        GameWorldCanvas(
            engine = viewModel.engine,
            frameTick = uiState.frameTick,
            onTreeDoorTap = { viewModel.openTreeDoor() },
            onReturnDoorTap = { viewModel.returnFromVillage() },
            onSantaTap = { viewModel.openSantaQuests() },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Safe-area HUD & Touch Controls Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopHudOverlay(
                    uiState = uiState,
                    onHeroBadgeClick = { viewModel.openModal(ActiveModal.PROFILE) },
                    onLevelsClick = { viewModel.openModal(ActiveModal.LEVELS) },
                    onQuestsClick = { viewModel.openModal(ActiveModal.QUESTS) },
                    onShopClick = { viewModel.openModal(ActiveModal.SHOP) },
                    onPassClick = { viewModel.openModal(ActiveModal.PASS) },
                    onSettingsClick = { viewModel.openModal(ActiveModal.SETTINGS) },
                    onRotateClick = { viewModel.toggleLandscapeLock() },
                    onSoundClick = { viewModel.toggleSound() },
                    onRestartClick = { viewModel.restartCurrentLevel() }
                )

                EffectsStatusStrip(
                    uiState = uiState,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Toast Notification Banner
                AnimatedVisibility(
                    visible = uiState.toastMessage != null,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 },
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicInk)
                            .border(2.5.dp, CosmicCyan, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = uiState.toastMessage.orEmpty(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Contextual Door / Santa Interaction Prompt above Touch Controls
            DimensionActionPrompt(
                uiState = uiState,
                onOpenTreeDoor = { viewModel.openTreeDoor() },
                onReturnFromVillage = { viewModel.returnFromVillage() },
                onOpenSantaQuests = { viewModel.openSantaQuests() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp)
            )

            // Touch Controls at Bottom
            TouchControlsOverlay(
                equippedHeroId = uiState.progress.equippedHero,
                abilityCdLeft = uiState.abilityCdLeft,
                abilityMaxCd = uiState.abilityMaxCd,
                effectsEnabled = uiState.touchEffectsEnabled,
                onLeftChanged = { viewModel.engine.input.left = it },
                onRightChanged = { viewModel.engine.input.right = it },
                onJumpPressed = {
                    viewModel.engine.input.jumpHeld = true
                    viewModel.engine.requestJump()
                },
                onJumpReleased = {
                    viewModel.engine.input.jumpHeld = false
                },
                onAbilityClick = { viewModel.engine.useDivinePower() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // 3. Entrance Menu (Play / Settings / Exit — no loading bar!)
        EntranceMenuOverlay(
            visible = uiState.showEntranceMenu,
            progress = uiState.progress,
            effectsEnabled = uiState.touchEffectsEnabled,
            onPlayClick = { viewModel.startGameFromEntrance() },
            onProfileClick = {
                viewModel.audio.playClick()
                viewModel.openModal(ActiveModal.PROFILE)
            },
            onSettingsClick = {
                viewModel.audio.playClick()
                viewModel.openModal(ActiveModal.SETTINGS)
            },
            onExitClick = {
                viewModel.audio.playClick()
                coroutineScope.launch {
                    delay(140L)
                    activity?.finish()
                }
            }
        )

        // 4. All Modals rendered above gameplay & entrance menu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            uiState.victorySummary?.let { summary ->
                VictoryModal(
                    summary = summary,
                    onNextLevel = { viewModel.loadLevel(summary.level + 1) }
                )
            }

            when (uiState.activeModal) {
                ActiveModal.LEVELS -> LevelsModal(
                    progress = uiState.progress,
                    onSelectLevel = { lvl -> viewModel.loadLevel(lvl) },
                    onMysteryClick = {
                        viewModel.showToast("🔒 Termină nivelul curent pentru a descoperi ce urmează!")
                    },
                    onDismiss = { viewModel.closeModal() }
                )

                ActiveModal.QUESTS -> QuestsModal(
                    progress = uiState.progress,
                    bonusRemainingMs = uiState.bonusQuestRemainingMs,
                    onClaimBonus = { viewModel.claimBonusQuest() },
                    onClaimStandard = { q -> viewModel.claimStandardQuest(q) },
                    onOpenSantaQuests = { viewModel.openSantaQuests() },
                    onDismiss = { viewModel.closeModal() }
                )

                ActiveModal.SANTA_QUESTS -> SantaQuestsModal(
                    progress = uiState.progress,
                    onClaimSantaQuest = { q -> viewModel.claimSantaQuest(q) },
                    onClaimGrandBonus = { viewModel.claimSantaGrandSleighBonus() },
                    onDismiss = { viewModel.closeModal() }
                )

                ActiveModal.SHOP -> ShopModal(
                    progress = uiState.progress,
                    selectedFilter = uiState.shopRarityFilter,
                    dailyRemainingMs = uiState.dailyRemainingMs,
                    onSelectFilter = { rarity -> viewModel.setShopFilter(rarity) },
                    onClaimDaily = { viewModel.claimDailyBonus() },
                    onEquipHero = { hero -> viewModel.equipHero(hero) },
                    onBuyWithCoins = { hero -> viewModel.buyHeroWithCoins(hero) },
                    onBuyWithDiamonds = { hero -> viewModel.buyHeroWithDiamonds(hero) },
                    onOpenPassFromHero = { viewModel.openModal(ActiveModal.PASS) },
                    onDismiss = { viewModel.closeModal() }
                )

                ActiveModal.PASS -> PassModal(
                    progress = uiState.progress,
                    onUnlockPremium = { viewModel.unlockPremiumPass() },
                    onClaimSlot = { tier, isPrem -> viewModel.claimPassSlot(tier, isPrem) },
                    onDismiss = { viewModel.closeModal() }
                )

                ActiveModal.SETTINGS -> SettingsModal(
                    uiState = uiState,
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleLandscape = { viewModel.toggleLandscapeLock() },
                    onToggleTouchEffects = { viewModel.toggleTouchEffects() },
                    onReturnToMenu = if (!uiState.showEntranceMenu) {
                        { viewModel.returnToEntranceMenu() }
                    } else null,
                    onDismiss = { viewModel.closeModal() }
                )

                ActiveModal.PROFILE -> ProfileModal(
                    uiState = uiState,
                    onSaveCheckpointNow = { viewModel.saveCheckpointNow() },
                    onGoogleSignInClick = { launchGoogleSignIn() },
                    onCloudPushClick = { viewModel.pushProgressToCloud() },
                    onCloudPullClick = { viewModel.pullProgressFromCloud() },
                    onSignOutClick = { viewModel.signOut(credentialManager) },
                    onOpenShopClick = { viewModel.openModal(ActiveModal.SHOP) },
                    onDismiss = { viewModel.closeModal() }
                )

                ActiveModal.NONE -> Unit
            }
        }
    }
}
