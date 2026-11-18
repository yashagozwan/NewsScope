package com.yasha.newsscope.data.remote

import com.yasha.newsscope.core.error.NewsError
import com.yasha.newsscope.core.network.ApiKeyInterceptor
import com.yasha.newsscope.core.network.BuildConfigApiKeyProvider
import com.yasha.newsscope.data.remote.api.ArticlesApi
import com.yasha.newsscope.data.remote.api.SourcesApi
import com.yasha.newsscope.data.remote.datasource.NewsErrorMapper
import com.yasha.newsscope.data.remote.datasource.NewsRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Exercises the real Retrofit/OkHttp/serialization stack against a local server. */
class NewsRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; explicitNulls = false }

    @Before fun setUp() { server = MockWebServer().apply { start() } }
    @After fun tearDown() { runCatching { server.shutdown() } }

    private fun dataSource(apiKey: String = "test-key", readTimeoutMs: Long = 5_000): NewsRemoteDataSource {
        val provider = BuildConfigApiKeyProvider(apiKey)
        val client = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(provider))
            .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/v2/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return NewsRemoteDataSource(
            retrofit.create(SourcesApi::class.java), retrofit.create(ArticlesApi::class.java),
            provider, NewsErrorMapper(json),
        )
    }

    private fun enqueue(code: Int, body: String) = server.enqueue(MockResponse().setResponseCode(code).setBody(body))

    private suspend fun failureOf(call: suspend () -> Result<*>): Throwable = call().exceptionOrNull()!!

    private val articlesJson = """{"status":"ok","totalResults":2,"articles":[
        {"source":{"id":null,"name":"X"},"author":null,"title":"A","description":null,"url":"https://x.com/1","urlToImage":null,"publishedAt":null,"content":null,"extra":1},
        {"title":"B","url":"https://x.com/2"}]}"""

    // ---- positive ----

    @Test fun `top headlines success parses nullable fields and sends query and key header`() = runTest {
        enqueue(200, articlesJson)
        val dto = dataSource().getTopHeadlines(sources = "bbc-news", page = 2, pageSize = 20).getOrThrow()

        assertEquals(2, dto.articles!!.size)
        val request = server.takeRequest()
        assertEquals("test-key", request.getHeader("X-Api-Key"))
        assertTrue(request.path!!.startsWith("/v2/top-headlines?"))
        assertTrue(request.path!!.contains("sources=bbc-news"))
        assertTrue(request.path!!.contains("page=2"))
        assertTrue(request.path!!.contains("pageSize=20"))
        assertTrue("api key must not be in the URL", !request.path!!.contains("test-key"))
    }

    @Test fun `sources success`() = runTest {
        enqueue(200, """{"status":"ok","sources":[{"id":"bbc","name":"BBC","category":"general"}]}""")
        val dto = dataSource().getSources("general", null, null).getOrThrow()
        assertEquals("bbc", dto.sources!!.single().id)
        assertTrue(server.takeRequest().path!!.startsWith("/v2/top-headlines/sources?category=general"))
    }

    @Test fun `search sends q sortBy page and pageSize`() = runTest {
        enqueue(200, articlesJson)
        dataSource().searchEverything("kotlin", "bbc-news", "publishedAt", 1, 20).getOrThrow()
        val path = server.takeRequest().path!!
        assertTrue(path.startsWith("/v2/everything?"))
        listOf("q=kotlin", "sources=bbc-news", "sortBy=publishedAt", "page=1", "pageSize=20").forEach {
            assertTrue("missing $it in $path", path.contains(it))
        }
    }

    @Test fun `empty articles list is a valid response`() = runTest {
        enqueue(200, """{"status":"ok","totalResults":0,"articles":[]}""")
        assertEquals(0, dataSource().getTopHeadlines(page = 1, pageSize = 20).getOrThrow().articles!!.size)
    }

    // ---- negative ----

    @Test fun `missing api key fails before any request is made`() = runTest {
        val error = failureOf { dataSource(apiKey = "").getTopHeadlines(page = 1, pageSize = 20) }
        assertTrue(error is NewsError.ApiKeyMissing)
        assertEquals(0, server.requestCount)
    }

    @Test fun `http 401 maps to Unauthorized with api message`() = runTest {
        enqueue(401, """{"status":"error","code":"apiKeyInvalid","message":"Your API key is invalid."}""")
        val error = failureOf { dataSource().getTopHeadlines(page = 1, pageSize = 20) }
        assertTrue(error is NewsError.Unauthorized)
        assertEquals("Your API key is invalid.", error.message)
    }

    @Test fun `http 426 maps to UpgradeRequired`() = runTest {
        enqueue(426, """{"status":"error","code":"maximumResultsReached","message":"limit"}""")
        assertTrue(failureOf { dataSource().getTopHeadlines(page = 6, pageSize = 20) } is NewsError.UpgradeRequired)
    }

    @Test fun `http 429 maps to RateLimited and is retryable`() = runTest {
        enqueue(429, """{"status":"error","code":"rateLimited","message":"slow down"}""")
        val error = failureOf { dataSource().getTopHeadlines(page = 1, pageSize = 20) } as NewsError
        assertTrue(error is NewsError.RateLimited)
        assertTrue(error.isRetryable)
    }

    @Test fun `http 500 maps to Server error even with a non json body`() = runTest {
        enqueue(500, "<html>boom</html>")
        val error = failureOf { dataSource().getTopHeadlines(page = 1, pageSize = 20) }
        assertTrue(error is NewsError.Server)
        assertEquals(500, (error as NewsError.Server).code)
    }

    @Test fun `unexpected http status maps to generic Http error`() = runTest {
        enqueue(418, "")
        assertEquals(418, (failureOf { dataSource().getSources(null, null, null) } as NewsError.Http).code)
    }

    @Test fun `malformed json maps to InvalidResponse`() = runTest {
        enqueue(200, "{ this is not json")
        assertTrue(failureOf { dataSource().getTopHeadlines(page = 1, pageSize = 20) } is NewsError.InvalidResponse)
    }

    @Test fun `ok response missing the articles array is invalid`() = runTest {
        enqueue(200, """{"status":"ok"}""")
        assertTrue(failureOf { dataSource().getTopHeadlines(page = 1, pageSize = 20) } is NewsError.InvalidResponse)
    }

    @Test fun `status error inside a 200 body is translated using its code`() = runTest {
        enqueue(200, """{"status":"error","code":"rateLimited","message":"too many"}""")
        assertTrue(failureOf { dataSource().getTopHeadlines(page = 1, pageSize = 20) } is NewsError.RateLimited)
    }

    @Test fun `read timeout maps to Timeout`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        assertTrue(failureOf { dataSource(readTimeoutMs = 200).getTopHeadlines(page = 1, pageSize = 20) } is NewsError.Timeout)
    }

    @Test fun `unreachable server maps to NoConnection`() = runTest {
        val ds = dataSource()
        server.shutdown()
        assertTrue(failureOf { ds.getTopHeadlines(page = 1, pageSize = 20) } is NewsError.NoConnection)
    }

    @Test fun `recovers after a failed request when retried`() = runTest {
        val ds = dataSource()
        enqueue(500, "")
        enqueue(200, articlesJson)
        assertTrue(ds.getTopHeadlines(page = 1, pageSize = 20).isFailure)
        assertTrue(ds.getTopHeadlines(page = 1, pageSize = 20).isSuccess)
    }
}
