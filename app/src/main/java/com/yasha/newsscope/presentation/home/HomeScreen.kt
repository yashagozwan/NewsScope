package com.yasha.newsscope.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yasha.newsscope.R
import com.yasha.newsscope.core.common.UiState
import com.yasha.newsscope.core.designsystem.spacing
import com.yasha.newsscope.domain.model.Article
import com.yasha.newsscope.domain.model.NewsCategory
import com.yasha.newsscope.presentation.category.CategoryChip
import com.yasha.newsscope.presentation.category.icon
import com.yasha.newsscope.presentation.category.tagline
import com.yasha.newsscope.presentation.components.ArticleRow
import com.yasha.newsscope.presentation.components.ArticleSkeletonItem
import com.yasha.newsscope.presentation.components.FeaturedArticleCard
import com.yasha.newsscope.presentation.components.SkeletonBlock
import com.yasha.newsscope.presentation.components.ErrorState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSearchClick: () -> Unit,
    onBrowseSources: (NewsCategory) -> Unit,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gutter = MaterialTheme.spacing.screenHorizontal

    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = MaterialTheme.spacing.xxl),
    ) {
        item(key = "header") {
            HomeHeader(isDarkTheme, onToggleTheme, onSearchClick, Modifier.padding(horizontal = gutter))
        }
        item(key = "featured-title") {
            SectionTitle(stringResource(R.string.home_featured_title), Modifier.padding(horizontal = gutter))
        }
        featuredSection(state.featured, gutter, onArticleClick, viewModel::retryFeatured)

        item(key = "categories-title") {
            SectionTitle(stringResource(R.string.home_categories_title), Modifier.padding(horizontal = gutter))
        }
        item(key = "categories") {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = gutter),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                items(state.categories, key = { it.apiValue }) { category ->
                    CategoryChip(
                        category = category,
                        selected = category == state.selectedCategory,
                        onClick = { viewModel.onCategorySelected(category) },
                    )
                }
            }
        }
        item(key = "discover-title") {
            SectionTitle(stringResource(R.string.home_discover_title), Modifier.padding(horizontal = gutter))
        }
        item(key = "discover") {
            DiscoverCard(
                category = state.selectedCategory,
                onBrowse = { onBrowseSources(state.selectedCategory) },
                modifier = Modifier.padding(horizontal = gutter),
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.featuredSection(
    featured: UiState<List<Article>>,
    gutter: androidx.compose.ui.unit.Dp,
    onArticleClick: (Article) -> Unit,
    onRetry: () -> Unit,
) {
    when (featured) {
        UiState.Loading -> item(key = "featured-loading") {
            Column(Modifier.padding(horizontal = gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBlock(Modifier.fillMaxWidth().height(180.dp))
                ArticleSkeletonItem()
            }
        }

        is UiState.Error -> item(key = "featured-error") {
            ErrorState(error = featured.error, onRetry = onRetry, modifier = Modifier.height(280.dp))
        }

        UiState.Empty -> Unit // Nothing to feature: omit the section body rather than invent content.

        is UiState.Success -> {
            val lead = featured.data.first()
            item(key = "lead-${lead.url}") {
                FeaturedArticleCard(lead, { onArticleClick(lead) }, Modifier.padding(horizontal = gutter))
            }
            items(featured.data.drop(1), key = { it.url }) { article ->
                ArticleRow(article, { onArticleClick(article) }, Modifier.padding(horizontal = gutter))
            }
        }
    }
}

@Composable
private fun HomeHeader(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }
    Column(modifier.statusBarsPadding().padding(top = MaterialTheme.spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoMark(painterResource(R.drawable.ic_newsscope_logo))
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = stringResource(R.string.cd_toggle_theme),
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_search))
            }
        }
        Text(
            text = today.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

/** The vector logo is white-on-transparent line art; it sits on a navy tile to stay legible in both themes. */
@Composable
private fun LogoMark(painter: Painter) {
    Image(
        painter = painter,
        contentDescription = stringResource(R.string.cd_app_logo),
        modifier = Modifier
            .size(40.dp)
            .background(com.yasha.newsscope.core.designsystem.BrandColors.Navy, MaterialTheme.shapes.small),
    )
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = MaterialTheme.spacing.xl, bottom = MaterialTheme.spacing.md)) {
        Spacer(
            Modifier
                .width(32.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.tertiary),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun DiscoverCard(category: NewsCategory, onBrowse: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
        Text(category.displayName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(category.tagline, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            stringResource(R.string.home_discover_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Button(
            onClick = onBrowse,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(stringResource(R.string.home_discover_cta, category.displayName))
        }
    }
}
