package com.yasha.newsscope.core.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations. */
sealed interface NewsScopeDestination {
    @Serializable data object Splash : NewsScopeDestination
    @Serializable data object Home : NewsScopeDestination
    @Serializable data object Discover : NewsScopeDestination

    /** [sourceId] scopes article search to one publisher when opened from an articles screen. */
    @Serializable
    data class Search(val sourceId: String? = null, val sourceName: String? = null) : NewsScopeDestination

    @Serializable data class Sources(val categoryApiValue: String) : NewsScopeDestination
    @Serializable data class Articles(val sourceId: String, val sourceName: String) : NewsScopeDestination
    @Serializable data class ArticleWeb(val url: String, val sourceName: String?) : NewsScopeDestination
}
