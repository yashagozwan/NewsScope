package com.yasha.newsscope.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yasha.newsscope.data.mapper.toDomain
import com.yasha.newsscope.data.remote.datasource.NewsRemoteDataSource
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.model.SearchSortOrder
import com.yasha.newsscope.domain.repository.ArticlesRepository
import kotlinx.coroutines.flow.Flow

class ArticlesRepositoryImpl(private val remote: NewsRemoteDataSource) : ArticlesRepository {

    override fun topHeadlines(sourceId: String): Flow<PagingData<Article>> =
        pager { page, size ->
            remote.getTopHeadlines(sources = sourceId, page = page, pageSize = size).map { it.toDomain() }
        }

    override fun search(query: String, sourceId: String?, sortOrder: SearchSortOrder): Flow<PagingData<Article>> =
        pager { page, size ->
            remote.searchEverything(query, sourceId, sortOrder.apiValue, page, size).map { it.toDomain() }
        }

    override suspend fun featuredHeadlines(limit: Int): Result<List<Article>> =
        remote.getTopHeadlines(
            country = FEATURED_COUNTRY,
            category = NewsCategory.GENERAL.apiValue,
            page = 1,
            pageSize = limit,
        ).map { dto -> dto.toDomain().articles.filter { it.imageUrl != null }.ifEmpty { dto.toDomain().articles } }

    private fun pager(
        fetch: suspend (page: Int, pageSize: Int) -> Result<com.yasha.newsscope.domain.model.ArticlePage>,
    ): Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE, // keeps server page numbers aligned with Paging's load sizes
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { ArticlesPagingSource(fetch) },
    ).flow

    companion object {
        const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
        private const val FEATURED_COUNTRY = "us"
    }
}
