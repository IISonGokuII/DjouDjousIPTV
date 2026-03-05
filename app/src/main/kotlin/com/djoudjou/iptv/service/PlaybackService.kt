package com.djoudjou.iptv.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.djoudjou.iptv.ui.player.PlaybackServiceConnection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * PlaybackService - Media3 MediaSessionService für Hintergrund-Wiedergabe.
 *
 * Verantwortlichkeiten:
 * - ExoPlayer Instanz verwalten
 * - MediaSession für Controller (PiP, Lockscreen, Notification)
 * - Foreground Service für kontinuierliche Wiedergabe
 * - Player-Events an UI kommunizieren
 *
 * ANDROIDMANIFEST:
 * <service
 *     android:name=".service.PlaybackService"
 *     android:exported="false"
 *     android:foregroundServiceType="mediaPlayback">
 *     <intent-filter>
 *         <action android:name="androidx.media3.session.MediaSessionService"/>
 *     </intent-filter>
 * </service>
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var playerManager: com.djoudjou.iptv.player.PlayerManager

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Wird beim Erstellen des Services aufgerufen.
     * Initialisiert ExoPlayer und MediaSession.
     */
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // ExoPlayer initialisieren
        playerManager.initialize()

        // MediaSession erstellen
        mediaSession = MediaSession.Builder(this, playerManager.player)
            .setSessionActivity(getSessionActivityPendingIntent())
            .setCustomLayout(getCustomLayout())
            .build()
    }

    /**
     * Gibt die MediaSession zurück.
     * Wird von MediaSessionService automatisch verwendet.
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    /**
     * Wird beim Zerstören des Services aufgerufen.
     * Gibt Player-Ressourcen frei.
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
    }

    /**
     * Erstellt PendingIntent für Session-Activity.
     * Wird verwendet wenn Nutzer auf Notification klickt.
     */
    private fun getSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, Class.forName(PLAYER_ACTIVITY_CLASS_NAME))
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Erstellt Custom Layout für MediaController.
     * Hier können benutzerdefinierte Buttons hinzugefügt werden.
     */
    private fun getCustomLayout(): List<MediaSession.CustomLayoutEntry> {
        return emptyList()
        // Beispiel für Custom Buttons:
        // listOf(
        //     MediaSession.CustomLayoutEntry.Builder(
        //         R.drawable.ic_aspect_ratio,
        //         "Aspect Ratio"
        //     ).build()
        // )
    }

    companion object {
        private const val PLAYER_ACTIVITY_CLASS_NAME = "com.djoudjou.iptv.ui.player.VideoPlayerActivity"

        /**
         * Channel ID für Notification (Android 8.0+).
         */
        const val NOTIFICATION_CHANNEL_ID = "playback_channel"

        /**
         * Notification ID für Foreground Service.
         */
        const val NOTIFICATION_ID = 1001
    }
}

/**
 * Interface für Kommunikation zwischen Service und UI.
 */
interface PlaybackServiceConnection {
    fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int)
    fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int)
    fun onTracksChanged(tracks: androidx.media3.common.Tracks)
}
