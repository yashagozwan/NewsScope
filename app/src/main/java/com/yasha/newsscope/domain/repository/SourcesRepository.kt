package com.yasha.newsscope.domain.repository

import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.model.NewsSource

interface SourcesRepository {
    /** [category] null returns every source. Failures are [com.yasha.newsscope.core.error.NewsError] instances. */
    suspend fun getSources(
        category: NewsCategory?,
        language: String? = null,
        country: String? = null,
    ): Result<List<NewsSource>>
}
