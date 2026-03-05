package com.djoudjou.iptv

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application-Klasse für DjouDjousIPTV.
 *
 * Wird als Entry Point für Dagger Hilt verwendet.
 * HiltAndroidApp aktiviert die Hilt-Codegenerierung und
 * stellt Dependency Injection für alle Android-Komponenten bereit.
 *
 * Diese Klasse kann für globale Initialisierungen erweitert werden:
 * - Crash-Reporting (Firebase Crashlytics)
 * - Logging (Timber)
 * - Leak-Canary für Debug-Builds
 */
@HiltAndroidApp
class DjouDjousIptvApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Globale Initialisierungen können hier hinzugefügt werden
        // Beispiel: Timber.plant(Timber.DebugTree())
    }
}
