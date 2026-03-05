package com.djoudjou.iptv.player

import android.app.Activity
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import kotlin.math.roundToInt

/**
 * AfrController - Auto Frame Rate Controller für Android TV.
 *
 * Passt die Display-Wiederholrate automatisch an die FPS des Videos an.
 * Nur wirksam auf Android TV-Geräten mit AFR-Support.
 *
 * UNTERSTÜTZTE FRAME RATES:
 * - 24 fps → 24Hz
 * - 25 fps → 50Hz (2x)
 * - 30 fps → 60Hz (2x)
 * - 50 fps → 50Hz (1x) oder 100Hz (2x)
 * - 60 fps → 60Hz (1x) oder 120Hz (2x)
 *
 * MOBILE FALLBACK:
 * Auf Mobile-Geräten wird AFR stillschweigend ignoriert.
 */
@OptIn(UnstableApi::class)
class AfrController(
    private val activity: Activity
) {
    /**
     * Ursprüngliche Display-Einstellungen (für Restore).
     */
    private var originalFrameRate: Float? = null
    private var originalMode: Display.Mode? = null

    /**
     * Ist AFR aktuell aktiv?
     */
    var is AfrActive: Boolean = false
        private set

    /**
     * Prüft ob das Gerät AFR unterstützt.
     *
     * @return true wenn AFR unterstützt wird
     */
    fun isAfrSupported(): Boolean {
        // Nur Android TV unterstützt AFR
        if (!isAndroidTv()) {
            return false
        }

        // API 23+ erforderlich
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }

        val display = activity.windowManager.defaultDisplay
        return display.supportedModes.isNotEmpty()
    }

    /**
     * Setzt die Display-Wiederholrate basierend auf Video-FPS.
     *
     * @param videoFps Die FPS des Videos
     * @return true wenn AFR erfolgreich gesetzt wurde
     */
    fun setFrameRate(videoFps: Float): Boolean {
        if (!isAfrSupported()) {
            return false
        }

        val display = activity.windowManager.defaultDisplay
        val currentMode = display.mode

        // Ziel-Framerate berechnen
        val targetRefreshRate = calculateTargetRefreshRate(videoFps, display)

        if (targetRefreshRate == null) {
            return false
        }

        // Passenden Display-Modus finden
        val bestMode = findBestDisplayMode(display, targetRefreshRate)

        if (bestMode != null && bestMode.modeId != currentMode.modeId) {
            // Original-Modus speichern
            if (originalMode == null) {
                originalMode = currentMode
                originalFrameRate = currentMode.refreshRate
            }

            // Modus wechseln
            activity.windowManager.setPreferredDisplayMode(bestMode.modeId)
            isAfrActive = true
            return true
        }

        return false
    }

    /**
     * Berechnet die Ziel-Wiederholrate basierend auf Video-FPS.
     */
    private fun calculateTargetRefreshRate(videoFps: Float, display: Display): Float? {
        val supportedRates = display.supportedModes.map { it.refreshRate }.distinct()

        return when {
            // 24 fps → 24Hz oder 48Hz oder 120Hz
            videoFps in 23.0f..25.0f -> {
                supportedRates.find { it in 23.0f..25.0f }
                    ?: supportedRates.find { it in 47.0f..50.0f }
                    ?: supportedRates.find { it in 119.0f..121.0f }
            }
            // 25 fps → 50Hz
            videoFps in 24.5f..25.5f -> {
                supportedRates.find { it in 49.0f..51.0f }
            }
            // 30 fps → 60Hz
            videoFps in 29.0f..31.0f -> {
                supportedRates.find { it in 59.0f..61.0f }
            }
            // 50 fps → 50Hz oder 100Hz
            videoFps in 49.0f..51.0f -> {
                supportedRates.find { it in 49.0f..51.0f }
                    ?: supportedRates.find { it in 99.0f..101.0f }
            }
            // 60 fps → 60Hz oder 120Hz
            videoFps in 59.0f..61.0f -> {
                supportedRates.find { it in 59.0f..61.0f }
                    ?: supportedRates.find { it in 119.0f..121.0f }
            }
            else -> null
        }
    }

    /**
     * Findet den besten Display-Modus für die Ziel-Wiederholrate.
     */
    private fun findBestDisplayMode(display: Display, targetRefreshRate: Float): Display.Mode? {
        val modes = display.supportedModes

        // Exakte Übereinstimmung suchen
        val exactMatch = modes.find {
            Math.abs(it.refreshRate - targetRefreshRate) < 0.1f
        }

        if (exactMatch != null) {
            return exactMatch
        }

        // Nächste Übereinstimmung suchen
        return modes.minByOrNull {
            Math.abs(it.refreshRate - targetRefreshRate)
        }
    }

    /**
     * Stellt die ursprüngliche Display-Wiederholrate wieder her.
     */
    fun restoreOriginalFrameRate() {
        if (!isAfrActive) {
            return
        }

        val originalMode = originalMode ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.windowManager.setPreferredDisplayMode(originalMode.modeId)
        }

        isAfrActive = false
        originalMode = null
        originalFrameRate = null
    }

    /**
     * Prüft ob es sich um ein Android TV-Gerät handelt.
     */
    private fun isAndroidTv(): Boolean {
        val pm = activity.packageManager
        return pm.hasSystemFeature("android.software.leanback") ||
                pm.hasSystemFeature("android.hardware.touchscreen") == false
    }

    /**
     * Wird aus dem Lifecycle aufgerufen (z.B. onPause).
     */
    fun onPause() {
        // AFR kann hier pausiert werden falls nötig
    }

    /**
     * Wird aus dem Lifecycle aufgerufen (z.B. onResume).
     */
    fun onResume() {
        // AFR kann hier fortgesetzt werden
    }

    /**
     * Gibt Ressourcen frei.
     */
    fun release() {
        restoreOriginalFrameRate()
    }
}

/**
 * Hilfsfunktion für WindowManager (API 30+).
 */
fun WindowManager.setPreferredDisplayMode(modeId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val mode = DisplayManagerCompat.getInstance(context)
            .getDisplay(Display.DEFAULT_DISPLAY)
            ?.getMode(modeId)

        mode?.let {
            setPreferredDisplayMode(it.modeId)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Fallback für ältere APIs
        val display = defaultDisplay
        val modes = display.supportedModes
        val targetMode = modes.find { it.modeId == modeId }

        targetMode?.let {
            // Auf API < 30 kein direkter Modus-Wechsel möglich
            // AFR wird über Surface.setFrameRate() handled
        }
    }
}

/**
 * DisplayManagerCompat für kompatible Display-Operationen.
 */
class DisplayManagerCompat private constructor(private val context: android.content.Context) {

    companion object {
        fun getInstance(context: android.content.Context): DisplayManagerCompat {
            return DisplayManagerCompat(context)
        }
    }

    fun getDisplay(displayId: Int): android.view.Display? {
        val displayManager = context.getSystemService(android.content.Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
        return displayManager.getDisplay(displayId)
    }
}
