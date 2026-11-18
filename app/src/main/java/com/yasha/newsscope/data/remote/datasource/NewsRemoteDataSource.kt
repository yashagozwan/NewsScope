package com.yasha.newsscope.data.remote.datasource

import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.core.network.ApiKeyProvider
import com.yasha.newsscope.data.remote.api.ArticlesApi
import com.yasha.newsscope.data.remote.api.SourcesApi
import com.yasha.newsscope.data.remote.dto.ArticlesResponseDto
import com.yasha.newsscope.data.remote.dto.SourcesResponseDto
import kotlinx.coroutines.CancellationException

/**
 * Single entry point to NewsAPI. Verifies the key is configured, executes the call and converts
 * every failure (including `status: error` bodies) into a [NewsError].
 */
class NewsRemoteDataSource(
    private val sourcesApi: SourcesApi,
    private val articlesApi: ArticlesApi,
    private val apiKeyProvider: ApiKeyProvider,
    private val errorMapper: NewsErrorMapper,
) {
    suspend fun getSources(category: String?, language: String?, country: String?): Result<SourcesResponseDto> =
        execute { sourcesApi.getSources(category, language, country) }
            .checkedSources()

    suspend fun getTopHeadlines(
        sources: String? = null,
        country: String? = null,
        category: String? = null,
        page: Int,
        pageSize: Int,
    ): Result<ArticlesResponseDto> =
        execute { articlesApi.getTopHeadlines(sources, country, category, page, pageSize) }
            .checkedArticles()

    suspend fun searchEverything(
        query: String,
        sources: String?,
        sortBy: String,
        page: Int,
        pageSize: Int,
    ): Result<ArticlesResponseDto> =
        execute { articlesApi.searchEverything(query, sources, sortBy, page, pageSize) }
            .checkedArticles()

    private suspend fun <T> execute(call: suspend () -> T): Result<T> {
        if (!apiKeyProvider.isConfigured) return Result.failure(NewsError.ApiKeyMissing())
        return try {
            Result.success(call())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(errorMapper.map(e))
        }
    }

    private fun Result<SourcesResponseDto>.checkedSources(): Result<SourcesResponseDto> = mapCatching { dto ->
        if (dto.status == STATUS_OK && dto.sources != null) dto
        else throw errorMapper.mapApiError(dto.code, dto.message)
    }

    private fun Result<ArticlesResponseDto>.checkedArticles(): Result<ArticlesResponseDto> = mapCatching { dto ->
        if (dto.status == STATUS_OK && dto.articles != null) dto
        else throw errorMapper.mapApiError(dto.code, dto.message)
    }

    private companion object {
        const val STATUS_OK = "ok"
    }
}
