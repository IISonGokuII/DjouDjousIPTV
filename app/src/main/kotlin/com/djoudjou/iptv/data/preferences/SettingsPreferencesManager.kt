package com.djoudjou.iptv.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore Extension Property für die Application-Context.
 * Erstellt eine einzelne DataStore-Instanz für die gesamte App.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * SettingsPreferencesManager - Single Source of Truth für alle App-Einstellungen.
 *
 * Verwendet Preferences DataStore für typsichere, Flow-basierte Einstellungen.
 * Alle Änderungen werden sofort an alle Observer weitergegeben (kein App-Restart nötig).
 *
 * THREAD-SAFETY: DataStore ist thread-safe und verwendet Coroutines für alle Operationen.
 */
@Singleton
class SettingsPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ═══════════════════════════════════════════════════════════════════════════════
    // PREFERENCE KEYS
    // ═══════════════════════════════════════════════════════════════════════════════

    // Onboarding
    private val isOnboardingCompleteKey = booleanPreferencesKey("isOnboardingComplete")

    // Provider Type
    private val providerTypeKey = stringPreferencesKey("providerType")

    // Xtream Credentials
    private val xtreamServerUrlKey = stringPreferencesKey("xtreamServerUrl")
    private val xtreamUsernameKey = stringPreferencesKey("xtreamUsername")
    private val xtreamPasswordKey = stringPreferencesKey("xtreamPassword")

    // M3U
    private val m3uUrlKey = stringPreferencesKey("m3uUrl")
    private val m3uFilePathKey = stringPreferencesKey("m3uFilePath")

    // Player Engine Settings
    private val bufferSizeKey = stringPreferencesKey("bufferSize")
    private val videoDecoderKey = stringPreferencesKey("videoDecoder")
    private val autoFrameRateKey = booleanPreferencesKey("autoFrameRate")
    private val deinterlacingKey = booleanPreferencesKey("deinterlacing")
    private val defaultAspectRatioKey = stringPreferencesKey("defaultAspectRatio")
    private val customBufferSizeMsKey = intPreferencesKey("customBufferSizeMs")

    // EPG & Sync Settings
    private val epgUpdateIntervalKey = stringPreferencesKey("epgUpdateInterval")
    private val epgTimeShiftKey = floatPreferencesKey("epgTimeShift")

    // Sicherheit & Allgemein
    private val parentalPinKey = stringPreferencesKey("parentalPin")
    private val autostartKey = booleanPreferencesKey("autostart")
    private val lastPlayedStreamIdKey = longPreferencesKey("lastPlayedStreamId")
    private val resumeVodPositionKey = booleanPreferencesKey("resumeVodPosition")

    // ═══════════════════════════════════════════════════════════════════════════════
    // FLOWS (READ)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Flow für Onboarding-Status.
     * @return true wenn Onboarding abgeschlossen wurde.
     */
    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[isOnboardingCompleteKey] ?: false }

    /**
     * Flow für Provider-Typ.
     * @return ProviderType Enum (XTREAM oder M3U).
     */
    val providerType: Flow<ProviderType> = context.dataStore.data
        .map { preferences ->
            ProviderType.valueOf(preferences[providerTypeKey] ?: ProviderType.XTREAME.name)
        }

    /**
     * Flow für Xtream Server-URL.
     */
    val xtreamServerUrl: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[xtreamServerUrlKey] }

    /**
     * Flow für Xtream Benutzername.
     */
    val xtreamUsername: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[xtreamUsernameKey] }

    /**
     * Flow für Xtream Passwort.
     */
    val xtreamPassword: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[xtreamPasswordKey] }

    /**
     * Flow für M3U Remote-URL.
     */
    val m3uUrl: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[m3uUrlKey] }

    /**
     * Flow für M3U Datei-Pfad.
     */
    val m3uFilePath: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[m3uFilePathKey] }

    /**
     * Flow für Buffer-Größe.
     * @return BufferSize Enum (SMALL, NORMAL, LARGE, CUSTOM).
     */
    val bufferSize: Flow<BufferSize> = context.dataStore.data
        .map { preferences ->
            BufferSize.valueOf(preferences[bufferSizeKey] ?: BufferSize.NORMAL.name)
        }

    /**
     * Flow für benutzerdefinierte Buffer-Größe in Millisekunden.
     * @return Buffer-Größe in ms (500-120000).
     */
    val customBufferSizeMs: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[customBufferSizeMsKey] ?: 30000 }

    /**
     * Flow für Video-Decoder-Typ.
     * @return VideoDecoder Enum (HARDWARE, SOFTWARE).
     */
    val videoDecoder: Flow<VideoDecoder> = context.dataStore.data
        .map { preferences ->
            VideoDecoder.valueOf(preferences[videoDecoderKey] ?: VideoDecoder.HARDWARE.name)
        }

    /**
     * Flow für Auto Frame Rate (AFR).
     * @return true wenn AFR aktiviert ist (nur Android TV).
     */
    val autoFrameRate: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[autoFrameRateKey] ?: false }

    /**
     * Flow für Deinterlacing.
     * @return true wenn Deinterlacing aktiviert ist.
     */
    val deinterlacing: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[deinterlacingKey] ?: false }

    /**
     * Flow für Standard-Seitenverhältnis.
     * @return AspectRatio Enum (FIT, FILL, RATIO_16_9, RATIO_4_3).
     */
    val defaultAspectRatio: Flow<AspectRatio> = context.dataStore.data
        .map { preferences ->
            AspectRatio.valueOf(preferences[defaultAspectRatioKey] ?: AspectRatio.FIT.name)
        }

    /**
     * Flow für EPG-Update-Intervall.
     * @return EpgUpdateInterval Enum.
     */
    val epgUpdateInterval: Flow<EpgUpdateInterval> = context.dataStore.data
        .map { preferences ->
            EpgUpdateInterval.valueOf(
                preferences[epgUpdateIntervalKey] ?: EpgUpdateInterval.ON_START.name
            )
        }

    /**
     * Flow für EPG-Zeitverschiebung in Stunden.
     * @return Zeitverschiebung in Stunden (-12.0 bis +12.0).
     */
    val epgTimeShift: Flow<Float> = context.dataStore.data
        .map { preferences -> preferences[epgTimeShiftKey] ?: 0f }

    /**
     * Flow für Parental PIN.
     * @return 4-stellige PIN oder null wenn deaktiviert.
     */
    val parentalPin: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[parentalPinKey] }

    /**
     * Flow für Autostart-Einstellung.
     * @return true wenn Autostart aktiviert ist.
     */
    val autostart: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[autostartKey] ?: false }

    /**
     * Flow für zuletzt gespielten Stream.
     * @return Stream-ID oder null.
     */
    val lastPlayedStreamId: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[lastPlayedStreamIdKey] }

    /**
     * Flow für VOD-Resume-Position.
     * @return true wenn Resume aktiviert ist.
     */
    val resumeVodPosition: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[resumeVodPositionKey] ?: true }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SUSPENDING FUNCTIONS (WRITE)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Setzt Onboarding als abgeschlossen.
     * Wird nach erfolgreicher Synchronisation in Stufe 4 aufgerufen.
     */
    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isOnboardingCompleteKey] = complete
        }
    }

    /**
     * Speichert Provider-Typ.
     * Wird in Stufe 1 des Onboarding aufgerufen.
     */
    suspend fun setProviderType(type: ProviderType) {
        context.dataStore.edit { preferences ->
            preferences[providerTypeKey] = type.name
        }
    }

    /**
     * Speichert Xtream-Credentials.
     * Wird in Stufe 2a des Onboarding aufgerufen.
     */
    suspend fun setXtreamCredentials(url: String, username: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[xtreamServerUrlKey] = url
            preferences[xtreamUsernameKey] = username
            preferences[xtreamPasswordKey] = password
        }
    }

    /**
     * Speichert M3U-URL.
     * Wird in Stufe 2b des Onboarding aufgerufen.
     */
    suspend fun setM3uUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[m3uUrlKey] = url
        }
    }

    /**
     * Speichert M3U-Datei-Pfad.
     * Wird in Stufe 2b des Onboarding aufgerufen.
     */
    suspend fun setM3uFilePath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[m3uFilePathKey] = path
        }
    }

    /**
     * Speichert Buffer-Größe.
     * Wird in Settings für Player Engine konfiguriert.
     */
    suspend fun setBufferSize(size: BufferSize) {
        context.dataStore.edit { preferences ->
            preferences[bufferSizeKey] = size.name
        }
    }

    /**
     * Speichert benutzerdefinierte Buffer-Größe in ms.
     * Validiert: 500-120000 ms.
     */
    suspend fun setCustomBufferSizeMs(sizeMs: Int) {
        val validatedSize = sizeMs.coerceIn(500, 120000)
        context.dataStore.edit { preferences ->
            preferences[customBufferSizeMsKey] = validatedSize
        }
    }

    /**
     * Speichert Video-Decoder-Typ.
     * SOFTWARE deaktiviert Hardware-Decoder für maximale Kompatibilität.
     */
    suspend fun setVideoDecoder(decoder: VideoDecoder) {
        context.dataStore.edit { preferences ->
            preferences[videoDecoderKey] = decoder.name
        }
    }

    /**
     * Speichert Auto Frame Rate Einstellung.
     * Nur wirksam auf Android TV-Geräten.
     */
    suspend fun setAutoFrameRate(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[autoFrameRateKey] = enabled
        }
    }

    /**
     * Speichert Deinterlacing-Einstellung.
     * Aktiviert MediaCodec-Parameter für interlaced Streams (576i, 1080i).
     */
    suspend fun setDeinterlacing(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[deinterlacingKey] = enabled
        }
    }

    /**
     * Speichert Standard-Seitenverhältnis.
     */
    suspend fun setDefaultAspectRatio(ratio: AspectRatio) {
        context.dataStore.edit { preferences ->
            preferences[defaultAspectRatioKey] = ratio.name
        }
    }

    /**
     * Speichert EPG-Update-Intervall.
     */
    suspend fun setEpgUpdateInterval(interval: EpgUpdateInterval) {
        context.dataStore.edit { preferences ->
            preferences[epgUpdateIntervalKey] = interval.name
        }
    }

    /**
     * Speichert EPG-Zeitverschiebung in Stunden.
     * Validiert: -12.0 bis +12.0 Stunden, Schritte: 0.5.
     */
    suspend fun setEpgTimeShift(hours: Float) {
        val validatedShift = hours.coerceIn(-12f, 12f)
        context.dataStore.edit { preferences ->
            preferences[epgTimeShiftKey] = validatedShift
        }
    }

    /**
     * Speichert Parental PIN.
     * @param pin 4-stellige PIN oder null zum Deaktivieren.
     */
    suspend fun setParentalPin(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin != null && pin.length == 4 && pin.all { it.isDigit() }) {
                preferences[parentalPinKey] = pin
            } else {
                preferences.remove(parentalPinKey)
            }
        }
    }

    /**
     * Speichert Autostart-Einstellung.
     */
    suspend fun setAutostart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[autostartKey] = enabled
        }
    }

    /**
     * Speichert zuletzt gespielten Stream.
     * Wird nach jedem Kanalwechsel aktualisiert.
     */
    suspend fun setLastPlayedStreamId(streamId: Long) {
        context.dataStore.edit { preferences ->
            preferences[lastPlayedStreamIdKey] = streamId
        }
    }

    /**
     * Speichert VOD-Resume-Einstellung.
     */
    suspend fun setResumeVodPosition(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[resumeVodPositionKey] = enabled
        }
    }

    /**
     * Löscht alle gespeicherten Einstellungen (Factory Reset).
     * Nützlich für Logout oder App-Reset.
     */
    suspend fun clearAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ENUMS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Provider-Typ für Multi-Provider-Support.
 */
enum class ProviderType {
    XTREAM,
    M3U
}

/**
 * Buffer-Größe für ExoPlayer LoadControl.
 *
 * SMALL: Instant Zapping (1s min, 5s max)
 * NORMAL: Ausgewogen (15s min, 30s max)
 * LARGE: Stabile Wiedergabe bei schlechtem Netzwerk (30s min, 60s max)
 * CUSTOM: Benutzerdefiniert (500-120000 ms)
 */
enum class BufferSize {
    SMALL,
    NORMAL,
    LARGE,
    CUSTOM
}

/**
 * Video-Decoder-Typ.
 */
enum class VideoDecoder {
    HARDWARE,
    SOFTWARE
}

/**
 * Seitenverhältnis für Video-Player.
 */
enum class AspectRatio {
    FIT,        // Anpassen (Letterbox)
    FILL,       // Füllen (Crop)
    RATIO_16_9, // 16:9 erzwingen
    RATIO_4_3   // 4:3 erzwingen
}

/**
 * EPG-Update-Intervall für WorkManager.
 */
enum class EpgUpdateInterval {
    ON_START,    // Bei jedem App-Start
    EVERY_12H,   // Alle 12 Stunden
    EVERY_24H,   // Alle 24 Stunden
    MANUAL       // Nur manuell
}
