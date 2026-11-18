package com.yasha.newsscope.data.repository

import androidx.paging.PagingSource
import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.ArticlePage
import com.yasha.newsscope.testutil.page
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticlesPagingSourceTest {
    private fun refresh(size: Int = 2, key: Int? = null) =
        PagingSource.LoadParams.Refresh(key, size, false)

    private fun append(key: Int, size: Int = 2) = PagingSource.LoadParams.Append(key, size, false)

    private fun PagingSource.LoadResult<Int, Article>.asPage() = this as PagingSource.LoadResult.Page<Int, Article>

    @Test fun `first load requests page 1 and exposes next key`() = runTest {
        val requested = mutableListOf<Pair<Int, Int>>()
        val source = ArticlesPagingSource { p, s -> requested += p to s; Result.success(page("a", "b", total = 10)) }

        val result = source.load(refresh()).asPage()

        assertEquals(listOf(1 to 2), requested)
        assertEquals(listOf("a", "b"), result.data.map { it.url })
        assertEquals(2, result.nextKey)
        assertNull(result.prevKey)
    }

    @Test fun `subsequent page uses the append key`() = runTest {
        val source = ArticlesPagingSource { p, _ -> Result.success(page("p$p-a", "p$p-b", total = 10)) }
        val result = source.load(append(key = 3)).asPage()
        assertEquals(4, result.nextKey)
        assertEquals("p3-a", result.data.first().url)
    }

    @Test fun `pagination ends when total results are reached`() = runTest {
        val source = ArticlesPagingSource { _, _ -> Result.success(page("a", "b", total = 4)) }
        assertNull(source.load(append(key = 2)).asPage().nextKey)
    }

    @Test fun `pagination ends on a short or empty page`() = runTest {
        assertNull(ArticlesPagingSource { _, _ -> Result.success(page("a", total = 99)) }.load(refresh()).asPage().nextKey)
        assertNull(ArticlesPagingSource { _, _ -> Result.success(page(total = 99)) }.load(refresh()).asPage().nextKey)
    }

    @Test fun `fetched count rather than filtered count decides exhaustion`() = runTest {
        // 2 raw items came back but one was discarded by the mapper: more pages may still exist.
        val source = ArticlesPagingSource { _, _ -> Result.success(ArticlePage(page("a").articles, 50, fetchedCount = 2)) }
        assertEquals(2, source.load(refresh()).asPage().nextKey)
    }

    @Test fun `duplicate articles across pages are emitted once`() = runTest {
        val pages = ArrayDeque(listOf(page("a", "b", total = 10), page("b", "c", total = 10)))
        val source = ArticlesPagingSource { _, _ -> Result.success(pages.removeFirst()) }

        val first = source.load(refresh()).asPage().data.map { it.url }
        val second = source.load(append(2)).asPage().data.map { it.url }

        assertEquals(listOf("a", "b"), first)
        assertEquals(listOf("c"), second)
    }

    @Test fun `failure is reported as error and retry succeeds`() = runTest {
        var fail = true
        val source = ArticlesPagingSource { _, _ ->
            if (fail) Result.failure(NewsError.RateLimited()) else Result.success(page("a", "b", total = 10))
        }

        val error = source.load(append(2))
        assertTrue((error as PagingSource.LoadResult.Error).throwable is NewsError.RateLimited)

        fail = false
        assertEquals(listOf("a", "b"), source.load(append(2)).asPage().data.map { it.url })
    }
}
