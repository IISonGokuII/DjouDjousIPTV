# DjouDjous IPTV

**Next-Gen IPTV Player für Android TV & Mobile**

Eine native, hochmoderne und extrem performante IPTV-Appliance mit modernem Design (Glassmorphism, Dark Theme, tv-material Focus-Effekte).

---

## 🎯 Projektstatus

### ✅ Phase 1: Core Setup & DataStore Preferences - ABGESCHLOSSEN

**Erstellte Dateien:**
- `build.gradle.kts` (App-Level) - Alle Dependencies mit exakten Versionen
- `build.gradle.kts` (Projekt-Level) - Plugins
- `gradle/libs.versions.toml` - Version Catalog
- `res/xml/network_security_config.xml` - HTTP-Cleartext für IPTV-Streams
- `AndroidManifest.xml` - Grundgerüst mit allen Permissions & Features
- `SettingsPreferencesManager.kt` - DataStore für alle Einstellungen
- `AppModule.kt` - Hilt Application-scoped Bindings
- `DataStoreModule.kt` - Hilt DataStore Injection
- `proguard-rules.pro` - ProGuard-Rules für Phase 1

**Zusätzliche Dateien für Build-Fähigkeit:**
- `DjouDjousIptvApplication.kt` - Hilt Application-Klasse
- `MainActivity.kt` - Placeholder für Phase 5
- `OnboardingActivity.kt` - Placeholder für Phase 3
- `strings.xml`, `themes.xml`, `colors.xml` - Ressourcen
- `ic_launcher_foreground.xml` - App-Icon
- `banner.xml` - TV-Homescreen Banner (320x180dp)
- `backup_rules.xml`, `data_extraction_rules.xml` - Backup-Konfiguration

---

## 🏗 Architektur-Übersicht

### Clean Architecture (Data / Domain / Presentation)

```
app/
├── data/
│   ├── preferences/      # DataStore Settings
│   ├── repository/       # Repositories (Phase 2)
│   ├── remote/           # Retrofit API (Phase 2)
│   └── local/            # Room Database (Phase 2)
├── domain/
│   ├── model/            # Entities (Phase 2)
│   ├── repository/       # Repository Interfaces (Phase 2)
│   └── usecase/          # Use Cases (Phase 3)
├── presentation/
│   ├── onboarding/       # Onboarding UI (Phase 3)
│   ├── main/             # Main Dashboard (Phase 5)
│   ├── player/           # Video Player (Phase 4)
│   └── settings/         # Settings UI (Phase 5)
└── di/                   # Hilt Modules
```

### Wichtige Architekturentscheidungen

1. **Multi-Provider Room-Schema**: Alle Entities erhalten `providerId` als Foreign Key
2. **Result<T>-Pattern**: Alle Repository-Methoden returnen `sealed class Result<T>(Success, Error, Loading)`
3. **DataStore als Single Source of Truth**: Flow-basierte Einstellungen ohne App-Restart
4. **AFR-Fallback-Strategie**: Auto Frame Rate nur für Android TV, Mobile Fallback

---

## 🚀 Build-Anleitung

### Voraussetzungen

- Android Studio Hedgehog (2023.1.1) oder neuer
- JDK 17
- Android SDK 34
- Kotlin 2.0.0

### Schritte

1. **Projekt öffnen**
   ```
   File → Open → DjouDjousIPTV Ordner
   ```

2. **Gradle Sync abwarten**
   - Alle Dependencies werden automatisch heruntergeladen

3. **Build ausführen**
   ```bash
   ./gradlew assembleDebug
   ```

4. **APK finden**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📦 Technologie-Stack

| Kategorie | Technologie | Version |
|-----------|-------------|---------|
| Sprache | Kotlin | 2.0.0 |
| UI Framework | Jetpack Compose + TV Material | BOM 2024.06.00 |
| Video Engine | Media3 (ExoPlayer) | 1.3.1 |
| Netzwerk | Retrofit2 + OkHttp | 2.11.0 / 4.12.0 |
| Datenbank | Room | 2.6.1 |
| Settings | Preferences DataStore | 1.1.1 |
| Image Loading | Coil | 2.6.0 |
| DI | Dagger Hilt | 2.51.1 |
| Background | WorkManager | 2.9.0 |
| Serialisierung | Kotlin Serialization | 1.7.0 |

---

## ⚙️ Build-Konfiguration

```kotlin
minSdk: 21      // Leanback Support ab API 21
targetSdk: 34
compileSdk: 34
JVM Target: 17
```

---

## 🔐 Netzwerk-Sicherheit

Die App erlaubt HTTP-Cleartext-Traffic (`network_security_config.xml`), da die Mehrheit der IPTV-Streams unverschlüsselte HTTP-URLs nutzt.

**Hinweis**: In Produktionsumgebungen sollte wann immer möglich HTTPS verwendet werden.

---

## 📱 Android TV Features

- ✅ Leanback Launcher Intent
- ✅ D-Pad Navigation vorbereitet
- ✅ TV-Banner (320x180dp) für Homescreen
- ✅ Picture-in-Picture Support
- ✅ Foreground Service für Media-Playback

---

## 📋 Nächste Schritte

### Phase 2: API & Datenbank (Xtream + M3U + Room)

**Geplante Dateien:**
1. `XtreamApiService.kt` - Retrofit Interface
2. `M3uParser.kt` - Eigenentwicklung für M3U-Parsing
3. `ProviderEntity.kt` + `ProviderDao.kt`
4. `CategoryEntity.kt` + `CategoryDao.kt`
5. `StreamEntity.kt` + `StreamDao.kt`
6. `EpgEventEntity.kt` + `EpgEventDao.kt`
7. `VodProgressEntity.kt` + `VodProgressDao.kt`
8. `AppDatabase.kt` - Room Database
9. `NetworkModule.kt` - Hilt für Retrofit
10. `DatabaseModule.kt` - Hilt für Room

---

## 📝 Lizenz

Dieses Projekt ist für Bildungszwecke gedacht.

---

## 👨‍💻 Entwickler

Entwickelt nach den Spezifikationen von **DjouDjousIPTV.txt**

**Lead Android TV Architect & Senior Kotlin Developer**
