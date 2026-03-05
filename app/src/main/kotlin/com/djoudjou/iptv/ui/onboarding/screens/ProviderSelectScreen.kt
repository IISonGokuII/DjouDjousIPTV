package com.djoudjou.iptv.ui.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*

/**
 * ProviderSelectScreen - Stufe 1 des Onboarding.
 *
 * Auswahl zwischen Xtream Codes API und M3U URL/Datei.
 * TV-optimiert mit D-Pad Navigation und Focus-Effekten.
 */
@Composable
fun ProviderSelectScreen(
    onProviderSelected: (ProviderType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Willkommen bei DjouDjous IPTV",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Wählen Sie Ihren Provider-Typ",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProviderCard(
                    title = "Xtream Codes",
                    description = "Server-URL, Benutzername und Passwort",
                    icon = "🔐",
                    onClick = { onProviderSelected(ProviderType.XTREAME) }
                )
            }

            item {
                ProviderCard(
                    title = "M3U URL",
                    description = "Remote M3U/M3U8 Playlist URL",
                    icon = "🌐",
                    onClick = { onProviderSelected(ProviderType.M3U) }
                )
            }

            item {
                ProviderCard(
                    title = "M3U Datei",
                    description = "Lokale M3U-Datei vom Gerät",
                    icon = "📁",
                    onClick = { onProviderSelected(ProviderType.M3U_FILE) }
                )
            }
        }
    }
}

@Composable
private fun ProviderCard(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit
) {
    var isFocused by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .selectable(
                selected = false,
                onClick = onClick,
                role = Role.Button
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onFocusChanged = { isFocused = it.isFocused }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * ProviderType für die UI.
 */
enum class ProviderType {
    XTREAME,
    M3U,
    M3U_FILE
}
