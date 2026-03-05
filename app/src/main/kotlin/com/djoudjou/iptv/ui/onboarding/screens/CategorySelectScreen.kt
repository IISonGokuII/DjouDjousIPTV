package com.djoudjou.iptv.ui.onboarding.screens

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
import com.djoudjou.iptv.data.local.StreamType
import com.djoudjou.iptv.ui.onboarding.CategoryUiModel

/**
 * CategorySelectScreen - Stufe 3 des Onboarding.
 *
 * Checkbox-Liste aller Kategorien mit Select-All/None/Invert Buttons.
 * TV-optimiert mit D-Pad Navigation und Focus-Effekten.
 *
 * Kategorien mit Adult-Inhalten werden markiert angezeigt.
 */
@Composable
fun CategorySelectScreen(
    categories: List<CategoryUiModel> = emptyList(),
    selectedCategoryIds: Set<String> = emptySet(),
    isLoading: Boolean = false,
    error: String? = null,
    onCategorySelectionChanged: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onInvertSelection: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    // Nach Stream-Typ gruppieren
    val liveCategories = remember(categories) {
        categories.filter { it.streamType == StreamType.LIVE }
    }
    val vodCategories = remember(categories) {
        categories.filter { it.streamType == StreamType.VOD }
    }
    val seriesCategories = remember(categories) {
        categories.filter { it.streamType == StreamType.SERIES }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "Kategorien auswählen",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Wählen Sie die Kategorien die synchronisiert werden sollen",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Action Buttons
        Spacer(modifier = Modifier.height(24.dp))

        TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = onSelectAll,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Alle auswählen")
                }
            }

            item {
                Button(
                    onClick = onDeselectAll,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Alle abwählen")
                }
            }

            item {
                Button(
                    onClick = onInvertSelection,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Auswahl umkehren")
                }
            }
        }

        // Selection Info
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${selectedCategoryIds.size} von ${categories.size} Kategorien ausgewählt",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Categories by Type
        if (liveCategories.isNotEmpty()) {
            Text(
                text = "📺 Live TV (${liveCategories.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategoryList(
                categories = liveCategories,
                selectedCategoryIds = selectedCategoryIds,
                onSelectionChanged = onCategorySelectionChanged,
                modifier = Modifier.weight(1f)
            )
        }

        if (vodCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🎬 Filme (${vodCategories.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategoryList(
                categories = vodCategories,
                selectedCategoryIds = selectedCategoryIds,
                onSelectionChanged = onCategorySelectionChanged,
                modifier = Modifier.weight(1f)
            )
        }

        if (seriesCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "📺 Serien (${seriesCategories.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategoryList(
                categories = seriesCategories,
                selectedCategoryIds = selectedCategoryIds,
                onSelectionChanged = onCategorySelectionChanged,
                modifier = Modifier.weight(1f)
            )
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

        // Bottom Buttons
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.align(Alignment.End)
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
                enabled = !isLoading && selectedCategoryIds.isNotEmpty()
            ) {
                Text("Weiter")
            }
        }
    }
}

@Composable
private fun CategoryList(
    categories: List<CategoryUiModel>,
    selectedCategoryIds: Set<String>,
    onSelectionChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    TvLazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryListItem(
                category = category,
                isSelected = category.id in selectedCategoryIds,
                onSelectionChanged = { onSelectionChanged(category.id, it) }
            )
        }
    }
}

@Composable
private fun CategoryListItem(
    category: CategoryUiModel,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onSelectionChanged(!isSelected) },
                role = androidx.compose.ui.semantics.Role.Checkbox
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectionChanged,
            colors = CheckboxDefaults.colors(
                checkedColor = if (category.isAdult) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (category.isAdult) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "18+",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (category.streamCount > 0) {
                Text(
                    text = "${category.streamCount} Streams",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
