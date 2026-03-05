package com.djoudjou.iptv.di

import com.djoudjou.iptv.data.remote.XtreamApiService
import com.djoudjou.iptv.data.remote.M3uParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * NetworkModule - Hilt Module für Netzwerk-Komponenten.
 *
 * Stellt Singleton-Instanzen bereit für:
 * - OkHttpClient mit Logging Interceptor
 * - Retrofit mit Kotlin Serialization Converter
 * - XtreamApiService
 * - M3uParser
 *
 * LOGGING:
 * - Debug: BODY (vollständige Requests/Responses)
 * - Release: NONE (kein Logging aus Performance-Gründen)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ═══════════════════════════════════════════════════════════════════════════════
    // KONSTANTEN
    // ═══════════════════════════════════════════════════════════════════════════════

    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 60L
    private const val WRITE_TIMEOUT_SECONDS = 60L

    // ═══════════════════════════════════════════════════════════════════════════════
    // OKHTTP CLIENT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Stellt OkHttpClient als Singleton bereit.
     *
     * Konfiguration:
     * - Connect Timeout: 30s
     * - Read Timeout: 60s (für große M3U-Dateien)
     * - Write Timeout: 60s
     * - Logging Interceptor (nur Debug)
     *
     * @return OkHttpClient Instanz
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                // User-Agent Header für alle Requests
                val request = chain.request().newBuilder()
                    .header("User-Agent", "DjouDjousIPTV/1.0")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Stellt Json-Instanz für Kotlin Serialization bereit.
     *
     * Konfiguration:
     * - ignoreUnknownKeys: true (für API-Kompatibilität)
     * - isLenient: true (für lockeres Parsing)
     *
     * @return Json Instanz
     */
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // RETROFIT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Stellt Retrofit-Builder als Singleton bereit.
     *
     * @param okHttpClient OkHttpClient Instanz
     * @param Json Json Instanz
     * @return Retrofit.Builder
     */
    @Provides
    @Singleton
    fun provideRetrofitBuilder(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // XTREAM API SERVICE
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Stellt XtreamApiService als Singleton bereit.
     *
     * WICHTIG: Die Base-URL wird zur Laufzeit gesetzt, da jeder Provider
     * eine eigene Server-URL hat. Verwende Retrofit.newCall() für dynamische URLs.
     *
     * @param retrofitBuilder Retrofit.Builder
     * @return XtreamApiService Instanz
     */
    @Provides
    @Singleton
    fun provideXtreamApiService(retrofitBuilder: Retrofit.Builder): XtreamApiService {
        // Placeholder-URL - wird zur Laufzeit überschrieben
        return retrofitBuilder
            .baseUrl("http://placeholder.com/")
            .build()
            .create(XtreamApiService::class.java)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // M3U PARSER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Stellt M3uParser als Singleton bereit.
     *
     * @return M3uParser Instanz
     */
    @Provides
    @Singleton
    fun provideM3uParser(): M3uParser {
        return M3uParser()
    }

    /**
     * Hilfsfunktion um XtreamApiService mit dynamischer Base-URL zu erstellen.
     *
     * Wird verwendet wenn ein Provider eine andere Server-URL hat.
     *
     * @param baseUrl Die Server-URL des Providers
     * @param okHttpClient OkHttpClient Instanz
     * @param json Json Instanz
     * @return XtreamApiService mit spezifischer Base-URL
     */
    @Singleton
    @Provides
    fun provideXtreamApiServiceForProvider(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        json: Json
    ): XtreamApiService {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(baseUrl)
            .build()
            .create(XtreamApiService::class.java)
    }
}

/**
 * Hilfsfunktion für MediaType (da nicht direkt importierbar).
 */
fun String.toMediaType(): okhttp3.MediaType {
    return okhttp3.MediaType.parse(this)!!
}
