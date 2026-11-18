package com.yasha.newsscope.domain.usecase

import androidx.paging.PagingData
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.repository.ArticlesRepository
import kotlinx.coroutines.flow.Flow

class GetTopHeadlinesUseCase(private val repository: ArticlesRepository) {
    operator fun invoke(sourceId: String): Flow<PagingData<Article>> = repository.topHeadlines(sourceId)
}
