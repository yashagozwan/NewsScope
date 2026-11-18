package com.yasha.newsscope.core.common

import com.yasha.newsscope.core.error.NewsError

/** Generic state for non-paginated screens. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: NewsError) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}
