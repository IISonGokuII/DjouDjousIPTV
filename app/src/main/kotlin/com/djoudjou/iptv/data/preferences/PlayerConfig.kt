package com.djoudjou.iptv.data.preferences

/**
 * Buffer Size Konfiguration für ExoPlayer LoadControl.
 *
 * Definiert minBufferMs, maxBufferMs, bufferForPlaybackMs und bufferForPlaybackAfterRebufferMs.
 */
enum class BufferSizeConfig(
    val minBufferMs: Int,
    val maxBufferMs: Int,
    val bufferForPlaybackMs: Int,
    val bufferForPlaybackAfterRebufferMs: Int
) {
    /**
     * SMALL: Instant Zapping
     * Minimale Pufferung für schnelle Kanalwechsel.
     */
    SMALL(
        minBufferMs = 1_000,
        maxBufferMs = 5_000,
        bufferForPlaybackMs = 500,
        bufferForPlaybackAfterRebufferMs = 500
    ),

    /**
     * NORMAL: Ausgewogen
     * Gute Balance zwischen Zapping-Geschwindigkeit und Stabilität.
     */
    NORMAL(
        minBufferMs = 15_000,
        maxBufferMs = 30_000,
        bufferForPlaybackMs = 2_500,
        bufferForPlaybackAfterRebufferMs = 5_000
    ),

    /**
     * LARGE: Stabile Wiedergabe
     * Größerer Puffer für instabile Netzwerkverbindungen.
     */
    LARGE(
        minBufferMs = 30_000,
        maxBufferMs = 60_000,
        bufferForPlaybackMs = 5_000,
        bufferForPlaybackAfterRebufferMs = 10_000
    ),

    /**
     * CUSTOM: Benutzerdefiniert
     * Wird zur Laufzeit konfiguriert.
     */
    CUSTOM(
        minBufferMs = 30_000,
        maxBufferMs = 60_000,
        bufferForPlaybackMs = 5_000,
        bufferForPlaybackAfterRebufferMs = 10_000
    );

    companion object {
        /**
         * Erstellt BufferSizeConfig aus Enum.
         */
        fun fromEnum(bufferSize: BufferSize, customMinMs: Int = 30000, customMaxMs: Int = 60000): BufferSizeConfig {
            return when (bufferSize) {
                BufferSize.SMALL -> SMALL
                BufferSize.NORMAL -> NORMAL
                BufferSize.LARGE -> LARGE
                BufferSize.CUSTOM -> CUSTOM.copy(
                    minBufferMs = customMinMs.coerceIn(500, 120_000),
                    maxBufferMs = customMaxMs.coerceIn(1_000, 300_000)
                )
            }
        }
    }
}

/**
 * Aspect Ratio für Video-Player.
 */
enum class AspectRatioConfig {
    /**
     * FIT: Anpassen (Letterbox/Pillarbox)
     * Behält das ursprüngliche Seitenverhältnis bei.
     */
    FIT,

    /**
     * FILL: Füllen (Crop)
     * Füllt den gesamten Bildschirm, schneidet ggf. Ränder ab.
     */
    FILL,

    /**
     * RATIO_16_9: 16:9 erzwingen
     */
    RATIO_16_9,

    /**
     * RATIO_4_3: 4:3 erzwingen
     */
    RATIO_4_3,

    /**
     * RATIO_21_9: 21:9 (Ultrawide) erzwingen
     */
    RATIO_21_9
}
