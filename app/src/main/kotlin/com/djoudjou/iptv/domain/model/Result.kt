package com.djoudjou.iptv.domain.model

/**
 * Result<T> - Sealed Class für strukturierte Fehlerbehandlung.
 *
 * Jede Repository-Methode gibt ein Result<T> zurück:
 * - Success: Operation erfolgreich mit Daten
 * - Error: Operation fehlgeschlagen mit Fehlermeldung
 * - Loading: Operation wird ausgeführt (für UI-Loading-States)
 *
 * THREAD-SAFETY: Diese Klasse ist immutable und damit thread-safe.
 */
sealed class Result<out T> {

    /**
     * Erfolg mit Daten.
     *
     * @param data Die zurückgegebenen Daten
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Fehler mit Fehlermeldung und optionalem Exception.
     *
     * @param message Fehlermeldung für die UI
     * @param exception Optionale Exception für Logging
     * @param code Optionaler Error-Code für spezifische Fehlerbehandlung
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val code: ErrorCode = ErrorCode.UNKNOWN
    ) : Result<Nothing>()

    /**
     * Loading-State für UI-Feedback.
     */
    object Loading : Result<Nothing>()

    /**
     * Error-Codes für spezifische Fehlerbehandlung.
     */
    enum class ErrorCode {
        UNKNOWN,
        NETWORK_ERROR,
        TIMEOUT,
        AUTH_ERROR,
        INVALID_CREDENTIALS,
        SERVER_ERROR,
        PARSE_ERROR,
        DATABASE_ERROR,
        NOT_FOUND,
        PERMISSION_DENIED
    }
}

/**
 * Hilfsfunktionen für Result<T>.
 */

/**
 * Prüft ob Result ein Success ist.
 */
inline val <T> Result<T>.isSuccess: Boolean
    get() = this is Result.Success

/**
 * Prüft ob Result ein Error ist.
 */
inline val <T> Result<T>.isError: Boolean
    get() = this is Result.Error

/**
 * Prüft ob Result Loading ist.
 */
inline val <T> Result<T>.isLoading: Boolean
    get() = this is Result.Loading

/**
 * Extrahiert die Daten aus einem Success oder wirft Exception.
 */
fun <T> Result<T>.getOrNull(): T? {
    return (this as? Result.Success)?.data
}

/**
 * Extrahiert die Daten aus einem Success oder wirft Exception.
 */
@Throws(IllegalStateException::class)
fun <T> Result<T>.getOrThrow(): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> throw exception ?: IllegalStateException(message)
        is Result.Loading -> throw IllegalStateException("Result is Loading")
    }
}

/**
 * Extrahiert die Daten oder gibt einen Default-Wert zurück.
 */
fun <T> Result<T>.getOrElse(defaultValue: T): T {
    return (this as? Result.Success)?.data ?: defaultValue
}

/**
 * Extrahiert die Daten oder führt eine Aktion aus.
 */
inline fun <T> Result<T>.getOrElse(onError: (Result.Error) -> T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> onError(this)
        is Result.Loading -> throw IllegalStateException("Result is Loading")
    }
}

/**
 * Führt eine Aktion nur bei Success aus.
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Führt eine Aktion nur bei Error aus.
 */
inline fun <T> Result<T>.onError(action: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(this)
    }
    return this
}

/**
 * Führt eine Aktion nur bei Loading aus.
 */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) {
        action()
    }
    return this
}

/**
 * Transformiert die Daten bei Success.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        is Result.Loading -> Result.Loading
    }
}

/**
 * Transformiert ein Result in ein anderes Result.
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> this
        is Result.Loading -> Result.Loading
    }
}

/**
 * Konvertiert eine Exception in ein Result.Error.
 */
fun <T> Throwable.toResultError(
    message: String = this.message ?: "Unknown error",
    code: Result.ErrorCode = when (this) {
        is java.net.UnknownHostException -> Result.ErrorCode.NETWORK_ERROR
        is java.net.SocketTimeoutException -> Result.ErrorCode.TIMEOUT
        is retrofit2.HttpException -> {
            when (code()) {
                401, 403 -> Result.ErrorCode.AUTH_ERROR
                404 -> Result.ErrorCode.NOT_FOUND
                in 500..599 -> Result.ErrorCode.SERVER_ERROR
                else -> Result.ErrorCode.UNKNOWN
            }
        }
        is kotlinx.serialization.SerializationException -> Result.ErrorCode.PARSE_ERROR
        else -> Result.ErrorCode.UNKNOWN
    }
): Result.Error {
    return Result.Error(message = message, exception = this, code = code)
}

/**
 * Erstellt ein Result.Success.
 */
fun <T> successOf(data: T): Result<T> = Result.Success(data)

/**
 * Erstellt ein Result.Error.
 */
fun errorOf(
    message: String,
    exception: Throwable? = null,
    code: Result.ErrorCode = Result.ErrorCode.UNKNOWN
): Result.Error = Result.Error(message, exception, code)

/**
 * Erstellt ein Result.Loading.
 */
fun loadingOf(): Result.Loading = Result.Loading
