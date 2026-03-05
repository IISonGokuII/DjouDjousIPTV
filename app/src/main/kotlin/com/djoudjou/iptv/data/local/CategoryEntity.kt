package com.djoudjou.iptv.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stream-Typ Enum.
 */
enum class StreamType {
    LIVE,       // Live-TV
    VOD,        // Video on Demand (Filme)
    SERIES      // Serien
}

/**
 * Category Entity - Repräsentiert eine Kategorie für Streams.
 *
 * Kategorien werden verwendet um Streams zu organisieren:
 * - Live TV: News, Sports, Movies, Kids, Adult, etc.
 * - VOD: Action, Comedy, Drama, Horror, etc.
 * - SERIES: Drama, Comedy, Documentary, etc.
 *
 * Jede Kategorie gehört zu genau einem Provider (Multi-Provider-Support).
 */
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["providerId"]),
        Index(value = ["providerId", "categoryId"]),
        Index(value = ["streamType"])
    ]
)
data class CategoryEntity(
    /**
     * Eindeutige Kategorie-ID (auto-generiert).
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Externe Kategorie-ID vom Provider.
     * Für Xtream: category_id aus der API
     * Für M3U: Hash des group-titles
     */
    val categoryId: String,

    /**
     * Name der Kategorie (z.B. "News", "Sports", "Movies").
     */
    val name: String,

    /**
     * Typ der Streams in dieser Kategorie.
     */
    val streamType: StreamType,

    /**
     * Foreign Key zum Provider.
     * Ermöglicht Multi-Provider-Support.
     */
    val providerId: Long,

    /**
     * True wenn diese Kategorie Adult-Inhalte enthält.
     * Wird automatisch gesetzt wenn:
     * - Xtream: category_name enthält Adult-Keywords
     * - M3U: group-title enthält Adult-Keywords
     */
    val isAdult: Boolean = false,

    /**
     * Anzahl der Streams in dieser Kategorie.
     * Wird bei Bedarf aktualisiert (denormalisiert für Performance).
     */
    val streamCount: Int = 0,

    /**
     * Logo/Icon der Kategorie (optional).
     */
    val iconUrl: String? = null,

    /**
     * Sortierreihenfolge (optional).
     */
    val order: Int = 0,

    /**
     * True wenn Kategorie vom Benutzer ausgewählt wurde (während Onboarding).
     */
    val isSelected: Boolean = true,

    /**
     * Zeitpunkt der Erstellung (Unix Timestamp in Millisekunden).
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Zeitpunkt der letzten Aktualisierung (Unix Timestamp in Millisekunden).
     */
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Erstellt eine eindeutige Kombination aus providerId und categoryId.
     * Wird verwendet um Duplikate zu vermeiden.
     */
    fun uniqueKey(): String {
        return "${providerId}_$categoryId"
    }
}
