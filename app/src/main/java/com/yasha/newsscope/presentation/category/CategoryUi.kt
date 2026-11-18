package com.yasha.newsscope.presentation.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector
import com.yasha.newsscope.domain.model.NewsCategory

/** Presentation-only visual identity per category (the domain model stays icon-free). */
val NewsCategory.icon: ImageVector
    get() = when (this) {
        NewsCategory.BUSINESS -> Icons.Outlined.BusinessCenter
        NewsCategory.ENTERTAINMENT -> Icons.Outlined.Movie
        NewsCategory.GENERAL -> Icons.Outlined.Public
        NewsCategory.HEALTH -> Icons.Outlined.HealthAndSafety
        NewsCategory.SCIENCE -> Icons.Outlined.Science
        NewsCategory.SPORTS -> Icons.Outlined.SportsSoccer
        NewsCategory.TECHNOLOGY -> Icons.Outlined.Memory
    }

val NewsCategory.tagline: String
    get() = when (this) {
        NewsCategory.BUSINESS -> "Markets, companies and the economy"
        NewsCategory.ENTERTAINMENT -> "Film, music, culture and celebrity"
        NewsCategory.GENERAL -> "The day's most important stories"
        NewsCategory.HEALTH -> "Medicine, wellbeing and public health"
        NewsCategory.SCIENCE -> "Discovery, research and the natural world"
        NewsCategory.SPORTS -> "Scores, leagues and athletes"
        NewsCategory.TECHNOLOGY -> "Software, devices and the internet"
    }
