package com.yasha.newsscope.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yasha.newsscope.R
import com.yasha.newsscope.core.designsystem.spacing

@Composable
private fun rememberShimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    return alpha
}

@Composable
fun SkeletonBlock(modifier: Modifier = Modifier) {
    val alpha = rememberShimmerAlpha()
    Box(
        modifier
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
    )
}

@Composable
fun ArticleSkeletonItem(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            SkeletonBlock(Modifier.height(12.dp).width(80.dp))
            SkeletonBlock(Modifier.height(18.dp).fillMaxWidth())
            SkeletonBlock(Modifier.height(18.dp).fillMaxWidth(0.8f))
            SkeletonBlock(Modifier.height(12.dp).fillMaxWidth(0.9f))
        }
        SkeletonBlock(Modifier.size(96.dp))
    }
}

@Composable
fun SourceSkeletonItem(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        SkeletonBlock(Modifier.height(20.dp).fillMaxWidth(0.5f))
        SkeletonBlock(Modifier.height(12.dp).fillMaxWidth())
        SkeletonBlock(Modifier.height(12.dp).fillMaxWidth(0.7f))
    }
}

/** Placeholder list shown during the first load. Announced once to screen readers. */
@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 6,
    item: @Composable () -> Unit,
) {
    val description = stringResource(R.string.loading)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = description }
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
        userScrollEnabled = false,
    ) {
        items(List(count) { it }, key = { it }) { item() }
    }
}
