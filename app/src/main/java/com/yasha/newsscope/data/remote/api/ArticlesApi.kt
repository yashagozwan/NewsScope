package com.yasha.newsscope.data.remote.api

import com.yasha.newsscope.data.remote.dto.ArticlesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ArticlesApi {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("sources") sources: String? = null,
        @Query("country") country: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ArticlesResponseDto

    @GET("everything")
    suspend fun searchEverything(
        @Query("q") query: String,
        @Query("sources") sources: String? = null,
        @Query("sortBy") sortBy: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ArticlesResponseDto
}
