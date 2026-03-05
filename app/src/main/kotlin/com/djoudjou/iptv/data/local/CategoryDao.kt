package com.djoudjou.iptv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object für CategoryEntity.
 *
 * Bietet CRUD-Operationen für Kategorien mit Flow-basierten Observables.
 */
@Dao
interface CategoryDao {

    // ═══════════════════════════════════════════════════════════════════════════════
    // INSERT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Fügt eine neue Kategorie hinzu.
     *
     * @param category Die hinzuzufügende Kategorie
     * @return Die generierte Kategorie-ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    /**
     * Fügt mehrere Kategorien hinzu (Bulk-Insert).
     *
     * @param categories Liste der Kategorien
     * @return Liste der generierten IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    // ═══════════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Aktualisiert eine bestehende Kategorie.
     *
     * @param category Die zu aktualisierende Kategorie
     */
    @Update
    suspend fun update(category: CategoryEntity)

    /**
     * Aktualisiert mehrere Kategorien (Bulk-Update).
     *
     * @param categories Liste der zu aktualisierenden Kategorien
     */
    @Update
    suspend fun updateAll(categories: List<CategoryEntity>)

    // ═══════════════════════════════════════════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Löscht eine Kategorie.
     *
     * @param category Die zu löschende Kategorie
     */
    @Delete
    suspend fun delete(category: CategoryEntity)

    /**
     * Löscht eine Kategorie nach ID.
     *
     * @param categoryId Die Kategorie-ID
     */
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteById(categoryId: Long)

    /**
     * Löscht alle Kategorien eines Providers.
     *
     * @param providerId Die Provider-ID
     */
    @Query("DELETE FROM categories WHERE providerId = :providerId")
    suspend fun deleteByProviderId(providerId: Long)

    /**
     * Löscht alle Kategorien.
     */
    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (FLOW)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle Kategorien eines Providers als Flow.
     *
     * @param providerId Die Provider-ID
     * @return Flow mit Liste aller Kategorien
     */
    @Query("SELECT * FROM categories WHERE providerId = :providerId ORDER BY name ASC")
    fun getCategoriesByProviderId(providerId: Long): Flow<List<CategoryEntity>>

    /**
     * Holt alle Kategorien eines Providers nach Stream-Typ als Flow.
     *
     * @param providerId Die Provider-ID
     * @param streamType Der Stream-Typ
     * @return Flow mit Liste der Kategorien
     */
    @Query("SELECT * FROM categories WHERE providerId = :providerId AND streamType = :streamType ORDER BY name ASC")
    fun getCategoriesByProviderAndType(providerId: Long, streamType: StreamType): Flow<List<CategoryEntity>>

    /**
     * Holt nur ausgewählte Kategorien eines Providers als Flow.
     *
     * @param providerId Die Provider-ID
     * @return Flow mit Liste der ausgewählten Kategorien
     */
    @Query("SELECT * FROM categories WHERE providerId = :providerId AND isSelected = 1 ORDER BY name ASC")
    fun getSelectedCategories(providerId: Long): Flow<List<CategoryEntity>>

    /**
     * Holt eine einzelne Kategorie nach ID als Flow.
     *
     * @param categoryId Die Kategorie-ID
     * @return Flow mit der Kategorie oder null
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>

    /**
     * Holt Adult-Kategorien als Flow.
     *
     * @param providerId Die Provider-ID
     * @return Flow mit Liste der Adult-Kategorien
     */
    @Query("SELECT * FROM categories WHERE providerId = :providerId AND isAdult = 1 ORDER BY name ASC")
    fun getAdultCategories(providerId: Long): Flow<List<CategoryEntity>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUERY (SUSPEND)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle Kategorien eines Providers als Liste.
     *
     * @param providerId Die Provider-ID
     * @return Liste aller Kategorien
     */
    @Query("SELECT * FROM categories WHERE providerId = :providerId ORDER BY name ASC")
    suspend fun getCategoriesByProviderIdSync(providerId: Long): List<CategoryEntity>

    /**
     * Holt Kategorien nach Stream-Typ als Liste.
     *
     * @param providerId Die Provider-ID
     * @param streamType Der Stream-Typ
     * @return Liste der Kategorien
     */
    @Query("SELECT * FROM categories WHERE providerId = :providerId AND streamType = :streamType ORDER BY name ASC")
    suspend fun getCategoriesByTypeSync(providerId: Long, streamType: StreamType): List<CategoryEntity>

    /**
     * Holt eine Kategorie nach ID.
     *
     * @param categoryId Die Kategorie-ID
     * @return Kategorie oder null
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryByIdSync(categoryId: Long): CategoryEntity?

    /**
     * Holt eine Kategorie nach externer ID und Provider.
     *
     * @param providerId Die Provider-ID
     * @param categoryId Die externe Kategorie-ID
     * @return Kategorie oder null
     */
    @Query("SELECT * FROM categories WHERE providerId = :providerId AND categoryId = :categoryId LIMIT 1")
    suspend fun getCategoryByExternalId(providerId: Long, categoryId: String): CategoryEntity?

    /**
     * Zählt alle Kategorien eines Providers.
     *
     * @param providerId Die Provider-ID
     * @return Anzahl der Kategorien
     */
    @Query("SELECT COUNT(*) FROM categories WHERE providerId = :providerId")
    suspend fun getCategoryCount(providerId: Long): Int

    /**
     * Zählt ausgewählte Kategorien eines Providers.
     *
     * @param providerId Die Provider-ID
     * @return Anzahl der ausgewählten Kategorien
     */
    @Query("SELECT COUNT(*) FROM categories WHERE providerId = :providerId AND isSelected = 1")
    suspend fun getSelectedCategoryCount(providerId: Long): Int

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Setzt isSelected-Status für eine Kategorie.
     *
     * @param categoryId Die Kategorie-ID
     * @param isSelected Der neue Status
     */
    @Query("UPDATE categories SET isSelected = :isSelected, updatedAt = :timestamp WHERE id = :categoryId")
    suspend fun setIsSelected(categoryId: Long, isSelected: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Setzt isSelected-Status für alle Kategorien eines Providers.
     *
     * @param providerId Die Provider-ID
     * @param isSelected Der neue Status
     */
    @Query("UPDATE categories SET isSelected = :isSelected, updatedAt = :timestamp WHERE providerId = :providerId")
    suspend fun setAllSelected(providerId: Long, isSelected: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Kehrt isSelected-Status für alle Kategorien um.
     *
     * @param providerId Die Provider-ID
     */
    @Query("UPDATE categories SET isSelected = NOT isSelected, updatedAt = :timestamp WHERE providerId = :providerId")
    suspend fun invertAllSelection(providerId: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Aktualisiert den Stream-Count einer Kategorie.
     *
     * @param categoryId Die Kategorie-ID
     * @param count Die neue Anzahl
     */
    @Query("UPDATE categories SET streamCount = :count, updatedAt = :timestamp WHERE id = :categoryId")
    suspend fun updateStreamCount(categoryId: Long, count: Int, timestamp: Long = System.currentTimeMillis())
}
