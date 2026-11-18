package com.yasha.newsscope.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.ArticlePage

/**
 * Server-side paging over a NewsAPI endpoint (1-based `page`).
 *
 * A new instance is created whenever the query changes, which resets pagination. The instance keeps
 * the URLs it has already emitted so the same article is never delivered twice, even when NewsAPI
 * shifts items between pages. Paging 3 serialises loads, so no concurrent duplicate requests occur.
 */
class ArticlesPagingSource(
    private val fetchPage: suspend (page: Int, pageSize: Int) -> Result<ArticlePage>,
) : PagingSource<Int, Article>() {

    private val seenUrls = HashSet<String>()

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: FIRST_PAGE
        return fetchPage(page, params.loadSize).fold(
            onSuccess = { result ->
                val fresh = result.articles.filter { seenUrls.add(it.url) }
                val exhausted = result.fetchedCount == 0 ||
                    result.fetchedCount < params.loadSize ||
                    page * params.loadSize >= result.totalResults
                LoadResult.Page(
                    data = fresh,
                    prevKey = null,
                    nextKey = if (exhausted) null else page + 1,
                )
            },
            onFailure = { LoadResult.Error(it) },
        )
    }

    companion object {
        const val FIRST_PAGE = 1
    }
}
