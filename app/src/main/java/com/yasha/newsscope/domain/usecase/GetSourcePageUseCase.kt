package com.yasha.newsscope.domain.usecase

import com.yasha.newsscope.domain.model.NewsSource
import com.yasha.newsscope.domain.model.SourcePage

/**
 * Filter first, then paginate, so the visible window always applies to the filtered result and
 * "has more" reflects the filtered total rather than the full list.
 */
class GetSourcePageUseCase(private val filterSources: FilterSourcesUseCase) {
    operator fun invoke(all: List<NewsSource>, query: String, visibleCount: Int): SourcePage {
        val matches = filterSources(all, query)
        return SourcePage(items = matches.take(visibleCount.coerceAtLeast(0)), totalMatches = matches.size)
    }
}
