package com.yasha.newsscope.domain.usecase

import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.testutil.FakeSourcesRepository
import com.yasha.newsscope.testutil.source
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceUseCasesTest {
    private val sources = listOf(
        source("bbc-news", "BBC News", "British public broadcaster", "gb"),
        source("cnn", "CNN", "Breaking news", "us"),
        source("the-verge", "The Verge", null, null),
    )
    private val filter = FilterSourcesUseCase()

    @Test fun `blank query returns every source`() = assertEquals(sources, filter(sources, "  "))

    @Test fun `matching is case insensitive on name and description`() {
        assertEquals(listOf("bbc-news"), filter(sources, "bBc").map { it.id })
        assertEquals(listOf("cnn"), filter(sources, "BREAKING").map { it.id })
    }

    @Test fun `no match yields empty list`() = assertTrue(filter(sources, "zzz").isEmpty())

    @Test fun `source without description or country does not crash filtering`() =
        assertEquals(listOf("the-verge"), filter(sources, "verge").map { it.id })

    @Test fun `page applies filter before pagination`() {
        val many = (1..40).map { source("s$it", "Source $it") } + source("other", "Other")
        val pager = GetSourcePageUseCase(filter)

        val first = pager(many, "source", visibleCount = 15)
        assertEquals(15, first.items.size)
        assertEquals(40, first.totalMatches)
        assertTrue(first.hasMore)

        val all = pager(many, "source", visibleCount = 60)
        assertEquals(40, all.items.size)
        assertFalse(all.hasMore)
    }

    @Test fun `get sources by category delegates to repository and passes the category`() = runTest {
        val repo = FakeSourcesRepository(Result.success(sources))
        val result = GetSourcesByCategoryUseCase(repo)(NewsCategory.SCIENCE)
        assertEquals(sources, result.getOrThrow())
        assertEquals(NewsCategory.SCIENCE, repo.lastCategory)
    }

    @Test fun `get sources propagates repository failure`() = runTest {
        val repo = FakeSourcesRepository(Result.failure(NewsError.NoConnection()))
        assertTrue(GetSourcesByCategoryUseCase(repo)(NewsCategory.GENERAL).exceptionOrNull() is NewsError.NoConnection)
    }

    @Test fun `categories are the seven NewsAPI categories`() {
        val categories = GetCategoriesUseCase()()
        assertEquals(
            listOf("business", "entertainment", "general", "health", "science", "sports", "technology"),
            categories.map { it.apiValue },
        )
    }
}
