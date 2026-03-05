package com.djoudjou.iptv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object für ProviderEntity.
 *
 * Bietet CRUD-Operationen für Provider mit Flow-basierten Observables.
 */
@Dao
interface ProviderDao {

    // ═══════════════════════════════════════════════════════════════════════════════
    // INSERT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Fügt einen neuen Provider hinzu.
     *
     * @param provider Der hinzuzufügende Provider
     * @return Die generierte Provider-ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: ProviderEntity): Long

    /**
     * Fügt mehrere Provider hinzu (Bulk-Insert).
     *
     * @param providers Liste der Provider
     * @return Liste der generierten IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<ProviderEntity>): List<Long>

    // ═══════════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Aktualisiert einen bestehenden Provider.
     *
     * @param provider Der zu aktualisierende Provider
     */
    @Update
    suspend fun update(provider: ProviderEntity)

    /**
     * Aktualisiert mehrere Provider (Bulk-Update).
     *
     * @param providers Liste der zu aktualisierenden Provider
     */
    @Update
    suspend fun updateAll(providers: List<ProviderEntity>)

    // ═══════════════════════════════════════════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Löscht einen Provider.
     *
     * @param provider Der zu löschende Provider
     */
    @Delete
    suspend fun delete(provider: ProviderEntity)

    /**
     * Löscht einen Provider nach ID.
     *
     * @param providerId Die ID des zu löschenden Providers
     */
    @Query("DELETE FROM providers WHERE id = :providerId")
    suspend fun deleteById(providerId: Long)

    /**
     * Löscht alle Provider (inkl. zugehöriger Categories und Streams via CASCADE).
     */
    @Query("DELETE FROM providers")
    suspend fun deleteAll()

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (FLOW)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle Provider als Flow.
     *
     * @return Flow mit Liste aller Provider
     */
    @Query("SELECT * FROM providers ORDER BY createdAt DESC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    /**
     * Holt einen einzelnen Provider nach ID als Flow.
     *
     * @param providerId Die Provider-ID
     * @return Flow mit dem Provider oder null
     */
    @Query("SELECT * FROM providers WHERE id = :providerId")
    fun getProviderById(providerId: Long): Flow<ProviderEntity?>

    /**
     * Holt alle aktiven Provider als Flow.
     *
     * @return Flow mit Liste aller aktiven Provider
     */
    @Query("SELECT * FROM providers WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveProviders(): Flow<List<ProviderEntity>>

    /**
     * Holt den ersten aktiven Provider als Flow.
     *
     * @return Flow mit dem ersten aktiven Provider oder null
     */
    @Query("SELECT * FROM providers WHERE isActive = 1 LIMIT 1")
    fun getFirstActiveProvider(): Flow<ProviderEntity?>

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (SUSPEND)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle Provider als Liste.
     *
     * @return Liste aller Provider
     */
    @Query("SELECT * FROM providers ORDER BY createdAt DESC")
    suspend fun getAllProvidersList(): List<ProviderEntity>

    /**
     * Holt einen Provider nach ID.
     *
     * @param providerId Die Provider-ID
     * @return Provider oder null
     */
    @Query("SELECT * FROM providers WHERE id = :providerId")
    suspend fun getProviderByIdSync(providerId: Long): ProviderEntity?

    /**
     * Zählt alle Provider.
     *
     * @return Anzahl der Provider
     */
    @Query("SELECT COUNT(*) FROM providers")
    suspend fun getProviderCount(): Int

    /**
     * Prüft ob ein Provider mit den gegebenen Credentials existiert.
     *
     * @param serverUrl Server-URL
     * @param username Benutzername
     * @return true wenn Provider existiert
     */
    @Query("SELECT EXISTS(SELECT 1 FROM providers WHERE serverUrl = :serverUrl AND username = :username LIMIT 1)")
    suspend fun existsByCredentials(serverUrl: String, username: String): Boolean

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Setzt den isActive-Status eines Providers.
     *
     * @param providerId Die Provider-ID
     * @param isActive Der neue Status
     */
    @Query("UPDATE providers SET isActive = :isActive, updatedAt = :timestamp WHERE id = :providerId")
    suspend fun setIsActive(providerId: Long, isActive: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Aktualisiert die ServerInfo eines Providers.
     *
     * @param providerId Die Provider-ID
     * @param serverInfoJson JSON-String mit ServerInfo
     */
    @Query("UPDATE providers SET serverInfoJson = :serverInfoJson, updatedAt = :timestamp WHERE id = :providerId")
    suspend fun updateServerInfo(providerId: Long, serverInfoJson: String?, timestamp: Long = System.currentTimeMillis())
}
