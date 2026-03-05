package com.djoudjou.iptv.ui.player

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.djoudjou.iptv.player.PlayerManager
import com.djoudjou.iptv.player.PipHandler
import com.djoudjou.iptv.player.enterPipMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * VideoPlayerActivity - Activity für Video-Wiedergabe.
 *
 * Verantwortlichkeiten:
 * - PlayerViewModel bereitstellen
 * - PiP-Modus verwalten
 * - Lifecycle-Events an ViewModel weitergeben
 * - SurfaceView bereitstellen
 *
 * PiP SUPPORT:
 * - Android 8.0+ für volles PiP
 * - android:supportsPictureInPicture="true" im Manifest
 * - configChanges für Screen-Size/Rotation
 */
@AndroidEntryPoint
class VideoPlayerActivity : ComponentActivity() {

    @Inject
    lateinit var playerManager: PlayerManager

    private val viewModel: PlayerViewModel by viewModels()
    private val pipHandler = PipHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Screen immer an lassen während Wiedergabe
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Player initialisieren
        playerManager.initialize()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    val playerReady by viewModel.playerReady.collectAsState()

                    if (playerReady && playerManager::player.isInitialized) {
                        VideoPlayerScreen(
                            playerManager = playerManager,
                            player = playerManager.player,
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Loading State
                        androidx.tv.material3.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.tv.material3.CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        // Observe Player State für PiP
        observePlayerState()
    }

    /**
     * Beobachtet Player-State für PiP-Ready.
     */
    private fun observePlayerState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // PiP Params aktualisieren
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val params = pipHandler.createPipParams()
                        if (params != null) {
                            setPictureInPictureParams(params)
                        }
                    }
                }
            }
        }
    }

    /**
     * Wird aufgerufen wenn Nutzer die Activity verlässt (Zurück-Taste).
     * Trigger für PiP-Modus.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (shouldEnterPipMode()) {
            enterPipModeCompat()
        }
    }

    /**
     * Prüft ob PiP-Modus möglich ist.
     */
    private fun shouldEnterPipMode(): Boolean {
        if (!isPipSupported()) {
            return false
        }

        val uiState = viewModel.uiState.value
        return uiState.isPlaying && uiState.isReady
    }

    /**
     * Prüft ob PiP unterstützt wird.
     */
    private fun isPipSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    /**
     * Tritt in PiP-Modus ein.
     */
    private fun enterPipModeCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(pipHandler.getAspectRatio() ?: Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    /**
     * Wird bei Änderung des PiP-Modus aufgerufen.
     */
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        pipHandler.onPictureInPictureModeChanged(isInPictureInPictureMode)

        // OSD im PiP-Modus verstecken
        if (isInPictureInPictureMode) {
            viewModel.onEvent(PlayerEvent.HideOsd)
        }
    }

    override fun onPause() {
        super.onPause()
        pipHandler.onPause()

        // Wiedergabe nicht pausieren im PiP-Modus
        if (!pipHandler.isInPictureInPictureMode) {
            // Optional: Pause beim Verlassen
        }
    }

    override fun onResume() {
        super.onResume()
        pipHandler.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
        pipHandler.release()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

/**
 * Extension für PiP-Enter.
 */
fun ComponentActivity.enterPipModeCompat(pipHandler: PipHandler): Boolean {
    if (!pipHandler.isPipSupported) {
        return false
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val params = pipHandler.createPipParams()
        if (params != null) {
            enterPictureInPictureMode(params)
            return true
        }
    }

    return false
}
