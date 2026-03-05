package com.djoudjou.iptv.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M3U-Parser - Eigenentwicklung ohne externe Bibliotheken.
 *
 * Parst M3U/M3U8-Dateien und extrahiert:
 * - #EXTINF-Tags mit allen Attributen (tvg-id, tvg-name, tvg-logo, group-title)
 * - Stream-URLs
 * - Erkennt Adult-Inhalte anhand von group-title oder stream-name
 *
 * WICHTIG: Läuft immer auf IO-Dispatcher, um den UI-Thread nicht zu blockieren.
 *
 * M3U-Format Beispiel:
 * ```
 * #EXTM3U
 * #EXTINF:-1 tvg-id="CNN.us" tvg-name="CNN" tvg-logo="http://example.com/logo.png" group-title="News",CNN HD
 * http://example.com/stream.m3u8
 * #EXTINF:-1 tvg-id="BBC.uk" tvg-name="BBC One" group-title="Adult",BBC Adult
 * http://example.com/adult-stream.m3u8
 * ```
 */
@Singleton
class M3uParser @Inject constructor() {

    /**
     * Parst eine M3U-Datei von einer Remote-URL.
     *
     * @param url Die URL zur M3U-Datei
     * @return Liste von M3U-Entry-Objekten
     */
    suspend fun parseFromUrl(url: String): List<M3uEntry> = withContext(Dispatchers.IO) {
        try {
            val inputStream = URL(url).openStream()
            parseFromStream(inputStream)
        } catch (e: Exception) {
            throw M3uParseException("Failed to parse M3U from URL: $url", e)
        }
    }

    /**
     * Parst eine M3U-Datei von einem InputStream.
     *
     * @param inputStream Der InputStream der M3U-Datei
     * @return Liste von M3U-Entry-Objekten
     */
    suspend fun parseFromStream(inputStream: InputStream): List<M3uEntry> = withContext(Dispatchers.IO) {
        try {
            inputStream.bufferedReader().use { reader ->
                parseReader(reader)
            }
        } catch (e: Exception) {
            throw M3uParseException("Failed to parse M3U from InputStream", e)
        }
    }

    /**
     * Parst eine M3U-Datei von einem String-Inhalt.
     *
     * @param content Der M3U-Inhalt als String
     * @return Liste von M3U-Entry-Objekten
     */
    suspend fun parseFromString(content: String): List<M3uEntry> = withContext(Dispatchers.IO) {
        try {
            content.bufferedReader().use { reader ->
                parseReader(reader)
            }
        } catch (e: Exception) {
            throw M3uParseException("Failed to parse M3U from String", e)
        }
    }

    /**
     * Haupt-Parsing-Logik.
     */
    private fun parseReader(reader: BufferedReader): List<M3uEntry> {
        val entries = mutableListOf<M3uEntry>()
        var currentEntry: M3uEntryBuilder? = null

        var lineNumber = 0
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            lineNumber++
            line = line?.trim()

            // Leere Zeilen überspringen
            if (line.isNullOrBlank()) continue

            // M3U-Header überspringen
            if (line == "#EXTM3U") continue

            // EXTINF-Zeile parsen
            if (line.startsWith("#EXTINF:")) {
                currentEntry = M3uEntryBuilder()
                parseExtInfLine(line, currentEntry)
            }
            // Stream-URL (nach EXTINF)
            else if (!line.startsWith("#") && currentEntry != null) {
                currentEntry.streamUrl = line
                entries.add(currentEntry.build())
                currentEntry = null
            }
            // #EXTGRP (Group-Titel als Alternative)
            else if (line.startsWith("#EXTGRP:") && currentEntry != null) {
                val groupTitle = line.substringAfter("#EXTGRP:").trim()
                if (currentEntry.groupTitle == null) {
                    currentEntry.groupTitle = groupTitle
                }
            }
            // #EXTTVG (weitere Metadaten)
            else if (line.startsWith("#EXTTVG:") && currentEntry != null) {
                parseExtTvgLine(line, currentEntry)
            }
        }

        return entries
    }

    /**
     * Parst eine #EXTINF-Zeile und extrahiert alle Attribute.
     *
     * Format: #EXTINF:-1 tvg-id="CNN.us" tvg-name="CNN" tvg-logo="..." group-title="News",CNN HD
     */
    private fun parseExtInfLine(line: String, builder: M3uEntryBuilder) {
        // Trenne Dauer von Attributen und Titel
        val parts = line.substringAfter("#EXTINF:").split(",", limit = 2)

        if (parts.size < 2) return

        val attributesPart = parts[0].trim()
        val titlePart = parts[1].trim()

        // Titel setzen
        builder.displayName = titlePart

        // Attribute parsen (Regex für key="value" oder key='value')
        val attributeRegex = """(\w+(?:-\w+)*)\s*=\s*["']([^"']*)["']""".toRegex()
        val matches = attributeRegex.findAll(attributesPart)

        for (match in matches) {
            val key = match.groupValues[1]
            val value = match.groupValues[2]

            when (key) {
                "tvg-id" -> builder.tvgId = value
                "tvg-name" -> builder.tvgName = value
                "tvg-logo" -> builder.tvgLogo = value
                "group-title" -> builder.groupTitle = value
                "tvg-country" -> builder.country = value
                "tvg-language" -> builder.language = value
                "tvg-category" -> builder.category = value
            }
        }

        // Adult-Erkennung anhand von group-title oder displayName
        builder.isAdult = detectAdultContent(builder.groupTitle, titlePart)
    }

    /**
     * Parst #EXTTVG-Zeilen für zusätzliche Metadaten.
     */
    private fun parseExtTvgLine(line: String, builder: M3uEntryBuilder) {
        val attributeRegex = """(\w+(?:-\w+)*)\s*=\s*["']([^"']*)["']""".toRegex()
        val matches = attributeRegex.findAll(line)

        for (match in matches) {
            val key = match.groupValues[1]
            val value = match.groupValues[2]

            when (key) {
                "logo" -> {
                    if (builder.tvgLogo == null) builder.tvgLogo = value
                }
            }
        }
    }

    /**
     * Erkennt Adult-Inhalte anhand von Gruppentitel oder Stream-Namen.
     */
    private fun detectAdultContent(groupTitle: String?, displayName: String): Boolean {
        val adultKeywords = listOf(
            "adult", "porn", "xxx", "erotik", "erotic", "sexy", "sex",
            "18+", "18plus", "hardcore", "nudity", "nsfw"
        )

        val searchText = "${groupTitle.orEmpty()} $displayName".lowercase()

        return adultKeywords.any { keyword -> searchText.contains(keyword) }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// DATENKLASSEN FÜR M3U-ENTRIES
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Repräsentiert einen einzelnen M3U-Eintrag nach dem Parsen.
 *
 * @param tvgId Eindeutige ID für EPG-Zuordnung (z.B. "CNN.us")
 * @param tvgName Technischer Name des Senders
 * @param tvgLogo URL zum Sender-Logo
 * @param groupTitle Kategorie/Gruppe (z.B. "News", "Sports", "Adult")
 * @param displayName Anzeigename des Streams
 * @param streamUrl URL zum Stream
 * @param country Ländercode (optional)
 * @param language Sprachcode (optional)
 * @param category Kategorie (optional, Alternative zu group-title)
 * @param isAdult true wenn Adult-Inhalt erkannt wurde
 */
data class M3uEntry(
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val groupTitle: String? = null,
    val displayName: String,
    val streamUrl: String,
    val country: String? = null,
    val language: String? = null,
    val category: String? = null,
    val isAdult: Boolean = false
) {
    /**
     * Extrahiert die Stream-ID aus der URL oder tvgId.
     * Wird verwendet, um eine eindeutige Long-ID für die Datenbank zu generieren.
     */
    fun extractId(): Long {
        // Versuche zuerst tvgId zu verwenden
        tvgId?.hashCode()?.toLong()?.absoluteValue?.let { return it }

        // Sonst Hash der URL
        return streamUrl.hashCode().toLong().absoluteValue
    }

    /**
     * Extrahiert die Kategorie-ID aus dem Gruppentitel.
     */
    fun extractCategoryId(): Long {
        val category = groupTitle ?: "Uncategorized"
        return category.hashCode().toLong().absoluteValue
    }
}

/**
 * Builder-Klasse für M3uEntry.
 */
class M3uEntryBuilder {
    var tvgId: String? = null
    var tvgName: String? = null
    var tvgLogo: String? = null
    var groupTitle: String? = null
    var displayName: String = ""
    var streamUrl: String = ""
    var country: String? = null
    var language: String? = null
    var category: String? = null
    var isAdult: Boolean = false

    fun build(): M3uEntry {
        return M3uEntry(
            tvgId = tvgId,
            tvgName = tvgName,
            tvgLogo = tvgLogo,
            groupTitle = groupTitle,
            displayName = displayName,
            streamUrl = streamUrl,
            country = country,
            language = language,
            category = category,
            isAdult = isAdult
        )
    }
}

/**
 * Exception für M3U-Parsing-Fehler.
 */
class M3uParseException(message: String, cause: Throwable? = null) : Exception(message, cause)
