package com.djoudjou.iptv.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * VOD Progress Entity - Speichert die Wiedergabeposition von VOD/Serien.
 *
 * Ermöglicht "Resume-Position" Feature:
 * - Beim nächsten Start wird an der letzten Position fortgesetzt
 * - Wird alle 10 Sekunden aktualisiert (debounced)
 * - Pro Stream (VOD oder Serie) ein Eintrag
 */
@Entity(
    tableName = "vod_progress",
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
        Index(value = ["streamId"], unique = true)
    ]
)
data class VodProgressEntity(
    /**
     * Eindeutige Progress-ID (auto-generiert).
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Foreign Key zum Stream.
     * Unique Constraint: Nur ein Progress-Eintrag pro Stream.
     */
    val streamId: Long,

    /**
     * Aktuelle Wiedergabeposition in Millisekunden.
     */
    val positionMs: Long = 0,

    /**
     * Gesamtdauer des Videos in Millisekunden.
     */
    val durationMs: Long = 0,

    /**
     * Fortschritt in Prozent (0.0 bis 100.0).
     * Wird berechnet aus positionMs / durationMs.
     */
    val progressPercent: Float = 0f,

    /**
     * True wenn Video vollständig angesehen wurde (>95%).
     */
    val isWatched: Boolean = false,

    /**
     * True wenn Video gerade angesehen wird (aktuelle Session).
     */
    val isInProgress: Boolean = false,

    /**
     * Zeitpunkt des letzten Updates (Unix Timestamp in Millisekunden).
     */
    val lastUpdated: Long = System.currentTimeMillis(),

    /**
     * Zeitpunkt der Erstellung (Unix Timestamp in Millisekunden).
     */
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Schwelle für "vollständig angesehen" (95%).
         */
        const val WATCHED_THRESHOLD = 0.95f

        /**
         * Minimale Position für Resume (5 Sekunden).
         * Videos die weniger als 5s angesehen wurden, werden nicht gespeichert.
         */
        const val MIN_RESUME_POSITION_MS = 5000L
    }

    /**
     * Berechnet den Fortschritt in Prozent.
     */
    fun calculateProgressPercent(): Float {
        if (durationMs <= 0) return 0f
        return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Prüft ob Resume-Position verfügbar ist.
     *
     * @return true wenn Position > 5 Sekunden und < 95%
     */
    fun hasResumePosition(): Boolean {
        return positionMs > MIN_RESUME_POSITION_MS && progressPercent < WATCHED_THRESHOLD
    }

    /**
     * Formatiert die Position als String (z.B. "1h 30m 45s").
     *
     * @return Formatierter Zeit-String
     */
    fun getFormattedPosition(): String {
        return formatDuration(positionMs)
    }

    /**
     * Formatiert die Gesamtdauer als String.
     *
     * @return Formatierter Zeit-String
     */
    fun getFormattedDuration(): String {
        return formatDuration(durationMs)
    }

    /**
     * Formatiert die verbleibende Zeit als String.
     *
     * @return Formatierter Zeit-String
     */
    fun getFormattedRemaining(): String {
        val remaining = durationMs - positionMs
        return formatDuration(remaining)
    }

    /**
     * Hilfsfunktion zur Formatierung von Millisekunden.
     */
    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    /**
     * Erstellt eine aktualisierte Kopie mit neuer Position.
     *
     * @param newPositionMs Neue Position in Millisekunden
     * @param durationMs Gesamtdauer in Millisekunden
     * @return Aktualisierte VodProgressEntity
     */
    fun withPosition(newPositionMs: Long, durationMs: Long): VodProgressEntity {
        val newProgress = if (durationMs > 0) {
            (newPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        return copy(
            positionMs = newPositionMs,
            durationMs = durationMs,
            progressPercent = newProgress,
            isWatched = newProgress >= WATCHED_THRESHOLD,
            isInProgress = newProgress > 0f && newProgress < WATCHED_THRESHOLD,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
