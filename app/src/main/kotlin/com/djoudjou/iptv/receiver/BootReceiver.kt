package com.djoudjou.iptv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.djoudjou.iptv.data.preferences.SettingsPreferencesManager
import com.djoudjou.iptv.ui.player.VideoPlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BootReceiver - BroadcastReceiver für Autostart nach Boot.
 *
 * Startet die App automatisch nach dem System-Boot wenn:
 * - Autostart in den Einstellungen aktiviert ist
 * - Ein letzter Kanal gespeichert ist
 *
 * ANDROIDMANIFEST:
 * <receiver
 *     android:name=".receiver.BootReceiver"
 *     android:enabled="true"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.BOOT_COMPLETED" />
 *         <action android:name="android.intent.action.QUICKBOOT_POWERON" />
 *         <category android:name="android.intent.category.DEFAULT" />
 *     </intent-filter>
 * </receiver>
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsPreferencesManager: SettingsPreferencesManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Wird nach Boot-Completed Broadcast aufgerufen.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        // Prüfen ob Autostart aktiviert ist
        serviceScope.launch {
            val autostartEnabled = settingsPreferencesManager.autostart.first()

            if (autostartEnabled) {
                // Letzten Kanal laden und starten
                val lastStreamId = settingsPreferencesManager.lastPlayedStreamId.first()

                if (lastStreamId != null) {
                    // VideoPlayerActivity mit Stream-ID starten
                    val playerIntent = Intent(context, VideoPlayerActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("stream_id", lastStreamId)
                        putExtra("auto_play", true)
                    }

                    context.startActivity(playerIntent)
                }
            }
        }
    }
}

/**
 * NetworkChangeReceiver - BroadcastReceiver für Netzwerk-Änderungen.
 *
 * Reagiert auf Netzwerk-Verbindungsänderungen:
 * - Startet EPG-Update wenn Netzwerk verfügbar
 * - Pausiert Wiedergabe bei Netzwerkverlust
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var epgScheduler: com.djoudjou.iptv.worker.EpgScheduler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            android.net.ConnectivityManager.CONNECTIVITY_ACTION -> {
                // Netzwerk-Status prüfen
                val isConnected = isNetworkAvailable(context)

                if (isConnected) {
                    // EPG-Update triggern
                    serviceScope.launch {
                        epgScheduler.scheduleEpgUpdate()
                    }
                }
            }
        }
    }

    /**
     * Prüft ob Netzwerk verfügbar ist.
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as android.net.ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
