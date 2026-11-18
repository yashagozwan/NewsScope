package com.yasha.newsscope.presentation.components

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yasha.newsscope.R
import com.yasha.newsscope.core.designsystem.spacing
import com.yasha.newsscope.domain.model.Article
import java.time.Instant

@Composable
fun rememberRelativeTime(instant: Instant?): String? = remember(instant) {
    instant?.let {
        DateUtils.getRelativeTimeSpanString(
            it.toEpochMilli(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString()
    }
}

@Composable
private fun ArticleMeta(article: Article, modifier: Modifier = Modifier) {
    val source = article.sourceName ?: stringResource(R.string.article_unknown_source)
    val time = rememberRelativeTime(article.publishedAt)
    Text(
        text = listOfNotNull(source.uppercase(), time).joinToString("  ·  "),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Compact editorial row: text on the left, small thumbnail on the right. */
@Composable
fun ArticleRow(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                ArticleMeta(article)
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                article.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            NewsImage(
                url = article.imageUrl,
                contentDescription = article.imageUrl?.let { stringResource(R.string.cd_article_image, article.title) },
                modifier = Modifier.size(ThumbnailSize),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

/** Large lead story used in the home screen's featured section. */
@Composable
fun FeaturedArticleCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.clickable(onClick = onClick)) {
        NewsImage(
            url = article.imageUrl,
            contentDescription = article.imageUrl?.let { stringResource(R.string.cd_article_image, article.title) },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        ArticleMeta(article)
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        article.description?.let {
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val ThumbnailSize = 96.dp
