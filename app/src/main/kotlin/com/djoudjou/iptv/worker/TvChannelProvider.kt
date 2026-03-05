package com.djoudjou.iptv.worker

import android.content.Context
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media.tv.TvContractCompat
import androidx.work.*
import com.djoudjou.iptv.data.local.ProviderDao
import com.djoudjou.iptv.data.local.StreamDao
import com.djoudjou.iptv.data.local.StreamEntity
import com.djoudjou.iptv.data.local.StreamType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TvChannelProvider - TV Channels API Integration.
 *
 * Erstellt "Play Next" Karten auf dem Android TV Homescreen für:
 * - Zuletzt angesehene VOD-Titel (mit Resume-Position)
 * - Live-TV Favoriten
 * - Zuletzt angesehene Serien-Episoden
 *
 * ANFORDERUNGEN:
 * - WRITE_TV_PROGRAMS Permission im Manifest
 * - TvContractCompat für kompatible API
 */
@Singleton
class TvChannelProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streamDao: StreamDao,
    private val providerDao: ProviderDao,
    private val workManager: WorkManager
) {

    companion object {
        /**
         * Watch Next Channel ID.
         */
        const val WATCH_NEXT_CHANNEL_ID = "watch_next"

        /**
         * Live TV Channel ID.
         */
        const val LIVE_TV_CHANNEL_ID = "live_tv"

        /**
         * Key für Stream-ID in Watch Next Data.
         */
        const val KEY_STREAM_ID = "stream_id"

        /**
         * Key für Startposition in Watch Next Data.
         */
        const val KEY_START_POSITION = "start_position"
    }

    /**
     * Initialisiert TV Channels.
     *
     * Erstellt notwendige Channels im TV Provider.
     */
    fun initializeChannels() {
        if (!TvContractCompat.isAvailable(context)) {
            return
        }

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            // Watch Next Channel erstellen
            createWatchNextChannel()

            // Live TV Channel erstellen
            createLiveTvChannel()
        }
    }

    /**
     * Erstellt Watch Next Channel.
     */
    private fun createWatchNextChannel() {
        val channel = TvContractCompat.WatchNextProgram.Builder()
            .setType(TvContractCompat.WatchNextProgram.TYPE_MOVIE)
            .setTitle("DjouDjous IPTV - Weiterschauen")
            .setDescription("Zuletzt angesehene Titel fortsetzen")
            .setInternalProviderId(WATCH_NEXT_CHANNEL_ID)
            .build()

        val uri = TvContractCompat.insertWatchNextProgram(context.contentResolver, channel)
        // URI speichern für spätere Updates
    }

    /**
     * Erstellt Live TV Channel.
     */
    private fun createLiveTvChannel() {
        val channel = TvContractCompat.PreviewChannel.Builder()
            .setDisplayName("DjouDjous IPTV - Live TV")
            .setDescription("Ihre Lieblings-Sender")
            .setInternalProviderId(LIVE_TV_CHANNEL_ID)
            .build()

        val uri = TvContractCompat.insertPreviewChannel(context.contentResolver, channel)
        // URI speichern für spätere Updates
    }

    /**
     * Aktualisiert Watch Next Karte für VOD-Titel.
     *
     * @param stream Der Stream (VOD oder Serie)
     * @param positionMs Aktuelle Wiedergabeposition in ms
     * @param durationMs Gesamtdauer in ms
     */
    fun updateWatchNextProgram(
        stream: StreamEntity,
        positionMs: Long,
        durationMs: Long
    ) {
        if (!TvContractCompat.isAvailable(context)) {
            return
        }

        // Nur anzeigen wenn Fortschritt > 5% und < 95%
        val progress = positionMs.toFloat() / durationMs.toFloat()
        if (progress < 0.05f || progress > 0.95f) {
            // Programm entfernen wenn fertig oder kaum angesehen
            removeWatchNextProgram(stream.id)
            return
        }

        val program = TvContractCompat.WatchNextProgram.Builder()
            .setType(
                if (stream.streamType == StreamType.SERIES) {
                    TvContractCompat.WatchNextProgram.TYPE_TV_EPISODE
                } else {
                    TvContractCompat.WatchNextProgram.TYPE_MOVIE
                }
            )
            .setTitle(stream.name)
            .setDisplayName(stream.name)
            .setDescription(stream.plot ?: "")
            .setPosterArtUri(stream.iconUrl?.let { Uri.parse(it) })
            .setIntentData(stream.id.toString())
            .setInternalProviderId("vod_${stream.id}")
            .setLastPlaybackPositionMs(positionMs.toInt())
            .setDurationMillis(durationMs.toInt())
            .build()

        TvContractCompat.insertWatchNextProgram(context.contentResolver, program)
    }

    /**
     * Entfernt Watch Next Programm.
     *
     * @param streamId Die Stream-ID
     */
    fun removeWatchNextProgram(streamId: Long) {
        if (!TvContractCompat.isAvailable(context)) {
            return
        }

        // Programm mit interner ID suchen und entfernen
        val resolver = context.contentResolver
        val projection = arrayOf(
            TvContractCompat.WatchNextProgramColumns._ID,
            TvContractCompat.WatchNextProgramColumns.INTERNAL_PROVIDER_ID
        )

        resolver.query(
            TvContractCompat.buildWatchNextProgramsUri(),
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(TvContractCompat.WatchNextProgramColumns._ID)
            val providerIdIndex = cursor.getColumnIndex(
                TvContractCompat.WatchNextProgramColumns.INTERNAL_PROVIDER_ID
            )

            while (cursor.moveToNext()) {
                val internalId = cursor.getString(providerIdIndex)
                if (internalId == "vod_$streamId") {
                    val programId = cursor.getLong(idIndex)
                    TvContractCompat.deleteWatchNextProgram(resolver, programId)
                    break
                }
            }
        }
    }

    /**
     * Plant Channel-Update Worker.
     */
    fun scheduleChannelUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<ChannelUpdateWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "channel_update_worker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}

/**
 * ChannelUpdateWorker - Aktualisiert TV Channels periodisch.
 */
class ChannelUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Channels aktualisieren
        // Wird von TvChannelProvider übernommen
        return Result.success()
    }
}
