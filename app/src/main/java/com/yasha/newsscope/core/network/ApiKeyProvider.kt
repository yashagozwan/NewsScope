package com.yasha.newsscope.core.network

interface ApiKeyProvider {
    val apiKey: String
    val isConfigured: Boolean get() = apiKey.isNotBlank()
}

/** Reads the key injected at build time from the git-ignored secrets.properties. */
class BuildConfigApiKeyProvider(override val apiKey: String) : ApiKeyProvider
