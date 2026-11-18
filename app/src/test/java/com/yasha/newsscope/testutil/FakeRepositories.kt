package com.yasha.newsscope.testutil

import androidx.paging.PagingData
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.model.NewsSource
import com.yasha.newsscope.domain.model.SearchSortOrder
import com.yasha.newsscope.domain.repository.ArticlesRepository
import com.yasha.newsscope.domain.repository.SourcesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSourcesRepository(var result: Result<List<NewsSource>>) : SourcesRepository {
    var calls = 0
    var lastCategory: NewsCategory? = null
    override suspend fun getSources(category: NewsCategory?, language: String?, country: String?): Result<List<NewsSource>> {
        calls++
        lastCategory = category
        return result
    }
}

class FakeArticlesRepository(
    var featured: Result<List<Article>> = Result.success(emptyList()),
) : ArticlesRepository {
    val searchQueries = mutableListOf<Pair<String, String?>>()
    val headlineSources = mutableListOf<String>()

    override fun topHeadlines(sourceId: String): Flow<PagingData<Article>> {
        headlineSources += sourceId
        return flowOf(PagingData.from(listOf(article("https://a.com/1"))))
    }

    override fun search(query: String, sourceId: String?, sortOrder: SearchSortOrder): Flow<PagingData<Article>> {
        searchQueries += query to sourceId
        return flowOf(PagingData.from(listOf(article("https://a.com/$query"))))
    }

    override suspend fun featuredHeadlines(limit: Int): Result<List<Article>> = featured
}
