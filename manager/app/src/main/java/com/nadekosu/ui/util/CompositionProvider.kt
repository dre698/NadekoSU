package com.nadekosu.ui.util

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.layout.LayoutCoordinates
import com.nadekosu.ui.activity.PermissionRequestInterface
import top.yukonga.miuix.kmp.blur.LayerBackdrop

val LocalSnackbarHost = compositionLocalOf<SnackbarHostState> {
    error("CompositionLocal LocalSnackbarController not present")
}

val LocalBlurState = compositionLocalOf<LayerBackdrop?> {
    error("CompositionLocal LocalBlurState not present")
}

val LocalPagerState = compositionLocalOf<PagerState> { error("No pager state") }
val LocalHandlePageChange = compositionLocalOf<(Int) -> Unit> { error("No handle page change") }
val LocalSelectedPage = compositionLocalOf<Int> { error("No selected page") }

/**
 * True only for the tab that is on screen and at rest. A tab composed in advance, or passed over
 * while switching tabs quickly, reads false and should hold back its data loading until it is true.
 */
val LocalIsCurrentPage = compositionLocalOf { true }

val LocalBackgroundBlurAnchor = staticCompositionLocalOf<LayoutCoordinates?> { null }

val LocalPortraitState = compositionLocalOf<Boolean> { error("No portrait state") }

val LocalPermissionRequestInterface = compositionLocalOf<PermissionRequestInterface> {
    error("CompositionLocal LocalPermissionRequestInterface not present")
}