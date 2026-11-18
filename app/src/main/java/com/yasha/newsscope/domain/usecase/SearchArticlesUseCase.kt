package com.yasha.newsscope.domain.usecase

import androidx.paging.PagingData
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.SearchSortOrder
import com.yasha.newsscope.domain.repository.ArticlesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SearchArticlesUseCase(private val repository: ArticlesRepository) {
    /** A blank query never reaches the network; it yields an empty paging stream. */
    operator fun invoke(
        query: String,
        sourceId: String? = null,
        sortOrder: SearchSortOrder = SearchSortOrder.PUBLISHED_AT,
    ): Flow<PagingData<Article>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return flowOf(PagingData.empty())
        return repository.search(trimmed, sourceId, sortOrder)
    }
}
