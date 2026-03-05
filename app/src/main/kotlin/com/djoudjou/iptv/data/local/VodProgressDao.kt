package com.djoudjou.iptv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object für VodProgressEntity.
 *
 * Bietet CRUD-Operationen für VOD-Progress mit Flow-basierten Observables.
 */
@Dao
interface VodProgressDao {

    // ═══════════════════════════════════════════════════════════════════════════════
    // INSERT / UPSERT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Fügt einen neuen Progress-Eintrag hinzu oder aktualisiert bestehenden.
     *
     * @param progress Der Progress-Eintrag
     * @return Die generierte/aktualisierte ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: VodProgressEntity): Long

    /**
     * Fügt mehrere Progress-Einträge hinzu (Bulk-Insert).
     *
     * @param progresses Liste der Progress-Einträge
     * @return Liste der generierten IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progresses: List<VodProgressEntity>): List<Long>

    // ═══════════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Aktualisiert einen bestehenden Progress-Eintrag.
     *
     * @param progress Der zu aktualisierende Eintrag
     */
    @Update
    suspend fun update(progress: VodProgressEntity)

    /**
     * Aktualisiert die Position eines Streams.
     *
     * @param streamId Die Stream-ID
     * @param positionMs Neue Position in Millisekunden
     * @param durationMs Gesamtdauer in Millisekunden
     */
    @Transaction
    suspend fun updatePosition(streamId: Long, positionMs: Long, durationMs: Long) {
        val existing = getByStreamIdSync(streamId)
        if (existing != null) {
            update(existing.withPosition(positionMs, durationMs))
        } else {
            insert(
                VodProgressEntity(
                    streamId = streamId,
                    positionMs = positionMs,
                    durationMs = durationMs
                )
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Löscht einen Progress-Eintrag.
     *
     * @param progress Der zu löschende Eintrag
     */
    @Delete
    suspend fun delete(progress: VodProgressEntity)

    /**
     * Löscht Progress nach Stream-ID.
     *
     * @param streamId Die Stream-ID
     */
    @Query("DELETE FROM vod_progress WHERE streamId = :streamId")
    suspend fun deleteByStreamId(streamId: Long)

    /**
     * Löscht alle abgeschlossenen Einträge (>95% angesehen).
     *
     * @return Anzahl der gelöschten Einträge
     */
    @Query("DELETE FROM vod_progress WHERE isWatched = 1")
    suspend fun deleteWatched(): Int

    /**
     * Löscht alle Progress-Einträge.
     */
    @Query("DELETE FROM vod_progress")
    suspend fun deleteAll()

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (FLOW)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt Progress für einen Stream als Flow.
     *
     * @param streamId Die Stream-ID
     * @return Flow mit Progress oder null
     */
    @Query("SELECT * FROM vod_progress WHERE streamId = :streamId")
    fun getProgressByStreamId(streamId: Long): Flow<VodProgressEntity?>

    /**
     * Holt alle laufenden (in-progress) Videos als Flow.
     *
     * @return Flow mit Liste der laufenden Videos
     */
    @Query("SELECT * FROM vod_progress WHERE isInProgress = 1 ORDER BY lastUpdated DESC")
    fun getInProgressVideos(): Flow<List<VodProgressEntity>>

    /**
     * Holt alle abgeschlossenen Videos als Flow.
     *
     * @return Flow mit Liste der abgeschlossenen Videos
     */
    @Query("SELECT * FROM vod_progress WHERE isWatched = 1 ORDER BY lastUpdated DESC")
    fun getWatchedVideos(): Flow<List<VodProgressEntity>>

    /**
     * Holt alle Progress-Einträge als Flow.
     *
     * @return Flow mit Liste aller Einträge
     */
    @Query("SELECT * FROM vod_progress ORDER BY lastUpdated DESC")
    fun getAllProgress(): Flow<List<VodProgressEntity>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (SUSPEND)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt Progress für einen Stream.
     *
     * @param streamId Die Stream-ID
     * @return Progress oder null
     */
    @Query("SELECT * FROM vod_progress WHERE streamId = :streamId")
    suspend fun getByStreamIdSync(streamId: Long): VodProgressEntity?

    /**
     * Holt Progress für mehrere Streams.
     *
     * @param streamIds Liste der Stream-IDs
     * @return Map von Stream-ID zu Progress
     */
    @Transaction
    suspend fun getProgressForStreams(streamIds: List<Long>): Map<Long, VodProgressEntity?> {
        return streamIds.associateWith { streamId ->
            getByStreamIdSync(streamId)
        }
    }

    /**
     * Holt alle laufenden Videos als Liste.
     *
     * @return Liste der laufenden Videos
     */
    @Query("SELECT * FROM vod_progress WHERE isInProgress = 1 ORDER BY lastUpdated DESC")
    suspend fun getInProgressVideosSync(): List<VodProgressEntity>

    /**
     * Holt alle abgeschlossenen Videos als Liste.
     *
     * @return Liste der abgeschlossenen Videos
     */
    @Query("SELECT * FROM vod_progress WHERE isWatched = 1 ORDER BY lastUpdated DESC")
    suspend fun getWatchedVideosSync(): List<VodProgressEntity>

    /**
     * Zählt alle Progress-Einträge.
     *
     * @return Anzahl der Einträge
     */
    @Query("SELECT COUNT(*) FROM vod_progress")
    suspend fun getProgressCount(): Int

    /**
     * Zählt alle laufenden Videos.
     *
     * @return Anzahl der laufenden Videos
     */
    @Query("SELECT COUNT(*) FROM vod_progress WHERE isInProgress = 1")
    suspend fun getInProgressCount(): Int

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Markiert ein Video als angesehen.
     *
     * @param streamId Die Stream-ID
     */
    @Query("UPDATE vod_progress SET isWatched = 1, isInProgress = 0, progressPercent = 1.0, lastUpdated = :timestamp WHERE streamId = :streamId")
    suspend fun markAsWatched(streamId: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Setzt den Progress zurück.
     *
     * @param streamId Die Stream-ID
     */
    @Query("UPDATE vod_progress SET positionMs = 0, progressPercent = 0, isWatched = 0, isInProgress = 0, lastUpdated = :timestamp WHERE streamId = :streamId")
    suspend fun resetProgress(streamId: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Holt die Resume-Position für einen Stream.
     *
     * @param streamId Die Stream-ID
     * @return Resume-Position in Millisekunden oder 0
     */
    suspend fun getResumePosition(streamId: Long): Long {
        val progress = getByStreamIdSync(streamId)
        return progress?.takeIf { it.hasResumePosition() }?.positionMs ?: 0L
    }
}
