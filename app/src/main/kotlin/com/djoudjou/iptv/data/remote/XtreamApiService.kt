package com.djoudjou.iptv.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Xtream Codes API Interface.
 *
 * Alle API-Endpoints für die Kommunikation mit Xtream Codes Servern.
 *
 * Base URL Format: {serverUrl}/player_api.php?username={username}&password={password}
 *
 * Wichtige Endpoints:
 * - /player_api.php?username=...&password=... → Authentication
 * - /player_api.php?username=...&password=...&action=get_live_categories → Live TV Kategorien
 * - /player_api.php?username=...&password=...&action=get_vod_categories → VOD Kategorien
 * - /player_api.php?username=...&password=...&action=get_series_categories → Serien Kategorien
 * - /player_api.php?username=...&password=...&action=get_live_streams → Live Streams
 * - /player_api.php?username=...&password=...&action=get_vod_streams → VOD Streams
 * - /player_api.php?username=...&password=...&action=get_series → Serien
 * - /player_api.php?username=...&password=...&action=get_series_info&series_id=... → Serien-Details
 * - /player_api.php?username=...&password=...&action=get_short_epg&stream_id=... → EPG
 * - /movie/{username}/{password}/{streamId}.{ext} → VOD Stream URL
 * - /series/{username}/{password}/{streamId}/{season}/{episode}.{ext} → Serie Stream URL
 */
interface XtreamApiService {

    // ═══════════════════════════════════════════════════════════════════════════════
    // AUTHENTICATION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Authentifizierung beim Xtream Server.
     *
     * @param username Benutzername
     * @param password Passwort
     * @return XtreamAuthResponse mit user_info und server_info
     */
    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): XtreamAuthResponse

    // ═══════════════════════════════════════════════════════════════════════════════
    // KATEGORIEN
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle Live-TV-Kategorien.
     *
     * @param username Benutzername
     * @param password Passwort
     * @return Liste aller Kategorien
     */
    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategoryResponse>

    /**
     * Holt alle VOD-Kategorien.
     *
     * @param username Benutzername
     * @param password Passwort
     * @return Liste aller VOD-Kategorien
     */
    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategoryResponse>

    /**
     * Holt alle Serien-Kategorien.
     *
     * @param username Benutzername
     * @param password Passwort
     * @return Liste aller Serien-Kategorien
     */
    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategoryResponse>

    // ═══════════════════════════════════════════════════════════════════════════════
    // STREAMS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt alle Live-Streams (optional nach Kategorie filterbar).
     *
     * @param username Benutzername
     * @param password Passwort
     * @param categoryId Optionale Kategorie-ID zur Filterung
     * @return Liste aller Live-Streams
     */
    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamLiveStreamResponse>

    /**
     * Holt alle VOD-Streams (optional nach Kategorie filterbar).
     *
     * @param username Benutzername
     * @param password Passwort
     * @param categoryId Optionale Kategorie-ID zur Filterung
     * @return Liste aller VOD-Streams
     */
    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamVodResponse>

    /**
     * Holt alle Serien (optional nach Kategorie filterbar).
     *
     * @param username Benutzername
     * @param password Passwort
     * @param categoryId Optionale Kategorie-ID zur Filterung
     * @return Liste aller Serien
     */
    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamSeriesResponse>

    // ═══════════════════════════════════════════════════════════════════════════════
    // DETAIL-INFORMATIONEN
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt detaillierte Informationen zu einer Serie mit Staffeln und Episoden.
     *
     * @param username Benutzername
     * @param password Passwort
     * @param seriesId Serien-ID
     * @return Serien-Details mit Staffeln/Episoden
     */
    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("series_id") seriesId: Int
    ): XtreamSeriesInfoResponse

    /**
     * Holt detaillierte Informationen zu einem VOD-Titel.
     *
     * @param username Benutzername
     * @param password Passwort
     * @param vodId VOD-ID
     * @return VOD-Details
     */
    @GET("player_api.php")
    suspend fun getVodInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("vod_id") vodId: Int
    ): XtreamVodInfoResponse

    // ═══════════════════════════════════════════════════════════════════════════════
    // EPG (Electronic Program Guide)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Holt EPG-Daten für einen bestimmten Stream (kurze Version).
     *
     * @param username Benutzername
     * @param password Passwort
     * @param streamId Stream-ID
     * @param limit Maximale Anzahl an EPG-Einträgen (optional)
     * @return EPG-Daten
     */
    @GET("player_api.php")
    suspend fun getShortEpg(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("stream_id") streamId: Int,
        @Query("limit") limit: Int? = null
    ): List<XtreamEpgResponse>

    // ═══════════════════════════════════════════════════════════════════════════════
    // STREAM-URLS (werden direkt als String zurückgegeben, nicht als Retrofit-Call)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Baut die URL für einen Live-Stream.
     *
     * Format: {baseUrl}/live/{username}/{password}/{streamId}.{ext}
     *
     * @param baseUrl Server-URL (z.B. http://example.com)
     * @param username Benutzername
     * @param password Passwort
     * @param streamId Stream-ID
     * @param extension Datei-Extension (m3u8, ts, etc.)
     * @return Komplette Stream-URL
     */
    fun buildLiveStreamUrl(
        baseUrl: String,
        username: String,
        password: String,
        streamId: Int,
        extension: String = "m3u8"
    ): String {
        return "$baseUrl/live/$username/$password/$streamId.$extension"
    }

    /**
     * Baut die URL für einen VOD-Stream.
     *
     * Format: {baseUrl}/movie/{username}/{password}/{streamId}.{ext}
     *
     * @param baseUrl Server-URL
     * @param username Benutzername
     * @param password Passwort
     * @param streamId Stream-ID
     * @param extension Datei-Extension (mp4, mkv, etc.)
     * @return Komplette Stream-URL
     */
    fun buildVodStreamUrl(
        baseUrl: String,
        username: String,
        password: String,
        streamId: Int,
        extension: String = "mp4"
    ): String {
        return "$baseUrl/movie/$username/$password/$streamId.$extension"
    }

    /**
     * Baut die URL für einen Serien-Episode.
     *
     * Format: {baseUrl}/series/{username}/{password}/{streamId}/{season}/{episode}.{ext}
     *
     * @param baseUrl Server-URL
     * @param username Benutzername
     * @param password Passwort
     * @param seriesId Serien-ID
     * @param season Staffel-Nummer
     * @param episode Episode-Nummer
     * @param extension Datei-Extension
     * @return Komplette Stream-URL
     */
    fun buildSeriesStreamUrl(
        baseUrl: String,
        username: String,
        password: String,
        seriesId: Int,
        season: Int,
        episode: Int,
        extension: String = "mp4"
    ): String {
        return "$baseUrl/series/$username/$password/$seriesId/$season/$episode.$extension"
    }

    /**
     * Baut die URL für Catch-Up / Timeshift.
     *
     * Format: {baseUrl}/stream/{username}/{password}/{streamId}/{startTimestamp}/{endTimestamp}.{ext}
     *
     * @param baseUrl Server-URL
     * @param username Benutzername
     * @param password Passwort
     * @param streamId Stream-ID
     * @param startTimestamp Start-Zeit (Unix Timestamp oder Format: YYYYMMDD-HHMMSS)
     * @param endTimestamp End-Zeit (Unix Timestamp oder Format: YYYYMMDD-HHMMSS)
     * @param extension Datei-Extension
     * @return Komplette Catch-Up-URL
     */
    fun buildCatchUpUrl(
        baseUrl: String,
        username: String,
        password: String,
        streamId: Int,
        startTimestamp: String,
        endTimestamp: String,
        extension: String = "m3u8"
    ): String {
        return "$baseUrl/stream/$username/$password/$streamId/$startTimestamp/$endTimestamp.$extension"
    }
}
