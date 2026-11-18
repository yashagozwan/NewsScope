package com.yasha.newsscope.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.yasha.newsscope.R
import com.yasha.newsscope.core.navigation.NewsScopeDestination
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.presentation.articles.ArticlesScreen
import com.yasha.newsscope.presentation.category.DiscoverScreen
import com.yasha.newsscope.presentation.home.HomeScreen
import com.yasha.newsscope.presentation.search.SearchScreen
import com.yasha.newsscope.presentation.sources.SourcesScreen
import com.yasha.newsscope.presentation.splash.SplashScreen
import com.yasha.newsscope.presentation.webview.ArticleWebScreen
import kotlin.reflect.KClass

private enum class TopLevelTab(
    val destination: NewsScopeDestination,
    val route: KClass<out NewsScopeDestination>,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME(NewsScopeDestination.Home, NewsScopeDestination.Home::class, R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home),
    DISCOVER(NewsScopeDestination.Discover, NewsScopeDestination.Discover::class, R.string.nav_discover, Icons.Outlined.Explore, Icons.Filled.Explore),
    SEARCH(NewsScopeDestination.Search(), NewsScopeDestination.Search::class, R.string.nav_search, Icons.Outlined.Search, Icons.Filled.Search),
}

private fun NavDestination?.isTab(tab: TopLevelTab) = this?.hasRoute(tab.route) == true

@Composable
fun NewsScopeApp(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    // Hidden on the splash and detail screens; shown only for the three top-level tabs.
    val showBottomBar = TopLevelTab.entries.any { destination.isTab(it) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NewsScopeBottomBar(destination) { tab -> navController.navigateToTab(tab) }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NewsScopeDestination.Splash,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            composable<NewsScopeDestination.Splash>(
                enterTransition = { EnterTransition.None },
                exitTransition = { fadeOut(tween(SPLASH_FADE_MS)) },
            ) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(NewsScopeDestination.Home) {
                            popUpTo(NewsScopeDestination.Splash) { inclusive = true }
                        }
                    },
                )
            }
            composable<NewsScopeDestination.Home> {
                HomeScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onSearchClick = { navController.navigateToTab(TopLevelTab.SEARCH) },
                    onBrowseSources = { category -> navController.openSources(category) },
                    onArticleClick = { article ->
                        navController.navigate(NewsScopeDestination.ArticleWeb(article.url, article.sourceName))
                    },
                )
            }
            composable<NewsScopeDestination.Discover> {
                DiscoverScreen(onCategoryClick = { navController.openSources(it) })
            }
            composable<NewsScopeDestination.Search> {
                SearchScreen(
                    onArticleClick = { article ->
                        navController.navigate(NewsScopeDestination.ArticleWeb(article.url, article.sourceName))
                    },
                    onSourceClick = { source ->
                        navController.navigate(NewsScopeDestination.Articles(source.id, source.name))
                    },
                )
            }
            composable<NewsScopeDestination.Sources> {
                SourcesScreen(
                    onBack = navController::popBackStack,
                    onSourceClick = { source ->
                        navController.navigate(NewsScopeDestination.Articles(source.id, source.name))
                    },
                )
            }
            composable<NewsScopeDestination.Articles> {
                ArticlesScreen(
                    onBack = navController::popBackStack,
                    onSearchClick = { id, name -> navController.navigate(NewsScopeDestination.Search(id, name)) },
                    onArticleClick = { article ->
                        navController.navigate(NewsScopeDestination.ArticleWeb(article.url, article.sourceName))
                    },
                )
            }
            composable<NewsScopeDestination.ArticleWeb> { entry ->
                val args = entry.toRoute<NewsScopeDestination.ArticleWeb>()
                ArticleWebScreen(
                    url = args.url,
                    sourceName = args.sourceName,
                    onBack = navController::popBackStack,
                )
            }
        }
    }
}

@Composable
private fun NewsScopeBottomBar(destination: NavDestination?, onSelect: (TopLevelTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        TopLevelTab.entries.forEach { tab ->
            val selected = destination.isTab(tab)
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(tab) },
                icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                    unselectedTextColor = MaterialTheme.colorScheme.secondary,
                ),
            )
        }
    }
}

private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.destination) {
        popUpTo(NewsScopeDestination.Home)
        launchSingleTop = true
    }
}

private fun NavHostController.openSources(category: NewsCategory) {
    navigate(NewsScopeDestination.Sources(category.apiValue))
}

private const val SPLASH_FADE_MS = 300
