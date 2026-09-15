package com.nadekosu.ui.activity.component

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailColors
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nadekosu.ksuApp
import com.nadekosu.ui.component.FloatingBottomBar
import com.nadekosu.ui.component.FloatingBottomBarItem
import com.nadekosu.ui.screen.BottomBarDestination
import com.nadekosu.ui.theme.BottomBarStyle
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.theme.blurEffect
import com.nadekosu.ui.util.LocalBlurState
import com.nadekosu.ui.util.LocalHandlePageChange
import com.nadekosu.ui.util.LocalSelectedPage
import com.nadekosu.ui.util.getModuleCount
import com.nadekosu.ui.util.getSuperuserCount
import com.nadekosu.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavigationBar(
    destinations: List<BottomBarDestination>,
    isBottomBar: Boolean
) {
    // 是否隐藏 badge
    val homeViewModel = viewModel<HomeViewModel>(viewModelStoreOwner = ksuApp)
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val isHideOtherInfo = uiState.isHideOtherInfo

    // 翻页处理
    val page = LocalSelectedPage.current
    val handlePageChange = LocalHandlePageChange.current

    // 收集计数数据
    var superuserCountSaved by rememberSaveable { mutableIntStateOf(0) }
    var moduleCountSaved by rememberSaveable { mutableIntStateOf(0) }

    val superuserCount by produceState(initialValue = superuserCountSaved) {
        withContext(Dispatchers.IO) {
            value = getSuperuserCount()
            superuserCountSaved = value
        }
    }
    val moduleCount by produceState(initialValue = moduleCountSaved) {
        withContext(Dispatchers.IO) {
            value = getModuleCount()
            moduleCountSaved = value
        }
    }

    if (isBottomBar) {
        if (ThemeConfig.bottomBarStyle == BottomBarStyle.FLOATING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
                    )
                    .padding(
                        bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    ),
                contentAlignment = Alignment.Center
            ) {
                FloatingBottomBar(
                    selectedIndex = page,
                    onSelected = { handlePageChange(it) },
                    tabsCount = destinations.size,
                    isBlurEnabled = LocalBlurState.current != null,
                ) { activateTab ->
                    destinations.forEachIndexed { index, destination ->
                        FloatingBottomBarItem(
                            selected = index == page,
                            onClick = { activateTab(index) },
                            modifier = Modifier.defaultMinSize(minWidth = 76.dp)
                        ) {
                            val contentColor = LocalContentColor.current
                            val count = when (destination) {
                                BottomBarDestination.SuperUser -> superuserCount
                                BottomBarDestination.Module -> moduleCount
                                else -> 0
                            }
                            val icon: @Composable () -> Unit = {
                                Icon(
                                    imageVector = destination.iconSelected,
                                    contentDescription = stringResource(destination.label),
                                    tint = contentColor
                                )
                            }
                            if (count > 0 && !isHideOtherInfo) {
                                BadgedBox(badge = { Badge { Text(count.toString()) } }) { icon() }
                            } else {
                                icon()
                            }
                            Text(
                                text = stringResource(destination.label),
                                color = contentColor,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                }
            }
            return
        }
        FlexibleBottomAppBar(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
                )
                .blurEffect(),
            containerColor =
                if (ThemeConfig.isEnableBlur)
                    Color.Transparent
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(CardConfig.cardAlpha),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            destinations.forEachIndexed { index, destination ->
                BottomBarNavigationItem(
                    isSelected = index == page,
                    destination = destination,
                    onClick = {
                        handlePageChange(index)
                    },
                    superuserCount = superuserCount,
                    moduleCount = moduleCount,
                    isHideOtherInfo = isHideOtherInfo,
                )
            }
        }
    } else {
        WideNavigationRail(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
                )
                .blurEffect(),
            colors = WideNavigationRailColors(
                containerColor =
                    if (ThemeConfig.isEnableBlur)
                        Color.Transparent
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(CardConfig.cardAlpha),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modalContainerColor = WideNavigationRailDefaults.colors().modalContainerColor,
                modalScrimColor = WideNavigationRailDefaults.colors().modalScrimColor,
                modalContentColor = WideNavigationRailDefaults.colors().modalContentColor,
            ),
        ) {
            destinations.forEachIndexed { index, destination ->
                NavigationRailItem(
                    isSelected = index == page,
                    destination = destination,
                    onClick = {
                        handlePageChange(index)
                    },
                    superuserCount = superuserCount,
                    moduleCount = moduleCount,
                    isHideOtherInfo = isHideOtherInfo,
                )
            }
        }
    }
}

@Composable
private fun NavigationRailItem(
    isSelected: Boolean,
    destination: BottomBarDestination,
    onClick: () -> Unit,
    superuserCount: Int,
    moduleCount: Int,
    isHideOtherInfo: Boolean
) {
    WideNavigationRailItem(
        railExpanded = false,
        selected = isSelected,
        onClick = onClick,
        icon = {
            BadgedBox(
                badge = {
                    DestinationBadge(
                        dest = destination,
                        superUser = superuserCount,
                        module = moduleCount,
                        isHideOtherInfo = isHideOtherInfo,
                    )
                }
            ) {
                if (isSelected) {
                    Icon(destination.iconSelected, stringResource(destination.label))
                } else {
                    Icon(destination.iconNotSelected, stringResource(destination.label))
                }
            }
        },
        label = {
            Text(
                stringResource(destination.label),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        },
    )
}

@Composable
private fun RowScope.BottomBarNavigationItem(
    isSelected: Boolean,
    destination: BottomBarDestination,
    onClick: () -> Unit,
    superuserCount: Int,
    moduleCount: Int,
    isHideOtherInfo: Boolean
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            BadgedBox(
                badge = {
                    DestinationBadge(
                        dest = destination,
                        superUser = superuserCount,
                        module = moduleCount,
                        isHideOtherInfo = isHideOtherInfo,
                    )
                }
            ) {
                if (isSelected) {
                    Icon(destination.iconSelected, stringResource(destination.label))
                } else {
                    Icon(destination.iconNotSelected, stringResource(destination.label))
                }
            }
        },
        label = {
            Text(
                stringResource(destination.label),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        },
        alwaysShowLabel = false
    )
}

@Composable
private fun DestinationBadge(
    dest: BottomBarDestination,
    superUser: Int,
    module: Int,
    isHideOtherInfo: Boolean
) {
    val count = when (dest) {
        BottomBarDestination.SuperUser -> superUser
        BottomBarDestination.Module -> module
        else -> 0
    }

    AnimatedVisibility(
        visible = count > 0 && !isHideOtherInfo,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Badge(
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(count.toString())
        }
    }
}
