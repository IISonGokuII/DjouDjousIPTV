package com.djoudjou.iptv.data.local

import androidx.room.*
import com.djoudjou.iptv.data.preferences.ProviderType

/**
 * App Database - Room Database für DjouDjousIPTV.
 *
 * Enthält alle Entities für:
 * - Provider (Multi-Provider-Support)
 * - Categories (Live TV, VOD, Serien)
 * - Streams (Live, VOD, Series)
 * - EPG Events (Electronic Program Guide)
 * - VOD Progress (Resume-Position)
 *
 * DATABASE VERSION: 1
 * - Initiale Version mit allen Tabellen
 *
 * MIGRATION STRATEGY:
 * - Bei Schema-Änderungen: Migration erstellen
 * - Bei destruktiven Änderungen: destructiveMigration
 */
@Database(
    entities = [
        ProviderEntity::class,
        CategoryEntity::class,
        StreamEntity::class,
        EpgEventEntity::class,
        VodProgressEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Data Access Objects für den Datenbankzugriff.
     */
    abstract fun providerDao(): ProviderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun streamDao(): StreamDao
    abstract fun epgEventDao(): EpgEventDao
    abstract fun vodProgressDao(): VodProgressDao

    companion object {
        const val DATABASE_NAME = "djoudjou_iptv_database"
    }
}

/**
 * Type Converters für Room.
 *
 * Wandelt komplexe Typen in database-kompatible Formate um.
 */
class Converters {

    /**
     * Wandelt ProviderType Enum in String um.
     */
    @TypeConverter
    fun fromProviderType(type: ProviderType): String {
        return type.name
    }

    /**
     * Wandelt String in ProviderType Enum um.
     */
    @TypeConverter
    fun toProviderType(value: String): ProviderType {
        return ProviderType.valueOf(value)
    }

    /**
     * Wandelt StreamType Enum in String um.
     */
    @TypeConverter
    fun fromStreamType(type: StreamType): String {
        return type.name
    }

    /**
     * Wandelt String in StreamType Enum um.
     */
    @TypeConverter
    fun toStreamType(value: String): StreamType {
        return StreamType.valueOf(value)
    }

    /**
     * Wandelt List<String> in JSON-String um.
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(separator = "|")
    }

    /**
     * Wandelt JSON-String in List<String> um.
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split("|")?.filter { it.isNotBlank() }
    }
}

/**
 * Datenbank-Migrationen.
 *
 * Wird verwendet um Schema-Änderungen zwischen Versionen zu handhaben.
 */
object DatabaseMigrations {

    /**
     * Migration von Version 1 zu Version 2.
     *
     * Beispiel für zukünftige Migrationen:
     * ```
     * val MIGRATION_1_2 = object : Migration(1, 2) {
     *     override fun migrate(database: SupportSQLiteDatabase) {
     *         // Neue Spalte hinzufügen
     *         database.execSQL("ALTER TABLE streams ADD COLUMN newColumn TEXT DEFAULT NULL")
     *     }
     * }
     * ```
     */
    val ALL_MIGRATIONS = arrayOf<Migration>(
        // Migrationen werden hier hinzugefügt wenn Version > 1
    )
}
