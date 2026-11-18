package com.yasha.newsscope.domain.model

/** The seven categories supported by NewsAPI. [apiValue] is the exact query parameter value. */
enum class NewsCategory(val apiValue: String, val displayName: String) {
    BUSINESS("business", "Business"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    GENERAL("general", "General"),
    HEALTH("health", "Health"),
    SCIENCE("science", "Science"),
    SPORTS("sports", "Sports"),
    TECHNOLOGY("technology", "Technology");

    companion object {
        fun fromApiValue(value: String?): NewsCategory? = entries.firstOrNull { it.apiValue == value }
    }
}
