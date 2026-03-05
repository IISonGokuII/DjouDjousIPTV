package com.djoudjou.iptv.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*

/**
 * MainScreen - Haupt-Dashboard der App.
 *
 * Zeigt Navigation zwischen:
 * - Live TV
 * - VOD (Filme)
 * - Serien
 * - Einstellungen
 *
 * TV-OPTIMIERT:
 * - D-Pad Navigation
 * - Focus-Effekte
 * - Horizontale Navigation
 */
@Composable
fun MainScreen(
    currentTab: MainTab = MainTab.LiveTv,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Navigation Bar
        TopNavigationBar(
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.fillMaxWidth()
        )

        // Content Area (wird von Activity gefüllt)
        Box(
            modifier = Modifier.weight(1f)
        ) {
            // Content wird durch Navigation ersetzt
        }
    }
}

/**
 * Top Navigation Bar mit Tabs.
 */
@Composable
private fun TopNavigationBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    ) {
        TvLazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MainTab.values.toList(), key = { it.id }) { tab ->
                NavigationChip(
                    tab = tab,
                    isSelected = tab == currentTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

/**
 * Navigation Chip für Tabs.
 */
@Composable
private fun NavigationChip(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = tab.icon,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            labelColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        border = if (isSelected) {
            FilterChipDefaults.filterChipBorder(
                borderColor = MaterialTheme.colorScheme.primary,
                borderWidth = 2.dp,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            )
        } else {
            null
        }
    )
}

/**
 * Main Tabs für Navigation.
 */
enum class MainTab(
    val id: String,
    val label: String,
    val icon: String
) {
    LiveTv(
        id = "live_tv",
        label = "Live TV",
        icon = "📺"
    ),
    Vod(
        id = "vod",
        label = "Filme",
        icon = "🎬"
    ),
    Series(
        id = "series",
        label = "Serien",
        icon = "📁"
    ),
    Settings(
        id = "settings",
        label = "Einstellungen",
        icon = "⚙️"
    )
}
