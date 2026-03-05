package com.djoudjou.iptv.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import com.djoudjou.iptv.data.local.EpgEventEntity
import com.djoudjou.iptv.data.local.EpgEventDao
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import com.djoudjou.iptv.player.AfrController
import com.djoudjou.iptv.player.PipHandler
import com.djoudjou.iptv.player.PlayerManager
import com.djoudjou.iptv.player.buildMediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PlayerViewModel - ViewModel für Video Player.
 *
 * Verantwortlichkeiten:
 * - PlayerManager steuern
 * - Player-Events verarbeiten
 * - UI-State bereitstellen
 * - EPG-Daten laden
 * - AFR und PiP verwalten
 * - Wiedergabeposition speichern (für Resume)
 *
 * ARCHITECTURE:
 * - Kein direkter Context-Zugriff (nur Application)
 * - Alle Abhängigkeiten via Hilt injiziert
 * - StateFlow für UI-Updates
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val playerManager: PlayerManager,
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val epgEventDao: EpgEventDao
) : AndroidViewModel(application), Player.Listener {

    // ═══════════════════════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Aktueller UI-State.
     */
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    /**
     * Player ist bereit.
     */
    private val _playerReady = MutableStateFlow(false)
    val playerReady: StateFlow<Boolean> = _playerReady.asStateFlow()

    /**
     * AFR Controller.
     */
    private var afrController: AfrController? = null

    /**
     * PiP Handler.
     */
    private val pipHandler = PipHandler()

    /**
     * OSD Auto-Hide Job.
     */
    private var osdHideJob: Job? = null

    /**
     * Position Update Job (für VOD Resume).
     */
    private var positionUpdateJob: Job? = null

    /**
     * Aktuelle Stream-ID für Resume-Position.
     */
    private var currentStreamId: Long? = null

    // ═══════════════════════════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════════════════════════

    init {
        // Player Listener registrieren
        if (playerManager::player.isInitialized) {
            playerManager.player.addListener(this)
        }

        // Settings beobachten
        observeSettings()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // PLAYER CONTROL
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Bereitet Stream zur Wiedergabe vor.
     *
     * @param streamUrl Die Stream-URL
     * @param title Stream-Titel
     * @param logoUrl Logo URL
     * @param streamId Stream-ID für Resume
     * @param startPosition Startposition in ms (für Resume)
     */
    fun prepareStream(
        streamUrl: String,
        title: String = "",
        logoUrl: String? = null,
        streamId: Long? = null,
        startPosition: Long = 0L
    ) {
        currentStreamId = streamId

        val mediaItem = buildMediaItem(
            uri = streamUrl,
            title = title,
            artist = "",
            artworkUri = logoUrl
        )

        playerManager.setMediaItem(mediaItem, playWhenReady = true, resetPosition = startPosition <= 0)

        // Resume-Position setzen
        if (startPosition > 0) {
            playerManager.player.seekTo(startPosition)
        }

        // Update UI
        _uiState.update { it.copy(title = title, logoUrl = logoUrl, isLoading = true) }

        // Position Update starten
        startPositionUpdates()
    }

    /**
     * Wechselt zu neuem Stream (Instant Zapping).
     */
    fun switchStream(
        streamUrl: String,
        title: String = "",
        logoUrl: String? = null,
        streamId: Long? = null
    ) {
        currentStreamId = streamId

        val mediaItem = buildMediaItem(
            uri = streamUrl,
            title = title,
            artworkUri = logoUrl
        )

        playerManager.switchStream(mediaItem)

        _uiState.update {
            it.copy(
                title = title,
                logoUrl = logoUrl,
                isLoading = true,
                currentEpgEvent = null,
                nextEpgEvent = null
            )
        }

        // EPG für neuen Stream laden
        streamId?.let { loadEpg(it) }
    }

    /**
     * Startet Wiedergabe.
     */
    fun play() {
        playerManager.player.play()
    }

    /**
     * Pausiert Wiedergabe.
     */
    fun pause() {
        playerManager.player.pause()
    }

    /**
     * Toggle Play/Pause.
     */
    fun togglePlayPause() {
        if (playerManager.player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    /**
     * Seek zu Position.
     */
    fun seekTo(positionMs: Long) {
        playerManager.player.seekTo(positionMs)
        updateCurrentPosition()
    }

    /**
     * Seek forward.
     */
    fun seekForward(seconds: Int = 10) {
        val newPosition = (playerManager.player.currentPosition + seconds * 1000)
            .coerceAtMost(playerManager.player.duration)
        seekTo(newPosition)
    }

    /**
     * Seek backward.
     */
    fun seekBackward(seconds: Int = 10) {
        val newPosition = (playerManager.player.currentPosition - seconds * 1000)
            .coerceAtLeast(0)
        seekTo(newPosition)
    }

    /**
     * Stoppt Wiedergabe.
     */
    fun stop() {
        // Position speichern vor Stop
        currentStreamId?.let { streamId ->
            saveResumePosition(streamId)
        }

        playerManager.player.stop()
        positionUpdateJob?.cancel()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // TRACK SELECTION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Wählt Audio-Track aus.
     */
    fun selectAudioTrack(index: Int) {
        val tracks = playerManager.player.currentTracks
        val trackGroups = tracks.groups

        var trackIndex = 0
        for (group in trackGroups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    if (trackIndex == index) {
                        playerManager.trackSelector.setParameters(
                            playerManager.trackSelector.buildUponParameters()
                                .setSelectionOverride(group.type, group.mediaTrackGroup, intArrayOf(i))
                        )
                        return
                    }
                    trackIndex++
                }
            }
        }
    }

    /**
     * Wählt Untertitel-Track aus.
     */
    fun selectSubtitleTrack(index: Int) {
        val tracks = playerManager.player.currentTracks
        val trackGroups = tracks.groups

        var trackIndex = 0
        for (group in trackGroups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    if (trackIndex == index) {
                        playerManager.trackSelector.setParameters(
                            playerManager.trackSelector.buildUponParameters()
                                .setSelectionOverride(group.type, group.mediaTrackGroup, intArrayOf(i))
                        )
                        return
                    }
                    trackIndex++
                }
            }
        }
    }

    /**
     * Deaktiviert Untertitel.
     */
    fun disableSubtitles() {
        playerManager.trackSelector.setParameters(
            playerManager.trackSelector.buildUponParameters()
                .setDisabledTrackTypes(setOf(C.TRACK_TYPE_TEXT))
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // OSD
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Zeigt OSD.
     */
    fun showOsd() {
        _uiState.update { it.copy(isOsdVisible = true) }
        scheduleOsdHide()
    }

    /**
     * Versteckt OSD.
     */
    fun hideOsd() {
        osdHideJob?.cancel()
        _uiState.update { it.copy(isOsdVisible = false) }
    }

    /**
     * Plant automatisches OSD-Hide.
     */
    private fun scheduleOsdHide(delayMs: Long = 5000) {
        osdHideJob?.cancel()
        osdHideJob = viewModelScope.launch {
            delay(delayMs)
            hideOsd()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // PLAYER LISTENER
    // ═══════════════════════════════════════════════════════════════════════════════

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                _playerReady.value = true
                _uiState.update {
                    it.copy(
                        isReady = true,
                        isLoading = false,
                        duration = playerManager.player.duration
                    )
                }

                // AFR anwenden
                applyAfr()

                // EPG laden
                currentStreamId?.let { loadEpg(it) }
            }
            Player.STATE_BUFFERING -> {
                _uiState.update { it.copy(isLoading = true) }
            }
            Player.STATE_IDLE -> {
                _playerReady.value = false
                _uiState.update { it.copy(isReady = false, isLoading = false) }
            }
            Player.STATE_ENDED -> {
                // Video beendet - Position speichern
                currentStreamId?.let { saveResumePosition(it) }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _uiState.update { it.copy(isPlaying = isPlaying) }

        if (isPlaying) {
            startPositionUpdates()
        } else {
            positionUpdateJob?.cancel()
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        _uiState.update {
            it.copy(
                videoWidth = videoSize.width,
                videoHeight = videoSize.height
            )
        }

        // PiP Handler update
        pipHandler.updateVideoSize(videoSize)
    }

    override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
        _uiState.update {
            it.copy(
                audioTracks = tracks.toAudioTracks(),
                subtitleTracks = tracks.toSubtitleTracks()
            )
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        _uiState.update {
            it.copy(
                error = error.message ?: "Unbekannter Fehler",
                isLoading = false
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Beobachtet Settings-Änderungen.
     */
    private fun observeSettings() {
        viewModelScope.launch {
            settingsPreferencesManager.autoFrameRate.collect { enabled ->
                // AFR wird bei nächstem Stream-Start angewendet
            }
        }
    }

    /**
     * Startet Position-Updates für Resume.
     */
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                delay(10_000) // Alle 10 Sekunden
                updateCurrentPosition()

                // Resume-Position speichern
                currentStreamId?.let { saveResumePosition(it) }
            }
        }
    }

    /**
     * Aktualisiert aktuelle Position im UI-State.
     */
    private fun updateCurrentPosition() {
        _uiState.update {
            it.copy(currentPosition = playerManager.player.currentPosition)
        }
    }

    /**
     * Speichert Resume-Position.
     */
    private fun saveResumePosition(streamId: Long) {
        viewModelScope.launch {
            // Wird in Phase 5 mit Room implementiert
        }
    }

    /**
     * Wendet AFR an.
     */
    private fun applyAfr() {
        // AFR wird vom AfrController übernommen
        // Video-FPS aus Track-Informationen extrahieren
    }

    /**
     * Lädt EPG-Daten für Stream.
     */
    private fun loadEpg(streamId: Long) {
        viewModelScope.launch {
            val currentEvent = epgEventDao.getCurrentEventSync(streamId)
            val nextEvent = epgEventDao.getNextEventSync(streamId)

            _uiState.update {
                it.copy(
                    currentEpgEvent = currentEvent?.toEpgEventUi(),
                    nextEpgEvent = nextEvent?.toEpgEventUi()
                )
            }
        }
    }

    /**
     * Gibt Ressourcen frei.
     */
    override fun onCleared() {
        super.onCleared()
        if (playerManager::player.isInitialized) {
            playerManager.player.removeListener(this)
        }
        osdHideJob?.cancel()
        positionUpdateJob?.cancel()
        afrController?.release()
        pipHandler.release()
    }
}

/**
 * Extension für EpgEventEntity zu EpgEventUi.
 */
fun EpgEventEntity.toEpgEventUi(): EpgEventUi {
    val currentTime = System.currentTimeMillis()
    val progress = getProgressPercent(currentTime)

    return EpgEventUi(
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        progress = progress
    )
}
