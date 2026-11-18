package com.yasha.newsscope.domain.usecase

import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.repository.ArticlesRepository

class GetFeaturedHeadlinesUseCase(private val repository: ArticlesRepository) {
    suspend operator fun invoke(limit: Int = DEFAULT_LIMIT): Result<List<Article>> =
        repository.featuredHeadlines(limit)

    private companion object {
        const val DEFAULT_LIMIT = 5
    }
}
