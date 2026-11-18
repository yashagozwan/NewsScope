package com.yasha.newsscope.presentation.sources

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.usecase.GetSourcePageUseCase
import com.yasha.newsscope.domain.usecase.GetSourcesByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SourcesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSourcesByCategory: GetSourcesByCategoryUseCase,
    getSourcePage: GetSourcePageUseCase,
) : ViewModel() {

    val category: NewsCategory =
        NewsCategory.fromApiValue(savedStateHandle.get<String>("categoryApiValue")) ?: NewsCategory.GENERAL

    private val list = SourceListState(
        scope = viewModelScope,
        loadSources = { getSourcesByCategory(category) },
        getSourcePage = getSourcePage,
    )

    val state = list.state

    init {
        list.load()
    }

    fun onQueryChange(query: String) = list.onQueryChange(query)
    fun loadMore() = list.loadMore()
    fun retry() = list.load()
}
