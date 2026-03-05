package com.djoudjou.iptv.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Xtream Codes API Service Definition.
 *
 * API-Endpoints für:
 * - Authentication (player_api.php)
 * - Kategorien (Live TV, VOD, Serien)
 * - Streams (Live, VOD, Series)
 * - EPG-Daten
 * - Catch-Up / Timeshift
 *
 * Dokumentation: https://xtream-codes.com/
 */

// ═══════════════════════════════════════════════════════════════════════════════
// API REQUEST / RESPONSE MODELS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Authentication Request für Xtream Codes API.
 */
@Serializable
data class XtreamAuthRequest(
    val username: String,
    val password: String
)

/**
 * Authentication Response von Xtream Codes API.
 */
@Serializable
data class XtreamAuthResponse(
    @SerialName("user_info") val userInfo: XtreamUserInfo,
    @SerialName("server_info") val serverInfo: XtreamServerInfo
)

/**
 * Benutzer-Informationen aus der Auth-Response.
 */
@Serializable
data class XtreamUserInfo(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("message") val message: String = "",
    @SerialName("auth") val auth: Int = 0, // 1 = authentifiziert
    @SerialName("status") val status: String = "Active",
    @SerialName("exp_date") val expDate: Long? = null, // Unix Timestamp oder null = unbegrenzt
    @SerialName("is_trial") val isTrial: String = "0",
    @SerialName("active_cons") val activeConnections: String = "1",
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("max_connections") val maxConnections: String = "1",
    @SerialName("allowed_output_formats") val allowedOutputFormats: List<String> = emptyList()
)

/**
 * Server-Informationen aus der Auth-Response.
 */
@Serializable
data class XtreamServerInfo(
    @SerialName("url") val url: String,
    @SerialName("port") val port: String,
    @SerialName("https_port") val httpsPort: String = "443",
    @SerialName("server_protocol") val serverProtocol: String = "http",
    @SerialName("rtmp_port") val rtmpPort: String = "25461",
    @SerialName("timezone") val timezone: String = "Europe/London",
    @SerialName("timestamp_now") val timestampNow: Long = 0,
    @SerialName("time_now") val timeNow: String = ""
)

/**
 * Kategorie-Response für Live TV, VOD und Serien.
 */
@Serializable
data class XtreamCategoryResponse(
    @SerialName("category_id") val categoryId: String,
    @SerialName("category_name") val categoryName: String,
    @SerialName("parent_id") val parentId: Int = 0
)

/**
 * Live Stream Response.
 */
@Serializable
data class XtreamLiveStreamResponse(
    @SerialName("num") val num: Int,
    @SerialName("name") val name: String,
    @SerialName("stream_type") val streamType: String, // "live"
    @SerialName("stream_id") val streamId: Int,
    @SerialName("stream_icon") val streamIcon: String = "",
    @SerialName("epg_channel_id") val epgChannelId: String? = null,
    @SerialName("added") val added: String = "",
    @SerialName("category_id") val categoryId: String,
    @SerialName("custom_sid") val customSid: String = "",
    @SerialName("tv_archive") val tvArchive: Int = 0, // 1 = Catch-Up verfügbar
    @SerialName("direct_source") val directSource: String = "",
    @SerialName("tv_archive_duration") val tvArchiveDuration: Int = 0
)

/**
 * VOD Response.
 */
@Serializable
data class XtreamVodResponse(
    @SerialName("num") val num: Int,
    @SerialName("name") val name: String,
    @SerialName("series_id") val seriesId: Int,
    @SerialName("cover") val cover: String = "",
    @SerialName("stream_type") val streamType: String, // "movie"
    @SerialName("stream_id") val streamId: Int,
    @SerialName("container_extension") val containerExtension: String = "",
    @SerialName("added") val added: String = "",
    @SerialName("category_id") val categoryId: String,
    @SerialName("rating") val rating: Rating = Rating(),
    @SerialName("rating_5based") val rating5based: Double = 0.0,
    @SerialName("base_url") val baseUrl: String? = null,
    @SerialName("direct_source") val directSource: String = "",
    @SerialName("movie_image") val movieImage: String = ""
)

/**
 * Series Response.
 */
@Serializable
data class XtreamSeriesResponse(
    @SerialName("num") val num: Int,
    @SerialName("name") val name: String,
    @SerialName("series_id") val seriesId: Int,
    @SerialName("cover") val cover: String = "",
    @SerialName("plot") val plot: String = "",
    @SerialName("cast") val cast: String = "",
    @SerialName("director") val director: String = "",
    @SerialName("genre") val genre: String = "",
    @SerialName("releaseDate") val releaseDate: String = "",
    @SerialName("last_modified") val lastModified: String = "",
    @SerialName("rating") val rating: String = "",
    @SerialName("rating_5based") val rating5based: Double = 0.0,
    @SerialName("backdrop_path") val backdropPath: List<String> = emptyList(),
    @SerialName("youtube_trailer") val youtubeTrailer: String = "",
    @SerialName("episode_run_time") val episodeRunTime: Int = 0,
    @SerialName("category_id") val categoryId: String
)

/**
 * Series Info Response (Detailinformationen mit Staffeln/Episoden).
 */
@Serializable
data class XtreamSeriesInfoResponse(
    @SerialName("name") val name: String,
    @SerialName("series_id") val seriesId: Int,
    @SerialName("cover") val cover: String = "",
    @SerialName("plot") val plot: String = "",
    @SerialName("cast") val cast: String = "",
    @SerialName("director") val director: String = "",
    @SerialName("genre") val genre: String = "",
    @SerialName("releaseDate") val releaseDate: String = "",
    @SerialName("last_modified") val lastModified: String = "",
    @SerialName("rating") val rating: String = "",
    @SerialName("rating_5based") val rating5based: Double = 0.0,
    @SerialName("backdrop_path") val backdropPath: List<String> = emptyList(),
    @SerialName("youtube_trailer") val youtubeTrailer: String = "",
    @SerialName("episode_run_time") val episodeRunTime: Int = 0,
    @SerialName("seasons") val seasons: List<XtreamSeason> = emptyList()
)

/**
 * Staffel-Informationen.
 */
@Serializable
data class XtreamSeason(
    @SerialName("ai_date") val aiDate: String = "",
    @SerialName("id") val id: Int,
    @SerialName("cover") val cover: String = "",
    @SerialName("name") val name: String,
    @SerialName("season_number") val seasonNumber: Int
)

/**
 * Episode-Informationen.
 */
@Serializable
data class XtreamEpisode(
    @SerialName("id") val id: Int,
    @SerialName("episode_num") val episodeNum: Int,
    @SerialName("title") val title: String = "",
    @SerialName("container_extension") val containerExtension: String = "",
    @SerialName("info") val info: XtreamEpisodeInfo,
    @SerialName("custom_sid") val customSid: String = "",
    @SerialName("added") val added: String = "",
    @SerialName("season") val season: Int,
    @SerialName("direct_source") val directSource: String = ""
)

/**
 * Episode-Info Details.
 */
@Serializable
data class XtreamEpisodeInfo(
    @SerialName("name") val name: String = "",
    @SerialName("plot") val plot: String = "",
    @SerialName("releasedate") val releaseDate: String = "",
    @SerialName("rating") val rating: String = "",
    @SerialName("rating_5based") val rating5based: Double = 0.0,
    @SerialName("duration_secs") val durationSecs: Int = 0,
    @SerialName("duration") val duration: String = "",
    @SerialName("video") val video: XtreamVideoInfo = XtreamVideoInfo(),
    @SerialName("bitrate") val bitrate: Int = 0,
    @SerialName("cast") val cast: String = "",
    @SerialName("director") val director: String = ""
)

/**
 * Video-Informationen (Codec, Auflösung, etc.).
 */
@Serializable
data class XtreamVideoInfo(
    @SerialName("index") val index: Int = 0,
    @SerialName("codec_name") val codecName: String = "",
    @SerialName("codec_long_name") val codecLongName: String = "",
    @SerialName("profile") val profile: String = "",
    @SerialName("codec_type") val codecType: String = "",
    @SerialName("width") val width: Int = 0,
    @SerialName("height") val height: Int = 0,
    @SerialName("avg_frame_rate") val avgFrameRate: String = "",
    @SerialName("color_space") val colorSpace: String = "",
    @SerialName("color_primaries") val colorPrimaries: String = "",
    @SerialName("color_transfer") val colorTransfer: String = "",
    @SerialName("refs") val refs: Int = 0
)

/**
 * EPG (Electronic Program Guide) Response.
 */
@Serializable
data class XtreamEpgResponse(
    @SerialName("id") val id: String,
    @SerialName("epg_id") val epgId: String,
    @SerialName("title") val title: String,
    @SerialName("lang") val lang: String = "",
    @SerialName("channel_id") val channelId: String,
    @SerialName("start") val start: Long, // Unix Timestamp
    @SerialName("end") val end: Long, // Unix Timestamp
    @SerialName("description") val description: String = "",
    @SerialName("category") val category: String = "",
    @SerialName("container_extension") val containerExtension: String = "",
    @SerialName("custom_sid") val customSid: String = "",
    @SerialName("added") val added: String = "",
    @SerialName("source") val source: String = "",
    @SerialName("start_arib") val startArib: String = "",
    @SerialName("stop_arib") val stopArib: String = ""
)

/**
 * VOD-Info Response (Detailinformationen zu einem Film).
 */
@Serializable
data class XtreamVodInfoResponse(
    @SerialName("info") val info: XtreamVodInfo,
    @SerialName("movie_data") val movieData: XtreamVodData
)

/**
 * VOD-Info Details.
 */
@Serializable
data class XtreamVodInfo(
    @SerialName("name") val name: String,
    @SerialName("o_name") val originalName: String = "",
    @SerialName("cover") val cover: String = "",
    @SerialName("plot") val plot: String = "",
    @SerialName("cast") val cast: String = "",
    @SerialName("director") val director: String = "",
    @SerialName("genre") val genre: String = "",
    @SerialName("releaseDate") val releaseDate: String = "",
    @SerialName("last_modified") val lastModified: String = "",
    @SerialName("rating") val rating: String = "",
    @SerialName("rating_5based") val rating5based: Double = 0.0,
    @SerialName("backdrop_path") val backdropPath: List<String> = emptyList(),
    @SerialName("youtube_trailer") val youtubeTrailer: String = "",
    @SerialName("episode_run_time") val episodeRunTime: Int = 0,
    @SerialName("duration") val duration: String = "",
    @SerialName("video") val video: XtreamVideoInfo = XtreamVideoInfo(),
    @SerialName("bitrate") val bitrate: Int = 0,
    @SerialName("age") val age: String = ""
)

/**
 * VOD-Daten (Stream-URL, etc.).
 */
@Serializable
data class XtreamVodData(
    @SerialName("stream_id") val streamId: Int,
    @SerialName("name") val name: String,
    @SerialName("added") val added: String = "",
    @SerialName("category_id") val categoryId: String,
    @SerialName("container_extension") val containerExtension: String = "",
    @SerialName("custom_sid") val customSid: String = "",
    @SerialName("direct_source") val directSource: String = ""
)

/**
 * Rating-Informationen.
 */
@Serializable
data class Rating(
    @SerialName("db") val db: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("rate") val rate: String = "",
    @SerialName("votes") val votes: String = "",
    @SerialName("date") val date: String = ""
)
