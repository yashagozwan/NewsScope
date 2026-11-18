package com.yasha.newsscope.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.yasha.newsscope.R
import com.yasha.newsscope.core.designsystem.spacing
import com.yasha.newsscope.domain.model.Article

/**
 * Renders a Paging 3 article stream: skeletons on first load, full-screen error with retry when
 * the first page fails, an empty state, and an independent footer for append loading/errors.
 */
@Composable
fun ArticlePagingList(
    items: LazyPagingItems<Article>,
    onArticleClick: (Article) -> Unit,
    emptyTitle: String,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val refresh = items.loadState.refresh
    val append = items.loadState.append
    val isEmpty = items.itemCount == 0

    when {
        refresh is LoadState.Loading && isEmpty ->
            SkeletonList(modifier) { ArticleSkeletonItem() }

        refresh is LoadState.Error && isEmpty ->
            ErrorState(
                error = refresh.error.asNewsError(),
                onRetry = items::refresh,
                modifier = modifier,
            )

        refresh is LoadState.NotLoading && isEmpty && append.endOfPaginationReached ->
            EmptyState(emptyTitle, emptyMessage, modifier)

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.Top,
        ) {
            items(
                count = items.itemCount,
                key = items.itemKey { it.url },
            ) { index ->
                items[index]?.let { article ->
                    ArticleRow(
                        article = article,
                        onClick = { onArticleClick(article) },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.screenHorizontal),
                    )
                }
            }
            item(key = "footer") {
                when {
                    append is LoadState.Loading -> PaginationFooter(true, null, items::retry)
                    append is LoadState.Error -> PaginationFooter(false, append.error.asNewsError(), items::retry)
                    append.endOfPaginationReached && !isEmpty -> Text(
                        text = stringResource(R.string.articles_end),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.xl),
                    )
                }
            }
        }
    }
}
