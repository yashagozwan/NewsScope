package com.yasha.newsscope.presentation.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.usecase.GetTopHeadlinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getTopHeadlines: GetTopHeadlinesUseCase,
) : ViewModel() {

    val sourceId: String = savedStateHandle.get<String>("sourceId").orEmpty()
    val sourceName: String = savedStateHandle.get<String>("sourceName").orEmpty()

    /** Cached in the ViewModel scope so rotation does not refetch loaded pages. */
    val articles: Flow<PagingData<Article>> = getTopHeadlines(sourceId).cachedIn(viewModelScope)
}
