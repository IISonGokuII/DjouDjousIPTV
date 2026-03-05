# ═══════════════════════════════════════════════════════════════════════════════
# PROGUARD / R8 RULES FÜR DJOUDJOUS IPTV
# ═══════════════════════════════════════════════════════════════════════════════
#
# Diese Datei enthält ProGuard-Rules für alle in Phase 1 verwendeten Bibliotheken.
# Weitere Rules werden in zukünftigen Phasen hinzugefügt (Room, Retrofit, Media3, etc.)

# ═══════════════════════════════════════════════════════════════════════════════
# DAGGER HILT
# ═══════════════════════════════════════════════════════════════════════════════

# Hilt verwendet Reflection für Dependency Injection.
# Diese Rules stellen sicher, dass Hilt-Klassen nicht entfernt werden.

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Hilt Generated Code
-keep class * extends com.djoudjou.iptv.Hilt_Application { *; }
-keep class * extends com.djoudjou.iptv.Hilt_Activity { *; }
-keep class * extends com.djoudjou.iptv.Hilt_Fragment { *; }
-keep class * extends com.djoudjou.iptv.Hilt_Service { *; }
-keep class * extends com.djoudjou.iptv.Hilt_BroadcastReceiver { *; }

# Hilt Worker Factory (für zukünftige WorkManager-Integration)
-keep class * extends com.djoudjou.iptv.Hilt_Worker { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# KOTLIN SERIALIZATION
# ═══════════════════════════════════════════════════════════════════════════════

# Kotlin Serialization verwendet Reflection für JSON-(De)Serialisierung.
# Data Classes mit @Serializable Annotation müssen erhalten bleiben.

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep companion objects for serializers
-keepclassmembers class ** {
    public static ** Companion;
}

# Keep serializer() methods
-keepclassmembers class ** {
    public static kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes
-keep @kotlinx.serialization.Serializable class **
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}

# ═══════════════════════════════════════════════════════════════════════════════
# ANDROIDX DATASTORE
# ═══════════════════════════════════════════════════════════════════════════════

# DataStore verwendet ProtoBuf und Reflection.
# Preferences-Keys müssen erhalten bleiben.

-keep class androidx.datastore.** { *; }
-keep class com.google.protobuf.** { *; }

# Keep Preference Keys
-keep class com.djoudjou.iptv.data.preferences.** { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# KOTLIN REFLECTION & COROUTINES
# ═══════════════════════════════════════════════════════════════════════════════

# Kotlin Standard Library
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }

# Coroutines
-keepclassmembers class ** {
    @kotlinx.coroutines.**) *;
}

# ═══════════════════════════════════════════════════════════════════════════════
# ANDROIDX TV MATERIAL & COMPOSE
# ═══════════════════════════════════════════════════════════════════════════════

# Compose und TV Material verwenden Reflection für UI-Rendering.

-keep class androidx.compose.** { *; }
-keep class androidx.tv.** { *; }
-keep class androidx.lifecycle.** { *; }

# Keep Compose generated classes
-keep class **$$Companion { *; }
-keepclassmembers class ** {
    public static ** Companion;
}

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 2: ROOM, RETROFIT, OKHTTP
# ═══════════════════════════════════════════════════════════════════════════════

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Room Entities (für Serialisierung)
-keep class com.djoudjou.iptv.data.local.ProviderEntity
-keep class com.djoudjou.iptv.data.local.CategoryEntity
-keep class com.djoudjou.iptv.data.local.StreamEntity
-keep class com.djoudjou.iptv.data.local.EpgEventEntity
-keep class com.djoudjou.iptv.data.local.VodProgressEntity

# Room DAOs
-keep class com.djoudjou.iptv.data.local.ProviderDao
-keep class com.djoudjou.iptv.data.local.CategoryDao
-keep class com.djoudjou.iptv.data.local.StreamDao
-keep class com.djoudjou.iptv.data.local.EpgEventDao
-keep class com.djoudjou.iptv.data.local.VodProgressDao

# Room Type Converters
-keep class com.djoudjou.iptv.data.local.Converters
-keepclassmembers class com.djoudjou.iptv.data.local.Converters { *; }

# Retrofit & OkHttp
-dontnote retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okhttp3.**
-dontwarn okio.**

# Xtream API Models (Kotlin Serialization)
-keep class com.djoudjou.iptv.data.remote.XtreamAuthResponse
-keep class com.djoudjou.iptv.data.remote.XtreamUserInfo
-keep class com.djoudjou.iptv.data.remote.XtreamServerInfo
-keep class com.djoudjou.iptv.data.remote.XtreamCategoryResponse
-keep class com.djoudjou.iptv.data.remote.XtreamLiveStreamResponse
-keep class com.djoudjou.iptv.data.remote.XtreamVodResponse
-keep class com.djoudjou.iptv.data.remote.XtreamSeriesResponse
-keep class com.djoudjou.iptv.data.remote.XtreamSeriesInfoResponse
-keep class com.djoudjou.iptv.data.remote.XtreamSeason
-keep class com.djoudjou.iptv.data.remote.XtreamEpisode
-keep class com.djoudjou.iptv.data.remote.XtreamEpisodeInfo
-keep class com.djoudjou.iptv.data.remote.XtreamEpgResponse
-keep class com.djoudjou.iptv.data.remote.XtreamVodInfoResponse
-keep class com.djoudjou.iptv.data.remote.XtreamVodInfo
-keep class com.djoudjou.iptv.data.remote.XtreamVodData
-keep class com.djoudjou.iptv.data.remote.XtreamVideoInfo
-keep class com.djoudjou.iptv.data.remote.Rating

# Media3 / ExoPlayer (wird in Phase 4 benötigt)
-keep class androidx.media3.** { *; }
-dontwarn com.google.android.exoplayer2.**

# Coil (Image Loading, wird in Phase 3-5 benötigt)
-keep class coil.** { *; }
-keep class coil3.** { *; }

# WorkManager (wird in Phase 5-6 benötigt)
-keep class androidx.work.** { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# ALLGEMEINE ANDROID RULES
# ═══════════════════════════════════════════════════════════════════════════════

# Keep Android Components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep R (resources)
-keep class com.djoudjou.iptv.R { *; }
-keep class com.djoudjou.iptv.R$* { *; }

# Keep BuildConfig
-keep class com.djoudjou.iptv.BuildConfig { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# DEBUGGING & LOGGING
# ═══════════════════════════════════════════════════════════════════════════════

# Für Debug-Builds: Stacktraces lesbar halten
-keepattributes SourceFile,LineNumberTable

# Für Release-Builds: Mapping-Datei für Crash-Reports
# (wird automatisch in build.gradle.kts konfiguriert)
