package com.djoudjou.iptv.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDrag
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme

/**
 * OsdOverlay - On-Screen-Display für Video-Player.
 *
 * Zeigt:
 * - Kanalname/Logo
 * - Aktuelles EPG-Event
 * - Fortschrittsbalken
 * - Controls (Play/Pause, Seek, Audio, Subtitles)
 *
 * TV-OPTIMIERT:
 * - D-Pad Navigation
 * - Focus-Effekte
 * - Auto-Hide nach 5 Sekunden
 */
@Composable
fun OsdOverlay(
    uiState: PlayerUiState,
    onEvent: (PlayerEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onEvent(PlayerEvent.TogglePlayPause) },
                    onDoubleTap = { onEvent(PlayerEvent.SeekForward(10)) }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDrag { _, dragAmount ->
                    if (dragAmount > 50) {
                        onEvent(PlayerEvent.SeekBackward(10))
                    } else if (dragAmount < -50) {
                        onEvent(PlayerEvent.SeekForward(10))
                    }
                }
            }
    ) {
        // Top Bar (Kanal-Info)
        TopBar(
            title = uiState.title,
            logoUrl = uiState.logoUrl,
            currentEpgEvent = uiState.currentEpgEvent,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // Bottom Bar (Controls)
        BottomBar(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )

        // Center Info (bei Play/Pause)
        if (!uiState.isPlaying && uiState.isReady) {
            CenterPauseIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Top Bar mit Kanal-Info und EPG.
 */
@Composable
private fun TopBar(
    title: String,
    logoUrl: String?,
    currentEpgEvent: EpgEventUi?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.7f),
                MaterialTheme.shapes.small
            )
            .padding(16.dp)
    ) {
        // Kanal-Name
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Placeholder
            if (logoUrl != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📺",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // EPG-Info
        currentEpgEvent?.let { epg ->
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = epg.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Fortschrittsbalken für EPG
            if (epg.progress > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = epg.progress / 100f,
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

/**
 * Bottom Bar mit Controls.
 */
@Composable
private fun BottomBar(
    uiState: PlayerUiState,
    onEvent: (PlayerEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.7f),
                MaterialTheme.shapes.small
            )
            .padding(16.dp)
    ) {
        // Seek Bar
        SeekBar(
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            onSeek = { onEvent(PlayerEvent.Seek(it)) },
            isLive = uiState.isLive()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play/Pause
                IconButton(
                    onClick = { onEvent(PlayerEvent.TogglePlayPause) }
                ) {
                    Text(
                        text = if (uiState.isPlaying) "⏸" else "▶",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // Seek Backward
                IconButton(
                    onClick = { onEvent(PlayerEvent.SeekBackward(10)) }
                ) {
                    Text(
                        text = "⏪",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // Seek Forward
                IconButton(
                    onClick = { onEvent(PlayerEvent.SeekForward(10)) }
                ) {
                    Text(
                        text = "⏩",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Time Display
            if (!uiState.isLive()) {
                Text(
                    text = "${uiState.getFormattedPosition()} / ${uiState.getFormattedDuration()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Right Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Audio Tracks
                if (uiState.audioTracks.isNotEmpty()) {
                    IconButton(
                        onClick = { /* Audio selection dialog */ }
                    ) {
                        Text(
                            text = "🔊",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                // Subtitles
                if (uiState.subtitleTracks.isNotEmpty()) {
                    IconButton(
                        onClick = { onEvent(PlayerEvent.DisableSubtitles) }
                    ) {
                        Text(
                            text = "📝",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                // Aspect Ratio
                IconButton(
                    onClick = { onEvent(PlayerEvent.ChangeAspectRatio(uiState.aspectRatio)) }
                ) {
                    Text(
                        text = "⬜",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

/**
 * Seek Bar mit Progress.
 */
@Composable
private fun SeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    isLive: Boolean
) {
    if (isLive) {
        // Live-Stream Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Red)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LIVE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        // Progress Bar
        val progress = if (duration > 0) {
            currentPosition.toFloat() / duration.toFloat()
        } else {
            0f
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
    }
}

/**
 * Center Pause Indicator.
 */
@Composable
private fun CenterPauseIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.5f),
                MaterialTheme.shapes.medium
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⏸",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Simple Icon Button.
 */
@Composable
private fun IconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.tv.material3.Button(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = androidx.tv.material3.ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        content()
    }
}
