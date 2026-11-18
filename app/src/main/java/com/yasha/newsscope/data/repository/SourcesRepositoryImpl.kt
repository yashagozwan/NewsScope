package com.yasha.newsscope.data.repository

import com.yasha.newsscope.data.mapper.toDomain
import com.yasha.newsscope.data.remote.datasource.NewsRemoteDataSource
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.domain.model.NewsSource
import com.yasha.newsscope.domain.repository.SourcesRepository

class SourcesRepositoryImpl(private val remote: NewsRemoteDataSource) : SourcesRepository {
    override suspend fun getSources(
        category: NewsCategory?,
        language: String?,
        country: String?,
    ): Result<List<NewsSource>> =
        remote.getSources(category?.apiValue, language, country).map { dto ->
            dto.sources.orEmpty().mapNotNull { it.toDomain() }.distinctBy { it.id }.sortedBy { it.name.lowercase() }
        }
}
