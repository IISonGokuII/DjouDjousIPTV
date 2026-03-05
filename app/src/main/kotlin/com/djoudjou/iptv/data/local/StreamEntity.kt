package com.djoudjou.iptv.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stream Entity - Repräsentiert einen einzelnen Stream (Live-TV, VOD oder Serie).
 *
 * Streams gehören immer zu einer Kategorie und einem Provider:
 * - Live-TV: CNN, BBC, Sky Sports, etc.
 * - VOD: Filme (Inception, Avatar, etc.)
 * - SERIES: Serien mit Staffeln/Episoden
 *
 * Jeder Stream enthält alle notwendigen Informationen für die Wiedergabe.
 */
@Entity(
    tableName = "streams",
    foreignKeys = [
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["providerId"]),
        Index(value = ["categoryId"]),
        Index(value = ["providerId", "streamId"]),
        Index(value = ["streamType"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isAdult"])
    ]
)
data class StreamEntity(
    /**
     * Eindeutige Stream-ID (auto-generiert).
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Externe Stream-ID vom Provider.
     * Für Xtream: stream_id aus der API
     * Für M3U: Hash der URL oder tvg-id
     */
    val streamId: String,

    /**
     * Name/Title des Streams.
     * Beispiel: "CNN HD", "Inception (2010)", "Breaking S01E01"
     */
    val name: String,

    /**
     * Typ des Streams.
     */
    val streamType: StreamType,

    /**
     * Foreign Key zur Kategorie.
     */
    val categoryId: Long? = null,

    /**
     * Foreign Key zum Provider.
     */
    val providerId: Long,

    /**
     * URL zum Stream-Logo/Poster.
     */
    val iconUrl: String? = null,

    /**
     * Stream-URL (wird bei Bedarf gebaut).
     * Für Xtream: wird zur Laufzeit generiert
     * Für M3U: direkte URL aus der Datei
     */
    val streamUrl: String? = null,

    /**
     * Datei-Extension (m3u8, mp4, mkv, ts, etc.).
     */
    val containerExtension: String? = null,

    /**
     * EPG Channel ID für Live-TV.
     * Wird verwendet um EPG-Daten zuzuordnen.
     */
    val epgChannelId: String? = null,

    /**
     * True wenn Stream Adult-Inhalte enthält.
     */
    val isAdult: Boolean = false,

    /**
     * True wenn Stream als Favorit markiert ist.
     */
    val isFavorite: Boolean = false,

    /**
     * True wenn Catch-Up/Timeshift verfügbar ist (nur Live-TV).
     */
    val hasCatchUp: Boolean = false,

    /**
     * Catch-Up Duration in Tagen (nur wenn hasCatchUp = true).
     */
    val catchUpDays: Int = 0,

    /**
     * Direct Source URL (alternative Stream-Quelle).
     */
    val directSource: String? = null,

    /**
     * Custom SID (benutzerdefinierte Stream-ID).
     */
    val customSid: String? = null,

    /**
     * Rating (z.B. "8.5/10" für VOD).
     */
    val rating: String? = null,

    /**
     * Rating als 5-basierte Skala (z.B. 4.2 für 5 Sterne).
     */
    val rating5based: Double = 0.0,

    /**
     * Plot/Beschreibung (nur VOD/Series).
     */
    val plot: String? = null,

    /**
     * Cast-Liste (nur VOD/Series).
     */
    val cast: String? = null,

    /**
     * Regisseur (nur VOD/Series).
     */
    val director: String? = null,

    /**
     * Genre (nur VOD/Series).
     */
    val genre: String? = null,

    /**
     * Release-Datum (nur VOD/Series).
     */
    val releaseDate: String? = null,

    /**
     * Dauer in Sekunden (nur VOD/Series).
     */
    val durationSecs: Int? = null,

    /**
     * Video-Informationen als JSON (Codec, Auflösung, etc.).
     */
    val videoInfoJson: String? = null,

    /**
     *Backdrop-Bilder als JSON-Array (nur VOD/Series).
     */
    val backdropPathJson: String? = null,

    /**
     * YouTube-Trailer URL (nur VOD/Series).
     */
    val youtubeTrailer: String? = null,

    /**
     * Serien-ID (nur für Series-Streams).
     * Verweist auf die übergeordnete Serie.
     */
    val seriesId: Long? = null,

    /**
     * Staffel-Nummer (nur für Series-Episoden).
     */
    val seasonNumber: Int? = null,

    /**
     * Episode-Nummer (nur für Series-Episoden).
     */
    val episodeNumber: Int? = null,

    /**
     * Episode-Titel (nur für Series-Episoden).
     */
    val episodeTitle: String? = null,

    /**
     * True wenn Stream vom Benutzer ausgewählt wurde.
     * Wird während Onboarding gesetzt.
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
     * Erstellt eine eindeutige Kombination aus providerId und streamId.
     */
    fun uniqueKey(): String {
        return "${providerId}_$streamId"
    }

    /**
     * Prüft ob es sich um einen Live-TV-Stream handelt.
     */
    fun isLive(): Boolean = streamType == StreamType.LIVE

    /**
     * Prüft ob es sich um einen VOD-Stream handelt.
     */
    fun isVod(): Boolean = streamType == StreamType.VOD

    /**
     * Prüft ob es sich um einen Serien-Stream handelt.
     */
    fun isSeries(): Boolean = streamType == StreamType.SERIES

    /**
     * Prüft ob Resume-Position verfügbar ist (nur VOD/Series).
     */
    fun hasResumePosition(): Boolean {
        return (streamType == StreamType.VOD || streamType == StreamType.SERIES) &&
                (durationSecs ?: 0) > 0
    }
}
