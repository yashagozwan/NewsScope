package com.yasha.newsscope.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yasha.newsscope.BuildConfig
import com.yasha.newsscope.core.network.ApiKeyInterceptor
import com.yasha.newsscope.core.network.ApiKeyProvider
import com.yasha.newsscope.core.network.BuildConfigApiKeyProvider
import com.yasha.newsscope.data.remote.api.ArticlesApi
import com.yasha.newsscope.data.remote.api.SourcesApi
import com.yasha.newsscope.data.remote.datasource.NewsErrorMapper
import com.yasha.newsscope.data.remote.datasource.NewsRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 20L

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides @Singleton
    fun provideApiKeyProvider(): ApiKeyProvider = BuildConfigApiKeyProvider(BuildConfig.NEWS_API_KEY)

    @Provides @Singleton
    fun provideOkHttpClient(apiKeyProvider: ApiKeyProvider): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(ApiKeyInterceptor(apiKeyProvider))
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC // never BODY/HEADERS: keeps the key out of logs
                    redactHeader(ApiKeyInterceptor.HEADER_NAME)
                },
            )
        }
        return builder.build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.NEWS_API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideSourcesApi(retrofit: Retrofit): SourcesApi = retrofit.create(SourcesApi::class.java)

    @Provides @Singleton
    fun provideArticlesApi(retrofit: Retrofit): ArticlesApi = retrofit.create(ArticlesApi::class.java)

    @Provides @Singleton
    fun provideErrorMapper(json: Json): NewsErrorMapper = NewsErrorMapper(json)

    @Provides @Singleton
    fun provideRemoteDataSource(
        sourcesApi: SourcesApi,
        articlesApi: ArticlesApi,
        apiKeyProvider: ApiKeyProvider,
        errorMapper: NewsErrorMapper,
    ): NewsRemoteDataSource = NewsRemoteDataSource(sourcesApi, articlesApi, apiKeyProvider, errorMapper)
}
