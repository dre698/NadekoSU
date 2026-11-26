package com.nadekosu.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.nadekosu.Natives
import com.nadekosu.R
import com.nadekosu.ksuApp
import com.nadekosu.ui.component.miuix.MiuixEmptyState
import com.nadekosu.ui.component.miuix.MiuixGroupCard
import com.nadekosu.ui.component.miuix.MiuixScrollableTabRow
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.component.miuix.miuixCardColors
import com.nadekosu.ui.component.miuix.MiuixListPage
import com.nadekosu.ui.component.miuix.MiuixLoadingBox
import com.nadekosu.ui.component.miuix.MiuixSearchField
import com.nadekosu.ui.component.miuix.MiuixStatusTag
import com.nadekosu.ui.component.miuix.MiuixBarAction
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.navigation.Route
import com.nadekosu.ui.util.LocalSnackbarHost
import com.nadekosu.ui.util.module.ModuleModify
import com.nadekosu.ui.viewmodel.AppCategory
import com.nadekosu.ui.viewmodel.SortType
import com.nadekosu.ui.viewmodel.SuperUserViewModel
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixSuperUserPage(bottomPadding: Dp) {
    val context = LocalContext.current
    val viewModel = viewModel<SuperUserViewModel>(viewModelStoreOwner = ksuApp)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = LocalSnackbarHost.current
    val backupLauncher = ModuleModify.rememberAllowlistBackupLauncher(context, snackBarHostState)
    val restoreLauncher = ModuleModify.rememberAllowlistRestoreLauncher(context, snackBarHostState)

    LaunchedEffect(Unit) { viewModel.updateSearch("") }

    val appCounts = remember(uiState.appGroupList) {
        mapOf(
            AppCategory.ALL to uiState.appGroupList.size,
            AppCategory.ROOT to uiState.appGroupList.count { it.allowSu },
            AppCategory.CUSTOM to uiState.appGroupList.count { !it.allowSu && it.hasCustomProfile },
            AppCategory.DEFAULT to uiState.appGroupList.count { !it.allowSu && !it.hasCustomProfile },
        )
    }

    val sortLabels = SortType.entries.map { stringResource(it.displayNameRes) }
    val refreshLabel = stringResource(R.string.refresh)
    val systemAppsLabel = stringResource(
        if (uiState.showSystemApps) R.string.hide_system_apps else R.string.show_system_apps
    )
    val backupLabel = stringResource(R.string.backup_allowlist)
    val restoreLabel = stringResource(R.string.restore_allowlist)

    MiuixListPage(
        title = stringResource(R.string.superuser),
        showBack = false,
        bottomPadding = bottomPadding,
        // While the list is still empty the body shows its own spinner; keeping the pull-to-refresh
        // header on as well would draw two loading indicators at once.
        isRefreshing = uiState.isRefreshing && uiState.appGroupList.isNotEmpty(),
        onRefresh = { scope.launch { viewModel.fetchAppList() } },
        navigationIcon = {
            MiuixBarAction(
                icon = Icons.AutoMirrored.Rounded.Article,
                contentDescription = stringResource(R.string.sulog),
                modifier = Modifier,
                onClick = { navigator.push(Route.Sulog) },
            )
        },
        actions = {
            OverlayIconDropdownMenu(
                entry = DropdownEntry(
                    items = SortType.entries.mapIndexed { index, type ->
                        DropdownItem(
                            text = sortLabels[index],
                            selected = uiState.currentSortType == type,
                            onClick = { viewModel.updateCurrentSortType(type) },
                        )
                    }
                ),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = stringResource(R.string.sort_options),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
            OverlayIconDropdownMenu(
                entry = DropdownEntry(
                    items = listOf(
                        DropdownItem(
                            text = refreshLabel,
                            onClick = { scope.launch { viewModel.fetchAppList() } },
                        ),
                        DropdownItem(
                            text = systemAppsLabel,
                            onClick = { viewModel.updateShowSystemApps(!uiState.showSystemApps) },
                        ),
                        DropdownItem(
                            text = backupLabel,
                            onClick = { backupLauncher.launch(ModuleModify.createAllowlistBackupIntent()) },
                        ),
                        DropdownItem(
                            text = restoreLabel,
                            onClick = { restoreLauncher.launch(ModuleModify.createAllowlistRestoreIntent()) },
                        ),
                    )
                ),
                modifier = Modifier.padding(end = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.menu_options),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        },
    ) {
        item(key = "search") {
            MiuixSearchField(
                query = uiState.search,
                onQueryChange = viewModel::updateSearch,
                label = stringResource(R.string.search_apps),
            )
        }
        item(key = "categories") {
            val tabs = AppCategory.entries.map {
                "${stringResource(it.displayNameRes)} (${appCounts[it] ?: 0})"
            }
            MiuixScrollableTabRow(
                tabs = tabs,
                selectedTabIndex = AppCategory.entries.indexOf(uiState.selectedCategory),
                onTabSelected = { viewModel.updateSelectedCategory(AppCategory.entries[it]) },
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            )
        }

        val apps = uiState.appGroupList
        when {
            apps.isEmpty() && uiState.search.isEmpty() &&
                (uiState.isRefreshing || uiState.selectedCategory == AppCategory.ALL) -> {
                item(key = "loading") { MiuixLoadingBox(Modifier.height(240.dp)) }
            }

            apps.isEmpty() -> item(key = "empty") {
                val searching = uiState.search.isNotEmpty()
                MiuixEmptyState(
                    icon = if (searching) Icons.Rounded.SearchOff else Icons.Rounded.Archive,
                    text = stringResource(
                        if (searching || uiState.selectedCategory == AppCategory.ALL) R.string.no_apps_found
                        else R.string.no_apps_in_category
                    ),
                )
            }

            else -> items(apps, key = { it.uid }, contentType = { "app-group" }) { group ->
                MiuixAppGroupCard(group) {
                    navigator.push(
                        Route.AppProfile(uid = group.uid, packageName = group.mainApp.packageName)
                    )
                }
            }
        }
        item(key = "bottom") { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
private fun MiuixAppGroupCard(
    group: SuperUserViewModel.AppGroup,
    onClick: () -> Unit,
) {
    val app = group.mainApp
    val colors = MiuixTheme.colorScheme
    val userId = group.uid / 100000
    val shouldUmount = remember(group.uid) { Natives.uidShouldUmount(group.uid) }
    val isDark = ThemeConfig.forceDarkMode ?: isSystemInDarkTheme()
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        colors = miuixCardColors(),
        onClick = onClick,
        showIndication = true,
        insideMargin = PaddingValues(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(app.packageInfo)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = app.label,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(48.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    color = colors.onSurface,
                    fontWeight = FontWeight(550),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (group.apps.size > 1) {
                        stringResource(R.string.group_contains_apps, group.apps.size)
                    } else {
                        app.packageName
                    },
                    color = colors.onSurfaceVariantSummary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight(550),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val showTags = group.allowSu || shouldUmount || group.hasCustomProfile || userId != 0
            if (showTags) {
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (group.allowSu) {
                        MiuixStatusTag(
                            "ROOT",
                            colors.tertiaryContainer.copy(alpha = 0.6f),
                            colors.onTertiaryContainer.copy(alpha = 0.8f),
                        )
                    }
                    if (shouldUmount) {
                        MiuixStatusTag(
                            "UMOUNT",
                            if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f),
                            if (isDark) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.8f),
                        )
                    }
                    if (group.hasCustomProfile) {
                        MiuixStatusTag(
                            "CUSTOM",
                            colors.secondaryContainer.copy(alpha = 0.8f),
                            colors.onSecondaryContainer,
                        )
                    }
                    if (userId != 0) {
                        MiuixStatusTag(
                            "USER $userId",
                            colors.secondaryContainer.copy(alpha = 0.8f),
                            colors.onSecondaryContainer,
                        )
                    }
                }
            }
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceVariantActions,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(width = 10.dp, height = 16.dp),
            )
        }
    }
}
