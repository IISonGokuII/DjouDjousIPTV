# DjouDjous IPTV

**Next-Gen IPTV Player für Android TV & Mobile**

Eine native, hochmoderne und extrem performante IPTV-Appliance mit modernem Design (Glassmorphism, Dark Theme, tv-material Focus-Effekte).

---

## 🎯 Projektstatus

### ✅ Alle Phasen Abgeschlossen!

| Phase | Status | Dateien | Zeilen |
|-------|--------|--------|--------|
| Phase 1: Core Setup & DataStore | ✅ Fertig | 25 | ~1.600 |
| Phase 2: API & Datenbank | ✅ Fertig | 17 | ~3.400 |
| Phase 3: Smart Onboarding | ✅ Fertig | 9 | ~2.300 |
| Phase 4: Media3 Player Engine | ✅ Fertig | 12 | ~2.600 |
| Phase 5: Dashboard & Navigation | ✅ Fertig | 10 | ~1.700 |
| Phase 6: TV Integration & Polish | ✅ Fertig | 6 | ~800 |
| **Gesamt** | **✅ Complete** | **79** | **~12.400** |

---

## 🏗 Architektur-Übersicht

### Clean Architecture (Data / Domain / Presentation)

```
app/
├── data/
│   ├── preferences/      # DataStore Settings
│   ├── remote/           # Retrofit API (Xtream, M3U Parser)
│   ├── local/            # Room Database (Entities, DAOs)
│   └── repository/       # Repositories
├── domain/
│   ├── model/            # Entities, Result<T>
│   └── usecase/          # Use Cases
├── presentation/
│   ├── onboarding/       # Onboarding UI (Phase 3)
│   ├── home/             # Main Dashboard (Phase 5)
│   ├── livetv/           # Live-TV Screen (Phase 5)
│   ├── vod/              # VOD Screen (Phase 5)
│   ├── series/           # Serien Screen (Phase 5)
│   ├── settings/         # Settings Screen (Phase 5)
│   └── player/           # Video Player (Phase 4)
├── di/                   # Hilt Modules
├── service/              # Services (PlaybackService)
├── worker/               # WorkManager Workers
└── receiver/             # Broadcast Receivers
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
   ```bash
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

## 📱 Features

### Phase 1: Core Setup & DataStore
- ✅ Build-Konfiguration mit allen Dependencies
- ✅ SettingsPreferencesManager (DataStore)
- ✅ Hilt Dependency Injection
- ✅ ProGuard Rules

### Phase 2: API & Datenbank
- ✅ Xtream Codes API (alle Endpoints)
- ✅ M3U-Parser (Eigenentwicklung)
- ✅ Room Database mit 5 Entities
- ✅ Flow-basierte DAOs

### Phase 3: Smart Onboarding
- ✅ Provider-Auswahl (Xtream vs M3U)
- ✅ Login-Screen (Xtream Credentials)
- ✅ M3U-Import (URL oder Datei)
- ✅ Kategorie-Filter (Select All/None/Invert)
- ✅ Synchronisation in Room

### Phase 4: Media3 Player Engine
- ✅ ExoPlayer mit LoadControl
- ✅ Buffer-Konfiguration (SMALL, NORMAL, LARGE, CUSTOM)
- ✅ Hardware/Software Decoder
- ✅ Auto Frame Rate (Android TV)
- ✅ Picture-in-Picture
- ✅ Instant Zapping
- ✅ OSD mit D-Pad Navigation
- ✅ Audio/Subtitle Track Selection

### Phase 5: Dashboard & Navigation
- ✅ Main Dashboard mit Tabs
- ✅ Live-TV Screen (Kategorien + Streams)
- ✅ VOD Screen (Grid Layout)
- ✅ Series Screen (Liste)
- ✅ Settings Screen (alle Optionen)
- ✅ Parental PIN Dialog
- ✅ EPG Worker (WorkManager)

### Phase 6: TV Integration & Polish
- ✅ TV Channels API (Watch Next)
- ✅ Boot Receiver (Autostart)
- ✅ App Update Worker
- ✅ TV Banner (320x180)
- ✅ Vollständige ProGuard Rules

---

## 📺 Android TV Features

- ✅ Leanback Launcher Intent
- ✅ D-Pad Navigation
- ✅ TV-Banner (320x180dp)
- ✅ Picture-in-Picture Support
- ✅ Foreground Service für Media-Playback
- ✅ Auto Frame Rate (AFR)
- ✅ Watch Next Integration
- ✅ Boot Autostart

---

## 🎨 UI-Design

### Glassmorphism Dark Theme
- Semi-transparente Overlays
- Blur-Effekte für OSD
- Dark Surface Colors (#0F0F0F)
- Primary: #6366F1 (Indigo)
- Accent: #22D3EE (Cyan)

### TV-Optimierte Components
- TvLazyColumn / TvLazyGrid
- Focus-Effekte (Scale, Border, Glow)
- D-Pad Navigation
- 48dp Minimum Touch Targets

---

## 📋 Berechtigungen

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
<uses-permission android:name="android.permission.WRITE_TV_PROGRAMS"/>
```

---

## 🐛 Bekannte Einschränkungen

1. **TV Channels API**: Erfordert manuelle Berechtigung auf einigen TVs
2. **AFR**: Nur auf Android TV mit Hardware-Support
3. **M3U-Parser**: Sehr große Dateien (>10.000 Zeilen) können langsam sein

---

## 📝 Lizenz

Dieses Projekt ist für Bildungszwecke gedacht.

---

## 👨‍💻 Entwickler

Entwickelt nach den Spezifikationen von **DjouDjousIPTV.txt**

**Lead Android TV Architect & Senior Kotlin Developer**

---

## 🔗 GitHub Repository

https://github.com/IISonGokuII/DjouDjousIPTV
