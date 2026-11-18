package com.yasha.newsscope.data.remote.api

import com.yasha.newsscope.data.remote.dto.SourcesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SourcesApi {
    @GET("top-headlines/sources")
    suspend fun getSources(
        @Query("category") category: String?,
        @Query("language") language: String?,
        @Query("country") country: String?,
    ): SourcesResponseDto
}
