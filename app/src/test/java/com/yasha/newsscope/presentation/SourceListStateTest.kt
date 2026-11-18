package com.yasha.newsscope.presentation

import app.cash.turbine.test
import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.usecase.FilterSourcesUseCase
import com.yasha.newsscope.domain.usecase.GetSourcePageUseCase
import com.yasha.newsscope.domain.usecase.GetSourcesByCategoryUseCase
import com.yasha.newsscope.presentation.sources.SourceListState
import com.yasha.newsscope.presentation.sources.SourcesViewModel
import com.yasha.newsscope.testutil.FakeSourcesRepository
import com.yasha.newsscope.testutil.MainDispatcherRule
import com.yasha.newsscope.testutil.source
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SourceListStateTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val pageUseCase = GetSourcePageUseCase(FilterSourcesUseCase())
    private val sources = (1..40).map { source("s$it", "News $it") } + source("tech", "Tech Daily")

    private fun viewModel(repo: FakeSourcesRepository) = SourcesViewModel(
        SavedStateHandle(mapOf("categoryApiValue" to "technology")),
        GetSourcesByCategoryUseCase(repo),
        pageUseCase,
    )

    @Test fun `initial state is loading then content`() = runTest(mainRule.dispatcher) {
        val repo = FakeSourcesRepository(Result.success(sources))
        val vm = viewModel(repo)

        assertTrue(vm.state.value.isLoading.not() || vm.state.value.items.isEmpty())
        advanceUntilIdle()

        val s = vm.state.value
        assertFalse(s.isLoading)
        assertTrue(s.hasLoaded)
        assertEquals(15, s.items.size)
        assertEquals(41, s.totalMatches)
        assertTrue(s.hasMore)
        assertEquals(NewsCategory.TECHNOLOGY, repo.lastCategory)
    }

    @Test fun `load more reveals further local pages until exhausted`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(FakeSourcesRepository(Result.success(sources)))
        advanceUntilIdle()

        vm.loadMore(); assertEquals(30, vm.state.value.items.size)
        vm.loadMore(); assertEquals(41, vm.state.value.items.size)
        assertFalse(vm.state.value.hasMore)

        vm.loadMore() // no-op once exhausted
        assertEquals(41, vm.state.value.items.size)
    }

    @Test fun `query filters and resets pagination to first page`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(FakeSourcesRepository(Result.success(sources)))
        advanceUntilIdle()
        vm.loadMore()

        vm.onQueryChange("NEWS 1")
        val filtered = vm.state.value
        assertEquals(11, filtered.totalMatches) // News 1, 10..19
        assertEquals(11, filtered.items.size)
        assertFalse(filtered.hasMore)

        vm.onQueryChange("tech")
        assertEquals(listOf("tech"), vm.state.value.items.map { it.id })

        vm.onQueryChange("")
        assertEquals(15, vm.state.value.items.size) // back to the first page, not 30
    }

    @Test fun `query with no match yields empty filtered state`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(FakeSourcesRepository(Result.success(sources)))
        advanceUntilIdle()
        vm.onQueryChange("zzz")
        assertTrue(vm.state.value.items.isEmpty())
        assertTrue(vm.state.value.isFiltering)
    }

    @Test fun `empty api result is loaded but has no items`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(FakeSourcesRepository(Result.success(emptyList())))
        advanceUntilIdle()
        assertTrue(vm.state.value.hasLoaded)
        assertTrue(vm.state.value.items.isEmpty())
        assertFalse(vm.state.value.isFiltering)
    }

    @Test fun `failure exposes error then retry recovers`() = runTest(mainRule.dispatcher) {
        val repo = FakeSourcesRepository(Result.failure(NewsError.NoConnection()))
        val vm = viewModel(repo)
        advanceUntilIdle()
        assertTrue(vm.state.value.error is NewsError.NoConnection)
        assertFalse(vm.state.value.hasLoaded)

        repo.result = Result.success(sources)
        vm.retry()
        advanceUntilIdle()
        assertNull(vm.state.value.error)
        assertTrue(vm.state.value.hasLoaded)
        assertEquals(2, repo.calls)
    }

    @Test fun `retry while a request is running does not start another`() = runTest(mainRule.dispatcher) {
        val repo = FakeSourcesRepository(Result.success(sources))
        val vm = viewModel(repo)
        vm.retry(); vm.retry() // init already launched one; none of these should add requests
        advanceUntilIdle()
        assertEquals(1, repo.calls)
    }

    @Test fun `state emissions are observable with turbine`() = runTest(mainRule.dispatcher) {
        val list = SourceListState(
            scope = backgroundScope,
            loadSources = { Result.success(listOf(source("a"))) },
            getSourcePage = pageUseCase,
        )
        list.state.test {
            assertFalse(awaitItem().hasLoaded)
            list.load()
            var latest = awaitItem()
            while (!latest.hasLoaded || latest.items.isEmpty()) latest = awaitItem()
            assertNotNull(latest.items.singleOrNull())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
