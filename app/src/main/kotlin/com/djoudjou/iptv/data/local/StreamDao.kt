package com.djoudjou.iptv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object für StreamEntity.
 *
 * Bietet CRUD-Operationen für Streams mit Flow-basierten Observables.
 */
@Dao
interface StreamDao {

    // ═══════════════════════════════════════════════════════════════════════════════
    // INSERT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Fügt einen neuen Stream hinzu.
     *
     * @param stream Der hinzuzufügende Stream
     * @return Die generierte Stream-ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stream: StreamEntity): Long

    /**
     * Fügt mehrere Streams hinzu (Bulk-Insert).
     *
     * @param streams Liste der Streams
     * @return Liste der generierten IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(streams: List<StreamEntity>): List<Long>

    // ═══════════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Aktualisiert einen bestehenden Stream.
     *
     * @param stream Der zu aktualisierende Stream
     */
    @Update
    suspend fun update(stream: StreamEntity)

    /**
     * Aktualisiert mehrere Streams (Bulk-Update).
     *
     * @param streams Liste der zu aktualisierenden Streams
     */
    @Update
    suspend fun updateAll(streams: List<StreamEntity>)

    // ═══════════════════════════════════════════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Löscht einen Stream.
     *
     * @param stream Der zu löschende Stream
     */
    @Delete
    suspend fun delete(stream: StreamEntity)

    /**
     * Löscht einen Stream nach ID.
     *
     * @param streamId Die Stream-ID
     */
    @Query("DELETE FROM streams WHERE id = :streamId")
    suspend fun deleteById(streamId: Long)

    /**
     * Löscht alle Streams eines Providers.
     *
     * @param providerId Die Provider-ID
     */
    @Query("DELETE FROM streams WHERE providerId = :providerId")
    suspend fun deleteByProviderId(providerId: Long)

    /**
     * Löscht alle Streams einer Kategorie.
     *
     * @param categoryId Die Kategorie-ID
     */
    @Query("DELETE FROM streams WHERE categoryId = :categoryId")
    suspend fun deleteByCategoryId(categoryId: Long)

    /**
     * Löscht alle Streams.
     */
    @Query("DELETE FROM streams")
    suspend fun deleteAll()

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (FLOW)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle Streams eines Providers als Flow.
     *
     * @param providerId Die Provider-ID
     * @return Flow mit Liste aller Streams
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId ORDER BY name ASC")
    fun getStreamsByProviderId(providerId: Long): Flow<List<StreamEntity>>

    /**
     * Holt alle Streams einer Kategorie als Flow.
     *
     * @param categoryId Die Kategorie-ID
     * @return Flow mit Liste aller Streams
     */
    @Query("SELECT * FROM streams WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getStreamsByCategoryId(categoryId: Long): Flow<List<StreamEntity>>

    /**
     * Holt Streams nach Provider und Typ als Flow.
     *
     * @param providerId Die Provider-ID
     * @param streamType Der Stream-Typ
     * @return Flow mit Liste der Streams
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId AND streamType = :streamType ORDER BY name ASC")
    fun getStreamsByProviderAndType(providerId: Long, streamType: StreamType): Flow<List<StreamEntity>>

    /**
     * Holt Streams nach Provider und Kategorie als Flow.
     *
     * @param providerId Die Provider-ID
     * @param categoryId Die Kategorie-ID
     * @return Flow mit Liste der Streams
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId AND categoryId = :categoryId ORDER BY name ASC")
    fun getStreamsByProviderAndCategory(providerId: Long, categoryId: Long): Flow<List<StreamEntity>>

    /**
     * Holt einen einzelnen Stream nach ID als Flow.
     *
     * @param streamId Die Stream-ID
     * @return Flow mit dem Stream oder null
     */
    @Query("SELECT * FROM streams WHERE id = :streamId")
    fun getStreamById(streamId: Long): Flow<StreamEntity?>

    /**
     * Holt Favoriten als Flow.
     *
     * @param providerId Die Provider-ID
     * @return Flow mit Liste der Favoriten
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId AND isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(providerId: Long): Flow<List<StreamEntity>>

    /**
     * Holt Adult-Streams als Flow.
     *
     * @param providerId Die Provider-ID
     * @return Flow mit Liste der Adult-Streams
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId AND isAdult = 1 ORDER BY name ASC")
    fun getAdultStreams(providerId: Long): Flow<List<StreamEntity>>

    /**
     * Holt Streams nach Suchbegriff als Flow.
     *
     * @param providerId Die Provider-ID
     * @param query Der Suchbegriff
     * @return Flow mit Liste der passenden Streams
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchStreams(providerId: Long, query: String): Flow<List<StreamEntity>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (SUSPEND)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle Streams eines Providers als Liste.
     *
     * @param providerId Die Provider-ID
     * @return Liste aller Streams
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId ORDER BY name ASC")
    suspend fun getStreamsByProviderIdSync(providerId: Long): List<StreamEntity>

    /**
     * Holt Streams nach Typ als Liste.
     *
     * @param providerId Die Provider-ID
     * @param streamType Der Stream-Typ
     * @return Liste der Streams
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId AND streamType = :streamType ORDER BY name ASC")
    suspend fun getStreamsByTypeSync(providerId: Long, streamType: StreamType): List<StreamEntity>

    /**
     * Holt Streams nach Kategorie als Liste.
     *
     * @param categoryId Die Kategorie-ID
     * @return Liste der Streams
     */
    @Query("SELECT * FROM streams WHERE categoryId = :categoryId ORDER BY name ASC")
    suspend fun getStreamsByCategoryIdSync(categoryId: Long): List<StreamEntity>

    /**
     * Holt einen Stream nach ID.
     *
     * @param streamId Die Stream-ID
     * @return Stream oder null
     */
    @Query("SELECT * FROM streams WHERE id = :streamId")
    suspend fun getStreamByIdSync(streamId: Long): StreamEntity?

    /**
     * Holt einen Stream nach externer ID und Provider.
     *
     * @param providerId Die Provider-ID
     * @param streamId Die externe Stream-ID
     * @return Stream oder null
     */
    @Query("SELECT * FROM streams WHERE providerId = :providerId AND streamId = :streamId LIMIT 1")
    suspend fun getStreamByExternalId(providerId: Long, streamId: String): StreamEntity?

    /**
     * Zählt alle Streams eines Providers.
     *
     * @param providerId Die Provider-ID
     * @return Anzahl der Streams
     */
    @Query("SELECT COUNT(*) FROM streams WHERE providerId = :providerId")
    suspend fun getStreamCount(providerId: Long): Int

    /**
     * Zählt Streams nach Typ.
     *
     * @param providerId Die Provider-ID
     * @param streamType Der Stream-Typ
     * @return Anzahl der Streams
     */
    @Query("SELECT COUNT(*) FROM streams WHERE providerId = :providerId AND streamType = :streamType")
    suspend fun getStreamCountByType(providerId: Long, streamType: StreamType): Int

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Setzt isFavorite-Status.
     *
     * @param streamId Die Stream-ID
     * @param isFavorite Der neue Status
     */
    @Query("UPDATE streams SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :streamId")
    suspend fun setIsFavorite(streamId: Long, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Setzt isSelected-Status.
     *
     * @param streamId Die Stream-ID
     * @param isSelected Der neue Status
     */
    @Query("UPDATE streams SET isSelected = :isSelected, updatedAt = :timestamp WHERE id = :streamId")
    suspend fun setIsSelected(streamId: Long, isSelected: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Aktualisiert die Stream-URL.
     *
     * @param streamId Die Stream-ID
     * @param streamUrl Die neue URL
     */
    @Query("UPDATE streams SET streamUrl = :streamUrl, updatedAt = :timestamp WHERE id = :streamId")
    suspend fun updateStreamUrl(streamId: Long, streamUrl: String?, timestamp: Long = System.currentTimeMillis())
}
