package com.yasha.newsscope.testutil

import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.ArticlePage
import com.yasha.newsscope.domain.model.NewsSource

fun article(url: String, title: String = "Title $url") = Article(
    url = url, title = title, description = null, imageUrl = null, author = null,
    sourceId = "bbc-news", sourceName = "BBC News", publishedAt = null,
)

fun page(vararg urls: String, total: Int = 100, fetched: Int = urls.size) =
    ArticlePage(urls.map { article(it) }, total, fetched)

fun source(id: String, name: String = id.replaceFirstChar { it.uppercase() }, description: String? = null, country: String? = "us") =
    NewsSource(id, name, description, "https://$id.com", "general", "en", country)
