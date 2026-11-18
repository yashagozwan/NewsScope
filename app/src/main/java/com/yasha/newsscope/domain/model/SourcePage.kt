package com.yasha.newsscope.domain.model

/** A client-side page of sources (NewsAPI's sources endpoint has no server pagination). */
data class SourcePage(
    val items: List<NewsSource>,
    val totalMatches: Int,
) {
    val hasMore: Boolean get() = items.size < totalMatches
}
