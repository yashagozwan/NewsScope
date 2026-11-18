package com.yasha.newsscope.data.remote.datasource

import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.data.remote.dto.ApiErrorDto
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Translates transport/HTTP/serialization failures into [NewsError]. */
class NewsErrorMapper(private val json: Json) {

    fun map(throwable: Throwable): NewsError = when (throwable) {
        is NewsError -> throwable
        is HttpException -> mapHttp(throwable)
        is SocketTimeoutException -> NewsError.Timeout(throwable)
        is IOException -> NewsError.NoConnection(throwable)
        is SerializationException, is IllegalArgumentException -> NewsError.InvalidResponse(throwable)
        else -> NewsError.Unknown(throwable)
    }

    /** Maps a 200 response whose body says `status: error`. */
    fun mapApiError(code: String?, message: String?): NewsError = when (code) {
        "apiKeyMissing", "apiKeyInvalid", "apiKeyDisabled", "apiKeyExhausted" -> NewsError.Unauthorized(message)
        "rateLimited" -> NewsError.RateLimited(message)
        "maximumResultsReached" -> NewsError.UpgradeRequired(message)
        else -> NewsError.InvalidResponse(detail = message)
    }

    private fun mapHttp(e: HttpException): NewsError {
        val body = parseBody(e)
        val message = body?.message
        return when (val status = e.code()) {
            401 -> NewsError.Unauthorized(message)
            426 -> NewsError.UpgradeRequired(message)
            429 -> NewsError.RateLimited(message)
            in 500..599 -> NewsError.Server(status, message)
            else -> NewsError.Http(status, message)
        }
    }

    private fun parseBody(e: HttpException): ApiErrorDto? = try {
        e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
            ?.let { json.decodeFromString<ApiErrorDto>(it) }
    } catch (c: CancellationException) {
        throw c
    } catch (_: Exception) {
        null
    }
}
