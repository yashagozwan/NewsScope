package com.yasha.newsscope.presentation.articles

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.yasha.newsscope.R
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.presentation.components.ArticlePagingList
import com.yasha.newsscope.presentation.components.ScreenTopBar

@Composable
fun ArticlesScreen(
    onBack: () -> Unit,
    onSearchClick: (sourceId: String, sourceName: String) -> Unit,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticlesViewModel = hiltViewModel(),
) {
    val items = viewModel.articles.collectAsLazyPagingItems()
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(
                title = viewModel.sourceName,
                subtitle = stringResource(R.string.articles_by_source, viewModel.sourceName),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { onSearchClick(viewModel.sourceId, viewModel.sourceName) }) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_search))
                    }
                },
            )
        },
    ) { padding ->
        ArticlePagingList(
            items = items,
            onArticleClick = onArticleClick,
            emptyTitle = stringResource(R.string.articles_empty_title),
            emptyMessage = stringResource(R.string.articles_empty_message),
            modifier = Modifier.padding(padding),
        )
    }
}
