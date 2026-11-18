package com.yasha.newsscope.core.util

import java.net.URI

/** Decides which URLs the in-app WebView is allowed to load. */
object UrlSafety {
    /**
     * Returns an https URL for a valid http(s) web address, or null for anything else
     * (blank, malformed, `javascript:`, `file:`, `intent:`, ...). Plain http is upgraded to https
     * because cleartext traffic is disabled.
     */
    fun toSafeWebUrl(raw: String?): String? {
        val candidate = raw?.trim().orEmpty()
        if (candidate.isEmpty() || candidate.any { it.isWhitespace() }) return null
        val uri = try {
            URI(candidate)
        } catch (_: Exception) {
            return null
        }
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme != "http" && scheme != "https") return null
        if (uri.host.isNullOrBlank()) return null
        return if (scheme == "http") "https" + candidate.substring(4) else candidate
    }

    fun isAllowedNavigation(url: String?): Boolean = toSafeWebUrl(url)?.let { true } ?: false
}
