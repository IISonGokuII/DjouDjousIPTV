package com.djoudjou.iptv.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.djoudjou.iptv.data.preferences.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DataStoreModule - Hilt Module für DataStore-Providing.
 *
 * Dieses Modul ist verantwortlich für die Bereitstellung der DataStore-Instanz.
 * Die eigentliche Extension Property 'dataStore' ist in SettingsPreferencesManager.kt definiert.
 *
 * Hinweis: Da AppModule bereits eine provideDataStore-Methode hat, kann dieses Modul
 * als Alternative oder Ergänzung verwendet werden. Für Phase 1 reicht AppModule aus.
 * Dieses Modul ist für zukünftige Erweiterungen vorgesehen (z.B. Proto DataStore).
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Stellt die Preferences DataStore-Instanz als Singleton bereit.
     *
     * @param context Application-Context (von Hilt injiziert)
     * @return DataStore<Preferences> für typsichere, Flow-basierte Einstellungen
     */
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}
