package com.djoudjou.iptv.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * EPG (Electronic Program Guide) Event Entity.
 *
 * Repräsentiert eine TV-Sendung/ein Programm in der EPG-Timeline.
 * Jedes EPG-Event gehört zu einem Live-TV-Stream.
 *
 * EPG-Daten werden im Hintergrund via WorkManager aktualisiert.
 */
@Entity(
    tableName = "epg_events",
    foreignKeys = [
        ForeignKey(
            entity = StreamEntity::class,
            parentColumns = ["id"],
            childColumns = ["streamId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["streamId"]),
        Index(value = ["streamId", "startTime"]),
        Index(value = ["epgId"]),
        Index(value = ["startTime", "endTime"])
    ]
)
data class EpgEventEntity(
    /**
     * Eindeutige EPG-Event-ID (auto-generiert).
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Externe EPG-ID vom Provider.
     */
    val epgId: String,

    /**
     * Titel der Sendung.
     */
    val title: String,

    /**
     * Beschreibung der Sendung (kann leer sein).
     */
    val description: String? = null,

    /**
     * Kategorie der Sendung (z.B. "News", "Sports", "Movie").
     */
    val category: String? = null,

    /**
     * Sprache der Sendung.
     */
    val language: String? = null,

    /**
     * Foreign Key zum Stream.
     */
    val streamId: Long,

    /**
     * Startzeit als Unix-Timestamp in Millisekunden.
     */
    val startTime: Long,

    /**
     * Endzeit als Unix-Timestamp in Millisekunden.
     */
    val endTime: Long,

    /**
     * Dauer in Sekunden (berechnet aus startTime und endTime).
     */
    val durationSecs: Int = ((endTime - startTime) / 1000).toInt(),

    /**
     * True wenn es sich um eine Neuaustrahlung handelt.
     */
    val isNew: Boolean = false,

    /**
     * True wenn es sich um eine Serien-Episode handelt.
     */
    val isSeries: Boolean = false,

    /**
     * Staffel-Nummer (nur für Serien).
     */
    val seasonNumber: Int? = null,

    /**
     * Episode-Nummer (nur für Serien).
     */
    val episodeNumber: Int? = null,

    /**
     * Icon/Cover URL.
     */
    val iconUrl: String? = null,

    /**
     * Rating der Sendung.
     */
    val rating: String? = null,

    /**
     * Zeitpunkt der Erstellung (Unix Timestamp in Millisekunden).
     */
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Prüft ob das Event gerade läuft (live ist).
     *
     * @param currentTime Aktuelle Zeit in Millisekunden (default: System.currentTimeMillis())
     * @return true wenn Event gerade läuft
     */
    fun isLive(currentTime: Long = System.currentTimeMillis()): Boolean {
        return currentTime in startTime until endTime
    }

    /**
     * Prüft ob das Event in der Zukunft liegt.
     *
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return true wenn Event in der Zukunft liegt
     */
    fun isFuture(currentTime: Long = System.currentTimeMillis()): Boolean {
        return startTime > currentTime
    }

    /**
     * Prüft ob das Event in der Vergangenheit liegt.
     *
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return true wenn Event in der Vergangenheit liegt
     */
    fun isPast(currentTime: Long = System.currentTimeMillis()): Boolean {
        return endTime <= currentTime
    }

    /**
     * Berechnet den Fortschritt in Prozent (0-100).
     *
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Fortschritt in Prozent (0.0 bis 100.0)
     */
    fun getProgressPercent(currentTime: Long = System.currentTimeMillis()): Float {
        if (currentTime < startTime) return 0f
        if (currentTime > endTime) return 100f

        val totalDuration = endTime - startTime
        val elapsed = currentTime - startTime

        return (elapsed.toFloat() / totalDuration.toFloat()) * 100f
    }

    /**
     * Formatiert die Startzeit als String (HH:mm).
     *
     * @return Formatierter Zeit-String
     */
    fun getFormattedStartTime(): String {
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return format.format(java.util.Date(startTime))
    }

    /**
     * Formatiert die Endzeit als String (HH:mm).
     *
     * @return Formatierter Zeit-String
     */
    fun getFormattedEndTime(): String {
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return format.format(java.util.Date(endTime))
    }

    /**
     * Formatiert die Dauer als String (z.B. "1h 30m").
     *
     * @return Formatierter Dauer-String
     */
    fun getFormattedDuration(): String {
        val hours = durationSecs / 3600
        val minutes = (durationSecs % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${durationSecs}s"
        }
    }
}
