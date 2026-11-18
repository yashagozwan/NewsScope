package com.yasha.newsscope.presentation

import androidx.lifecycle.SavedStateHandle
import com.yasha.newsscope.core.common.UiState
import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.usecase.FilterSourcesUseCase
import com.yasha.newsscope.domain.usecase.GetCategoriesUseCase
import com.yasha.newsscope.domain.usecase.GetFeaturedHeadlinesUseCase
import com.yasha.newsscope.domain.usecase.GetSourcePageUseCase
import com.yasha.newsscope.domain.usecase.GetSourcesByCategoryUseCase
import com.yasha.newsscope.domain.usecase.GetTopHeadlinesUseCase
import com.yasha.newsscope.domain.usecase.SearchArticlesUseCase
import com.yasha.newsscope.presentation.articles.ArticlesViewModel
import com.yasha.newsscope.presentation.home.HomeViewModel
import com.yasha.newsscope.presentation.search.SearchTab
import com.yasha.newsscope.presentation.search.SearchViewModel
import com.yasha.newsscope.testutil.FakeArticlesRepository
import com.yasha.newsscope.testutil.FakeSourcesRepository
import com.yasha.newsscope.testutil.MainDispatcherRule
import com.yasha.newsscope.testutil.article
import com.yasha.newsscope.testutil.source
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeAndSearchViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    // ---- Home ----

    private fun home(repo: FakeArticlesRepository) =
        HomeViewModel(GetCategoriesUseCase(), GetFeaturedHeadlinesUseCase(repo))

    @Test fun `home loads all categories with general selected and featured content`() = runTest(mainRule.dispatcher) {
        val vm = home(FakeArticlesRepository(Result.success(listOf(article("https://a/1")))))
        assertTrue(vm.state.value.featured is UiState.Loading)
        advanceUntilIdle()

        assertEquals(7, vm.state.value.categories.size)
        assertEquals(NewsCategory.GENERAL, vm.state.value.selectedCategory)
        assertEquals(1, (vm.state.value.featured as UiState.Success).data.size)
    }

    @Test fun `home selecting a category updates selection`() = runTest(mainRule.dispatcher) {
        val vm = home(FakeArticlesRepository())
        vm.onCategorySelected(NewsCategory.SPORTS)
        assertEquals(NewsCategory.SPORTS, vm.state.value.selectedCategory)
    }

    @Test fun `home featured empty and error states with retry`() = runTest(mainRule.dispatcher) {
        val repo = FakeArticlesRepository(Result.success(emptyList()))
        val vm = home(repo)
        advanceUntilIdle()
        assertTrue(vm.state.value.featured is UiState.Empty)

        repo.featured = Result.failure(NewsError.Timeout())
        vm.retryFeatured(); advanceUntilIdle()
        assertTrue((vm.state.value.featured as UiState.Error).error is NewsError.Timeout)

        repo.featured = Result.success(listOf(article("https://a/1")))
        vm.retryFeatured(); advanceUntilIdle()
        assertTrue(vm.state.value.featured is UiState.Success)
    }

    // ---- Articles ----

    @Test fun `articles screen requests headlines for its source and exposes the stream`() = runTest(mainRule.dispatcher) {
        val repo = FakeArticlesRepository()
        val vm = ArticlesViewModel(
            SavedStateHandle(mapOf("sourceId" to "bbc-news", "sourceName" to "BBC News")),
            GetTopHeadlinesUseCase(repo),
        )
        assertEquals(listOf("bbc-news"), repo.headlineSources)
        assertEquals("BBC News", vm.sourceName)
        vm.articles.launchIn(backgroundScope)
        runCurrent()
        assertEquals(listOf("bbc-news"), repo.headlineSources)
    }

    // ---- Search ----

    private fun search(articles: FakeArticlesRepository, sources: FakeSourcesRepository, handle: Map<String, Any?> = emptyMap()) =
        SearchViewModel(
            SavedStateHandle(handle),
            SearchArticlesUseCase(articles),
            GetSourcesByCategoryUseCase(sources),
            GetSourcePageUseCase(FilterSourcesUseCase()),
        )

    @Test fun `search debounces typing and only queries the final text`() = runTest(mainRule.dispatcher) {
        val repo = FakeArticlesRepository()
        val vm = search(repo, FakeSourcesRepository(Result.success(emptyList())))
        vm.articles.launchIn(backgroundScope)
        runCurrent()

        vm.onArticleQueryChange("k"); advanceTimeBy(100)
        vm.onArticleQueryChange("ko"); advanceTimeBy(100)
        vm.onArticleQueryChange("kotlin "); advanceTimeBy(SearchViewModel.DEBOUNCE_MS - 1)
        assertTrue("no request before debounce elapses", repo.searchQueries.isEmpty())

        advanceTimeBy(2); runCurrent()
        assertEquals(listOf("kotlin" to null), repo.searchQueries)
    }

    @Test fun `empty query never triggers a search`() = runTest(mainRule.dispatcher) {
        val repo = FakeArticlesRepository()
        val vm = search(repo, FakeSourcesRepository(Result.success(emptyList())))
        vm.articles.launchIn(backgroundScope)
        runCurrent() // the UI is already collecting before the user types
        vm.onArticleQueryChange("   ")
        advanceUntilIdle()
        assertTrue(repo.searchQueries.isEmpty())
    }

    @Test fun `changing the query starts a fresh stream and clearing stops searching`() = runTest(mainRule.dispatcher) {
        val repo = FakeArticlesRepository()
        val vm = search(repo, FakeSourcesRepository(Result.success(emptyList())))
        vm.articles.launchIn(backgroundScope)
        runCurrent()

        vm.onArticleQueryChange("one"); advanceUntilIdle()
        vm.onArticleQueryChange("two"); advanceUntilIdle()
        vm.onArticleQueryChange(""); advanceUntilIdle()

        assertEquals(listOf("one", "two"), repo.searchQueries.map { it.first })
        assertEquals("", vm.articleQuery.value)
    }

    @Test fun `search scoped to a source passes the source id`() = runTest(mainRule.dispatcher) {
        val repo = FakeArticlesRepository()
        val vm = search(repo, FakeSourcesRepository(Result.success(emptyList())), mapOf("sourceId" to "cnn", "sourceName" to "CNN"))
        vm.articles.launchIn(backgroundScope)
        runCurrent() // the UI is already collecting before the user types
        vm.onArticleQueryChange("economy"); advanceUntilIdle()
        assertEquals(listOf("economy" to "cnn"), repo.searchQueries)
    }

    @Test fun `sources tab loads lazily once and filters case insensitively`() = runTest(mainRule.dispatcher) {
        val sourcesRepo = FakeSourcesRepository(Result.success(listOf(source("bbc-news", "BBC News"), source("cnn", "CNN"))))
        val vm = search(FakeArticlesRepository(), sourcesRepo)
        advanceUntilIdle()
        assertEquals(0, sourcesRepo.calls)

        vm.onTabSelected(SearchTab.SOURCES); advanceUntilIdle()
        vm.onTabSelected(SearchTab.ARTICLES)
        vm.onTabSelected(SearchTab.SOURCES); advanceUntilIdle()
        assertEquals(1, sourcesRepo.calls)
        assertEquals(null, sourcesRepo.lastCategory) // searches across every category

        vm.onSourceQueryChange("bbc")
        assertEquals(listOf("bbc-news"), vm.sources.value.items.map { it.id })
        vm.onSourceQueryChange("")
        assertEquals(2, vm.sources.value.items.size)
    }
}
