package com.djoudjou.iptv.ui.player

import androidx.compose.runtime.Immutable
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Tracks

/**
 * PlayerUiState - State für Video Player UI.
 *
 * Hält alle UI-relevanten Daten für den Player.
 */
@Immutable
data class PlayerUiState(
    /**
     * Aktuelle Wiedergabeposition in ms.
     */
    val currentPosition: Long = 0L,

    /**
     * Gesamtdauer in ms.
     */
    val duration: Long = 0L,

    /**
     * Ist Player bereit für Wiedergabe?
     */
    val isReady: Boolean = false,

    /**
     * Wird gerade abgespielt?
     */
    val isPlaying: Boolean = false,

    /**
     * Ist Player im Loading-Zustand?
     */
    val isLoading: Boolean = false,

    /**
     * Aktuelle Fehlernachricht oder null.
     */
    val error: String? = null,

    /**
     * Aktueller Stream-Titel.
     */
    val title: String = "",

    /**
     * Aktueller Stream-Logo URL.
     */
    val logoUrl: String? = null,

    /**
     * Aktuelles EPG-Event (für Live-TV).
     */
    val currentEpgEvent: EpgEventUi? = null,

    /**
     * Nächstes EPG-Event (für Live-TV).
     */
    val nextEpgEvent: EpgEventUi? = null,

    /**
     * Verfügbare Audio-Tracks.
     */
    val audioTracks: List<AudioTrackUi> = emptyList(),

    /**
     * Verfügbare Untertitel-Tracks.
     */
    val subtitleTracks: List<SubtitleTrackUi> = emptyList(),

    /**
     * Aktuell ausgewählter Audio-Track Index.
     */
    val selectedAudioTrackIndex: Int = -1,

    /**
     * Aktuell ausgewählter Untertitel-Track Index.
     */
    val selectedSubtitleTrackIndex: Int = -1,

    /**
     * Aktuelle Video-Breite.
     */
    val videoWidth: Int = 0,

    /**
     * Aktuelle Video-Höhe.
     */
    val videoHeight: Int = 0,

    /**
     * Ist OSD sichtbar?
     */
    val isOsdVisible: Boolean = false,

    /**
     * Ist Player im PiP-Modus?
     */
    val isInPipMode: Boolean = false,

    /**
     * Aktuelles Aspect Ratio.
     */
    val aspectRatio: AspectRatioMode = AspectRatioMode.FIT
) {
    /**
     * Berechnet Fortschritt in Prozent (0-100).
     */
    fun getProgressPercent(): Float {
        if (duration <= 0) return 0f
        return (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 100f)
    }

    /**
     * Formatierte aktuelle Position (HH:MM:SS).
     */
    fun getFormattedPosition(): String {
        return formatTime(currentPosition)
    }

    /**
     * Formatierte Gesamtdauer (HH:MM:SS).
     */
    fun getFormattedDuration(): String {
        return formatTime(duration)
    }

    /**
     * Formatierte verbleibende Zeit.
     */
    fun getFormattedRemaining(): String {
        return formatTime(duration - currentPosition)
    }

    /**
     * Ist Live-TV?
     */
    fun isLive(): Boolean = duration == C.TIME_UNSET || duration <= 0

    companion object {
        private fun formatTime(timeMs: Long): String {
            if (timeMs < 0) return "--:--"

            val totalSeconds = timeMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }
}

/**
 * EPG-Event UI-Model.
 */
data class EpgEventUi(
    val title: String,
    val description: String? = null,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val progress: Float = 0f
)

/**
 * Audio-Track UI-Model.
 */
data class AudioTrackUi(
    val id: String,
    val label: String,
    val language: String? = null,
    val isSelected: Boolean = false
)

/**
 * Untertitel-Track UI-Model.
 */
data class SubtitleTrackUi(
    val id: String,
    val label: String,
    val language: String? = null,
    val isSelected: Boolean = false
)

/**
 * Aspect Ratio Modus.
 */
enum class AspectRatioMode {
    FIT,
    FILL,
    RATIO_16_9,
    RATIO_4_3
}

/**
 * Player-Event für UI-Interaktionen.
 */
sealed class PlayerEvent {
    /** Play/Pause toggle. */
    object TogglePlayPause : PlayerEvent()

    /** Play. */
    object Play : PlayerEvent()

    /** Pause. */
    object Pause : PlayerEvent()

    /** Seek to position. */
    data class Seek(val positionMs: Long) : PlayerEvent()

    /** Seek forward by seconds. */
    data class SeekForward(val seconds: Int = 10) : PlayerEvent()

    /** Seek backward by seconds. */
    data class SeekBackward(val seconds: Int = 10) : PlayerEvent()

    /** Show OSD. */
    object ShowOsd : PlayerEvent()

    /** Hide OSD. */
    object HideOsd : PlayerEvent()

    /** Toggle OSD visibility. */
    object ToggleOsd : PlayerEvent()

    /** Select audio track. */
    data class SelectAudioTrack(val index: Int) : PlayerEvent()

    /** Select subtitle track. */
    data class SelectSubtitleTrack(val index: Int) : PlayerEvent()

    /** Disable subtitles. */
    object DisableSubtitles : PlayerEvent()

    /** Change aspect ratio. */
    data class ChangeAspectRatio(val mode: AspectRatioMode) : PlayerEvent()

    /** Enter PiP mode. */
    object EnterPip : PlayerEvent()

    /** Exit PiP mode. */
    object ExitPip : PlayerEvent()

    /** Stop playback. */
    object Stop : PlayerEvent()

    /** Retry after error. */
    object Retry : PlayerEvent()
}

/**
 * Extension für Tracks zu AudioTrackUi Liste.
 */
fun Tracks.toAudioTracks(): List<AudioTrackUi> {
    val audioTracks = mutableListOf<AudioTrackUi>()
    var index = 0

    groups.forEach { group ->
        for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            if (format.selectionFlags and C.SELECTION_FLAG_DEFAULT != 0) {
                audioTracks.add(
                    AudioTrackUi(
                        id = "${group.type}-${index}",
                        label = format.label ?: "Audio ${index + 1}",
                        language = format.language,
                        isSelected = group.isTrackSelected(i)
                    )
                )
                index++
            }
        }
    }

    return audioTracks
}

/**
 * Extension für Tracks zu SubtitleTrackUi Liste.
 */
fun Tracks.toSubtitleTracks(): List<SubtitleTrackUi> {
    val subtitleTracks = mutableListOf<SubtitleTrackUi>()
    var index = 0

    groups.forEach { group ->
        if (group.type == C.TRACK_TYPE_TEXT) {
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                subtitleTracks.add(
                    SubtitleTrackUi(
                        id = "${group.type}-${index}",
                        label = format.label ?: "Subtitle ${index + 1}",
                        language = format.language,
                        isSelected = group.isTrackSelected(i)
                    )
                )
                index++
            }
        }
    }

    return subtitleTracks
}
