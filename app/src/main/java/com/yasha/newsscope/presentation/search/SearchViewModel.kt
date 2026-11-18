package com.yasha.newsscope.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.usecase.GetSourcePageUseCase
import com.yasha.newsscope.domain.usecase.GetSourcesByCategoryUseCase
import com.yasha.newsscope.domain.usecase.SearchArticlesUseCase
import com.yasha.newsscope.presentation.sources.SourceListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class SearchTab { ARTICLES, SOURCES }

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    searchArticles: SearchArticlesUseCase,
    getSources: GetSourcesByCategoryUseCase,
    getSourcePage: GetSourcePageUseCase,
) : ViewModel() {

    /** Set when search was opened from a source's article list; scopes `/everything` to it. */
    val scopedSourceId: String? = savedStateHandle.get<String>("sourceId")
    val scopedSourceName: String? = savedStateHandle.get<String>("sourceName")

    private val _articleQuery = MutableStateFlow("")
    val articleQuery: StateFlow<String> = _articleQuery.asStateFlow()

    private val _tab = MutableStateFlow(SearchTab.ARTICLES)
    val tab: StateFlow<SearchTab> = _tab.asStateFlow()

    /**
     * Debounced article search. Clearing the box is applied immediately; every distinct query
     * creates a fresh paging stream, which resets pagination back to page 1.
     */
    val articles: Flow<PagingData<Article>> = _articleQuery
        .debounce { if (it.isBlank()) 0L else DEBOUNCE_MS }
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { searchArticles(it, scopedSourceId) }
        .cachedIn(viewModelScope)

    private val sourceList = SourceListState(
        scope = viewModelScope,
        loadSources = { getSources(category = null) },
        getSourcePage = getSourcePage,
    )
    val sources = sourceList.state

    fun onArticleQueryChange(query: String) = _articleQuery.update { query }

    fun onTabSelected(tab: SearchTab) {
        _tab.value = tab
        if (tab == SearchTab.SOURCES && !sources.value.hasLoaded) sourceList.load()
    }

    fun onSourceQueryChange(query: String) = sourceList.onQueryChange(query)
    fun loadMoreSources() = sourceList.loadMore()
    fun retrySources() = sourceList.load()

    companion object {
        const val DEBOUNCE_MS = 400L
    }
}
