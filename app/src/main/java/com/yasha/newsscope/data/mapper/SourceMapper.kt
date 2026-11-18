package com.yasha.newsscope.data.mapper

import com.yasha.newsscope.data.remote.dto.SourceDto
import com.yasha.newsscope.domain.model.NewsSource

/** Sources without an id or a name cannot be queried or displayed and are dropped. */
fun SourceDto.toDomain(): NewsSource? {
    val sourceId = id?.takeIf { it.isNotBlank() } ?: return null
    val sourceName = name?.takeIf { it.isNotBlank() } ?: return null
    return NewsSource(
        id = sourceId,
        name = sourceName,
        description = description?.takeIf { it.isNotBlank() },
        url = url?.takeIf { it.isNotBlank() },
        category = category?.takeIf { it.isNotBlank() },
        language = language?.takeIf { it.isNotBlank() },
        country = country?.takeIf { it.isNotBlank() },
    )
}
