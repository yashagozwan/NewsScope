package com.yasha.newsscope.domain.usecase

import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.model.NewsSource
import com.yasha.newsscope.domain.repository.SourcesRepository

class GetSourcesByCategoryUseCase(private val repository: SourcesRepository) {
    suspend operator fun invoke(
        category: NewsCategory?,
        language: String? = null,
        country: String? = null,
    ): Result<List<NewsSource>> = repository.getSources(category, language, country)
}
