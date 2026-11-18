package com.yasha.newsscope.domain.model

import java.time.Instant

data class Article(
    /** The article URL is the stable identity used for de-duplication and list keys. */
    val url: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val author: String?,
    val sourceId: String?,
    val sourceName: String?,
    val publishedAt: Instant?,
) {
    val id: String get() = url
}

/**
 * One server page. [fetchedCount] is the raw item count before invalid items were discarded,
 * so pagination exhaustion is computed from what the server actually returned.
 */
data class ArticlePage(
    val articles: List<Article>,
    val totalResults: Int,
    val fetchedCount: Int,
)

enum class SearchSortOrder(val apiValue: String) {
    PUBLISHED_AT("publishedAt"),
    RELEVANCY("relevancy"),
    POPULARITY("popularity"),
}
