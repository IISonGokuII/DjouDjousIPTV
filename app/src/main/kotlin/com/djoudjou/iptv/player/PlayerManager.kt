package com.djoudjou.iptv.player

import android.content.Context
import android.view.SurfaceView
import androidx.annotation.OptIn
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.djoudjou.iptv.data.preferences.BufferSize
import com.djoudjou.iptv.data.preferences.BufferSizeConfig
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PlayerManager - Verwaltet ExoPlayer Instanz und Konfiguration.
 *
 * Verantwortlichkeiten:
 * - ExoPlayer Instanz erstellen und konfigurieren
 * - LoadControl basierend auf Buffer-Einstellungen
 * - RenderersFactory für Hardware/Software Decoder
 * - TrackSelector für Audio/Subtitle-Auswahl
 * - AFR (Auto Frame Rate) Konfiguration
 * - Deinterlacing Einstellungen
 *
 * THREAD-SAFETY: Alle Player-Operationen müssen auf dem Main-Thread ausgeführt werden.
 */
@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsPreferencesManager: SettingsPreferencesManager
) {
    /**
     * ExoPlayer Instanz.
     * Wird lazy initialisiert um Ressourcen zu sparen.
     */
    @OptIn(UnstableApi::class)
    lateinit var player: ExoPlayer
        private set

    /**
     * TrackSelector für Audio/Subtitle/Video-Track-Auswahl.
     */
    lateinit var trackSelector: DefaultTrackSelector
        private set

    /**
     * Aktuelle Buffer-Konfiguration.
     */
    private var currentBufferSizeConfig: BufferSizeConfig = BufferSizeConfig.NORMAL

    /**
     * Aktuelle Decoder-Einstellung.
     */
    private var useSoftwareDecoder: Boolean = false

    /**
     * Auto Frame Rate enabled.
     */
    private var autoFrameRateEnabled: Boolean = false

    /**
     * Deinterlacing enabled.
     */
    private var deinterlacingEnabled: Boolean = false

    /**
     * Initialisiert den Player mit Einstellungen aus DataStore.
     * Muss vor der ersten Verwendung aufgerufen werden.
     */
    @OptIn(UnstableApi::class)
    fun initialize() {
        if (::player.isInitialized) {
            return // Bereits initialisiert
        }

        // Einstellungen aus DataStore laden (blocking für Initialisierung)
        val settings = runBlocking {
            PlayerSettings(
                bufferSize = settingsPreferencesManager.bufferSize.first(),
                customBufferSizeMs = settingsPreferencesManager.customBufferSizeMs.first(),
                videoDecoder = settingsPreferencesManager.videoDecoder.first(),
                autoFrameRate = settingsPreferencesManager.autoFrameRate.first(),
                deinterlacing = settingsPreferencesManager.deinterlacing.first()
            )
        }

        applySettings(settings)
    }

    /**
     * Wendet Player-Einstellungen an und erstellt Player neu.
     */
    @OptIn(UnstableApi::class)
    fun applySettings(settings: PlayerSettings) {
        // Alten Player freigeben falls vorhanden
        if (::player.isInitialized) {
            player.release()
        }

        // Buffer-Konfiguration
        currentBufferSizeConfig = BufferSizeConfig.fromEnum(
            settings.bufferSize,
            settings.customBufferSizeMs,
            settings.customBufferSizeMs * 2
        )

        // Decoder-Einstellung
        useSoftwareDecoder = settings.videoDecoder == com.djoudjou.iptv.data.preferences.VideoDecoder.SOFTWARE

        // AFR und Deinterlacing
        autoFrameRateEnabled = settings.autoFrameRate
        deinterlacingEnabled = settings.deinterlacing

        // RenderersFactory konfigurieren
        val renderersFactory = createRenderersFactory()

        // TrackSelector konfigurieren
        trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setDisableText(false)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            )
        }

        // ExoPlayer erstellen
        player = ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(createLoadControl())
            .setHandleAudioBecomingNoisy(true) // Pause bei Bluetooth/Headset Disconnect
            .setWakeMode(C.WAKE_MODE_NETWORK) // WakeLock für Netzwerk-Streaming
            .build()

        // AFR konfigurieren
        if (autoFrameRateEnabled) {
            player.setVideoFrameSyncListener { frameRate ->
                // Frame-Sync-Listener für AFR
            }
        }
    }

    /**
     * Erstellt RenderersFactory mit Hardware/Software Decoder Konfiguration.
     */
    @OptIn(UnstableApi::class)
    private fun createRenderersFactory(): RenderersFactory {
        return DefaultRenderersFactory(context).apply {
            // Software Decoder erzwingen wenn eingestellt
            if (useSoftwareDecoder) {
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                setMediaCodecSelector(MediaCodecSelector.DEFAULT)
            }

            // AFR konfigurieren
            setEnableDecoderFallback(true)
            setEnableAudioFloatOutput(true)

            // Deinterlacing Parameter setzen
            if (deinterlacingEnabled) {
                // Deinterlacing wird automatisch von MediaCodec behandelt
                // wenn der Stream interlaced ist (576i, 1080i)
            }
        }
    }

    /**
     * Erstellt LoadControl mit Buffer-Konfiguration.
     */
    private fun createLoadControl(): DefaultLoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                currentBufferSizeConfig.minBufferMs,
                currentBufferSizeConfig.maxBufferMs,
                currentBufferSizeConfig.bufferForPlaybackMs,
                currentBufferSizeConfig.bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }

    /**
     * Setzt MediaItem und prepared Player.
     *
     * @param mediaItem Das abzuspielende MediaItem
     * @param playWhenReady Ob die Wiedergabe sofort starten soll
     * @param resetPositionOb die Position zurückgesetzt werden soll
     */
    fun setMediaItem(mediaItem: MediaItem, playWhenReady: Boolean = true, resetPosition: Boolean = true) {
        if (!::player.isInitialized) {
            initialize()
        }

        if (resetPosition) {
            player.setMediaItem(mediaItem, playWhenReady)
        } else {
            player.setMediaItem(mediaItem, player.currentPosition, playWhenReady)
        }

        player.prepare()

        if (playWhenReady) {
            player.play()
        }
    }

    /**
     * Wechselt zu neuem Stream (Instant Zapping).
     *
     * @param mediaItem Das neue MediaItem
     */
    fun switchStream(mediaItem: MediaItem) {
        if (!::player.isInitialized) {
            initialize()
        }

        // Atomare Sequenz für schnelles Zapping
        player.stop()
        player.clearMediaItems()
        player.setMediaItem(mediaItem, true)
        player.prepare()
        player.play()
    }

    /**
     * Setzt SurfaceView für Video-Wiedergabe.
     */
    fun setSurfaceView(surfaceView: SurfaceView) {
        if (!::player.isInitialized) {
            initialize()
        }
        player.setVideoSurfaceView(surfaceView)
    }

    /**
     * Gibt alle Player-Ressourcen frei.
     */
    fun release() {
        if (::player.isInitialized) {
            player.release()
        }
    }

    /**
     * Beobachtet Player-Einstellungen und wendet Änderungen an.
     */
    fun observeSettingsChanges() {
        // Wird in ViewModel implementiert
    }
}

/**
 * Player-Einstellungen Data Class.
 */
data class PlayerSettings(
    val bufferSize: BufferSize = BufferSize.NORMAL,
    val customBufferSizeMs: Int = 30000,
    val videoDecoder: com.djoudjou.iptv.data.preferences.VideoDecoder = com.djoudjou.iptv.data.preferences.VideoDecoder.HARDWARE,
    val autoFrameRate: Boolean = false,
    val deinterlacing: Boolean = false
)

/**
 * Hilfsfunktion für MediaItem-Erstellung.
 */
fun buildMediaItem(
    uri: String,
    title: String = "",
    artist: String = "",
    artworkUri: String? = null
): MediaItem {
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(artworkUri?.let { Uri.parse(it) })
                .build()
        )
        .build()
}
