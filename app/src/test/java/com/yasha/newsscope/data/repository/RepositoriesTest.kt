package com.yasha.newsscope.data.repository

import androidx.paging.testing.asSnapshot
import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.core.network.BuildConfigApiKeyProvider
import com.yasha.newsscope.data.remote.api.ArticlesApi
import com.yasha.newsscope.data.remote.api.SourcesApi
import com.yasha.newsscope.data.remote.datasource.NewsErrorMapper
import com.yasha.newsscope.data.remote.datasource.NewsRemoteDataSource
import com.yasha.newsscope.data.remote.dto.ArticleDto
import com.yasha.newsscope.data.remote.dto.ArticlesResponseDto
import com.yasha.newsscope.data.remote.dto.SourceDto
import com.yasha.newsscope.data.remote.dto.SourcesResponseDto
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.model.SearchSortOrder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RepositoriesTest {
    private val sourcesApi = mockk<SourcesApi>()
    private val articlesApi = mockk<ArticlesApi>()
    private val remote = NewsRemoteDataSource(
        sourcesApi, articlesApi, BuildConfigApiKeyProvider("key"), NewsErrorMapper(Json),
    )
    private val sourcesRepo = SourcesRepositoryImpl(remote)
    private val articlesRepo = ArticlesRepositoryImpl(remote)

    private fun dto(vararg urls: String) = ArticlesResponseDto(
        status = "ok", totalResults = 100,
        articles = urls.map { ArticleDto(title = "T $it", url = it) },
    )

    @Test fun `sources are mapped sorted by name and deduplicated`() = runTest {
        coEvery { sourcesApi.getSources("sports", null, null) } returns SourcesResponseDto(
            status = "ok",
            sources = listOf(
                SourceDto("z", "Zeta"), SourceDto("a", "alpha"), SourceDto("a", "alpha again"), SourceDto(null, "no id"),
            ),
        )
        val result = sourcesRepo.getSources(NewsCategory.SPORTS).getOrThrow()
        assertEquals(listOf("a", "z"), result.map { it.id })
    }

    @Test fun `sources without category requests everything`() = runTest {
        coEvery { sourcesApi.getSources(null, null, null) } returns SourcesResponseDto("ok", sources = emptyList())
        assertTrue(sourcesRepo.getSources(null).getOrThrow().isEmpty())
    }

    @Test fun `sources failure is exposed as NewsError`() = runTest {
        coEvery { sourcesApi.getSources(any(), any(), any()) } throws IOException("offline")
        assertTrue(sourcesRepo.getSources(NewsCategory.GENERAL).exceptionOrNull() is NewsError.NoConnection)
    }

    @Test fun `top headlines stream loads first page for the source`() = runTest {
        coEvery { articlesApi.getTopHeadlines("bbc-news", null, null, 1, 20) } returns dto("https://x/1", "https://x/2")
        val items = articlesRepo.topHeadlines("bbc-news").asSnapshot()
        assertEquals(listOf("https://x/1", "https://x/2"), items.map { it.url })
    }

    @Test fun `search stream requests everything with the chosen sort order`() = runTest {
        coEvery { articlesApi.searchEverything("kotlin", null, "relevancy", 1, 20) } returns dto("https://x/1")
        val items = articlesRepo.search("kotlin", null, SearchSortOrder.RELEVANCY).asSnapshot()
        assertEquals(1, items.size)
        coVerify { articlesApi.searchEverything("kotlin", null, "relevancy", 1, 20) }
    }

    @Test fun `featured headlines prefer articles that have images`() = runTest {
        coEvery { articlesApi.getTopHeadlines(null, "us", "general", 1, 5) } returns ArticlesResponseDto(
            status = "ok", totalResults = 2,
            articles = listOf(
                ArticleDto(title = "no image", url = "https://x/1"),
                ArticleDto(title = "with image", url = "https://x/2", urlToImage = "https://x/2.jpg"),
            ),
        )
        assertEquals(listOf("https://x/2"), articlesRepo.featuredHeadlines(5).getOrThrow().map { it.url })
    }

    @Test fun `featured headlines failure is surfaced`() = runTest {
        coEvery { articlesApi.getTopHeadlines(any(), any(), any(), any(), any()) } throws IOException()
        assertTrue(articlesRepo.featuredHeadlines(5).isFailure)
    }
}
