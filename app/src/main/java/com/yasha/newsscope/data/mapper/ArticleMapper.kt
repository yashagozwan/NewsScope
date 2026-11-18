package com.yasha.newsscope.data.mapper

import com.yasha.newsscope.data.remote.dto.ArticleDto
import com.yasha.newsscope.data.remote.dto.ArticlesResponseDto
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.ArticlePage
import java.time.Instant
import java.time.format.DateTimeParseException

private const val REMOVED_MARKER = "[Removed]"

/**
 * Returns null for entries that cannot be shown: no title, no URL, or the `[Removed]` placeholder
 * NewsAPI substitutes for deleted articles. Every other field is optional.
 */
fun ArticleDto.toDomain(): Article? {
    val articleUrl = url?.trim()?.takeIf { it.isNotEmpty() && it != REMOVED_MARKER } ?: return null
    val headline = title?.trim()?.takeIf { it.isNotEmpty() && it != REMOVED_MARKER } ?: return null
    return Article(
        url = articleUrl,
        title = headline.stripMarkup().takeIf { it.isNotEmpty() } ?: return null,
        description = description?.stripMarkup()?.takeIf { it.isNotEmpty() && it != REMOVED_MARKER },
        imageUrl = urlToImage?.trim()?.takeIf { it.isNotEmpty() },
        author = author?.trim()?.takeIf { it.isNotEmpty() },
        sourceId = source?.id?.takeIf { it.isNotBlank() },
        sourceName = source?.name?.takeIf { it.isNotBlank() },
        publishedAt = publishedAt.toInstantOrNull(),
    )
}

fun ArticlesResponseDto.toDomain(): ArticlePage {
    val raw = articles.orEmpty()
    return ArticlePage(
        articles = raw.mapNotNull { it.toDomain() },
        totalResults = totalResults ?: 0,
        fetchedCount = raw.size,
    )
}

private val HtmlTag = Regex("<[^>]*>")
private val Whitespace = Regex("\\s+")

/** Some publishers ship HTML snippets in title/description; show them as plain text. */
internal fun String.stripMarkup(): String =
    replace(HtmlTag, " ")
        .replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"")
        .replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">")
        .replace(Whitespace, " ")
        .trim()

private fun String?.toInstantOrNull(): Instant? =
    try {
        this?.let(Instant::parse)
    } catch (_: DateTimeParseException) {
        null
    }
