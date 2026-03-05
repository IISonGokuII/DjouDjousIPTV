package com.djoudjou.iptv.ui.onboarding.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

/**
 * SyncProgressScreen - Stufe 4 des Onboarding.
 *
 * Zeigt den Fortschritt der Synchronisation an.
 * TV-optimiert mit animiertem Lade-Indicator.
 */
@Composable
fun SyncProgressScreen(
    currentStep: Int = 1,
    totalSteps: Int = 4,
    statusText: String = "Synchronisiere...",
    isComplete: Boolean = false,
    error: String? = null,
    onComplete: () -> Unit
) {
    val progress = currentStep.toFloat() / totalSteps.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isComplete) {
            // Erfolg
            SuccessAnimation()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Onboarding abgeschlossen!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sie werden zur Hauptseite weitergeleitet...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            // Loading
            SyncAnimation()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.width(400.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Schritt $currentStep von $totalSteps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Error Message
        if (error != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SyncAnimation() {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "⚙️",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
private fun SuccessAnimation() {
    val infiniteTransition = rememberInfiniteTransition()

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )

        Text(
            text = "✅",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.rotate(rotation)
        )
    }
}
