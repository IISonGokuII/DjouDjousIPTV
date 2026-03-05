package com.djoudjou.iptv.ui.player

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import com.djoudjou.iptv.player.PlayerManager
import kotlinx.coroutines.flow.collectLatest

/**
 * VideoPlayerScreen - Compose UI für Video-Wiedergabe.
 *
 * Verantwortlichkeiten:
 * - SurfaceView für Video-Rendering
 * - OSD Overlay einblenden
 * - Loading-Indicator zeigen
 * - Error-Handling UI
 * - PiP-Trigger
 *
 * TV-OPTIMIERT:
 * - D-Pad Navigation für OSD-Controls
 * - Focus-Effekte für Buttons
 * - Auto-Hide nach Inaktivität
 */
@Composable
fun VideoPlayerScreen(
    playerManager: PlayerManager,
    player: Player,
    uiState: PlayerUiState,
    onEvent: (PlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // SurfaceView für Video-Rendering
        SurfaceViewContainer(
            playerManager = playerManager,
            modifier = Modifier.fillMaxSize()
        )

        // Loading Indicator
        if (uiState.isLoading && !uiState.isReady) {
            LoadingIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error Display
        uiState.error?.let { error ->
            ErrorDisplay(
                error = error,
                onRetry = { onEvent(PlayerEvent.Retry) },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // OSD Overlay
        if (uiState.isOsdVisible || !uiState.isPlaying) {
            OsdOverlay(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize()
            )
        }

        // PiP Hint (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PipHint(
                isPlaying = uiState.isPlaying,
                canEnterPip = true,
                onEnterPip = { onEvent(PlayerEvent.EnterPip) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * SurfaceView Container für Video-Rendering.
 */
@Composable
private fun SurfaceViewContainer(
    playerManager: PlayerManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            SurfaceView(ctx).apply {
                playerManager.setSurfaceView(this)
            }
        },
        update = { surfaceView ->
            // SurfaceView wird beim Init gesetzt
        }
    )
}

/**
 * Loading Indicator.
 */
@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.tv.material3.CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Error Display.
 */
@Composable
private fun ErrorDisplay(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Wiedergabe-Fehler",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        androidx.tv.material3.Button(
            onClick = onRetry
        ) {
            Text("Erneut versuchen")
        }
    }
}

/**
 * PiP Hint Button.
 */
@Composable
private fun PipHint(
    isPlaying: Boolean,
    canEnterPip: Boolean,
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isPlaying && canEnterPip) {
        androidx.tv.material3.IconButton(
            onClick = onEnterPip,
            modifier = modifier
        ) {
            Text(
                text = "📺",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
