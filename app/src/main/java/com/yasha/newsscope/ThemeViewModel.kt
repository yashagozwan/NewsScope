package com.yasha.newsscope

import androidx.lifecycle.ViewModel
import com.yasha.newsscope.core.designsystem.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** Session-scoped theme override. Follows the system until the user toggles it. */
@HiltViewModel
class ThemeViewModel @Inject constructor() : ViewModel() {
    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun toggle(currentlyDark: Boolean) {
        _mode.value = if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
    }
}
