package com.djoudjou.iptv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudjou.iptv.data.local.CategoryDao
import com.djoudjou.iptv.data.local.CategoryEntity
import com.djoudjou.iptv.data.local.StreamDao
import com.djoudjou.iptv.data.local.StreamEntity
import com.djoudjou.iptv.data.local.StreamType
import com.djoudjou.iptv.data.preferences.ProviderType
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainViewModel - ViewModel für Haupt-Dashboard.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val categoryDao: CategoryDao,
    private val streamDao: StreamDao
) : ViewModel() {

    private val _currentTab = MutableStateFlow(MainTab.LiveTv)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _providerId = MutableStateFlow<Long?>(null)
    val providerId: StateFlow<Long?> = _providerId.asStateFlow()

    init {
        viewModelScope.launch {
            settingsPreferencesManager.isOnboardingComplete.collect { isComplete ->
                if (!isComplete) {
                    // Onboarding nicht abgeschlossen
                }
            }

            // Aktiven Provider laden
            categoryDao.getAllProvidersList().firstOrNull()?.firstOrNull()?.id?.let { id ->
                _providerId.value = id
            }
        }
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }
}

/**
 * LiveTvViewModel - ViewModel für Live-TV Screen.
 */
@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val streamDao: StreamDao,
    private val settingsPreferencesManager: SettingsPreferencesManager
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = providerId.flatMapLatest { providerId ->
        if (providerId != null) {
            categoryDao.getCategoriesByProviderAndType(providerId, StreamType.LIVE)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streams: StateFlow<List<StreamEntity>> = combine(
        providerId,
        selectedCategoryId
    ) { providerId, categoryId ->
        if (providerId == null) return@combine emptyList()

        if (categoryId == null || categoryId == 0L) {
            // Alle Streams
            streamDao.getStreamsByTypeSync(providerId, StreamType.LIVE)
        } else {
            // Streams der Kategorie
            streamDao.getStreamsByCategoryIdSync(categoryId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val providerId: StateFlow<Long?> = settingsPreferencesManager.isOnboardingComplete
        .map { null } // TODO: Provider ID laden

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun selectStream(stream: StreamEntity) {
        // Stream zur Wiedergabe öffnen
    }
}

/**
 * VodViewModel - ViewModel für VOD Screen.
 */
@HiltViewModel
class VodViewModel @Inject constructor(
    private val streamDao: StreamDao,
    private val settingsPreferencesManager: SettingsPreferencesManager
) : ViewModel() {

    val movies: StateFlow<List<StreamEntity>> = providerId.flatMapLatest { providerId ->
        if (providerId != null) {
            streamDao.getStreamsByType(providerId, StreamType.VOD)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val providerId: StateFlow<Long?> = settingsPreferencesManager.isOnboardingComplete
        .map { null }

    fun selectMovie(movie: StreamEntity) {
        // Movie zur Wiedergabe öffnen
    }
}
