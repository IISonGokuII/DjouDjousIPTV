package com.djoudjou.iptv.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.djoudjou.iptv.worker.EpgScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * WorkManagerModule - Hilt Module für WorkManager.
 *
 * Stellt bereit:
 * - WorkManager Instanz
 * - Custom WorkerFactory für Hilt
 * - Configuration für WorkManager
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkManagerModule {

    /**
     * Binds HiltWorkerFactory.
     */
    @Binds
    abstract fun bindHiltWorkerFactory(
        workerFactory: HiltWorkerFactoryImpl
    ): HiltWorkerFactory

    companion object {
        /**
         * Stellt WorkManager Instanz bereit.
         */
        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context,
            workerFactory: HiltWorkerFactory
        ): WorkManager {
            val configuration = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()

            WorkManager.initialize(context, configuration)
            return WorkManager.getInstance(context)
        }

        /**
         * Stellt EpgScheduler bereit.
         */
        @Provides
        @Singleton
        fun provideEpgScheduler(
            workManager: WorkManager,
            settingsPreferencesManager: com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
        ): EpgScheduler {
            return EpgScheduler(workManager, settingsPreferencesManager)
        }
    }
}

/**
 * HiltWorkerFactory Implementation.
 */
class HiltWorkerFactoryImpl @javax.inject.Inject constructor() : HiltWorkerFactory {
    override fun createWorker(
        appContext: android.content.Context,
        workerClassName: String,
        workerParameters: androidx.work.WorkerParameters
    ): androidx.work.ListenableWorker? {
        return null // Wird von Hilt automatisch generiert
    }
}
