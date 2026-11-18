package com.yasha.newsscope.di

import com.yasha.newsscope.data.remote.datasource.NewsRemoteDataSource
import com.yasha.newsscope.data.repository.ArticlesRepositoryImpl
import com.yasha.newsscope.data.repository.SourcesRepositoryImpl
import com.yasha.newsscope.domain.repository.ArticlesRepository
import com.yasha.newsscope.domain.repository.SourcesRepository
import com.yasha.newsscope.domain.usecase.FilterSourcesUseCase
import com.yasha.newsscope.domain.usecase.GetCategoriesUseCase
import com.yasha.newsscope.domain.usecase.GetFeaturedHeadlinesUseCase
import com.yasha.newsscope.domain.usecase.GetSourcePageUseCase
import com.yasha.newsscope.domain.usecase.GetSourcesByCategoryUseCase
import com.yasha.newsscope.domain.usecase.GetTopHeadlinesUseCase
import com.yasha.newsscope.domain.usecase.SearchArticlesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Wires repository implementations and use cases; keeps the domain layer free of DI annotations. */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun provideSourcesRepository(remote: NewsRemoteDataSource): SourcesRepository = SourcesRepositoryImpl(remote)

    @Provides @Singleton
    fun provideArticlesRepository(remote: NewsRemoteDataSource): ArticlesRepository = ArticlesRepositoryImpl(remote)

    @Provides fun provideGetCategories() = GetCategoriesUseCase()
    @Provides fun provideFilterSources() = FilterSourcesUseCase()
    @Provides fun provideSourcePage(filter: FilterSourcesUseCase) = GetSourcePageUseCase(filter)
    @Provides fun provideGetSources(repo: SourcesRepository) = GetSourcesByCategoryUseCase(repo)
    @Provides fun provideTopHeadlines(repo: ArticlesRepository) = GetTopHeadlinesUseCase(repo)
    @Provides fun provideSearchArticles(repo: ArticlesRepository) = SearchArticlesUseCase(repo)
    @Provides fun provideFeatured(repo: ArticlesRepository) = GetFeaturedHeadlinesUseCase(repo)
}
