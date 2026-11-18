package com.yasha.newsscope.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArticlesResponseDto(
    val status: String? = null,
    val code: String? = null,
    val message: String? = null,
    val totalResults: Int? = null,
    val articles: List<ArticleDto>? = null,
)

@Serializable
data class ArticleDto(
    val source: ArticleSourceDto? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null,
)

@Serializable
data class ArticleSourceDto(
    val id: String? = null,
    val name: String? = null,
)

/** Body NewsAPI returns for failed requests: `{"status":"error","code":"apiKeyInvalid","message":"..."}`. */
@Serializable
data class ApiErrorDto(
    val status: String? = null,
    val code: String? = null,
    val message: String? = null,
)
