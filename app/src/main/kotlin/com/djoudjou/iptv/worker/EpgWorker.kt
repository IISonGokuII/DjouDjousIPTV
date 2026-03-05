package com.djoudjou.iptv.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.djoudjou.iptv.data.local.EpgEventDao
import com.djoudjou.iptv.data.local.ProviderDao
import com.djoudjou.iptv.data.local.StreamDao
import com.djoudjou.iptv.data.preferences.EpgUpdateInterval
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import com.djoudjou.iptv.data.remote.XtreamApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * EpgWorker - WorkManager Worker für EPG-Updates.
 *
 * Verantwortlichkeiten:
 * - EPG-Daten von Xtream API laden
 * - EPG-Daten in Room Database speichern
 * - Periodische Updates basierend auf Settings
 *
 * WIRD VERWENDET FÜR:
 * - EpgUpdateInterval.ON_START: Beim App-Start
 * - EpgUpdateInterval.EVERY_12H: Alle 12 Stunden
 * - EpgUpdateInterval.EVERY_24H: Alle 24 Stunden
 * - EpgUpdateInterval.MANUAL: Nur manuell
 *
 * ANDROIDMANIFEST:
 * <provider
 *     android:name="androidx.startup.InitializationProvider"
 *     android:authorities="${applicationId}.androidx-startup"
 *     android:exported="false"
 *     tools:node="merge">
 *     <meta-data
 *         android:name="androidx.work.WorkManagerInitializer"
 *         android:value="androidx.startup" />
 * </provider>
 */
@HiltWorker
class EpgWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val providerDao: ProviderDao,
    private val streamDao: StreamDao,
    private val epgEventDao: EpgEventDao,
    private val settingsPreferencesManager: SettingsPreferencesManager
) : CoroutineWorker(context, params) {

    companion object {
        /**
         * Unique Work Name für EPG-Updates.
         */
        const val WORK_NAME = "epg_update_worker"

        /**
         * Key für Stream-IDs im Worker-Input.
         */
        const val KEY_STREAM_IDS = "stream_ids"

        /**
         * Key für Provider-Credentials im Worker-Input.
         */
        const val KEY_PROVIDER_URL = "provider_url"
        const val KEY_PROVIDER_USERNAME = "provider_username"
        const val KEY_PROVIDER_PASSWORD = "provider_password"
    }

    /**
     * Führt EPG-Update durch.
     *
     * @return Result.success() oder Result.failure()
     */
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Stream-IDs aus Input holen
                val streamIdsString = inputData.getString(KEY_STREAM_IDS)
                val streamIds = streamIdsString?.split(",")?.map { it.toLong() } ?: emptyList()

                if (streamIds.isEmpty()) {
                    return@withContext Result.failure()
                }

                // Provider-Credentials aus Input holen
                val baseUrl = inputData.getString(KEY_PROVIDER_URL) ?: return@withContext Result.failure()
                val username = inputData.getString(KEY_PROVIDER_USERNAME) ?: return@withContext Result.failure()
                val password = inputData.getString(KEY_PROVIDER_PASSWORD) ?: return@withContext Result.failure()

                // Xtream API Service erstellen
                val apiService = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }.asConverterFactory("application/json".toMediaType()))
                    .build()
                    .create(XtreamApiService::class.java)

                // EPG für jeden Stream laden
                var successCount = 0
                var errorCount = 0

                for (streamId in streamIds) {
                    try {
                        // Stream-Info holen für externe ID
                        val stream = streamDao.getStreamByIdSync(streamId)
                        if (stream == null || stream.epgChannelId.isNullOrBlank()) {
                            continue
                        }

                        // EPG von API laden
                        val epgResponse = apiService.getShortEpg(username, password, stream.streamId.toInt())

                        // In Entities konvertieren
                        val epgEntities = epgResponse.map { epg ->
                            com.djoudjou.iptv.data.local.EpgEventEntity(
                                epgId = epg.id,
                                title = epg.title,
                                description = epg.description,
                                category = epg.category,
                                streamId = streamId,
                                startTime = epg.start,
                                endTime = epg.end,
                                isNew = false,
                                isSeries = false
                            )
                        }

                        // In Datenbank speichern
                        epgEventDao.insertAll(epgEntities)
                        successCount++
                    } catch (e: Exception) {
                        errorCount++
                        // Continue mit nächstem Stream
                    }
                }

                // Log output
                android.util.Log.d(
                    "EpgWorker",
                    "EPG Update completed: $successCount successful, $errorCount errors"
                )

                if (successCount > 0) {
                    Result.success()
                } else {
                    Result.failure()
                }
            } catch (e: Exception) {
                android.util.Log.e("EpgWorker", "EPG Update failed", e)
                Result.retry()
            }
        }
    }
}

/**
 * Extension für MediaType.
 */
fun String.toMediaType(): okhttp3.MediaType {
    return okhttp3.MediaType.parse(this)!!
}

/**
 * Helper-Funktion für EpgWorker Scheduling.
 */
class EpgScheduler @AssistedInject constructor(
    private val workManager: WorkManager,
    private val settingsPreferencesManager: SettingsPreferencesManager
) {

    /**
     * Plant EPG-Update basierend auf Settings.
     */
    suspend fun scheduleEpgUpdate() {
        val interval = settingsPreferencesManager.epgUpdateInterval.first()

        when (interval) {
            EpgUpdateInterval.ON_START -> {
                // Einmaliges Update beim Start
                enqueueOneTimeEpgUpdate()
            }
            EpgUpdateInterval.EVERY_12H -> {
                // Alle 12 Stunden
                enqueuePeriodicEpgUpdate(12)
            }
            EpgUpdateInterval.EVERY_24H -> {
                // Alle 24 Stunden
                enqueuePeriodicEpgUpdate(24)
            }
            EpgUpdateInterval.MANUAL -> {
                // Kein automatisches Update
                cancelEpgUpdate()
            }
        }
    }

    /**
     * Einmaliges EPG-Update.
     */
    private fun enqueueOneTimeEpgUpdate() {
        val workRequest = OneTimeWorkRequestBuilder<EpgWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            EpgWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Periodisches EPG-Update.
     */
    private fun enqueuePeriodicEpgUpdate(intervalHours: Long) {
        val workRequest = PeriodicWorkRequestBuilder<EpgWorker>(
            repeatInterval = intervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            EpgWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel EPG-Update.
     */
    private fun cancelEpgUpdate() {
        workManager.cancelUniqueWork(EpgWorker.WORK_NAME)
    }

    /**
     * Manuelles EPG-Update triggern.
     */
    fun triggerManualEpgUpdate(streamIds: List<Long>, providerUrl: String, username: String, password: String) {
        val inputData = Data.Builder()
            .putString(EpgWorker.KEY_STREAM_IDS, streamIds.joinToString(","))
            .putString(EpgWorker.KEY_PROVIDER_URL, providerUrl)
            .putString(EpgWorker.KEY_PROVIDER_USERNAME, username)
            .putString(EpgWorker.KEY_PROVIDER_PASSWORD, password)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EpgWorker>()
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueue(workRequest)
    }
}
