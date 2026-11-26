package com.nadekosu.ui.screen.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.background
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.system.Os
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nadekosu.BuildConfig
import com.nadekosu.Natives
import com.nadekosu.R
import com.nadekosu.data.HomeLayoutStyle
import com.nadekosu.data.update.ManagerUpdateChannel
import com.nadekosu.data.update.ManagerUpdateInfo
import com.nadekosu.ksuApp
import com.nadekosu.magica.MagicaService
import com.nadekosu.ui.component.KsuIsValid
import com.nadekosu.ui.component.SwipeableSnackbarHost
import com.nadekosu.ui.component.ksuIsValid
import com.nadekosu.ui.component.miuix.MiuixWarningCard
import com.nadekosu.ui.component.miuix.MiuixGroupCard
import com.nadekosu.ui.component.miuix.miuixCardColors
import com.nadekosu.ui.component.rememberConfirmDialog
import com.nadekosu.ui.component.rememberLoadingDialog
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.navigation.Route
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.NadekoMiuixTheme
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.util.HomeBannerStorage
import com.nadekosu.ui.util.LocalPermissionRequestInterface
import com.nadekosu.ui.util.LocalSnackbarHost
import com.nadekosu.ui.util.downloader.downloadManagerUpdate
import com.nadekosu.ui.util.reboot
import com.nadekosu.ui.viewmodel.HomeUiState
import com.nadekosu.ui.viewmodel.HomeViewModel
import com.nadekosu.ui.viewmodel.ModuleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import com.nadekosu.ui.component.ListPopupDefaults
import com.nadekosu.ui.component.miuix.DropdownItem
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.MiuixPopupHost
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical

private class MiuixInfoRow(val title: String, val summary: String, val icon: ImageVector)

@Composable
internal fun MiuixHomePage(bottomPadding: Dp) {
    NadekoMiuixTheme { MiuixHomeContent(bottomPadding) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MiuixHomeContent(bottomPadding: Dp) {
    val context = LocalContext.current
    val viewModel = viewModel<HomeViewModel>(viewModelStoreOwner = ksuApp)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val moduleViewModel = viewModel<ModuleViewModel>(viewModelStoreOwner = ksuApp)
    val moduleUiState by moduleViewModel.uiState.collectAsStateWithLifecycle()
    val moduleUpdateCount = moduleUiState.moduleList.count { it.moduleUpdate != null }

    LaunchedEffect(Unit) {
        viewModel.awaitInitialData(context)
    }
    LaunchedEffect(Unit) {
        if (moduleUiState.moduleList.isEmpty()) {
            moduleViewModel.fetchModuleList()
        }
    }

    if (!uiState.isInitialDataLoaded) return

    val scrollBehavior = MiuixScrollBehavior()
    val navigator = LocalNavigator.current
    val loadingDialog = rememberLoadingDialog()
    val scope = rememberCoroutineScope()
    val hasBackground = ThemeConfig.customBackgroundUri != null
    val surface = MiuixTheme.colorScheme.surface
    val dashboard = uiState.homeLayoutStyle == HomeLayoutStyle.DASHBOARD
    val bannerFile = remember(uiState.homeBannerFile) {
        HomeBannerStorage.fileOf(context, uiState.homeBannerFile)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                color = if (hasBackground) Color.Transparent else surface,
                title = stringResource(R.string.app_name),
                actions = {
                    if (uiState.isCoreDataLoaded) {
                        if (uiState.systemInfo.susfsVersionSupported) {
                            IconButton(onClick = { navigator.push(Route.SuSFSConfig) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = stringResource(R.string.susfs_config_setting_title),
                                    tint = MiuixTheme.colorScheme.onBackground,
                                )
                            }
                        }
                        KsuIsValid { MiuixRebootAction() }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = {
            SwipeableSnackbarHost(
                modifier = Modifier.padding(bottom = bottomPadding),
                hostState = LocalSnackbarHost.current
            )
        },
        popupHost = { MiuixPopupHost() },
        containerColor = if (hasBackground) Color.Transparent else surface,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshData(context, refreshUI = true) },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + bottomPadding + 12.dp,
                ),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.isCoreDataLoaded) {
                            MiuixHomeWarnings(uiState)
                            MiuixStatusCard(
                                uiState = uiState,
                                moduleUpdateCount = moduleUpdateCount,
                                big = dashboard,
                                bannerFile = if (dashboard) bannerFile else null,
                                onClickInstall = {
                                    navigator.push(Route.Install(preselectedKernelUri = null))
                                },
                                onClickJailbreak = {
                                    loadingDialog.showLoading()
                                    context.startService(Intent(context, MagicaService::class.java))
                                    scope.launch(Dispatchers.IO) {
                                        delay(30_000)
                                        withContext(Dispatchers.Main) {
                                            loadingDialog.hide()
                                            Toast.makeText(
                                                context,
                                                R.string.jailbreak_timeout,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                            )
                            if (dashboard && uiState.systemStatus.ksuVersion != null) {
                                MiuixCountTiles(uiState)
                            }
                        }

                        MiuixManagerUpdateCard(uiState.stableManagerUpdate)
                        MiuixManagerUpdateCard(uiState.betaManagerUpdate)
                        if (uiState.isBetaManagerUpdateCheckFailed) {
                            MiuixWarningCard(stringResource(R.string.beta_update_check_failed))
                        }
                    }
                }

                if (uiState.isExtendedDataLoaded) {
                    item { MiuixInfoSections(uiState, combined = dashboard) }
                }

                if (!uiState.isSimpleMode && !uiState.isHideLinkCard) {
                    item { MiuixLearnMoreCard(uiState.showHomeCardIcons || dashboard) }
                }
            }
        }
    }
}

@Composable
private fun MiuixHomeIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.padding(end = 6.dp),
        tint = MiuixTheme.colorScheme.onBackground,
    )
}

@Composable
private fun MiuixRebootAction() {
    var expanded by remember { mutableStateOf(false) }
    val pm = LocalContext.current.getSystemService(Context.POWER_SERVICE) as PowerManager?
    var methods = mapOf(
        R.string.reboot to "",
        R.string.reboot_soft to "soft_reboot",
        R.string.reboot_recovery to "recovery",
        R.string.reboot_bootloader to "bootloader",
        R.string.reboot_download to "download",
        R.string.reboot_edl to "edl"
    )

    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pm?.isRebootingUserspaceSupported == true) {
        methods = methods + (R.string.reboot_userspace to "userspace")
    }

    val options = methods.entries.toList()
    IconButton(
        onClick = { expanded = true },
        holdDownState = expanded,
    ) {
        Icon(
            imageVector = Icons.Rounded.PowerSettingsNew,
            contentDescription = stringResource(id = R.string.reboot),
            tint = MiuixTheme.colorScheme.onBackground,
        )
    }
    OverlayListPopup(
        show = expanded,
        popupPositionProvider = ListPopupDefaults.MenuPositionProvider,
        alignment = PopupPositionProvider.Align.TopEnd,
        onDismissRequest = { expanded = false },
        content = {
            ListPopupColumn {
                options.forEachIndexed { idx, (id, reason) ->
                    DropdownItem(
                        text = stringResource(id),
                        optionSize = options.size,
                        index = idx,
                        onSelectedIndexChange = {
                            expanded = false
                            reboot(reason)
                        },
                    )
                }
            }
        },
    )
}


@Composable
private fun MiuixHomeWarnings(uiState: HomeUiState) {
    val status = uiState.systemStatus
    if (status.requireNewKernel) {
        val message = if ((status.ksuVersion ?: 0) > BuildConfig.VERSION_CODE) {
            stringResource(R.string.require_manager_version, BuildConfig.VERSION_CODE, status.ksuVersion ?: 0)
        } else {
            stringResource(R.string.require_kernel_version, status.ksuVersion ?: 0, BuildConfig.VERSION_CODE)
        }
        MiuixWarningCard(message)
    }
    if (BuildConfig.DEBUG) {
        MiuixWarningCard(stringResource(R.string.debug_version_notice))
    }
    if (!status.isOfficialSignature) {
        MiuixWarningCard(
            stringResource(R.string.unofficial_version_notice, stringResource(R.string.app_name))
        )
    }
    if (BuildConfig.IS_PR_BUILD || Natives.isPrBuild) {
        MiuixWarningCard(stringResource(R.string.home_pr_build_warning))
    }
    if (status.kernelPatchImplement == Natives.KernelPatchImplement.KERNEL_PATCH_OFFICIAL) {
        MiuixWarningCard(stringResource(R.string.conflict_with_apatch))
    }
    if (status.ksuVersion != null && !status.isRootAvailable) {
        MiuixWarningCard(stringResource(R.string.grant_root_failed))
    }
}

@Composable
private fun MiuixTag(label: String, background: Color, content: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = content)
    }
}

@Composable
private fun MiuixStatusCard(
    uiState: HomeUiState,
    moduleUpdateCount: Int,
    big: Boolean,
    bannerFile: java.io.File?,
    onClickInstall: () -> Unit,
    onClickJailbreak: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val systemStatus = uiState.systemStatus
    val onClick = {
        if (systemStatus.isRootAvailable || systemStatus.kernelVersion.isGKI()) {
            onClickInstall()
        }
    }
    val isDark = ThemeConfig.forceDarkMode ?: isSystemInDarkTheme()

    when {
        systemStatus.ksuVersion != null -> {
            val accent = if (ThemeConfig.miuixMonet) colors.primary else Color(0xFF36D167)
            val tagBackground = when {
                ThemeConfig.miuixMonet -> colors.tertiaryContainer
                isDark -> Color(0xFF315D3E)
                else -> Color(0xFFB8E8C5)
            }
            val tagContent = when {
                ThemeConfig.miuixMonet -> colors.onTertiaryContainer
                isDark -> Color(0xFFB8E8C5)
                else -> Color(0xFF164A29)
            }
            val cardColor = when {
                ThemeConfig.miuixMonet -> colors.secondaryContainer
                isDark -> Color(0xFF1A3825)
                else -> Color(0xFFDFFAE4)
            }
            val title = if (Natives.isSafeMode) {
                stringResource(id = R.string.safe_mode)
            } else {
                stringResource(id = R.string.home_working)
            }
            val surfaceText = if (systemStatus.lkmMode == true) "LKM" else "Built-in"
            val summary = if (moduleUpdateCount > 0) {
                pluralStringResource(
                    R.plurals.home_short_info_with_update,
                    moduleUpdateCount,
                    uiState.systemInfo.superuserCount,
                    uiState.systemInfo.moduleCount,
                    moduleUpdateCount
                )
            } else {
                stringResource(
                    R.string.home_short_info,
                    uiState.systemInfo.superuserCount,
                    uiState.systemInfo.moduleCount
                )
            }
            val hasBanner = bannerFile != null
            val versionLabel = stringResource(R.string.version)
            val versionText = buildString {
                append(systemStatus.ksuVersion ?: 0)
                systemStatus.kernelUAPIVersion?.let { append("-").append(it) }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(
                    color = if (hasBanner) Color.Transparent else cardColor,
                ),
                onClick = onClick,
                pressFeedbackType = PressFeedbackType.Tilt,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (bannerFile != null) {
                        AsyncImage(
                            model = bannerFile,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                        )
                    }
                    if (big && !hasBanner) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(27.dp, 31.dp)
                                .size(150.dp),
                            imageVector = Icons.Rounded.CheckCircleOutline,
                            tint = accent.copy(alpha = if (ThemeConfig.miuixMonet) 0.8f else 1f),
                            contentDescription = null,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (big) Modifier.heightIn(min = 188.dp) else Modifier)
                            .padding(if (big) 22.dp else 18.dp),
                        verticalArrangement = if (big) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp),
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            MiuixTag(
                                label = surfaceText,
                                background = accent,
                                content = if (ThemeConfig.miuixMonet) colors.onPrimary else Color.White,
                            )
                            if (Natives.isLateLoadMode) {
                                MiuixTag(
                                    label = stringResource(id = R.string.jailbreak_mode),
                                    background = colors.errorContainer,
                                    content = colors.onErrorContainer,
                                )
                            }
                            if (Os.uname().machine != "aarch64") {
                                MiuixTag(Os.uname().machine, tagBackground, tagContent)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = title,
                                fontSize = if (big) 36.sp else 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasBanner) Color.White else colors.onSurface,
                            )
                            Text(
                                text = if (big) "$versionLabel: $versionText" else summary,
                                // keep the long version string clear of the big check icon
                                modifier = if (big && !hasBanner) Modifier.fillMaxWidth(0.66f) else Modifier,
                                fontSize = 14.sp,
                                fontFamily = if (big) FontFamily.Monospace else FontFamily.Default,
                                color = if (hasBanner) Color.White.copy(alpha = 0.85f)
                                else colors.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        }

        else -> {
            val notInstalled = systemStatus.kernelVersion.isGKI()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = miuixCardColors(),
                onClick = if (notInstalled) onClick else null,
                pressFeedbackType = PressFeedbackType.Tilt,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (big) Modifier.heightIn(min = 188.dp) else Modifier)
                        .padding(22.dp),
                    verticalArrangement = if (big) Arrangement.SpaceBetween else Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(
                                if (notInstalled) R.string.home_click_to_install
                                else R.string.home_unsupported_reason
                            ),
                            fontSize = 14.sp,
                            color = colors.error,
                        )
                    }
                    Text(
                        text = stringResource(
                            if (notInstalled) R.string.home_not_installed else R.string.home_unsupported
                        ),
                        fontSize = if (big) 36.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    if (notInstalled && systemStatus.isSELinuxPermissive) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                text = stringResource(R.string.home_jailbreak),
                                onClick = onClickJailbreak,
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixCountTiles(uiState: HomeUiState) {
    val colors = MiuixTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(
            uiState.systemInfo.superuserCount to R.string.superuser,
            uiState.systemInfo.moduleCount to R.string.module,
        ).forEach { (count, label) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = miuixCardColors(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    Text(
                        text = stringResource(label),
                        fontSize = 13.sp,
                        color = colors.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixManagerUpdateCard(update: ManagerUpdateInfo?) {
    val updateInfo = update ?: return
    val context = LocalContext.current
    val permissionRequestInterface = LocalPermissionRequestInterface.current
    val channelTitle = stringResource(
        if (updateInfo.channel == ManagerUpdateChannel.STABLE) {
            R.string.manager_update_stable
        } else {
            R.string.manager_update_beta
        }
    )
    val message = if (updateInfo.channel == ManagerUpdateChannel.STABLE) {
        stringResource(R.string.new_version_available, updateInfo.versionCode)
    } else {
        stringResource(R.string.beta_version_available, updateInfo.versionCode)
    }
    val updateText = stringResource(R.string.module_update)
    val details = stringResource(
        R.string.manager_update_details,
        updateInfo.versionName,
        updateInfo.versionCode,
        updateInfo.abi,
    )
    val dialogContent = if (updateInfo.changelog.isBlank()) {
        details
    } else {
        "$details\n\n${updateInfo.changelog}"
    }
    val updateDialog = rememberConfirmDialog(
        onConfirm = {
            downloadManagerUpdate(context, permissionRequestInterface, updateInfo)
        }
    )

    MiuixWarningCard(
        message = message,
        neutral = true,
        onClick = {
            updateDialog.showConfirm(
                title = channelTitle,
                content = dialogContent,
                markdown = updateInfo.changelog.isNotBlank(),
                confirm = updateText,
            )
        },
    )
}

@Composable
private fun MiuixInfoGroup(title: String?, rows: List<MiuixInfoRow>, showIcons: Boolean) {
    if (rows.isEmpty()) return
    if (title != null) SmallTitle(text = title)
    MiuixGroupCard {
        rows.forEach { row ->
            BasicComponent(
                title = row.title,
                summary = row.summary,
                startAction = if (showIcons) {
                    { MiuixHomeIcon(row.icon) }
                } else null,
            )
        }
    }
}

@Composable
private fun MiuixInfoSections(uiState: HomeUiState, combined: Boolean) {
    val systemStatus = uiState.systemStatus
    val systemInfo = uiState.systemInfo
    val isSimpleMode = uiState.isSimpleMode
    val managersList = systemInfo.managersList

    val versionRows = buildList {
        add(MiuixInfoRow(stringResource(R.string.home_device_model), systemInfo.deviceModel, Icons.Rounded.Smartphone))
        add(MiuixInfoRow(stringResource(R.string.home_kernel), systemInfo.kernelRelease, Icons.Rounded.DeveloperBoard))
        if (!isSimpleMode) {
            add(MiuixInfoRow(stringResource(R.string.home_android_version), systemInfo.androidVersion, Icons.Rounded.Android))
        }
        if (ksuIsValid()) {
            add(MiuixInfoRow(stringResource(R.string.home_kernel_version), systemStatus.ksuFullVersion.orEmpty(), Icons.Rounded.Memory))
        }
        add(
            MiuixInfoRow(
                stringResource(R.string.home_manager_version),
                "${systemInfo.managerVersion.first} (${systemInfo.managerVersion.second}/${systemInfo.managerVersion.third})",
                Icons.Rounded.Tag
            )
        )
        if (!isSimpleMode && !uiState.isHideSusfsStatus && systemInfo.susfsEnabled && systemInfo.susfsVersion.isNotEmpty()) {
            add(MiuixInfoRow(stringResource(R.string.home_susfs_version), systemInfo.susfsVersion, Icons.Rounded.Info))
        }
    }

    val seccompDisplay = when (systemInfo.seccompStatus) {
        -1 -> stringResource(R.string.seccomp_status_not_supported)
        0 -> stringResource(R.string.seccomp_status_disabled)
        1 -> stringResource(R.string.seccomp_status_strict)
        2 -> stringResource(R.string.seccomp_status_filter)
        else -> stringResource(R.string.seccomp_status_unknown)
    }
    val appName = stringResource(R.string.app_name)
    val dynamicSignature = stringResource(R.string.dynamic_managerature)
    val unknownSignature = stringResource(R.string.unknown_signature)
    val noActiveManager = stringResource(R.string.no_active_manager)
    val managersText = remember(managersList, appName, dynamicSignature, unknownSignature) {
        buildString {
            managersList?.managers.orEmpty().groupBy { it.signatureIndex }.toSortedMap()
                .forEach { (signatureIndex, managers) ->
                    append(managers.joinToString(", ") { "UID: ${it.uid}" })
                    append(" ")
                    append(
                        when (signatureIndex) {
                            0 -> "($appName)"
                            255 -> "($dynamicSignature)"
                            else -> if (signatureIndex >= 1) "($signatureIndex)" else "($unknownSignature)"
                        }
                    )
                    append(" | ")
                }
        }.trimEnd(' ', '|')
    }

    val statusRows = buildList {
        add(MiuixInfoRow(stringResource(R.string.home_selinux_status), systemInfo.selinuxStatus, Icons.Rounded.Security))
        add(MiuixInfoRow(stringResource(R.string.home_seccomp_status), seccompDisplay, Icons.Rounded.FilterList))
        if (!isSimpleMode && managersList != null) {
            add(MiuixInfoRow(stringResource(R.string.multi_manager_list), managersText.ifEmpty { noActiveManager }, Icons.Rounded.Group))
        }
        if (!isSimpleMode && ksuIsValid()) {
            add(MiuixInfoRow(stringResource(R.string.home_hook_type), Natives.getHookType(), Icons.Rounded.Tune))
        }
        if (!uiState.isHideZygiskImplement && !isSimpleMode && systemInfo.zygiskImplement.isNotEmpty() && systemInfo.zygiskImplement != "None") {
            add(MiuixInfoRow(stringResource(R.string.home_zygisk_implement), systemInfo.zygiskImplement, Icons.Rounded.Extension))
        }
        if (!uiState.isHideMetaModuleImplement && !isSimpleMode && systemInfo.metaModuleImplement.isNotEmpty() && systemInfo.metaModuleImplement != "None") {
            add(MiuixInfoRow(stringResource(R.string.home_meta_module_implement), systemInfo.metaModuleImplement, Icons.Rounded.Extension))
        }
    }

    // Group by what the row describes; each group is its own titled card. The icon identifies
    // the row, so the two lists above stay as they are.
    val allRows = versionRows + statusRows
    val deviceIcons = setOf(Icons.Rounded.Smartphone, Icons.Rounded.DeveloperBoard, Icons.Rounded.Android)
    val securityIcons = setOf(Icons.Rounded.Security, Icons.Rounded.FilterList, Icons.Rounded.Group)
    val deviceRows = allRows.filter { it.icon in deviceIcons }
    val securityRows = allRows.filter { it.icon in securityIcons }
    val softwareRows = allRows.filter { it.icon !in deviceIcons && it.icon !in securityIcons }
    // Dashboard style always shows the leading icons
    val showIcons = combined || uiState.showHomeCardIcons
    // Untitled cards, spaced like the groups on the Settings page
    MiuixInfoGroup(null, deviceRows, showIcons)
    MiuixInfoGroup(null, softwareRows, showIcons)
    MiuixInfoGroup(null, securityRows, showIcons)
}

@Composable
private fun MiuixLearnMoreCard(showIcons: Boolean) {
    val uriHandler = LocalUriHandler.current
    val url = stringResource(R.string.home_learn_kernelsu_url)
    MiuixGroupCard {
        ArrowPreference(
            title = stringResource(R.string.home_learn_kernelsu),
            summary = stringResource(R.string.home_click_to_learn_kernelsu),
            onClick = { uriHandler.openUri(url) },
        )
    }
}
