package com.yasha.newsscope.core.error

/**
 * Structured failure model shared by all layers. Pure Kotlin so the domain layer can depend on it.
 * Presentation maps each case to a user-facing message.
 */
sealed class NewsError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** Whether retrying the same request can plausibly succeed. */
    open val isRetryable: Boolean get() = true

    class NoConnection(cause: Throwable? = null) : NewsError("No internet connection", cause)
    class Timeout(cause: Throwable? = null) : NewsError("Request timed out", cause)
    class ApiKeyMissing : NewsError("NewsAPI key is not configured") {
        override val isRetryable: Boolean get() = false
    }
    class Unauthorized(detail: String? = null) : NewsError(detail ?: "HTTP 401: invalid API key") {
        override val isRetryable: Boolean get() = false
    }
    /** HTTP 426 – NewsAPI uses it when a plan's result window is exceeded. */
    class UpgradeRequired(detail: String? = null) : NewsError(detail ?: "HTTP 426: upgrade required") {
        override val isRetryable: Boolean get() = false
    }
    class RateLimited(detail: String? = null) : NewsError(detail ?: "HTTP 429: too many requests")
    class Server(val code: Int, detail: String? = null) : NewsError(detail ?: "HTTP $code: server error")
    class Http(val code: Int, detail: String? = null) : NewsError(detail ?: "HTTP $code")
    class InvalidResponse(cause: Throwable? = null, detail: String? = null) :
        NewsError(detail ?: "Invalid response from server", cause)
    class Unknown(cause: Throwable? = null) : NewsError(cause?.message ?: "Unknown error", cause)
}
