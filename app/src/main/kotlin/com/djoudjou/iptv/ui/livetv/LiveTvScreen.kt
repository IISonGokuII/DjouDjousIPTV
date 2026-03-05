package com.djoudjou.iptv.ui.livetv

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import com.djoudjou.iptv.data.local.CategoryEntity
import com.djoudjou.iptv.data.local.StreamEntity

/**
 * LiveTvScreen - Live-TV Übersicht.
 *
 * Zeigt:
 * - Kategorien in linker Spalte
 * - Streams in rechtem Grid
 * - EPG-Vorschau für ausgewählten Stream
 *
 * TV-OPTIMIERT:
 * - D-Pad Navigation
 * - Focus-Effekte
 * - Zweispaltiges Layout
 */
@Composable
fun LiveTvScreen(
    categories: List<CategoryEntity> = emptyList(),
    streams: List<StreamEntity> = emptyList(),
    selectedCategoryId: Long? = null,
    isLoading: Boolean = false,
    error: String? = null,
    onCategorySelected: (Long) -> Unit,
    onStreamSelected: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Linke Spalte: Kategorien
        CategorySidebar(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = onCategorySelected,
            modifier = Modifier.width(250.dp)
        )

        // Rechte Spalte: Streams
        StreamGrid(
            streams = streams,
            onStreamSelected = onStreamSelected,
            isLoading = isLoading,
            error = error,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Kategorie Sidebar.
 */
@Composable
private fun CategorySidebar(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        TvLazyColumn(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // "Alle" Kategorie
            item {
                CategoryItem(
                    name = "Alle",
                    isSelected = selectedCategoryId == null,
                    onClick = { onCategorySelected(0) }
                )
            }

            // Kategorien
            items(categories, key = { it.id }) { category ->
                CategoryItem(
                    name = category.name,
                    isSelected = selectedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) },
                    isAdult = category.isAdult
                )
            }
        }
    }
}

/**
 * Kategorie Item.
 */
@Composable
private fun CategoryItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isAdult: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    NavigationDrawerItem(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (isAdult) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "18+",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedContainerColor = MaterialTheme.colorScheme.transparent,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Stream Grid.
 */
@Composable
private fun StreamGrid(
    streams: List<StreamEntity>,
    onStreamSelected: (StreamEntity) -> Unit,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(16.dp)
    ) {
        if (isLoading) {
            LoadingIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (error != null) {
            ErrorDisplay(
                error = error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (streams.isEmpty()) {
            EmptyState(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            TvLazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(streams, key = { it.id }) { stream ->
                    StreamItem(
                        stream = stream,
                        onClick = { onStreamSelected(stream) }
                    )
                }
            }
        }
    }
}

/**
 * Stream Item.
 */
@Composable
private fun StreamItem(
    stream: StreamEntity,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stream Icon/Logo
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (stream.iconUrl != null) {
                    // Coil Image Loading würde hier verwendet werden
                    Text(
                        text = "📺",
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        text = "📺",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Stream Name
            Text(
                text = stream.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            // Catch-Up Indicator
            if (stream.hasCatchUp) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "↩",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Loading Indicator.
 */
@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lade Streams...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Error Display.
 */
@Composable
private fun ErrorDisplay(
    error: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Empty State.
 */
@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📺",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Keine Streams verfügbar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
