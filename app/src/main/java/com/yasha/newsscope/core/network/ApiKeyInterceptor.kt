package com.yasha.newsscope.core.network

import okhttp3.Interceptor
import okhttp3.Response

/** Sends the key in the `X-Api-Key` header so it never appears in URLs or logs. */
class ApiKeyInterceptor(private val provider: ApiKeyProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(HEADER_NAME, provider.apiKey)
            .build()
        return chain.proceed(request)
    }

    companion object {
        const val HEADER_NAME = "X-Api-Key"
    }
}
