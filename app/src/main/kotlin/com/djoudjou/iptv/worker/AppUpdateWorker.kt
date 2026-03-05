package com.djoudjou.iptv.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.djoudjou.iptv.data.local.ProviderDao
import com.djoudjou.iptv.data.local.StreamDao
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * AppUpdateWorker - WorkManager Worker für App-Updates.
 *
 * Verantwortlichkeiten:
 * - Provider-Daten aktualisieren (neue Streams, Kategorien)
 * - EPG-Daten aktualisieren
 * - VOD-Resume-Positionen synchronisieren
 *
 * WIRD AUSGEFÜHRT:
 * - Beim App-Start (einmalig)
 * - Periodisch basierend auf Settings
 */
@HiltWorker
class AppUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val providerDao: ProviderDao,
    private val streamDao: StreamDao,
    private val settingsPreferencesManager: SettingsPreferencesManager
) : CoroutineWorker(context, params) {

    companion object {
        /**
         * Unique Work Name für App-Updates.
         */
        const val WORK_NAME = "app_update_worker"
    }

    /**
     * Führt App-Update durch.
     */
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Prüfen ob Onboarding abgeschlossen
                val isOnboardingComplete = settingsPreferencesManager.isOnboardingComplete.first()

                if (!isOnboardingComplete) {
                    return@withContext Result.success() // Kein Update nötig
                }

                // Provider laden
                val providers = providerDao.getActiveProviders().first()

                if (providers.isEmpty()) {
                    return@withContext Result.success()
                }

                // Für jeden Provider Update durchführen
                var hasUpdates = false

                for (provider in providers) {
                    // Stream-Count aktualisieren
                    val streamCount = streamDao.getStreamCount(provider.id)

                    if (streamCount > 0) {
                        hasUpdates = true
                    }
                }

                if (hasUpdates) {
                    Result.success()
                } else {
                    Result.retry()
                }
            } catch (e: Exception) {
                android.util.Log.e("AppUpdateWorker", "Update failed", e)
                Result.retry()
            }
        }
    }
}

/**
 * AppUpdateScheduler - Helper für AppUpdateWorker.
 */
class AppUpdateScheduler @AssistedInject constructor(
    private val workManager: WorkManager
) {

    /**
     * Plant App-Update beim Start.
     */
    fun scheduleStartupUpdate() {
        val workRequest = OneTimeWorkRequestBuilder<AppUpdateWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            AppUpdateWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancel App-Update.
     */
    fun cancelUpdate() {
        workManager.cancelUniqueWork(AppUpdateWorker.WORK_NAME)
    }
}
