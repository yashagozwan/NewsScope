package com.yasha.newsscope

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yasha.newsscope.core.designsystem.NewsScopeTheme
import com.yasha.newsscope.core.designsystem.ThemeMode
import com.yasha.newsscope.presentation.NewsScopeApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate. No keep-on-screen condition: the system splash is dismissed
        // as soon as the first frame is ready, and Compose takes over with the animated brand splash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val mode by themeViewModel.mode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (mode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose { }
            }
            NewsScopeTheme(darkTheme = darkTheme) {
                NewsScopeApp(
                    isDarkTheme = darkTheme,
                    onToggleTheme = { themeViewModel.toggle(darkTheme) },
                )
            }
        }
    }
}
