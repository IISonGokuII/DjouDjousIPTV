package com.djoudjou.iptv.ui.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

/**
 * M3uImportScreen - Stufe 2b des Onboarding (M3U Import).
 *
 * Eingabe einer Remote-M3U-URL oder Auswahl einer lokalen Datei.
 * TV-optimiert mit D-Pad Navigation.
 */
@Composable
fun M3uImportScreen(
    importType: M3uImportType = M3uImportType.URL,
    url: String = "",
    filePath: String? = null,
    isLoading: Boolean = false,
    error: String? = null,
    onImportTypeChange: (M3uImportType) -> Unit,
    onUrlChange: (String) -> Unit,
    onFileSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    var showFilePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "M3U Import",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Wählen Sie die M3U-Quelle",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Import Type Selection
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterChip(
                selected = importType == M3uImportType.URL,
                onClick = { onImportTypeChange(M3uImportType.URL) },
                label = { Text("Remote URL") },
                enabled = !isLoading
            )

            FilterChip(
                selected = importType == M3uImportType.FILE,
                onClick = { onImportTypeChange(M3uImportType.FILE) },
                label = { Text("Lokale Datei") },
                enabled = !isLoading
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (importType) {
            M3uImportType.URL -> {
                // URL Input
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text("M3U URL") },
                    placeholder = { Text("http://example.com/playlist.m3u") },
                    modifier = Modifier.width(500.dp),
                    enabled = !isLoading,
                    maxLines = 3
                )
            }
            M3uImportType.FILE -> {
                // File Picker
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (filePath != null) {
                        Text(
                            text = "Ausgewählt: ${filePath.substringAfterLast('/')}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "Keine Datei ausgewählt",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* File Picker Logic - wird in Activity implementiert */ },
                        enabled = !isLoading
                    ) {
                        Text("Datei auswählen")
                    }
                }
            }
        }

        // Error Message
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onBack,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Zurück")
            }

            Button(
                onClick = onSubmit,
                enabled = !isLoading && (url.isNotBlank() || filePath != null)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Parsen..." : "Importieren")
            }
        }
    }
}

/**
 * M3U Import Typ.
 */
enum class M3uImportType {
    URL,
    FILE
}
