package com.yasha.newsscope.domain.usecase

import com.yasha.newsscope.domain.model.NewsCategory

class GetCategoriesUseCase {
    operator fun invoke(): List<NewsCategory> = NewsCategory.entries
}
