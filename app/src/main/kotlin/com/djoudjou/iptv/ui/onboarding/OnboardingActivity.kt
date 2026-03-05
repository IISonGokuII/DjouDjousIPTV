package com.djoudjou.iptv.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.djoudjou.iptv.ui.main.MainActivity
import com.djoudjou.iptv.ui.onboarding.screens.CategorySelectScreen
import com.djoudjou.iptv.ui.onboarding.screens.LoginScreen
import com.djoudjou.iptv.ui.onboarding.screens.M3uImportScreen
import com.djoudjou.iptv.ui.onboarding.screens.M3uImportType
import com.djoudjou.iptv.ui.onboarding.screens.ProviderSelectScreen
import com.djoudjou.iptv.ui.onboarding.screens.ProviderType as UiProviderType
import com.djoudjou.iptv.ui.onboarding.screens.SyncProgressScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * OnboardingActivity - Haupt-Activity für das Smart Onboarding.
 *
 * Zeigt den mehrstufigen Wizard:
 * 1. Provider-Auswahl (Xtream vs M3U)
 * 2a. Xtream Login
 * 2b. M3U Import
 * 3. Kategorie-Auswahl
 * 4. Synchronisation
 *
 * Nach erfolgreichem Abschluss wird zur MainActivity weitergeleitet.
 */
@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val state by viewModel.state.collectAsState()

                    OnboardingContent(
                        state = state,
                        onEvent = viewModel::onEvent,
                        onComplete = {
                            // Zur MainActivity navigieren
                            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Haupt-Content für das Onboarding.
 *
 * Rendert den entsprechenden Screen basierend auf dem State.
 */
@androidx.compose.runtime.Composable
private fun OnboardingContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    onComplete: () -> Unit
) {
    when (val currentState = state) {
        is OnboardingState.ProviderSelect -> {
            ProviderSelectScreen(
                onProviderSelected = { type ->
                    onEvent(
                        OnboardingEvent.ProviderSelected(
                            when (type) {
                                UiProviderType.XTREAME -> com.djoudjou.iptv.data.preferences.ProviderType.XTREAME
                                UiProviderType.M3U -> com.djoudjou.iptv.data.preferences.ProviderType.M3U
                                UiProviderType.M3U_FILE -> com.djoudjou.iptv.data.preferences.ProviderType.M3U
                            }
                        )
                    )
                }
            )
        }

        is OnboardingState.Login -> {
            LoginScreen(
                url = currentState.url,
                username = currentState.username,
                password = currentState.password,
                isLoading = currentState.isLoading,
                error = currentState.error,
                onUrlChange = { url ->
                    onEvent(OnboardingEvent.LoginDataChanged(url, currentState.username, currentState.password))
                },
                onUsernameChange = { username ->
                    onEvent(OnboardingEvent.LoginDataChanged(currentState.url, username, currentState.password))
                },
                onPasswordChange = { password ->
                    onEvent(OnboardingEvent.LoginDataChanged(currentState.url, currentState.username, password))
                },
                onSubmit = { onEvent(OnboardingEvent.LoginSubmit) },
                onBack = { onEvent(OnboardingEvent.GoBack) }
            )
        }

        is OnboardingState.M3uImport -> {
            M3uImportScreen(
                importType = when {
                    currentState.importType == com.djoudjou.iptv.ui.onboarding.M3uImportType.URL -> M3uImportType.URL
                    else -> M3uImportType.FILE
                },
                url = currentState.url,
                filePath = currentState.filePath,
                isLoading = currentState.isLoading,
                error = currentState.error,
                onImportTypeChange = { type ->
                    onEvent(
                        OnboardingEvent.M3uImportTypeChanged(
                            when (type) {
                                M3uImportType.URL -> com.djoudjou.iptv.ui.onboarding.M3uImportType.URL
                                M3uImportType.FILE -> com.djoudjou.iptv.ui.onboarding.M3uImportType.FILE
                            }
                        )
                    )
                },
                onUrlChange = { url -> onEvent(OnboardingEvent.M3uUrlChanged(url)) },
                onFileSelected = { path -> onEvent(OnboardingEvent.M3uFileSelected(path)) },
                onSubmit = { onEvent(OnboardingEvent.M3uSubmit) },
                onBack = { onEvent(OnboardingEvent.GoBack) }
            )
        }

        is OnboardingState.CategoriesSelect -> {
            CategorySelectScreen(
                categories = currentState.categories.map { cat ->
                    com.djoudjou.iptv.ui.onboarding.screens.CategoryUiModel(
                        id = cat.id,
                        name = cat.name,
                        streamType = cat.streamType,
                        isAdult = cat.isAdult,
                        streamCount = cat.streamCount
                    )
                },
                selectedCategoryIds = currentState.selectedCategoryIds,
                isLoading = false,
                error = currentState.error,
                onCategorySelectionChanged = { id, selected ->
                    onEvent(OnboardingEvent.CategorySelectionChanged(id, selected))
                },
                onSelectAll = { onEvent(OnboardingEvent.SelectAllCategories) },
                onDeselectAll = { onEvent(OnboardingEvent.DeselectAllCategories) },
                onInvertSelection = { onEvent(OnboardingEvent.InvertCategorySelection) },
                onSubmit = { onEvent(OnboardingEvent.StartSync) },
                onBack = { onEvent(OnboardingEvent.GoBack) }
            )
        }

        is OnboardingState.Syncing -> {
            SyncProgressScreen(
                currentStep = currentState.currentStep,
                totalSteps = currentState.totalSteps,
                statusText = currentState.statusText,
                isComplete = currentState.isComplete,
                error = currentState.error,
                onComplete = onComplete
            )

            // Auto-navigate on complete
            if (currentState.isComplete) {
                onComplete()
            }
        }

        is OnboardingState.Complete -> {
            onComplete()
        }

        is OnboardingState.Error -> {
            // Error handling - show error and allow retry
            SyncProgressScreen(
                error = currentState.message,
                onComplete = {
                    currentState.retryAction?.invoke()
                }
            )
        }
    }
}
