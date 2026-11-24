package com.nadekosu.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import kotlin.math.abs
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import top.yukonga.miuix.kmp.utils.PagerInterceptionMode
import top.yukonga.miuix.kmp.utils.pagerGestureOverride
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nadekosu.ui.activity.component.NavigationBar
import com.nadekosu.ui.rememberMaterial3BlurBackdrop
import com.nadekosu.ui.screen.BottomBarDestination
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.theme.blurSource
import com.nadekosu.ui.util.LocalBlurState
import com.nadekosu.ui.util.LocalHandlePageChange
import com.nadekosu.ui.util.LocalIsCurrentPage
import com.nadekosu.ui.util.LocalPagerState
import com.nadekosu.ui.util.LocalSelectedPage
import com.nadekosu.ui.util.LocalSnackbarHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** True after the first frame, so the first frame only has to build the visible tab. */
@Composable
private fun rememberContentReady(): Boolean {
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        ready = true
    }
    return ready
}

private val NavigationSpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

@Composable
fun MainScreen(pagerGestureMode: Int = 0) {
    var savedPages by rememberSaveable<MutableState<List<BottomBarDestination>>> {
        mutableStateOf(emptyList())
    }

    val pages by produceState(initialValue = savedPages) {
        value = withContext(Dispatchers.IO) {
            savedPages = BottomBarDestination.getPages()
            return@withContext savedPages
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var uiSelectedPage by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = uiSelectedPage,
        pageCount = { pages.size }
    )
    // Only the current tab is built for the first frame; the others follow once it is on screen.
    val contentReady = rememberContentReady()

    // True while a tab tap is moving the pager, so tabs passed on the way do not count as current.
    var isNavigating by remember { mutableStateOf(false) }

    // The job that is moving the pager for the latest tab tap; null while the user is in control.
    val navJob = remember { arrayOfNulls<Job>(1) }

    val handlePageChange: (Int) -> Unit = remember(pagerState, coroutineScope) {
        { page ->
            // A repeated tap on the page we are already heading to must not restart the animation.
            if (page != uiSelectedPage || page != pagerState.currentPage) {
                uiSelectedPage = page
                navJob[0]?.cancel()
                isNavigating = true
                navJob[0] = coroutineScope.launch {
                    val self = coroutineContext.job
                    try {
                        val from = pagerState.currentPage
                        if (abs(page - from) > 1) {
                            // Hop next to the target without animating, so the tabs in between
                            // are never drawn sliding past.
                            pagerState.scrollToPage(if (page > from) page - 1 else page + 1)
                        }
                        pagerState.animateScrollToPage(page, animationSpec = NavigationSpringSpec)
                    } finally {
                        if (navJob[0] === self) isNavigating = false
                    }
                }
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            // Follow swipes, but not the pages a tab tap passes through.
            if (navJob[0]?.isActive != true) uiSelectedPage = page
        }
    }

    BackHandler(pagerState.currentPage != 0) {
        handlePageChange(0)
    }

    CompositionLocalProvider(
        LocalPagerState provides pagerState,
        LocalHandlePageChange provides handlePageChange,
        LocalSelectedPage provides uiSelectedPage
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val isPortrait = maxWidth < maxHeight || (maxHeight / maxWidth > 1.4f)
            val content = @Composable { paddingBottom: Dp ->
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxSize()
                        .blurSource()
                        .pagerGestureOverride(
                            pagerState = pagerState,
                            mode = PagerInterceptionMode.entries
                                .getOrElse(pagerGestureMode) { PagerInterceptionMode.Native },
                            enabled = true,
                        ),
                    state = pagerState,
                    beyondViewportPageCount = if (contentReady) (pages.size - 1).coerceAtLeast(1) else 0,
                    overscrollEffect = null,
                    flingBehavior = PagerDefaults.flingBehavior(
                        state = pagerState,
                        snapAnimationSpec = NavigationSpringSpec,
                    ),
                ) { pageIndex ->
                    if (pages.isEmpty()) return@HorizontalPager

                    val isCurrentPage = !isNavigating && pageIndex == pagerState.settledPage
                    if (contentReady || pageIndex == pagerState.currentPage) {
                        val snackBarHostState = remember { SnackbarHostState() }
                        CompositionLocalProvider(
                            LocalSnackbarHost provides snackBarHostState,
                            LocalBlurState provides rememberMaterial3BlurBackdrop(ThemeConfig.isEnableBlur || ThemeConfig.isEnableLiquidGlass),
                            LocalIsCurrentPage provides isCurrentPage,
                        ) {
                            val destination = pages[pageIndex]
                            destination.direction(paddingBottom)
                        }
                    }
                }
            }

            if (isPortrait) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            destinations = pages,
                            isBottomBar = true,
                        )
                    },
                    containerColor = Color.Transparent,
                ) { innerPadding ->
                    content(innerPadding.calculateBottomPadding())
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationBar(
                        destinations = pages,
                        isBottomBar = false,
                    )
                    content(0.dp)
                }
            }
        }
    }
}
