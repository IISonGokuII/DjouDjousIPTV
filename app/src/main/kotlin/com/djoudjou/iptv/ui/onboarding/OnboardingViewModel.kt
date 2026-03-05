package com.djoudjou.iptv.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudjou.iptv.data.local.CategoryEntity
import com.djoudjou.iptv.data.local.ProviderEntity
import com.djoudjou.iptv.data.local.StreamEntity
import com.djoudjou.iptv.data.preferences.ProviderType
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import com.djoudjou.iptv.data.remote.M3uEntry
import com.djoudjou.iptv.data.remote.M3uParser
import com.djoudjou.iptv.data.remote.XtreamApiService
import com.djoudjou.iptv.data.local.CategoryDao
import com.djoudjou.iptv.data.local.ProviderDao
import com.djoudjou.iptv.data.local.StreamDao
import com.djoudjou.iptv.data.local.StreamType
import com.djoudjou.iptv.domain.model.Result
import com.djoudjou.iptv.domain.model.successOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * OnboardingViewModel - State Machine für das Smart Onboarding.
 *
 * Verantwortlichkeiten:
 * - Provider-Auswahl (Xtream vs M3U)
 * - Xtream Authentication
 * - M3U Parsing
 * - Kategorie-Filterung
 * - Synchronisation in Room Database
 *
 * ARCHITECTURE:
 * - Kein direkter Context-Zugriff (für Testbarkeit)
 * - Alle Abhängigkeiten via Hilt injiziert
 * - StateFlow für UI-Updates
 * - Coroutines für Background-Operations
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val providerDao: ProviderDao,
    private val categoryDao: CategoryDao,
    private val streamDao: StreamDao,
    private val m3uParser: M3uParser,
    private val retrofitBuilder: Retrofit.Builder
) : ViewModel() {

    // ═══════════════════════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Aktueller Onboarding-State.
     */
    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.ProviderSelect)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    /**
     * Aktueller Provider-Typ.
     */
    private var currentProviderType: ProviderType = ProviderType.XTREAME

    /**
     * Temporäre Provider-ID für die Synchronisation.
     */
    private var currentProviderId: Long? = null

    // ═══════════════════════════════════════════════════════════════════════════════
    // EVENT HANDLING
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Verarbeitet Onboarding-Events.
     */
    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.ProviderSelected -> handleProviderSelected(event.type)
            is OnboardingEvent.LoginDataChanged -> handleLoginDataChanged(event.url, event.username, event.password)
            OnboardingEvent.LoginSubmit -> handleLoginSubmit()
            is OnboardingEvent.M3uImportTypeChanged -> handleM3uImportTypeChanged(event.type)
            is OnboardingEvent.M3uUrlChanged -> handleM3uUrlChanged(event.url)
            is OnboardingEvent.M3uFileSelected -> handleM3uFileSelected(event.filePath)
            OnboardingEvent.M3uSubmit -> handleM3uSubmit()
            is OnboardingEvent.CategorySelectionChanged -> handleCategorySelectionChanged(event.categoryId, event.isSelected)
            OnboardingEvent.SelectAllCategories -> handleSelectAllCategories()
            OnboardingEvent.DeselectAllCategories -> handleDeselectAllCategories()
            OnboardingEvent.InvertCategorySelection -> handleInvertCategorySelection()
            OnboardingEvent.StartSync -> handleStartSync()
            OnboardingEvent.GoBack -> handleGoBack()
            OnboardingEvent.Skip -> handleSkip()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // STUFE 1: PROVIDER-AUSWAHL
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Behandelt Provider-Auswahl.
     */
    private fun handleProviderSelected(type: ProviderType) {
        currentProviderType = type

        viewModelScope.launch {
            settingsPreferencesManager.setProviderType(type)
        }

        _state.value = when (type) {
            ProviderType.XTREAME -> OnboardingState.Login()
            ProviderType.M3U -> OnboardingState.M3uImport()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // STUFE 2a: XTREAM LOGIN
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Speichert Login-Daten im State.
     */
    private fun handleLoginDataChanged(url: String, username: String, password: String) {
        val currentState = _state.value as? OnboardingState.Login ?: return
        _state.value = currentState.copy(
            url = url,
            username = username,
            password = password
        )
    }

    /**
     * Führt Xtream Authentication durch.
     */
    private fun handleLoginSubmit() {
        val currentState = _state.value as? OnboardingState.Login ?: return

        // Validierung
        if (currentState.url.isBlank() || currentState.username.isBlank() || currentState.password.isBlank()) {
            _state.value = currentState.copy(error = "Bitte alle Felder ausfüllen")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            // Authentication durchführen
            val result = authenticateXtream(
                url = currentState.url,
                username = currentState.username,
                password = currentState.password
            )

            result.onSuccess { authResponse ->
                // Provider speichern
                val providerId = saveProvider(
                    name = currentState.username,
                    type = ProviderType.XTREAME,
                    serverUrl = currentState.url,
                    username = currentState.username,
                    password = currentState.password
                )

                currentProviderId = providerId

                // Credentials in Preferences speichern
                settingsPreferencesManager.setXtreamCredentials(
                    url = currentState.url,
                    username = currentState.username,
                    password = currentState.password
                )

                // Kategorien laden für Stufe 3
                loadCategoriesForProvider(currentState.url, currentState.username, currentState.password)
            }.onError { error ->
                _state.value = currentState.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    /**
     * Authentifiziert bei Xtream Codes API.
     */
    private suspend fun authenticateXtream(
        url: String,
        username: String,
        password: String
    ): Result<com.djoudjou.iptv.data.remote.XtreamAuthResponse> {
        return try {
            // Retrofit mit dynamischer Base-URL erstellen
            val apiService = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(XtreamApiService::class.java)

            val response = apiService.authenticate(username, password)

            if (response.userInfo.auth == 1) {
                successOf(response)
            } else {
                Result.Error(
                    message = response.userInfo.message.ifBlank { "Authentifizierung fehlgeschlagen" },
                    code = Result.ErrorCode.AUTH_ERROR
                )
            }
        } catch (e: Exception) {
            e.toResultError(
                message = when (e) {
                    is java.net.UnknownHostException -> "Server nicht erreichbar"
                    is java.net.SocketTimeoutException -> "Zeitüberschreitung"
                    else -> "Authentifizierung fehlgeschlagen: ${e.message}"
                }
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // STUFE 2b: M3U IMPORT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Speichert M3U Import-Typ im State.
     */
    private fun handleM3uImportTypeChanged(type: M3uImportType) {
        val currentState = _state.value as? OnboardingState.M3uImport ?: return
        _state.value = currentState.copy(importType = type)
    }

    /**
     * Speichert M3U URL im State.
     */
    private fun handleM3uUrlChanged(url: String) {
        val currentState = _state.value as? OnboardingState.M3uImport ?: return
        _state.value = currentState.copy(url = url)
    }

    /**
     * Speichert M3U Datei-Pfad im State.
     */
    private fun handleM3uFileSelected(filePath: String) {
        val currentState = _state.value as? OnboardingState.M3uImport ?: return
        _state.value = currentState.copy(filePath = filePath)
    }

    /**
     * Führt M3U Import durch.
     */
    private fun handleM3uSubmit() {
        val currentState = _state.value as? OnboardingState.M3uImport ?: return

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            val result = when (currentState.importType) {
                M3uImportType.URL -> {
                    if (currentState.url.isBlank()) {
                        _state.value = currentState.copy(
                            isLoading = false,
                            error = "Bitte M3U-URL eingeben"
                        )
                        return@launch
                    }
                    parseM3uFromUrl(currentState.url)
                }
                M3uImportType.FILE -> {
                    if (currentState.filePath.isNullOrBlank()) {
                        _state.value = currentState.copy(
                            isLoading = false,
                            error = "Bitte M3U-Datei auswählen"
                        )
                        return@launch
                    }
                    parseM3uFromFile(currentState.filePath!!)
                }
            }

            result.onSuccess { entries ->
                // Provider speichern
                val providerId = saveProvider(
                    name = "M3U Provider",
                    type = ProviderType.M3U,
                    m3uUrl = if (currentState.importType == M3uImportType.URL) currentState.url else null,
                    m3uFilePath = if (currentState.importType == M3uImportType.FILE) currentState.filePath else null
                )

                currentProviderId = providerId

                // Preferences speichern
                if (currentState.importType == M3uImportType.URL) {
                    settingsPreferencesManager.setM3uUrl(currentState.url)
                } else {
                    currentState.filePath?.let { settingsPreferencesManager.setM3uFilePath(it) }
                }

                // Kategorien aus Entries extrahieren
                loadCategoriesFromM3u(entries)
            }.onError { error ->
                _state.value = currentState.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    /**
     * Parst M3U von URL.
     */
    private suspend fun parseM3uFromUrl(url: String): Result<List<M3uEntry>> {
        return try {
            val entries = m3uParser.parseFromUrl(url)
            if (entries.isEmpty()) {
                Result.Error("Keine Streams in M3U-Datei gefunden")
            } else {
                successOf(entries)
            }
        } catch (e: Exception) {
            e.toResultError(message = "M3U-Parsing fehlgeschlagen: ${e.message}")
        }
    }

    /**
     * Parst M3U von Datei.
     */
    private suspend fun parseM3uFromFile(filePath: String): Result<List<M3uEntry>> {
        return try {
            val file = java.io.File(filePath)
            if (!file.exists()) {
                return Result.Error("Datei nicht gefunden: $filePath")
            }

            val entries = m3uParser.parseFromStream(file.inputStream())
            if (entries.isEmpty()) {
                Result.Error("Keine Streams in M3U-Datei gefunden")
            } else {
                successOf(entries)
            }
        } catch (e: Exception) {
            e.toResultError(message = "M3U-Parsing fehlgeschlagen: ${e.message}")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // STUFE 3: KATEGORIE-AUSWAHL
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Lädt Kategorien für Xtream Provider.
     */
    private suspend fun loadCategoriesForProvider(url: String, username: String, password: String) {
        try {
            val apiService = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(XtreamApiService::class.java)

            // Alle Kategorien-Typen laden
            val liveCategories = apiService.getLiveCategories(username, password)
            val vodCategories = apiService.getVodCategories(username, password)
            val seriesCategories = apiService.getSeriesCategories(username, password)

            // In UI-Model konvertieren
            val allCategories = mutableListOf<CategoryUiModel>()

            liveCategories.forEach { cat ->
                allCategories.add(
                    CategoryUiModel(
                        id = cat.categoryId,
                        name = cat.categoryName,
                        streamType = StreamType.LIVE,
                        isAdult = detectAdultContent(cat.categoryName)
                    )
                )
            }

            vodCategories.forEach { cat ->
                allCategories.add(
                    CategoryUiModel(
                        id = cat.categoryId,
                        name = cat.categoryName,
                        streamType = StreamType.VOD,
                        isAdult = detectAdultContent(cat.categoryName)
                    )
                )
            }

            seriesCategories.forEach { cat ->
                allCategories.add(
                    CategoryUiModel(
                        id = cat.categoryId,
                        name = cat.categoryName,
                        streamType = StreamType.SERIES,
                        isAdult = detectAdultContent(cat.categoryName)
                    )
                )
            }

            _state.value = OnboardingState.CategoriesSelect(
                categories = allCategories,
                selectedCategoryIds = allCategories.filterNot { it.isAdult }.map { it.id }.toSet()
            )
        } catch (e: Exception) {
            val currentState = _state.value as? OnboardingState.Login ?: return
            _state.value = currentState.copy(
                isLoading = false,
                error = "Kategorien konnten nicht geladen werden: ${e.message}"
            )
        }
    }

    /**
     * Extrahiert Kategorien aus M3U Entries.
     */
    private suspend fun loadCategoriesFromM3u(entries: List<M3uEntry>) {
        // Gruppen nach group-title extrahieren
        val categoriesByGroup = entries.groupBy { it.groupTitle ?: "Uncategorized" }

        val allCategories = categoriesByGroup.map { (groupName, groupEntries) ->
            CategoryUiModel(
                id = groupName.hashCode().toString(),
                name = groupName,
                streamType = StreamType.LIVE, // M3U ist typischerweise Live-TV
                isAdult = groupEntries.any { it.isAdult },
                streamCount = groupEntries.size
            )
        }

        _state.value = OnboardingState.CategoriesSelect(
            categories = allCategories,
            selectedCategoryIds = allCategories.filterNot { it.isAdult }.map { it.id }.toSet()
        )
    }

    /**
     * Behandelt Kategorie-Auswahl.
     */
    private fun handleCategorySelectionChanged(categoryId: String, isSelected: Boolean) {
        val currentState = _state.value as? OnboardingState.CategoriesSelect ?: return

        val newSelection = if (isSelected) {
            currentState.selectedCategoryIds + categoryId
        } else {
            currentState.selectedCategoryIds - categoryId
        }

        _state.value = currentState.copy(selectedCategoryIds = newSelection)
    }

    /**
     * Alle Kategorien auswählen.
     */
    private fun handleSelectAllCategories() {
        val currentState = _state.value as? OnboardingState.CategoriesSelect ?: return
        _state.value = currentState.copy(
            selectedCategoryIds = currentState.categories.map { it.id }.toSet()
        )
    }

    /**
     * Alle Kategorien abwählen.
     */
    private fun handleDeselectAllCategories() {
        val currentState = _state.value as? OnboardingState.CategoriesSelect ?: return
        _state.value = currentState.copy(selectedCategoryIds = emptySet())
    }

    /**
     * Auswahl umkehren.
     */
    private fun handleInvertCategorySelection() {
        val currentState = _state.value as? OnboardingState.CategoriesSelect ?: return
        _state.value = currentState.copy(
            selectedCategoryIds = currentState.categories
                .filterNot { it.id in currentState.selectedCategoryIds }
                .map { it.id }
                .toSet()
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // STUFE 4: SYNCHRONISATION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Startet Synchronisation.
     */
    private fun handleStartSync() {
        val currentState = _state.value as? OnboardingState.CategoriesSelect ?: return
        val providerId = currentProviderId ?: return

        viewModelScope.launch {
            _state.value = OnboardingState.Syncing(
                currentStep = 1,
                statusText = "Bereite Synchronisation vor..."
            )

            try {
                // Schritt 1: Kategorien speichern
                _state.value = OnboardingState.Syncing(
                    currentStep = 1,
                    statusText = "Speichere Kategorien..."
                )
                saveCategories(currentState.categories, currentState.selectedCategoryIds, providerId)

                // Schritt 2: Streams laden und speichern
                _state.value = OnboardingState.Syncing(
                    currentStep = 2,
                    statusText = "Lade Streams..."
                )

                val providerEntity = providerDao.getProviderByIdSync(providerId)
                if (providerEntity == null) {
                    _state.value = OnboardingState.Syncing(error = "Provider nicht gefunden")
                    return@launch
                }

                when (providerEntity.type) {
                    ProviderType.XTREAME -> syncXtreamStreams(
                        providerEntity = providerEntity,
                        selectedCategoryIds = currentState.selectedCategoryIds
                    )
                    ProviderType.M3U -> syncM3uStreams(
                        providerId = providerId,
                        selectedCategoryIds = currentState.selectedCategoryIds
                    )
                }

                // Schritt 3: Abschluss
                _state.value = OnboardingState.Syncing(
                    currentStep = 3,
                    statusText = "Abschluss..."
                )

                // Onboarding als abgeschlossen markieren
                settingsPreferencesManager.setOnboardingComplete(true)

                _state.value = OnboardingState.Complete
            } catch (e: Exception) {
                _state.value = OnboardingState.Syncing(
                    error = "Synchronisation fehlgeschlagen: ${e.message}"
                )
            }
        }
    }

    /**
     * Synchronisiert Xtream Streams.
     */
    private suspend fun syncXtreamStreams(
        providerEntity: ProviderEntity,
        selectedCategoryIds: Set<String>
    ) {
        val url = providerEntity.serverUrl!!
        val username = providerEntity.username!!
        val password = providerEntity.password!!

        val apiService = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(XtreamApiService::class.java)

        // Streams für jede ausgewählte Kategorie laden
        val allStreams = mutableListOf<StreamEntity>()

        // Live-Streams
        val liveCategories = categoryDao.getCategoriesByTypeSync(providerEntity.id, StreamType.LIVE)
            .filter { it.categoryId in selectedCategoryIds }

        for (category in liveCategories) {
            val streams = apiService.getLiveStreams(username, password, category.categoryId)
            allStreams.addAll(streams.map { it.toStreamEntity(providerEntity.id, category.id, StreamType.LIVE) })
        }

        // VOD-Streams
        val vodCategories = categoryDao.getCategoriesByTypeSync(providerEntity.id, StreamType.VOD)
            .filter { it.categoryId in selectedCategoryIds }

        for (category in vodCategories) {
            val streams = apiService.getVodStreams(username, password, category.categoryId)
            allStreams.addAll(streams.map { it.toStreamEntity(providerEntity.id, category.id, StreamType.VOD) })
        }

        // Serien
        val seriesCategories = categoryDao.getCategoriesByTypeSync(providerEntity.id, StreamType.SERIES)
            .filter { it.categoryId in selectedCategoryIds }

        for (category in seriesCategories) {
            val streams = apiService.getSeries(username, password, category.categoryId)
            allStreams.addAll(streams.map { it.toStreamEntity(providerEntity.id, category.id, StreamType.SERIES) })
        }

        // Bulk-Insert
        streamDao.insertAll(allStreams)
    }

    /**
     * Synchronisiert M3U Streams.
     */
    private suspend fun syncM3uStreams(
        providerId: Long,
        selectedCategoryIds: Set<String>
    ) {
        val provider = providerDao.getProviderByIdSync(providerId)
        if (provider == null) return

        val entries = when {
            !provider.m3uUrl.isNullOrBlank() -> m3uParser.parseFromUrl(provider.m3uUrl)
            !provider.m3uFilePath.isNullOrBlank() -> {
                val file = java.io.File(provider.m3uFilePath)
                if (file.exists()) m3uParser.parseFromStream(file.inputStream()) else emptyList()
            }
            else -> emptyList()
        }

        // Streams aus Entries erstellen
        val streams = entries
            .filter { it.groupTitle in selectedCategoryIds }
            .map { entry ->
                StreamEntity(
                    streamId = entry.extractId().toString(),
                    name = entry.displayName,
                    streamType = StreamType.LIVE,
                    categoryId = categoryDao.getCategoryByExternalId(
                        providerId,
                        entry.groupTitle?.hashCode()?.toString() ?: "0"
                    )?.id,
                    providerId = providerId,
                    iconUrl = entry.tvgLogo,
                    streamUrl = entry.streamUrl,
                    isAdult = entry.isAdult,
                    epgChannelId = entry.tvgId
                )
            }

        streamDao.insertAll(streams)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Speichert Provider in Datenbank.
     */
    private suspend fun saveProvider(
        name: String,
        type: ProviderType,
        serverUrl: String? = null,
        username: String? = null,
        password: String? = null,
        m3uUrl: String? = null,
        m3uFilePath: String? = null
    ): Long {
        val provider = ProviderEntity(
            name = name,
            type = type,
            serverUrl = serverUrl,
            username = username,
            password = password,
            m3uUrl = m3uUrl,
            m3uFilePath = m3uFilePath
        )
        return providerDao.insert(provider)
    }

    /**
     * Speichert Kategorien in Datenbank.
     */
    private suspend fun saveCategories(
        categories: List<CategoryUiModel>,
        selectedCategoryIds: Set<String>,
        providerId: Long
    ) {
        val entities = categories.map { cat ->
            CategoryEntity(
                categoryId = cat.id,
                name = cat.name,
                streamType = cat.streamType,
                providerId = providerId,
                isAdult = cat.isAdult,
                isSelected = cat.id in selectedCategoryIds
            )
        }
        categoryDao.insertAll(entities)
    }

    /**
     * Erkennt Adult-Inhalte.
     */
    private fun detectAdultContent(name: String): Boolean {
        val adultKeywords = listOf("adult", "porn", "xxx", "erotik", "erotic", "sexy", "18+", "18plus")
        return adultKeywords.any { name.lowercase().contains(it) }
    }

    /**
     * Zurück zur vorherigen Stufe.
     */
    private fun handleGoBack() {
        _state.value = when (_state.value) {
            is OnboardingState.Login -> OnboardingState.ProviderSelect
            is OnboardingState.M3uImport -> OnboardingState.ProviderSelect
            is OnboardingState.CategoriesSelect -> OnboardingState.ProviderSelect
            else -> _state.value
        }
    }

    /**
     * Onboarding überspringen.
     */
    private fun handleSkip() {
        viewModelScope.launch {
            settingsPreferencesManager.setOnboardingComplete(true)
        }
        _state.value = OnboardingState.Complete
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// EXTENSIONS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Konvertiert XtreamLiveStreamResponse zu StreamEntity.
 */
fun com.djoudjou.iptv.data.remote.XtreamLiveStreamResponse.toStreamEntity(
    providerId: Long,
    categoryId: Long?,
    streamType: StreamType
): StreamEntity {
    return StreamEntity(
        streamId = streamId.toString(),
        name = name,
        streamType = streamType,
        categoryId = categoryId,
        providerId = providerId,
        iconUrl = streamIcon,
        epgChannelId = epgChannelId,
        hasCatchUp = tvArchive == 1,
        catchUpDays = tvArchiveDuration,
        directSource = directSource,
        customSid = customSid
    )
}

/**
 * Konvertiert XtreamVodResponse zu StreamEntity.
 */
fun com.djoudjou.iptv.data.remote.XtreamVodResponse.toStreamEntity(
    providerId: Long,
    categoryId: Long?,
    streamType: StreamType
): StreamEntity {
    return StreamEntity(
        streamId = streamId.toString(),
        name = name,
        streamType = streamType,
        categoryId = categoryId,
        providerId = providerId,
        iconUrl = cover,
        rating = rating.rate,
        rating5based = rating5based,
        releaseDate = null, // Wird aus VOD-Info geladen
        directSource = directSource
    )
}

/**
 * Konvertiert XtreamSeriesResponse zu StreamEntity.
 */
fun com.djoudjou.iptv.data.remote.XtreamSeriesResponse.toStreamEntity(
    providerId: Long,
    categoryId: Long?,
    streamType: StreamType
): StreamEntity {
    return StreamEntity(
        streamId = seriesId.toString(),
        name = name,
        streamType = streamType,
        categoryId = categoryId,
        providerId = providerId,
        iconUrl = cover,
        plot = plot,
        director = director,
        genre = genre,
        releaseDate = releaseDate,
        rating = rating,
        rating5based = rating5based,
        backdropPathJson = backdropPath.joinToString("|"),
        youtubeTrailer = youtubeTrailer
    )
}

/**
 * Hilfsfunktion für MediaType.
 */
fun String.toMediaType(): okhttp3.MediaType {
    return okhttp3.MediaType.parse(this)!!
}
