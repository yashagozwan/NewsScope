package com.yasha.newsscope.core.designsystem

import androidx.compose.ui.graphics.Color

// Light palette
internal val LightBackground = Color(0xFFF8F8F6)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightSurfaceVariant = Color(0xFFEFEFEB)
internal val LightPrimary = Color(0xFF172C46)
internal val LightOnPrimary = Color(0xFFF8F8F6)
internal val LightSecondary = Color(0xFF6B7785)
internal val LightOnBackground = Color(0xFF17212B)
internal val LightOnSurfaceVariant = Color(0xFF5E6873) // darkened from #77818C for AA contrast on #F8F8F6
internal val LightOutline = Color(0xFFD9DBD8)

// Dark palette
internal val DarkBackground = Color(0xFF111820)
internal val DarkSurface = Color(0xFF1B2530)
internal val DarkSurfaceVariant = Color(0xFF243140)
internal val DarkPrimary = Color(0xFFDCE7F3)
internal val DarkOnPrimary = Color(0xFF111820)
internal val DarkSecondary = Color(0xFF9BA9B8)
internal val DarkOnBackground = Color(0xFFF4F6F8)
internal val DarkOnSurfaceVariant = Color(0xFFA1ACB7)
internal val DarkOutline = Color(0xFF334255)

// Shared accent
internal val Accent = Color(0xFFD6A85F)
internal val OnAccent = Color(0xFF17212B)
internal val ErrorLight = Color(0xFFB3261E)
internal val ErrorDark = Color(0xFFF2B8B5)

/** Fixed brand colors used where the theme must not change (e.g. the splash screen). */
object BrandColors {
    val Navy = LightPrimary
    val Paper = LightBackground
    val Gold = Accent
}
