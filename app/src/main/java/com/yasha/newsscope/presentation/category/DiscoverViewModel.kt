package com.yasha.newsscope.presentation.category

import androidx.lifecycle.ViewModel
import com.yasha.newsscope.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(getCategories: GetCategoriesUseCase) : ViewModel() {
    val categories = getCategories()
}
