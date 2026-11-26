package com.nadekosu.ui.component.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadekosu.R
import com.nadekosu.ui.component.SwipeableSnackbarHost
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.theme.NadekoMiuixTheme
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.util.LocalSnackbarHost
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.MiuixPopupHost
import top.yukonga.miuix.kmp.utils.overScrollVertical

/** Page background for Miuix pages: see-through over a custom background, plain otherwise. */
@Composable
fun miuixPageColor(): Color =
    if (ThemeConfig.customBackgroundUri != null) Color.Transparent else MiuixTheme.colorScheme.surface

/** Back arrow for the navigation slot of a Miuix top bar. */
@Composable
fun MiuixBackButton(onClick: (() -> Unit)? = null) {
    val navigator = LocalNavigator.current
    IconButton(
        modifier = Modifier.padding(start = 16.dp),
        onClick = onClick ?: { navigator.pop() },
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = MiuixTheme.colorScheme.onBackground,
        )
    }
}

/** Icon button for the actions slot of a Miuix top bar. */
@Composable
fun MiuixBarAction(
    icon: ImageVector,
    contentDescription: String?,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.padding(end = 8.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) MiuixTheme.colorScheme.onBackground
            else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
        )
    }
}

/**
 * Standard Miuix page: themed, with a collapsing large title (or a small one), a back button when
 * [showBack] is set, and a scrolling [LazyColumn] body. Use [MiuixPageScaffold] for custom bodies.
 */
@Composable
fun MiuixListPage(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    onBack: (() -> Unit)? = null,
    smallTitle: Boolean = false,
    subtitle: String = "",
    bottomPadding: Dp = 0.dp,
    listState: LazyListState = rememberLazyListState(),
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    isRefreshing: Boolean? = null,
    onRefresh: () -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    MiuixPageScaffold(
        title = title,
        modifier = modifier,
        showBack = showBack,
        onBack = onBack,
        smallTitle = smallTitle,
        subtitle = subtitle,
        bottomPadding = bottomPadding,
        actions = actions,
        floatingActionButton = floatingActionButton,
    ) { innerPadding, scrollBehavior ->
        val padding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 12.dp,
            bottom = innerPadding.calculateBottomPadding() + bottomPadding + 12.dp,
        )
        val list = @Composable {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = padding,
                content = content,
            )
        }
        if (isRefreshing != null) {
            PullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                pullToRefreshState = rememberPullToRefreshState(),
                contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
                topAppBarScrollBehavior = scrollBehavior,
            ) { list() }
        } else {
            list()
        }
    }
}

/** Themed Miuix page frame with top bar, snackbar and popup host; the body is up to the caller. */
@Composable
fun MiuixPageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    onBack: (() -> Unit)? = null,
    smallTitle: Boolean = false,
    subtitle: String = "",
    bottomPadding: Dp = 0.dp,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues, ScrollBehavior) -> Unit,
) {
    NadekoMiuixTheme {
        val scrollBehavior = MiuixScrollBehavior()
        val color = miuixPageColor()
        val navigation: @Composable () -> Unit = {
            if (showBack) MiuixBackButton(onBack)
        }
        Scaffold(
            modifier = modifier,
            topBar = {
                if (smallTitle) {
                    SmallTopAppBar(
                        title = title,
                        subtitle = subtitle,
                        color = color,
                        navigationIcon = navigation,
                        actions = actions,
                        scrollBehavior = scrollBehavior,
                    )
                } else {
                    TopAppBar(
                        title = title,
                        subtitle = subtitle,
                        color = color,
                        navigationIcon = navigation,
                        actions = actions,
                        scrollBehavior = scrollBehavior,
                    )
                }
            },
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            snackbarHost = {
                SwipeableSnackbarHost(
                    modifier = Modifier.padding(bottom = bottomPadding),
                    hostState = LocalSnackbarHost.current,
                )
            },
            popupHost = { MiuixPopupHost() },
            containerColor = color,
            contentWindowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            ),
        ) { innerPadding ->
            content(innerPadding, scrollBehavior)
        }
    }
}

/** Rounded search box shown at the top of Miuix list pages. */
@Composable
fun MiuixSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = {},
        expanded = false,
        onExpandedChange = {},
        label = label,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
            )
        },
    )
}

/** Single-line Miuix text field bound to a [TextFieldState]. */
@Composable
fun MiuixTextInput(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextField(
        state = state,
        label = label,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
    )
}

/** Small rounded label, e.g. ROOT / CUSTOM on app rows. */
@Composable
fun MiuixStatusTag(
    label: String,
    backgroundColor: Color = MiuixTheme.colorScheme.secondaryContainer,
    contentColor: Color = MiuixTheme.colorScheme.onSecondaryContainer,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

/** Centered spinner for pages that are still loading. */
@Composable
fun MiuixLoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        InfiniteProgressIndicator(color = MiuixTheme.colorScheme.primary)
    }
}

/** Centered icon and message for empty lists. */
@Composable
fun MiuixEmptyState(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
