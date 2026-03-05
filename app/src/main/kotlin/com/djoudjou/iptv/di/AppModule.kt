package com.djoudjou.iptv.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule - Hilt Module für Application-scoped Bindings.
 *
 * Stellt Singleton-Instanzen für die gesamte Application bereit.
 * Wird einmalig beim App-Start erstellt und lebt bis zum Prozess-Ende.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Binds SettingsPreferencesManager als Singleton.
     *
     * @param manager Die konkrete Implementierung
     * @return SettingsPreferencesManager als Interface (falls zukünftig erweitert)
     */
    @Binds
    @Singleton
    abstract fun bindSettingsPreferencesManager(
        manager: SettingsPreferencesManager
    ): SettingsPreferencesManager

    companion object {
        /**
         * Stellt DataStore-Instanz als Singleton bereit.
         *
         * @param context Application-Context (wird von Hilt automatisch injiziert)
         * @return DataStore<Preferences> für typsichere Einstellungen
         */
        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> {
            return context.dataStore
        }
    }
}
