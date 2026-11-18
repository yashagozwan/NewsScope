package com.yasha.newsscope.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlSafetyTest {
    @Test fun `https url is accepted unchanged`() =
        assertEquals("https://example.com/a?b=1", UrlSafety.toSafeWebUrl("https://example.com/a?b=1"))

    @Test fun `http url is upgraded to https`() =
        assertEquals("https://example.com/a", UrlSafety.toSafeWebUrl("http://example.com/a"))

    @Test fun `null blank and malformed urls are rejected`() {
        assertNull(UrlSafety.toSafeWebUrl(null))
        assertNull(UrlSafety.toSafeWebUrl("   "))
        assertNull(UrlSafety.toSafeWebUrl("not a url"))
        assertNull(UrlSafety.toSafeWebUrl("example.com/no-scheme"))
        assertNull(UrlSafety.toSafeWebUrl("https://"))
    }

    @Test fun `non web schemes are rejected`() {
        listOf("javascript:alert(1)", "file:///etc/passwd", "intent://x#Intent;end", "content://a/b", "ftp://x.com/f")
            .forEach { assertNull(it, UrlSafety.toSafeWebUrl(it)) }
    }
}
