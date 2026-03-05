package com.djoudjou.iptv.ui.player

import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi

/**
 * PipHandler - Picture-in-Picture Handler für Android.
 *
 * Verantwortlichkeiten:
 * - PiP-Modus aktivieren/deaktivieren
 * - Seitenverhältnis für PiP-Fenster berechnen
 * - PiP-Lifecycle-Events verarbeiten
 * - PiP-Params für Android O+ konfigurieren
 *
 * ANFORDERUNGEN:
 * - Android 8.0+ für volles PiP-Support
 * - Android 7.0+ für eingeschränktes PiP
 * - Manifest: android:supportsPictureInPicture="true"
 */
@OptIn(UnstableApi::class)
class PipHandler {

    /**
     * Aktuelles Video-Seitenverhältnis.
     */
    private var currentVideoSize: VideoSize? = null

    /**
     * Ist PiP aktuell aktiv?
     */
    var isInPictureInPictureMode: Boolean = false
        private set

    /**
     * PiP-Support verfügbar?
     */
    val isPipSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    /**
     * Aktualisiert das Video-Seitenverhältnis.
     *
     * @param videoSize Die aktuelle Video-Größe
     */
    fun updateVideoSize(videoSize: VideoSize) {
        currentVideoSize = videoSize
    }

    /**
     * Berechnet das Seitenverhältnis für PiP-Fenster.
     *
     * @return Rational für PiP-Params oder null wenn keine Video-Größe bekannt
     */
    fun getAspectRatio(): Rational? {
        val videoSize = currentVideoSize ?: return null

        if (videoSize.width == 0 || videoSize.height == 0) {
            return null
        }

        return Rational(videoSize.width, videoSize.height)
    }

    /**
     * Erstellt PictureInPictureParams für Android O+.
     *
     * @return PictureInPictureParams oder null wenn nicht unterstützt
     */
    fun createPipParams(): PictureInPictureParams? {
        if (!isPipSupported) {
            return null
        }

        val params = PictureInPictureParams.Builder()

        // Seitenverhältnis setzen
        getAspectRatio()?.let { ratio ->
            params.setAspectRatio(ratio)
        }

        // Auto Enter enabled (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setAutoEnterEnabled(true)
        }

        return params.build()
    }

    /**
     * Wird aufgerufen wenn Player-Zustand sich ändert.
     *
     * @param player Der Player
     * @param onPipReady Callback wenn PiP bereit ist
     */
    fun onPlayerStateChanged(
        player: Player,
        onPipReady: () -> Unit
    ) {
        when (player.playbackState) {
            Player.STATE_READY -> {
                // Player bereit - PiP kann aktiviert werden
                onPipReady()
            }
            Player.STATE_ENDED -> {
                // Video beendet - PiP verlassen
                exitPipMode()
            }
            Player.STATE_IDLE -> {
                // Player idle - PiP verlassen
                exitPipMode()
            }
        }
    }

    /**
     * Wird aufgerufen wenn Activity in PiP-Modus wechselt.
     *
     * @param isInPip True wenn im PiP-Modus
     */
    fun onPictureInPictureModeChanged(isInPip: Boolean) {
        isInPictureInPictureMode = isInPip
    }

    /**
     * Verlässt den PiP-Modus.
     */
    fun exitPipMode() {
        isInPictureInPictureMode = false
    }

    /**
     * Prüft ob PiP-Modus möglich ist.
     *
     * @param player Der Player
     * @return true wenn PiP möglich ist
     */
    fun canEnterPipMode(player: Player): Boolean {
        return isPipSupported &&
                player.isPlaying &&
                player.playbackState == Player.STATE_READY
    }

    /**
     * Wird beim Verlassen der Activity aufgerufen.
     */
    fun onUserLeaveHint(): Boolean {
        // Return true wenn PiP automatisch aktiviert werden soll
        return isPipSupported
    }

    /**
     * Gibt Ressourcen frei.
     */
    fun release() {
        currentVideoSize = null
        isInPictureInPictureMode = false
    }
}

/**
 * Extension für Activity um PiP zu betreten.
 */
fun androidx.activity.ComponentActivity.enterPipMode(pipHandler: PipHandler): Boolean {
    if (!pipHandler.isPipSupported) {
        return false
    }

    val params = pipHandler.createPipParams()
    if (params != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        enterPictureInPictureMode(params)
        return true
    }

    return false
}
