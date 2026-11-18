package com.yasha.newsscope.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.yasha.newsscope.R
import com.yasha.newsscope.core.designsystem.spacing
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.NewsSource
import com.yasha.newsscope.presentation.components.ArticlePagingList
import com.yasha.newsscope.presentation.components.MessageState
import com.yasha.newsscope.presentation.components.SearchField
import com.yasha.newsscope.presentation.sources.SourceListContent

private val Suggestions = listOf("Climate", "Elections", "Markets", "Artificial intelligence", "Space", "Football")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onArticleClick: (Article) -> Unit,
    onSourceClick: (NewsSource) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val tab by viewModel.tab.collectAsStateWithLifecycle()
    val articleQuery by viewModel.articleQuery.collectAsStateWithLifecycle()
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val scoped = viewModel.scopedSourceId != null
    val gutter = MaterialTheme.spacing.screenHorizontal

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Column(Modifier.padding(horizontal = gutter, vertical = MaterialTheme.spacing.lg)) {
            Text(
                stringResource(R.string.search_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (scoped) {
                Text(
                    stringResource(R.string.search_scoped_to, viewModel.scopedSourceName.orEmpty()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        if (!scoped) {
            PrimaryTabRow(
                selectedTabIndex = tab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                Tab(
                    selected = tab == SearchTab.ARTICLES,
                    onClick = { viewModel.onTabSelected(SearchTab.ARTICLES) },
                    text = { Text(stringResource(R.string.search_tab_articles)) },
                )
                Tab(
                    selected = tab == SearchTab.SOURCES,
                    onClick = { viewModel.onTabSelected(SearchTab.SOURCES) },
                    text = { Text(stringResource(R.string.search_tab_sources)) },
                )
            }
        }

        val isArticles = tab == SearchTab.ARTICLES
        SearchField(
            query = if (isArticles) articleQuery else sources.query,
            onQueryChange = if (isArticles) viewModel::onArticleQueryChange else viewModel::onSourceQueryChange,
            placeholder = stringResource(if (isArticles) R.string.search_articles_hint else R.string.sources_search_hint),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = gutter, vertical = MaterialTheme.spacing.md),
        )

        if (isArticles) {
            if (articleQuery.isBlank()) {
                Column(Modifier.fillMaxSize().padding(horizontal = gutter)) {
                    Text(
                        stringResource(R.string.search_suggestions_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                    ) {
                        Suggestions.forEach { suggestion ->
                            AssistChip(
                                onClick = { viewModel.onArticleQueryChange(suggestion) },
                                label = { Text(suggestion) },
                                shape = MaterialTheme.shapes.small,
                            )
                        }
                    }
                    MessageState(
                        icon = Icons.Outlined.Search,
                        title = stringResource(R.string.search_idle_title),
                        message = stringResource(R.string.search_idle_message),
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                ArticlePagingList(
                    items = articles,
                    onArticleClick = onArticleClick,
                    emptyTitle = stringResource(R.string.search_empty_title),
                    emptyMessage = stringResource(R.string.search_empty_message, articleQuery.trim()),
                )
            }
        } else {
            SourceListContent(
                state = sources,
                onSourceClick = onSourceClick,
                onLoadMore = viewModel::loadMoreSources,
                onRetry = viewModel::retrySources,
                emptyTitle = stringResource(R.string.sources_empty_title),
                emptyMessage = stringResource(R.string.sources_empty_message),
            )
        }
    }
}
