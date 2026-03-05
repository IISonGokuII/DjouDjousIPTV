package com.djoudjou.iptv.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import com.djoudjou.iptv.data.preferences.*

/**
 * SettingsScreen - Einstellungen.
 */
@Composable
fun SettingsScreen(
    bufferSize: BufferSize = BufferSize.NORMAL,
    videoDecoder: VideoDecoder = VideoDecoder.HARDWARE,
    autoFrameRate: Boolean = false,
    deinterlacing: Boolean = false,
    aspectRatio: AspectRatio = AspectRatio.FIT,
    epgUpdateInterval: EpgUpdateInterval = EpgUpdateInterval.ON_START,
    autostart: Boolean = false,
    parentalPinSet: Boolean = false,
    onBufferSizeChanged: (BufferSize) -> Unit = {},
    onVideoDecoderChanged: (VideoDecoder) -> Unit = {},
    onAutoFrameRateChanged: (Boolean) -> Unit = {},
    onDeinterlacingChanged: (Boolean) -> Unit = {},
    onAspectRatioChanged: (AspectRatio) -> Unit = {},
    onEpgUpdateIntervalChanged: (EpgUpdateInterval) -> Unit = {},
    onAutostartChanged: (Boolean) -> Unit = {},
    onParentalPinChanged: (String?) -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TvLazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle("Player-Einstellungen") }

        item {
            SettingItem(
                title = "Buffer-Größe",
                description = when (bufferSize) {
                    BufferSize.SMALL -> "Klein (schnelles Zapping)"
                    BufferSize.NORMAL -> "Normal (ausgewogen)"
                    BufferSize.LARGE -> "Groß (stabil)"
                    BufferSize.CUSTOM -> "Benutzerdefiniert"
                },
                onClick = { /* Show dialog */ }
            )
        }

        item {
            ToggleSettingItem(
                title = "Hardware-Decoder",
                description = "Hardware-Beschleunigung verwenden",
                checked = videoDecoder == VideoDecoder.HARDWARE,
                onCheckedChange = { onVideoDecoderChanged(if (it) VideoDecoder.HARDWARE else VideoDecoder.SOFTWARE) }
            )
        }

        item {
            ToggleSettingItem(
                title = "Auto Frame Rate",
                description = "Bildrate automatisch anpassen (Android TV)",
                checked = autoFrameRate,
                onCheckedChange = onAutoFrameRateChanged
            )
        }

        item {
            ToggleSettingItem(
                title = "Deinterlacing",
                description = "Für interlaced Streams (576i, 1080i)",
                checked = deinterlacing,
                onCheckedChange = onDeinterlacingChanged
            )
        }

        item { SectionTitle("EPG-Einstellungen") }

        item {
            SettingItem(
                title = "EPG-Update",
                description = when (epgUpdateInterval) {
                    EpgUpdateInterval.ON_START -> "Beim App-Start"
                    EpgUpdateInterval.EVERY_12H -> "Alle 12 Stunden"
                    EpgUpdateInterval.EVERY_24H -> "Alle 24 Stunden"
                    EpgUpdateInterval.MANUAL -> "Manuell"
                },
                onClick = { /* Show dialog */ }
            )
        }

        item { SectionTitle("Allgemein") }

        item {
            ToggleSettingItem(
                title = "Autostart",
                description = "Letzten Kanal beim Start wiedergeben",
                checked = autostart,
                onCheckedChange = onAutostartChanged
            )
        }

        item {
            SettingItem(
                title = "Kindersicherung",
                description = if (parentalPinSet) "PIN geändert" else "PIN festlegen",
                onClick = { /* Show PIN dialog */ }
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Abmelden & Alle Daten löschen")
            }
        }
    }
}

@Composable private fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable private fun SettingItem(title: String, description: String, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick, colors = CardDefaults.cardColors(containerColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface); Spacer(modifier = Modifier.height(4.dp)); Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(text = "›", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable private fun ToggleSettingItem(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), onClick = { onCheckedChange(!checked) }, colors = CardDefaults.cardColors(containerColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = title, style = MaterialTheme.typography.bodyLarge); Spacer(modifier = Modifier.height(4.dp)); Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
