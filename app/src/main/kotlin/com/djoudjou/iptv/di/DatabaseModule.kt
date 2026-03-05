package com.djoudjou.iptv.di

import android.content.Context
import androidx.room.Room
import com.djoudjou.iptv.data.local.AppDatabase
import com.djoudjou.iptv.data.local.CategoryDao
import com.djoudjou.iptv.data.local.EpgEventDao
import com.djoudjou.iptv.data.local.ProviderDao
import com.djoudjou.iptv.data.local.StreamDao
import com.djoudjou.iptv.data.local.VodProgressDao
import com.djoudjou.iptv.data.local.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule - Hilt Module für Datenbank-Komponenten.
 *
 * Stellt Singleton-Instanzen bereit für:
 * - AppDatabase (Room Database)
 * - Alle DAOs (ProviderDao, CategoryDao, StreamDao, EpgEventDao, VodProgressDao)
 *
 * DATABASE CONFIGURATION:
 * - Name: djoudjou_iptv_database
 * - Version: 1
 * - Migration: Support für zukünftige Schema-Änderungen
 * - Fallback: Destructive migration (nur für Development)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ═══════════════════════════════════════════════════════════════════════════════
    // ROOM DATABASE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Stellt AppDatabase als Singleton bereit.
     *
     * Konfiguration:
     * - Database Name: djoudjou_iptv_database
     * - Fallback to destructive migration (für Development)
     * - Migrations: Array von Migration-Objekten für Version-Updates
     *
     * WICHTIG: Für Production sollte fallbackToDestructiveMigration() entfernt
     * und stattdessen korrekte Migrationen verwendet werden.
     *
     * @param context Application-Context (von Hilt injiziert)
     * @return AppDatabase Instanz
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = AppDatabase.DATABASE_NAME
        )
            // Fallback für Development - in Production entfernen!
            .fallbackToDestructiveMigration()

            // Migrationen hinzufügen wenn Version > 1
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)

            // Optional: Callbacks für Datenbank-Events
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Wird beim ersten Erstellen der Datenbank aufgerufen
                    // Nützlich für initiale Daten
                }

                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Wird bei jedem Öffnen der Datenbank aufgerufen
                    // Nützlich für PRAGMA-Einstellungen
                    db.execSQL("PRAGMA foreign_keys = ON")
                }
            })

            // Optional: Query-Listener für Debugging
            // .setQueryExecutor { command -> Log.d("SQL", command) }

            .build()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DATA ACCESS OBJECTS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Stellt ProviderDao als Singleton bereit.
     *
     * @param database AppDatabase Instanz
     * @return ProviderDao Instanz
     */
    @Provides
    @Singleton
    fun provideProviderDao(database: AppDatabase): ProviderDao {
        return database.providerDao()
    }

    /**
     * Stellt CategoryDao als Singleton bereit.
     *
     * @param database AppDatabase Instanz
     * @return CategoryDao Instanz
     */
    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    /**
     * Stellt StreamDao als Singleton bereit.
     *
     * @param database AppDatabase Instanz
     * @return StreamDao Instanz
     */
    @Provides
    @Singleton
    fun provideStreamDao(database: AppDatabase): StreamDao {
        return database.streamDao()
    }

    /**
     * Stellt EpgEventDao als Singleton bereit.
     *
     * @param database AppDatabase Instanz
     * @return EpgEventDao Instanz
     */
    @Provides
    @Singleton
    fun provideEpgEventDao(database: AppDatabase): EpgEventDao {
        return database.epgEventDao()
    }

    /**
     * Stellt VodProgressDao als Singleton bereit.
     *
     * @param database AppDatabase Instanz
     * @return VodProgressDao Instanz
     */
    @Provides
    @Singleton
    fun provideVodProgressDao(database: AppDatabase): VodProgressDao {
        return database.vodProgressDao()
    }
}
