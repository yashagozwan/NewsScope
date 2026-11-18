package com.yasha.newsscope.presentation.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yasha.newsscope.R
import com.yasha.newsscope.core.designsystem.spacing
import com.yasha.newsscope.domain.model.NewsSource
import com.yasha.newsscope.presentation.components.EmptyState
import com.yasha.newsscope.presentation.components.ErrorState
import com.yasha.newsscope.presentation.components.OnNearEnd
import com.yasha.newsscope.presentation.components.PaginationFooter
import com.yasha.newsscope.presentation.components.ScreenTopBar
import com.yasha.newsscope.presentation.components.SearchField
import com.yasha.newsscope.presentation.components.SkeletonList
import com.yasha.newsscope.presentation.components.SourceRow
import com.yasha.newsscope.presentation.components.SourceSkeletonItem

@Composable
fun SourcesScreen(
    onBack: () -> Unit,
    onSourceClick: (NewsSource) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SourcesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.sources_title, viewModel.category.displayName),
                subtitle = if (state.hasLoaded) stringResource(R.string.sources_count, state.totalMatches) else null,
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SearchField(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                placeholder = stringResource(R.string.sources_search_hint),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.screenHorizontal, vertical = MaterialTheme.spacing.sm),
            )
            SourceListContent(
                state = state,
                onSourceClick = onSourceClick,
                onLoadMore = viewModel::loadMore,
                onRetry = viewModel::retry,
                emptyTitle = stringResource(R.string.sources_empty_title),
                emptyMessage = stringResource(R.string.sources_empty_message),
            )
        }
    }
}

/** Source list body shared with the Search screen's Sources tab. */
@Composable
fun SourceListContent(
    state: SourceListUiState,
    onSourceClick: (NewsSource) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    emptyTitle: String,
    emptyMessage: String,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    OnNearEnd(listState, enabled = state.hasMore, onLoadMore = onLoadMore)

    when {
        !state.hasLoaded && state.isLoading -> SkeletonList(modifier) { SourceSkeletonItem() }
        !state.hasLoaded && state.error != null -> ErrorState(state.error, onRetry, modifier)
        !state.hasLoaded -> SkeletonList(modifier) { SourceSkeletonItem() }
        state.items.isEmpty() && state.isFiltering -> EmptyState(
            stringResource(R.string.sources_no_match_title),
            stringResource(R.string.sources_no_match_message, state.query.trim()),
            modifier,
        )
        state.items.isEmpty() -> EmptyState(emptyTitle, emptyMessage, modifier)
        else -> LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            items(state.items, key = { it.id }) { source ->
                SourceRow(
                    source = source,
                    onClick = { onSourceClick(source) },
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.screenHorizontal),
                )
            }
            if (state.hasMore) {
                item(key = "more") { PaginationFooter(isLoading = true, error = null, onRetry = onRetry) }
            }
        }
    }
}
