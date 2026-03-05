package com.djoudjou.iptv.ui.series

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import com.djoudjou.iptv.data.local.StreamEntity

/**
 * SeriesScreen - Serien Übersicht.
 */
@Composable
fun SeriesScreen(
    series: List<StreamEntity> = emptyList(),
    isLoading: Boolean = false,
    error: String? = null,
    onSeriesSelected: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null) {
            ErrorDisplay(error = error, modifier = Modifier.align(Alignment.Center))
        } else if (series.isEmpty()) {
            EmptyState(modifier = Modifier.align(Alignment.Center))
        } else {
            TvLazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(series, key = { it.id }) { item ->
                    SeriesCard(series = item, onClick = onSeriesSelected)
                }
            }
        }
    }
}

@Composable
private fun SeriesCard(series: StreamEntity, onClick: (StreamEntity) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Cover
            Box(
                modifier = Modifier.size(76.dp).aspectRatio(2f/3f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📁", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
                Text(
                    text = series.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!series.genre.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = series.genre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!series.rating.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⭐ ${series.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Lade Serien...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable private fun ErrorDisplay(error: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "⚠️", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }
}

@Composable private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "📁", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Keine Serien verfügbar", style = MaterialTheme.typography.bodyLarge)
    }
}
