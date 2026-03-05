package com.djoudjou.iptv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object für EpgEventEntity.
 *
 * Bietet CRUD-Operationen für EPG-Events mit Flow-basierten Observables.
 */
@Dao
interface EpgEventDao {

    // ═══════════════════════════════════════════════════════════════════════════════
    // INSERT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Fügt ein neues EPG-Event hinzu.
     *
     * @param event Das hinzuzufügende Event
     * @return Die generierte Event-ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EpgEventEntity): Long

    /**
     * Fügt mehrere EPG-Events hinzu (Bulk-Insert).
     *
     * @param events Liste der Events
     * @return Liste der generierten IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EpgEventEntity>): List<Long>

    // ═══════════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Aktualisiert ein bestehendes EPG-Event.
     *
     * @param event Das zu aktualisierende Event
     */
    @Update
    suspend fun update(event: EpgEventEntity)

    // ═══════════════════════════════════════════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Löscht ein EPG-Event.
     *
     * @param event Das zu löschende Event
     */
    @Delete
    suspend fun delete(event: EpgEventEntity)

    /**
     * Löscht alle EPG-Events eines Streams.
     *
     * @param streamId Die Stream-ID
     */
    @Query("DELETE FROM epg_events WHERE streamId = :streamId")
    suspend fun deleteByStreamId(streamId: Long)

    /**
     * Löscht alle vergangenen EPG-Events.
     *
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Anzahl der gelöschten Events
     */
    @Query("DELETE FROM epg_events WHERE endTime < :currentTime")
    suspend fun deletePastEvents(currentTime: Long = System.currentTimeMillis()): Int

    /**
     * Löscht alle EPG-Events.
     */
    @Query("DELETE FROM epg_events")
    suspend fun deleteAll()

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (FLOW)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle EPG-Events eines Streams als Flow.
     *
     * @param streamId Die Stream-ID
     * @return Flow mit Liste aller Events
     */
    @Query("SELECT * FROM epg_events WHERE streamId = :streamId ORDER BY startTime ASC")
    fun getEventsByStreamId(streamId: Long): Flow<List<EpgEventEntity>>

    /**
     * Holt aktuelle und zukünftige EPG-Events eines Streams als Flow.
     *
     * @param streamId Die Stream-ID
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Flow mit Liste der Events
     */
    @Query("SELECT * FROM epg_events WHERE streamId = :streamId AND endTime >= :currentTime ORDER BY startTime ASC")
    fun getCurrentAndFutureEvents(streamId: Long, currentTime: Long = System.currentTimeMillis()): Flow<List<EpgEventEntity>>

    /**
     * Holt das aktuell laufende EPG-Event als Flow.
     *
     * @param streamId Die Stream-ID
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Flow mit dem aktuellen Event oder null
     */
    @Query("SELECT * FROM epg_events WHERE streamId = :streamId AND startTime <= :currentTime AND endTime > :currentTime LIMIT 1")
    fun getCurrentEvent(streamId: Long, currentTime: Long = System.currentTimeMillis()): Flow<EpgEventEntity?>

    /**
     * Holt das nächste EPG-Event als Flow.
     *
     * @param streamId Die Stream-ID
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Flow mit dem nächsten Event oder null
     */
    @Query("SELECT * FROM epg_events WHERE streamId = :streamId AND startTime > :currentTime ORDER BY startTime ASC LIMIT 1")
    fun getNextEvent(streamId: Long, currentTime: Long = System.currentTimeMillis()): Flow<EpgEventEntity?>

    /**
     * Holt EPG-Events eines bestimmten Zeitraums als Flow.
     *
     * @param startTime Startzeit in Millisekunden
     * @param endTime Endzeit in Millisekunden
     * @return Flow mit Liste der Events
     */
    @Query("SELECT * FROM epg_events WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime ASC")
    fun getEventsByTimeRange(startTime: Long, endTime: Long): Flow<List<EpgEventEntity>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (SUSPEND)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle EPG-Events eines Streams als Liste.
     *
     * @param streamId Die Stream-ID
     * @return Liste aller Events
     */
    @Query("SELECT * FROM epg_events WHERE streamId = :streamId ORDER BY startTime ASC")
    suspend fun getEventsByStreamIdSync(streamId: Long): List<EpgEventEntity>

    /**
     * Holt aktuelle und zukünftige Events als Liste.
     *
     * @param streamId Die Stream-ID
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Liste der Events
     */
    @Query("SELECT * FROM epg_events WHERE streamId = :streamId AND endTime >= :currentTime ORDER BY startTime ASC")
    suspend fun getCurrentAndFutureEventsSync(streamId: Long, currentTime: Long = System.currentTimeMillis()): List<EpgEventEntity>

    /**
     * Holt das aktuell laufende Event.
     *
     * @param streamId Die Stream-ID
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Aktuelles Event oder null
     */
    @Query("SELECT * FROM epg_events WHERE streamId = :streamId AND startTime <= :currentTime AND endTime > :currentTime LIMIT 1")
    suspend fun getCurrentEventSync(streamId: Long, currentTime: Long = System.currentTimeMillis()): EpgEventEntity?

    /**
     * Holt das nächste Event.
     *
     * @param streamId Die Stream-ID
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Nächstes Event oder null
     */
    @Query("SELECT * FROM epg_events WHERE streamId = :streamId AND startTime > :currentTime ORDER BY startTime ASC LIMIT 1")
    suspend fun getNextEventSync(streamId: Long, currentTime: Long = System.currentTimeMillis()): EpgEventEntity?

    /**
     * Holt ein Event nach ID.
     *
     * @param eventId Die Event-ID
     * @return Event oder null
     */
    @Query("SELECT * FROM epg_events WHERE id = :eventId")
    suspend fun getEventByIdSync(eventId: Long): EpgEventEntity?

    /**
     * Zählt alle EPG-Events eines Streams.
     *
     * @param streamId Die Stream-ID
     * @return Anzahl der Events
     */
    @Query("SELECT COUNT(*) FROM epg_events WHERE streamId = :streamId")
    suspend fun getEventCount(streamId: Long): Int

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt die aktuelle Programm-Übersicht für mehrere Streams.
     *
     * @param streamIds Liste der Stream-IDs
     * @param currentTime Aktuelle Zeit in Millisekunden
     * @return Map von Stream-ID zu aktuellem Event
     */
    @Transaction
    suspend fun getCurrentProgramForStreams(
        streamIds: List<Long>,
        currentTime: Long = System.currentTimeMillis()
    ): Map<Long, EpgEventEntity?> {
        return streamIds.associateWith { streamId ->
            getCurrentEventSync(streamId, currentTime)
        }
    }
}
