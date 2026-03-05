package com.djoudjou.iptv.ui.main

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
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.djoudjou.iptv.data.local.StreamDao
import com.djoudjou.iptv.data.local.StreamType
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import com.djoudjou.iptv.ui.home.MainScreen
import com.djoudjou.iptv.ui.home.MainTab
import com.djoudjou.iptv.ui.home.MainViewModel
import com.djoudjou.iptv.ui.livetv.LiveTvScreen
import com.djoudjou.iptv.ui.livetv.LiveTvViewModel
import com.djoudjou.iptv.ui.onboarding.OnboardingActivity
import com.djoudjou.iptv.ui.settings.SettingsScreen
import com.djoudjou.iptv.ui.series.SeriesScreen
import com.djoudjou.iptv.ui.vod.VodScreen
import com.djoudjou.iptv.ui.vod.VodViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainActivity - Haupt-Activity der App.
 *
 * Zeigt das Main Dashboard mit Navigation zwischen:
 * - Live TV
 * - VOD
 * - Serien
 * - Einstellungen
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val liveTvViewModel: LiveTvViewModel by viewModels()
    private val vodViewModel: VodViewModel by viewModels()
    private val seriesViewModel: SeriesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prüfen ob Onboarding abgeschlossen
        checkOnboarding()

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val currentTab by mainViewModel.currentTab.collectAsState()

                    MainScreen(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            mainViewModel.setTab(tab)
                        }
                    )

                    // Screen Content wird hier gerendert
                    when (currentTab) {
                        MainTab.LiveTv -> {
                            val categories by liveTvViewModel.categories.collectAsState()
                            val streams by liveTvViewModel.streams.collectAsState()
                            val selectedCategoryId by liveTvViewModel.selectedCategoryId.collectAsState()

                            LiveTvScreen(
                                categories = categories,
                                streams = streams,
                                selectedCategoryId = selectedCategoryId,
                                onCategorySelected = { liveTvViewModel.selectCategory(it) },
                                onStreamSelected = { /* Player öffnen */ }
                            )
                        }
                        MainTab.Vod -> {
                            val movies by vodViewModel.movies.collectAsState()

                            VodScreen(
                                movies = movies,
                                onMovieSelected = { /* Player öffnen */ }
                            )
                        }
                        MainTab.Series -> {
                            val series by seriesViewModel.series.collectAsState()

                            SeriesScreen(
                                series = series,
                                onSeriesSelected = { /* Series Detail öffnen */ }
                            )
                        }
                        MainTab.Settings -> {
                            SettingsScreen(
                                onLogout = {
                                    // Logout und zum Onboarding
                                    startActivity(Intent(this, OnboardingActivity::class.java))
                                    finish()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Prüft ob Onboarding abgeschlossen wurde.
     */
    private fun checkOnboarding() {
        // Wird über Flow im ViewModel gemacht
        // Hier nur Placeholder
    }
}

/**
 * SeriesViewModel - ViewModel für Serien Screen.
 */
@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val streamDao: StreamDao,
    private val settingsPreferencesManager: com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
) : ViewModel() {

    val series: StateFlow<List<com.djoudjou.iptv.data.local.StreamEntity>> = providerId.flatMapLatest { providerId ->
        if (providerId != null) {
            streamDao.getStreamsByType(providerId, com.djoudjou.iptv.data.local.StreamType.SERIES)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val providerId: StateFlow<Long?> = settingsPreferencesManager.isOnboardingComplete
        .map { null }

    fun selectSeries(series: com.djoudjou.iptv.data.local.StreamEntity) {
        // Series Detail öffnen
    }
}
