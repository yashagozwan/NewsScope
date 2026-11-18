package com.yasha.newsscope.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yasha.newsscope.core.common.UiState
import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.usecase.GetCategoriesUseCase
import com.yasha.newsscope.domain.usecase.GetFeaturedHeadlinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val categories: List<NewsCategory>,
    val selectedCategory: NewsCategory,
    val featured: UiState<List<Article>> = UiState.Loading,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getCategories: GetCategoriesUseCase,
    private val getFeaturedHeadlines: GetFeaturedHeadlinesUseCase,
) : ViewModel() {

    private val categories = getCategories()

    private val _state = MutableStateFlow(
        HomeUiState(
            categories = categories,
            selectedCategory = categories.firstOrNull { it == NewsCategory.GENERAL } ?: categories.first(),
        ),
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var featuredJob: Job? = null

    init {
        loadFeatured()
    }

    fun onCategorySelected(category: NewsCategory) = _state.update { it.copy(selectedCategory = category) }

    fun retryFeatured() = loadFeatured()

    private fun loadFeatured() {
        if (featuredJob?.isActive == true) return
        featuredJob = viewModelScope.launch {
            _state.update { it.copy(featured = UiState.Loading) }
            getFeaturedHeadlines().fold(
                onSuccess = { articles ->
                    _state.update {
                        it.copy(featured = if (articles.isEmpty()) UiState.Empty else UiState.Success(articles))
                    }
                },
                onFailure = { failure ->
                    val error = failure as? NewsError ?: NewsError.Unknown(failure)
                    _state.update { it.copy(featured = UiState.Error(error)) }
                },
            )
        }
    }
}
