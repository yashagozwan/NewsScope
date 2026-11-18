package com.yasha.newsscope.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yasha.newsscope.R
import com.yasha.newsscope.core.error.NewsError

@Composable
fun NewsError.userMessage(): String = when (this) {
    is NewsError.NoConnection -> stringResource(R.string.error_no_connection)
    is NewsError.Timeout -> stringResource(R.string.error_timeout)
    is NewsError.ApiKeyMissing -> stringResource(R.string.error_api_key_missing)
    is NewsError.Unauthorized -> stringResource(R.string.error_unauthorized)
    is NewsError.UpgradeRequired -> stringResource(R.string.error_upgrade_required)
    is NewsError.RateLimited -> stringResource(R.string.error_rate_limited)
    is NewsError.Server -> stringResource(R.string.error_server, code)
    is NewsError.Http -> stringResource(R.string.error_http, code)
    is NewsError.InvalidResponse -> stringResource(R.string.error_invalid_response)
    is NewsError.Unknown -> stringResource(R.string.error_unknown)
}

/** Paging delivers arbitrary Throwables; anything that is not a [NewsError] becomes Unknown. */
fun Throwable.asNewsError(): NewsError = this as? NewsError ?: NewsError.Unknown(this)
