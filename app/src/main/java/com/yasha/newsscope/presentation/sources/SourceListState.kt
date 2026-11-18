package com.yasha.newsscope.presentation.sources

import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.domain.model.NewsSource
import com.yasha.newsscope.domain.usecase.GetSourcePageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SourceListUiState(
    val isLoading: Boolean = false,
    val error: NewsError? = null,
    val query: String = "",
    val items: List<NewsSource> = emptyList(),
    val totalMatches: Int = 0,
    val hasLoaded: Boolean = false,
    val hasMore: Boolean = false,
) {
    val isFiltering: Boolean get() = query.isNotBlank()
}

/**
 * Holds the full source list fetched once from NewsAPI and exposes a filtered, client-side
 * paginated window of it. Shared by the Sources screen and the Search screen's Sources tab.
 */
class SourceListState(
    private val scope: CoroutineScope,
    private val loadSources: suspend () -> Result<List<NewsSource>>,
    private val getSourcePage: GetSourcePageUseCase,
    private val pageSize: Int = PAGE_SIZE,
) {
    private val _state = MutableStateFlow(SourceListUiState())
    val state: StateFlow<SourceListUiState> = _state.asStateFlow()

    private var all: List<NewsSource> = emptyList()
    private var visibleCount = pageSize
    private var loadJob: Job? = null

    /** Ignored while a request is already running, so retries cannot stack up. */
    fun load() {
        if (loadJob?.isActive == true) return
        loadJob = scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            loadSources().fold(
                onSuccess = { sources ->
                    all = sources
                    _state.update { it.copy(isLoading = false, hasLoaded = true) }
                    publish()
                },
                onFailure = { failure ->
                    val error = failure as? NewsError ?: NewsError.Unknown(failure)
                    _state.update { it.copy(isLoading = false, error = error) }
                },
            )
        }
    }

    fun onQueryChange(query: String) {
        visibleCount = pageSize // a new query always starts from the first page
        _state.update { it.copy(query = query) }
        publish()
    }

    fun loadMore() {
        if (!_state.value.hasMore) return
        visibleCount += pageSize
        publish()
    }

    private fun publish() {
        val query = _state.value.query
        val page = getSourcePage(all, query, visibleCount)
        _state.update {
            it.copy(items = page.items, totalMatches = page.totalMatches, hasMore = page.hasMore)
        }
    }

    companion object {
        const val PAGE_SIZE = 15
    }
}
