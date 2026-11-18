package com.yasha.newsscope.domain.repository

import androidx.paging.PagingData
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.SearchSortOrder
import kotlinx.coroutines.flow.Flow

interface ArticlesRepository {
    fun topHeadlines(sourceId: String): Flow<PagingData<Article>>

    fun search(query: String, sourceId: String?, sortOrder: SearchSortOrder): Flow<PagingData<Article>>

    /** A handful of current headlines for the home screen's featured section. */
    suspend fun featuredHeadlines(limit: Int): Result<List<Article>>
}
