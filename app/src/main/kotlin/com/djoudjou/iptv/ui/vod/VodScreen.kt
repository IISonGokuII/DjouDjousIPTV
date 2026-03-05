package com.djoudjou.iptv.ui.vod

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.*
import com.djoudjou.iptv.data.local.StreamEntity

/**
 * VodScreen - VOD (Filme) Übersicht.
 *
 * Zeigt Filme als Grid mit Postern.
 */
@Composable
fun VodScreen(
    movies: List<StreamEntity> = emptyList(),
    isLoading: Boolean = false,
    error: String? = null,
    onMovieSelected: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        if (isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null) {
            ErrorDisplay(error = error, modifier = Modifier.align(Alignment.Center))
        } else if (movies.isEmpty()) {
            EmptyState(modifier = Modifier.align(Alignment.Center))
        } else {
            TvLazyVerticalGrid(
                columns = androidx.tv.foundation.lazy.grid.GridCells.Adaptive(150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = onMovieSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: StreamEntity,
    onClick: (StreamEntity) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.aspectRatio(2f/3f),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            // Poster Placeholder
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎬", style = MaterialTheme.typography.headlineLarge)
            }

            // Title
            Text(
                text = movie.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Rating
            if (!movie.rating.isNullOrBlank()) {
                Text(
                    text = "⭐ ${movie.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Lade Filme...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorDisplay(error: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "⚠️", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "🎬", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Keine Filme verfügbar", style = MaterialTheme.typography.bodyLarge)
    }
}
