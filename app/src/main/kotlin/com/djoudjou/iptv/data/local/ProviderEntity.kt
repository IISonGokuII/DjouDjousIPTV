package com.djoudjou.iptv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.djoudjou.iptv.data.preferences.ProviderType

/**
 * Provider Entity - Repräsentiert einen IPTV-Provider.
 *
 * Unterstützt Multi-Provider von Anfang an:
 * - Xtream Codes Provider (URL + Credentials)
 * - M3U Provider (Remote-URL oder lokale Datei)
 *
 * Jede Category und jeder Stream verweist auf einen Provider via providerId.
 */
@Entity(tableName = "providers")
data class ProviderEntity(
    /**
     * Eindeutige Provider-ID.
     * Wird automatisch generiert und als Foreign Key verwendet.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Anzeigename des Providers (z.B. "Mein IPTV Abo").
     */
    val name: String,

    /**
     * Typ des Providers.
     */
    val type: ProviderType,

    /**
     * Server-URL für Xtream Codes (z.B. "http://example.com").
     * Null für M3U-Provider.
     */
    val serverUrl: String? = null,

    /**
     * Benutzername für Xtream Codes.
     * Null für M3U-Provider.
     */
    val username: String? = null,

    /**
     * Passwort für Xtream Codes (im Klartext - für Production verschlüsseln!).
     * Null für M3U-Provider.
     */
    val password: String? = null,

    /**
     * M3U-URL für Remote-M3U-Provider.
     * Null für Xtream-Provider.
     */
    val m3uUrl: String? = null,

    /**
     * Pfad zur lokalen M3U-Datei.
     * Null für Xtream-Provider oder Remote-M3U.
     */
    val m3uFilePath: String? = null,

    /**
     * Server-Informationen von Xtream API.
     * JSON-String mit ServerInfo-Daten.
     */
    val serverInfoJson: String? = null,

    /**
     * True wenn Provider aktiv ist.
     */
    val isActive: Boolean = true,

    /**
     * Zeitpunkt der Erstellung (Unix Timestamp in Millisekunden).
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Zeitpunkt der letzten Aktualisierung (Unix Timestamp in Millisekunden).
     */
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Baut die Base-URL für Stream-URLs.
     */
    fun buildBaseUrl(): String {
        return when (type) {
            ProviderType.XTREAME -> serverUrl ?: ""
            ProviderType.M3U -> m3uUrl?.substringBeforeLast("/") ?: ""
        }
    }

    /**
     * Prüft ob Xtream-Credentials vorhanden sind.
     */
    fun hasXtreamCredentials(): Boolean {
        return type == ProviderType.XTREAME &&
                !serverUrl.isNullOrBlank() &&
                !username.isNullOrBlank() &&
                !password.isNullOrBlank()
    }

    /**
     * Prüft ob M3U-Quelle vorhanden ist.
     */
    fun hasM3uSource(): Boolean {
        return type == ProviderType.M3U &&
                (!m3uUrl.isNullOrBlank() || !m3uFilePath.isNullOrBlank())
    }
}
