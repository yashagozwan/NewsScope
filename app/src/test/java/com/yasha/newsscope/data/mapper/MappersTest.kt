package com.yasha.newsscope.data.mapper

import com.yasha.newsscope.data.remote.dto.ArticleDto
import com.yasha.newsscope.data.remote.dto.ArticleSourceDto
import com.yasha.newsscope.data.remote.dto.ArticlesResponseDto
import com.yasha.newsscope.data.remote.dto.SourceDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {
    private val full = ArticleDto(
        source = ArticleSourceDto("bbc-news", "BBC News"),
        author = " Jane ", title = " Headline ", description = "Desc", url = "https://x.com/a",
        urlToImage = "https://x.com/a.jpg", publishedAt = "2026-11-18T10:15:30Z", content = "c",
    )

    @Test fun `complete article maps every field`() {
        val a = full.toDomain()!!
        assertEquals("Headline", a.title)
        assertEquals("Jane", a.author)
        assertEquals("BBC News", a.sourceName)
        assertEquals("https://x.com/a.jpg", a.imageUrl)
        assertEquals(1_794_996_930L, a.publishedAt!!.epochSecond)
    }

    @Test fun `missing image description author and source are tolerated`() {
        val a = ArticleDto(title = "T", url = "https://x.com/a").toDomain()!!
        assertNull(a.imageUrl)
        assertNull(a.description)
        assertNull(a.author)
        assertNull(a.sourceName)
        assertNull(a.publishedAt)
    }

    @Test fun `blank description and image become null`() {
        val a = full.copy(description = "  ", urlToImage = "").toDomain()!!
        assertNull(a.description)
        assertNull(a.imageUrl)
    }

    @Test fun `invalid publication date becomes null instead of failing`() =
        assertNull(full.copy(publishedAt = "yesterday").toDomain()!!.publishedAt)

    @Test fun `articles without url or title or removed placeholders are dropped`() {
        assertNull(full.copy(url = null).toDomain())
        assertNull(full.copy(url = " ").toDomain())
        assertNull(full.copy(title = null).toDomain())
        assertNull(full.copy(title = "[Removed]").toDomain())
        assertNull(full.copy(url = "[Removed]").toDomain())
    }

    @Test fun `html markup in description and title is stripped to plain text`() {
        val a = full.copy(
            title = "Fish &amp; Chips",
            description = "<section class=\"video-page\"><p>Check  out the <b>trailer</b></p></section>",
        ).toDomain()!!
        assertEquals("Fish & Chips", a.title)
        assertEquals("Check out the trailer", a.description)
    }

    @Test fun `description that is only markup becomes null`() =
        assertNull(full.copy(description = "<p> </p>").toDomain()!!.description)

    @Test fun `description placeholder is hidden`() = assertNull(full.copy(description = "[Removed]").toDomain()!!.description)

    @Test fun `page keeps raw fetched count and total`() {
        val dto = ArticlesResponseDto(
            status = "ok", totalResults = 57,
            articles = listOf(full, full.copy(title = null), full.copy(url = "https://x.com/b")),
        )
        val page = dto.toDomain()
        assertEquals(2, page.articles.size)
        assertEquals(3, page.fetchedCount)
        assertEquals(57, page.totalResults)
    }

    @Test fun `missing articles list maps to empty page`() {
        val page = ArticlesResponseDto(status = "ok").toDomain()
        assertEquals(0, page.articles.size)
        assertEquals(0, page.totalResults)
    }

    @Test fun `source mapping and null handling`() {
        val s = SourceDto("bbc", "BBC", "d", "https://bbc.com", "general", "en", "gb").toDomain()
        assertNotNull(s)
        assertEquals("gb", s!!.country)
        assertNull(SourceDto(id = null, name = "x").toDomain())
        assertNull(SourceDto(id = "x", name = " ").toDomain())
        assertNull(SourceDto(id = "x", name = "X", description = "").toDomain()!!.description)
    }
}
