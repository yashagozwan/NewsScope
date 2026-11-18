package com.yasha.newsscope.presentation.splash

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.yasha.newsscope.R
import com.yasha.newsscope.core.designsystem.BrandColors
import com.yasha.newsscope.core.designsystem.NewsScopeTheme
import com.yasha.newsscope.core.designsystem.spacing
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LogoDurationMs = 600
private const val TextStaggerMs = 220
private const val TextDurationMs = 500
private const val HoldAfterAnimationMs = 450L

/**
 * Brand presentation shown right after the system splash screen. It uses the same background as the
 * system splash so the hand-off is seamless. The only wait is the entrance animation itself
 * (~1.3s); no work is artificially delayed.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    val logoProgress = remember { Animatable(0f) }
    val wordmarkProgress = remember { Animatable(0f) }
    val taglineProgress = remember { Animatable(0f) }

    // The splash is navy in both themes, so the status bar icons must be light while it is showing.
    val activity = LocalActivity.current
    val view = LocalView.current
    DisposableEffect(activity) {
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { previous?.let { controller.isAppearanceLightStatusBars = it } }
    }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { logoProgress.animateTo(1f, tween(LogoDurationMs, easing = FastOutSlowInEasing)) }
            launch {
                delay(TextStaggerMs.toLong())
                wordmarkProgress.animateTo(1f, tween(TextDurationMs, easing = FastOutSlowInEasing))
            }
            launch {
                delay(TextStaggerMs * 2L)
                taglineProgress.animateTo(1f, tween(TextDurationMs, easing = FastOutSlowInEasing))
            }
        }
        delay(HoldAfterAnimationMs)
        currentOnFinished()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandColors.Navy),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_newsscope_logo),
            contentDescription = stringResource(R.string.cd_app_logo),
            modifier = Modifier
                .size(LogoSize)
                .alpha(logoProgress.value)
                .scale(0.88f + 0.12f * logoProgress.value),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = BrandColors.Paper,
            textAlign = TextAlign.Center,
            modifier = Modifier.slideUpFade(wordmarkProgress.value),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Spacer(
            Modifier
                .width(DividerWidth)
                .height(1.dp)
                .alpha(taglineProgress.value)
                .background(BrandColors.Gold),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(R.string.app_tagline).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = BrandColors.Paper.copy(alpha = 0.78f),
            textAlign = TextAlign.Center,
            modifier = Modifier.slideUpFade(taglineProgress.value),
        )
    }
}

private val LogoSize = 144.dp
private val DividerWidth = 40.dp

private fun Modifier.slideUpFade(progress: Float): Modifier =
    graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 12.dp.toPx()
    }

@Preview(showBackground = true, backgroundColor = 0xFF172C46)
@Composable
private fun SplashScreenPreview() {
    NewsScopeTheme { SplashScreen(onFinished = {}) }
}
