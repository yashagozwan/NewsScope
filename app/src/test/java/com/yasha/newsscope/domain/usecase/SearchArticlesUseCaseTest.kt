package com.yasha.newsscope.domain.usecase

import androidx.paging.testing.asSnapshot
import com.yasha.newsscope.testutil.FakeArticlesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchArticlesUseCaseTest {
    private val repo = FakeArticlesRepository()
    private val useCase = SearchArticlesUseCase(repo)

    @Test fun `blank query never reaches the repository`() = runTest {
        assertTrue(useCase("   ").asSnapshot().isEmpty())
        assertTrue(repo.searchQueries.isEmpty())
    }

    @Test fun `query is trimmed and scoped to source`() = runTest {
        useCase("  kotlin ", sourceId = "bbc-news").asSnapshot()
        assertEquals(listOf("kotlin" to "bbc-news"), repo.searchQueries)
    }
}
