package com.yasha.newsscope.domain.usecase

import com.yasha.newsscope.domain.model.NewsSource

/** Case-insensitive match on name, description, country and URL. A blank query returns everything. */
class FilterSourcesUseCase {
    operator fun invoke(sources: List<NewsSource>, query: String): List<NewsSource> {
        val needle = query.trim()
        if (needle.isEmpty()) return sources
        return sources.filter { source ->
            listOfNotNull(source.name, source.description, source.country, source.url)
                .any { it.contains(needle, ignoreCase = true) }
        }
    }
}
