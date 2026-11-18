package com.yasha.newsscope.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SourcesResponseDto(
    val status: String? = null,
    val code: String? = null,
    val message: String? = null,
    val sources: List<SourceDto>? = null,
)

@Serializable
data class SourceDto(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val url: String? = null,
    val category: String? = null,
    val language: String? = null,
    val country: String? = null,
)
