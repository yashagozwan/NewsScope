package com.yasha.newsscope.presentation.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.SslErrorHandler
import android.net.http.SslError
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yasha.newsscope.R
import com.yasha.newsscope.core.util.UrlSafety
import com.yasha.newsscope.presentation.components.MessageState
import com.yasha.newsscope.presentation.components.ScreenTopBar
import kotlinx.coroutines.launch

/**
 * In-app article reader. Only http(s) pages are loaded (http is upgraded to https); every other
 * scheme, including redirects to `intent:`/`file:`/`javascript:`, is blocked.
 */
@Composable
fun ArticleWebScreen(
    url: String,
    sourceName: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeUrl = remember(url) { UrlSafety.toSafeWebUrl(url) }
    val host = remember(safeUrl) { safeUrl?.toUri()?.host?.removePrefix("www.") }
    var progress by remember { mutableFloatStateOf(0f) }
    var loadFailed by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val blockedMessage = stringResource(R.string.web_blocked_navigation)

    BackHandler(enabled = canGoBack && !loadFailed) { webView?.goBack() }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(
                title = host ?: sourceName ?: stringResource(R.string.app_name),
                subtitle = sourceName.takeIf { host != null },
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                safeUrl == null -> MessageState(
                    icon = Icons.Outlined.BrokenImage,
                    title = stringResource(R.string.web_invalid_url_title),
                    message = stringResource(R.string.web_invalid_url_message),
                )

                else -> {
                    ManagedWebView(
                        initialUrl = safeUrl,
                        onWebViewReady = { webView = it },
                        onProgress = { progress = it },
                        onPageStarted = { loadFailed = false },
                        onLoadFailed = { loadFailed = true },
                        onHistoryChanged = { canGoBack = it },
                        onBlocked = { scope.launch { snackbarHost.showSnackbar(blockedMessage) } },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (loadFailed) {
                        MessageState(
                            icon = Icons.Outlined.CloudOff,
                            title = stringResource(R.string.web_load_failed_title),
                            message = stringResource(R.string.web_load_failed_message),
                            actionLabel = stringResource(R.string.action_retry),
                            onAction = {
                                loadFailed = false
                                webView?.reload()
                            },
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        )
                    } else if (progress < 1f) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled") // Article sites require JavaScript; navigation is restricted below.
@Composable
private fun ManagedWebView(
    initialUrl: String,
    onWebViewReady: (WebView) -> Unit,
    onProgress: (Float) -> Unit,
    onPageStarted: () -> Unit,
    onLoadFailed: () -> Unit,
    onHistoryChanged: (canGoBack: Boolean) -> Unit,
    onBlocked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var current by remember { mutableStateOf<WebView?>(null) }

    // Pause/resume with the screen so background pages stop consuming CPU and network.
    DisposableEffect(lifecycleOwner, current) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> current?.onPause()
                Lifecycle.Event.ON_RESUME -> current?.onResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = false
                    allowContentAccess = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    safeBrowsingEnabled = true
                    setSupportMultipleWindows(false)
                    javaScriptCanOpenWindowsAutomatically = false
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView, newProgress: Int) {
                        onProgress(newProgress / 100f)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val target = request.url.toString()
                        val safe = UrlSafety.toSafeWebUrl(target)
                        return when {
                            safe == null -> { onBlocked(); true }
                            safe != target -> { view.loadUrl(safe); true } // http -> https upgrade
                            else -> false
                        }
                    }

                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) = onPageStarted()

                    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                        onHistoryChanged(view.canGoBack())
                    }

                    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                        if (request.isForMainFrame) onLoadFailed()
                    }

                    override fun onReceivedHttpError(
                        view: WebView,
                        request: WebResourceRequest,
                        errorResponse: WebResourceResponse,
                    ) {
                        if (request.isForMainFrame && errorResponse.statusCode >= HTTP_ERROR_THRESHOLD) onLoadFailed()
                    }

                    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                        handler.cancel() // never proceed past certificate problems
                        onLoadFailed()
                    }
                }
                current = this
                onWebViewReady(this)
                loadUrl(initialUrl)
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            webView.destroy()
        },
    )
}

private const val HTTP_ERROR_THRESHOLD = 400
