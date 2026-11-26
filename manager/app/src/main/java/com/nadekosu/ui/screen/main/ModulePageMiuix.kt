package com.nadekosu.ui.screen.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Wysiwyg
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadekosu.R
import com.nadekosu.data.AppPreferencesRepository
import com.nadekosu.ui.component.miuix.MiuixBarAction
import com.nadekosu.ui.component.miuix.MiuixEmptyState
import com.nadekosu.ui.component.miuix.MiuixPageScaffold
import com.nadekosu.ui.component.miuix.MiuixSearchField
import com.nadekosu.ui.component.miuix.MiuixStatusTag
import com.nadekosu.ui.component.miuix.miuixCardColors
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.navigation.Route
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.data.appPreferences
import com.nadekosu.ui.viewmodel.ModuleUiState
import com.nadekosu.ui.viewmodel.ModuleViewModel
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * Miuix frame for the module tab. The list itself is the shared [ModuleList], drawn with
 * [MiuixModuleItem] cards, so install, update, shortcut and uninstall logic stay in one place.
 */
@Composable
internal fun MiuixModuleScaffold(
    viewModel: ModuleViewModel,
    uiState: ModuleUiState,
    prefs: AppPreferencesRepository,
    bottomPadding: Dp,
    hasMagisk: Boolean,
    hideInstallButton: Boolean,
    onInstallClick: () -> Unit,
    moduleList: @Composable (modifier: Modifier, topPadding: Dp, bottomPadding: Dp, header: @Composable () -> Unit) -> Unit,
) {
    val navigator = LocalNavigator.current
    var showMenu by remember { mutableStateOf(false) }

    MiuixPageScaffold(
        title = stringResource(R.string.module),
        showBack = false,
        bottomPadding = bottomPadding,
        navigationIcon = {
            MiuixBarAction(
                icon = Icons.Rounded.Cloud,
                contentDescription = stringResource(R.string.module_repo),
                modifier = Modifier,
                onClick = { navigator.push(Route.ModuleRepo) },
            )
        },
        actions = {
            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = { showMenu = true },
                holdDownState = showMenu,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.menu_options),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        },
        floatingActionButton = {
            if (!hideInstallButton) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = bottomPadding + 5.dp, end = 4.dp),
                    onClick = onInstallClick,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.package_import),
                        contentDescription = stringResource(R.string.install),
                        tint = MiuixTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
    ) { innerPadding, scrollBehavior ->
        val search: @Composable () -> Unit = {
            MiuixSearchField(
                query = uiState.search,
                onQueryChange = viewModel::updateSearch,
                label = stringResource(R.string.search_modules),
            )
        }
        when {
            hasMagisk -> Column(Modifier.padding(top = innerPadding.calculateTopPadding())) {
                MiuixEmptyState(
                    icon = Icons.Rounded.Warning,
                    text = stringResource(R.string.module_magisk_conflict),
                )
            }

            uiState.moduleList.isEmpty() -> Column(Modifier.padding(top = innerPadding.calculateTopPadding() + 12.dp)) {
                search()
                MiuixEmptyState(
                    icon = Icons.Rounded.Extension,
                    text = stringResource(
                        if (uiState.search.isNotEmpty()) R.string.search_no_any_match else R.string.module_empty
                    ),
                )
            }

            else -> moduleList(
                Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                innerPadding.calculateTopPadding(),
                bottomPadding + innerPadding.calculateBottomPadding(),
                search,
            )
        }

        MiuixModuleMenuSheet(
            show = showMenu,
            onDismiss = { showMenu = false },
            viewModel = viewModel,
            uiState = uiState,
            prefs = prefs,
            bottomPadding = bottomPadding,
        )
    }
}

@Composable
private fun MiuixModuleMenuSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: ModuleViewModel,
    uiState: ModuleUiState,
    prefs: AppPreferencesRepository,
    bottomPadding: Dp,
) {
    val context = LocalContext.current
    val restoreLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) viewModel.restoreModules(context, uri) }

    OverlayBottomSheet(
        show = show,
        title = stringResource(R.string.menu_options),
        onDismissRequest = onDismiss,
    ) {
        Column(Modifier.padding(bottom = bottomPadding + 16.dp)) {
            Card(colors = miuixCardColors(), modifier = Modifier.padding(bottom = 12.dp)) {
                SwitchPreference(
                    title = stringResource(R.string.module_sort_action_first),
                    checked = uiState.sortActionFirst,
                    onCheckedChange = { checked ->
                        viewModel.setSortActionFirst(checked)
                        prefs.putBoolean("module_sort_action_first", checked)
                    },
                )
                SwitchPreference(
                    title = stringResource(R.string.module_sort_enabled_first),
                    checked = uiState.sortEnabledFirst,
                    onCheckedChange = { checked ->
                        viewModel.setSortEnabledFirst(checked)
                        prefs.putBoolean("module_sort_enabled_first", checked)
                    },
                )
            }
            Text(
                text = stringResource(R.string.module_backup_restore),
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onBackgroundVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    text = stringResource(R.string.module_backup),
                    onClick = { viewModel.backupModules(context) },
                    enabled = !uiState.isBackupRestoreRunning,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(R.string.module_restore),
                    onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                    enabled = !uiState.isBackupRestoreRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
            if (uiState.isBackupRestoreRunning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
            }
            uiState.backupRestoreResult?.let { message ->
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MiuixModuleActionButton(
    icon: ImageVector,
    contentDescription: String?,
    enabled: Boolean = true,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    IconButton(
        onClick = onClick,
        enabled = enabled,
        backgroundColor = if (primary) colors.primary else colors.secondaryContainer,
        minHeight = 36.dp,
        minWidth = 52.dp,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = when {
                !enabled -> colors.disabledOnSecondaryVariant
                primary -> colors.onPrimary
                else -> colors.onSecondaryContainer
            },
        )
    }
}

/** Miuix card for one module; same callbacks as the Material [ModuleItem]. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MiuixModuleItem(
    viewModel: ModuleViewModel,
    module: ModuleViewModel.ModuleInfo,
    moduleSizes: Map<String, String>,
    moduleBanners: Map<String, ImageBitmap?>,
    updateUrl: String,
    onUninstallClicked: (ModuleViewModel.ModuleInfo) -> Unit,
    onCheckChanged: (Boolean) -> Unit,
    onUpdate: (ModuleViewModel.ModuleInfo) -> Unit,
    onClick: (ModuleViewModel.ModuleInfo) -> Unit,
    onModuleAddShortcut: (ModuleViewModel.ModuleInfo) -> Unit,
) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val prefs = context.appPreferences
    val isHideTagRow = prefs.getBoolean("is_hide_tag_row", false)
    val showMoreModuleInfo = prefs.getBoolean("show_more_module_info", false)
    val hapticFeedback = LocalHapticFeedback.current
    val colors = MiuixTheme.colorScheme
    val bannerBitmap = moduleBanners[module.dirId]
    val textDecoration = if (module.remove) TextDecoration.LineThrough else null

    LaunchedEffect(module.dirId) {
        viewModel.loadSize(module.dirId)
        viewModel.loadBanner(module.dirId)
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
        colors = miuixCardColors(),
        insideMargin = PaddingValues(0.dp),
    ) {
        Box {
            if (bannerBitmap != null) {
                Box(Modifier.matchParentSize()) {
                    Image(
                        bitmap = bannerBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = CardConfig.moduleBannerAlpha,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        colors.surfaceContainer.copy(alpha = 0f),
                                        colors.surfaceContainer.copy(alpha = 0.85f),
                                    )
                                )
                            )
                    )
                }
            }
            Column(
                modifier = Modifier
                    .then(
                        if (module.hasActionScript || module.hasWebUi) {
                            Modifier.combinedClickable(
                                onLongClick = { onModuleAddShortcut(module) },
                                onClick = { if (module.hasWebUi) onClick(module) },
                            )
                        } else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = module.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                            textDecoration = textDecoration,
                        )
                        Text(
                            text = "${stringResource(R.string.module_version)}: ${module.version}",
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariantSummary,
                            textDecoration = textDecoration,
                        )
                        Text(
                            text = "${stringResource(R.string.module_author)}: ${module.author}",
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariantSummary,
                            textDecoration = textDecoration,
                        )
                        if (showMoreModuleInfo && module.updateJson.isNotEmpty()) {
                            Text(
                                text = "${stringResource(R.string.module_update_json)}: ${module.updateJson}",
                                fontSize = 13.sp,
                                color = colors.primary,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Update JSON URL", module.updateJson))
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.module_update_json_copied),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    },
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = module.enabled,
                        enabled = !module.update,
                        onCheckedChange = onCheckChanged,
                    )
                }

                if (module.description.isNotBlank()) {
                    Text(
                        text = module.description,
                        fontSize = 14.sp,
                        color = colors.onSurfaceSecondary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = textDecoration,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                if (!isHideTagRow) {
                    FlowRow(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        MiuixStatusTag(module.dirId, colors.primary.copy(alpha = 0.15f), colors.primary)
                        if (module.metamodule) {
                            MiuixStatusTag("META", colors.tertiaryContainer, colors.onTertiaryContainer)
                        }
                        MiuixStatusTag(moduleSizes[module.dirId] ?: "0 KB")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (module.hasActionScript) {
                        MiuixModuleActionButton(
                            icon = Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(R.string.action),
                            enabled = !module.remove && module.enabled,
                        ) {
                            navigator.push(Route.ExecuteModuleAction(module.dirId))
                            viewModel.markNeedRefresh()
                        }
                    }
                    if (module.hasWebUi) {
                        MiuixModuleActionButton(
                            icon = Icons.AutoMirrored.Rounded.Wysiwyg,
                            contentDescription = null,
                            enabled = !module.remove && module.enabled,
                        ) { onClick(module) }
                    }
                    Spacer(Modifier.weight(1f))
                    if (updateUrl.isNotEmpty()) {
                        MiuixModuleActionButton(
                            icon = Icons.Rounded.Download,
                            contentDescription = stringResource(R.string.module_update),
                            enabled = !module.remove,
                            primary = true,
                        ) { onUpdate(module) }
                    }
                    MiuixModuleActionButton(
                        icon = if (module.remove) Icons.Rounded.Restore else Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.uninstall),
                    ) { onUninstallClicked(module) }
                }
            }
        }
    }
}
