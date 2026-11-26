package com.nadekosu.ui.screen.moduleRepo

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Code
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.KeyboardArrowDown
import androidx.compose.material.icons.twotone.Link
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material.icons.twotone.WebAsset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.TopDownloads
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.nadekosu.R
import com.nadekosu.data.AppPreferencesRepository
import com.nadekosu.data.modulestore.DEFAULT_REPOSITORY_URL
import com.nadekosu.data.modulestore.StoreSourceKind
import com.nadekosu.ui.component.GithubMarkdown
import com.nadekosu.ui.component.miuix.MiuixDialogButtons
import com.nadekosu.ui.component.miuix.MiuixLoadingBox
import com.nadekosu.ui.component.miuix.MiuixStatusTag
import com.nadekosu.ui.component.miuix.miuixCardColors
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.navigation.Route
import com.nadekosu.ui.util.LocalPermissionRequestInterface
import com.nadekosu.ui.util.module.ReleaseAssetInfo
import com.nadekosu.ui.util.module.ReleaseInfo
import com.nadekosu.ui.viewmodel.ModuleRepoUiState
import com.nadekosu.ui.viewmodel.ModuleRepoViewModel
import com.nadekosu.ui.viewmodel.ModuleRepoViewModel.RepoModule
import com.nadekosu.ui.viewmodel.formatFileSize
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * Install flow for Miuix module repo pages: confirm with release notes, then pick an asset when
 * the release ships more than one, then download and flash.
 */
@Stable
class MiuixRepoInstallState {
    var module by mutableStateOf<RepoModule?>(null)
        private set
    var release by mutableStateOf<ReleaseInfo?>(null)
        private set
    var asset by mutableStateOf<ReleaseAssetInfo?>(null)
        private set
    var showConfirm by mutableStateOf(false)
    var showChoose by mutableStateOf(false)

    /** Starts the flow; [asset] skips the chooser when the user already picked one. */
    fun request(module: RepoModule, release: ReleaseInfo, asset: ReleaseAssetInfo? = null) {
        this.module = module
        this.release = release
        this.asset = asset
        showChoose = false
        showConfirm = true
    }
}

@Composable
fun rememberMiuixRepoInstallState(): MiuixRepoInstallState = remember { MiuixRepoInstallState() }

/** Dialogs driven by [MiuixRepoInstallState]; place inside the page's overlay slot. */
@Composable
fun MiuixRepoInstallDialogs(state: MiuixRepoInstallState, viewModel: ModuleRepoViewModel) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val permissionRequestInterface = LocalPermissionRequestInterface.current
    val module = state.module
    val release = state.release
    val colors = MiuixTheme.colorScheme

    fun install(asset: ReleaseAssetInfo) {
        val target = module ?: return
        downloadAssetAndInstall(
            context,
            permissionRequestInterface,
            target,
            asset,
            navigator,
            viewModel.viewModelScope,
        )
    }

    OverlayDialog(
        show = state.showConfirm && module != null && release != null,
        title = module?.let { stringResource(R.string.confirm_install_module_title, it.moduleName) },
        onDismissRequest = { state.showConfirm = false },
    ) {
        Column {
            if (release != null && release.descriptionHTML.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    GithubMarkdown(
                        content = release.descriptionHTML,
                        backgroundColor = Color.Transparent,
                        callerProvideLoadingIndicator = true,
                        linkColor = colors.primary,
                    )
                }
            }
            MiuixDialogButtons(
                onDismiss = { state.showConfirm = false },
                onConfirm = {
                    state.showConfirm = false
                    val assets = release?.assets.orEmpty()
                    val picked = state.asset
                    when {
                        picked != null -> install(picked)
                        assets.size <= 1 -> assets.firstOrNull()?.let(::install)
                        else -> state.showChoose = true
                    }
                },
            )
        }
    }

    var selected by remember(release) { mutableStateOf<ReleaseAssetInfo?>(null) }
    OverlayDialog(
        show = state.showChoose && release != null,
        title = stringResource(R.string.assets_multiple_select_dialog_title),
        onDismissRequest = { state.showChoose = false },
    ) {
        Column {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                release?.assets.orEmpty().forEach { asset ->
                    RadioButtonPreference(
                        title = asset.name,
                        summary = stringResource(
                            R.string.assets_multiple_select_dialog_content_description,
                            formatFileSize(asset.size),
                            asset.downloadCount,
                        ),
                        selected = selected == asset,
                        onClick = { selected = asset },
                        insideMargin = PaddingValues(vertical = 10.dp),
                    )
                }
            }
            MiuixDialogButtons(
                onDismiss = { state.showChoose = false },
                onConfirm = {
                    val asset = selected
                    if (asset == null) {
                        Toast.makeText(
                            context,
                            R.string.assets_multiple_select_dialog_warning,
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        state.showChoose = false
                        install(asset)
                    }
                },
            )
        }
    }
}

@Composable
private fun MiuixSmallIconButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        backgroundColor = MiuixTheme.colorScheme.secondaryContainer,
        minHeight = 34.dp,
        minWidth = 48.dp,
        cornerRadius = 12.dp,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Compact star count for list rows: "999", "1.2k", "12.3k", "123k", "1.2M". */
private fun formatStarCount(count: Int): String {
    fun compact(value: Int, unit: Int, suffix: String): String {
        if (value >= unit * 100) return "${value / unit}$suffix"
        val tenths = value / (unit / 10)
        return if (tenths % 10 == 0) "${tenths / 10}$suffix" else "${tenths / 10}.${tenths % 10}$suffix"
    }
    return when {
        count < 1_000 -> count.toString()
        count < 1_000_000 -> compact(count, 1_000, "k")
        else -> compact(count, 1_000_000, "M")
    }
}

@Composable
private fun MiuixRepoTag(text: String, background: Color, tint: Color) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = tint,
        modifier = Modifier
            .padding(start = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        fontWeight = FontWeight(750),
        maxLines = 1,
    )
}

/** Miuix card for one module in the repo list; tapping it opens the detail page. */
@Composable
fun MiuixOnlineModuleItem(
    module: RepoModule,
    viewModel: ModuleRepoViewModel,
    installState: MiuixRepoInstallState,
) {
    val navigator = LocalNavigator.current
    val colors = MiuixTheme.colorScheme
    val moduleAuthor = stringResource(id = R.string.module_author)

    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
        colors = miuixCardColors(),
        insideMargin = PaddingValues(16.dp),
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = { navigator.push(Route.ModuleRepoDetail(moduleId = module.moduleId)) },
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = module.moduleName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(550),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (module.metamodule) {
                    MiuixRepoTag(
                        text = "META",
                        background = colors.tertiaryContainer.copy(alpha = 0.6f),
                        tint = colors.onTertiaryContainer.copy(alpha = 0.8f),
                    )
                }
                if (module.installed) {
                    MiuixRepoTag(
                        text = stringResource(R.string.installed),
                        background = colors.secondaryContainer.copy(alpha = 0.8f),
                        tint = colors.onSecondaryContainer,
                    )
                }
            }
            if (module.moduleId.isNotEmpty()) {
                Text(
                    text = "ID: ${module.moduleId}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                    fontWeight = FontWeight(550),
                    color = colors.onSurfaceVariantSummary,
                )
            }
            if (module.authors.isNotEmpty()) {
                Text(
                    text = "$moduleAuthor: ${module.authors}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 1.dp),
                    fontWeight = FontWeight(550),
                    color = colors.onSurfaceVariantSummary,
                )
            }
            if (module.summary.isNotBlank()) {
                Text(
                    text = module.summary,
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariantSummary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = colors.outline.copy(alpha = 0.5f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = module.latestRelease,
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (module.stargazerCount > 0) {
                    Icon(
                        imageVector = MiuixIcons.TopDownloads,
                        contentDescription = "stars",
                        tint = colors.onSurfaceVariantSummary,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(16.dp),
                    )
                    Text(
                        text = formatStarCount(module.stargazerCount),
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariantSummary,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                if (module.latestReleaseTime.isNotEmpty()) {
                    Text(
                        text = module.latestReleaseTime,
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariantSummary,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

/** Options sheet for the module repo: sorting and module source. */
@Composable
fun MiuixModuleRepoOptionsSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: ModuleRepoViewModel,
    uiState: ModuleRepoUiState,
    prefs: AppPreferencesRepository,
) {
    var showRepositoryDialog by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var urlText by remember(showUrlDialog) { mutableStateOf(uiState.customUrl) }
    val kind = uiState.sourceKind
    val colors = MiuixTheme.colorScheme

    OverlayBottomSheet(
        show = show,
        title = stringResource(R.string.menu_options),
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            SmallTitle(text = stringResource(R.string.sort_options), insideMargin = PaddingValues(vertical = 8.dp))
            Card(colors = miuixCardColors(), modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = stringResource(R.string.module_sort_star_first),
                    checked = uiState.sortStargazerCountFirst,
                    onCheckedChange = { checked ->
                        viewModel.setSortStargazerCountFirst(checked)
                        prefs.putBoolean("module_repo_sort_star_first", checked)
                        onDismiss()
                    },
                )
            }
            SmallTitle(text = stringResource(R.string.module_repo_source), insideMargin = PaddingValues(top = 16.dp, bottom = 8.dp))
            Card(colors = miuixCardColors(), modifier = Modifier.fillMaxWidth()) {
                RadioButtonPreference(
                    title = stringResource(R.string.module_repo_source_kernelsu),
                    selected = kind == StoreSourceKind.KERNELSU,
                    onClick = {
                        viewModel.useKernelSu()
                        onDismiss()
                    },
                )
                RadioButtonPreference(
                    title = stringResource(R.string.module_repo_source_community),
                    summary = if (kind == StoreSourceKind.CLUSTER) {
                        uiState.repositoryUrl.ifBlank { DEFAULT_REPOSITORY_URL }
                    } else {
                        null
                    },
                    selected = kind == StoreSourceKind.CLUSTER,
                    onClick = {
                        viewModel.loadRepositories()
                        showRepositoryDialog = true
                    },
                )
                RadioButtonPreference(
                    title = stringResource(R.string.module_repo_source_custom),
                    summary = uiState.customUrl.takeIf { kind == StoreSourceKind.CUSTOM && it.isNotBlank() },
                    selected = kind == StoreSourceKind.CUSTOM,
                    onClick = { showUrlDialog = true },
                )
            }
            if (kind != StoreSourceKind.KERNELSU) {
                Text(
                    text = stringResource(R.string.module_repo_source_warning),
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 10.dp, start = 4.dp, end = 4.dp),
                )
            }
        }
    }

    val current = uiState.repositoryUrl.ifBlank { DEFAULT_REPOSITORY_URL }
    OverlayDialog(
        show = showRepositoryDialog,
        title = stringResource(R.string.module_repo_choose_repository),
        summary = if (uiState.repositories.isEmpty()) {
            stringResource(R.string.module_repo_repositories_loading)
        } else {
            null
        },
        onDismissRequest = { showRepositoryDialog = false },
    ) {
        Column {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                uiState.repositories.forEach { repo ->
                    RadioButtonPreference(
                        title = repo.name.ifBlank { repo.url },
                        summary = if (repo.modulesCount > 0) "${repo.modulesCount} · ${repo.url}" else repo.url,
                        selected = kind == StoreSourceKind.CLUSTER && repo.url == current,
                        onClick = {
                            showRepositoryDialog = false
                            viewModel.useCluster(repo.url)
                            onDismiss()
                        },
                        insideMargin = PaddingValues(vertical = 10.dp),
                    )
                }
            }
            top.yukonga.miuix.kmp.basic.TextButton(
                text = stringResource(R.string.cancel),
                onClick = { showRepositoryDialog = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
        }
    }

    OverlayDialog(
        show = showUrlDialog,
        title = stringResource(R.string.module_repo_source_custom),
        summary = stringResource(R.string.module_repo_custom_url_summary),
        onDismissRequest = { showUrlDialog = false },
    ) {
        Column {
            TextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = stringResource(R.string.module_repo_custom_url_hint),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            val value = urlText.trim()
            MiuixDialogButtons(
                onDismiss = { showUrlDialog = false },
                confirmEnabled = value.startsWith("http://", true) || value.startsWith("https://", true),
                onConfirm = {
                    showUrlDialog = false
                    viewModel.useCustom(value)
                    onDismiss()
                },
            )
        }
    }
}

/* ---------- Online module detail tabs ---------- */

@Composable
fun MiuixReadmeTab(
    module: RepoModule,
    nestedScrollConnection: NestedScrollConnection,
    innerPadding: PaddingValues,
) {
    val loading = remember { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    colors = miuixCardColors(),
                    insideMargin = PaddingValues(4.dp),
                ) {
                    GithubMarkdown(
                        content = module.readme,
                        backgroundColor = Color.Transparent,
                        loading = loading,
                        callerProvideLoadingIndicator = true,
                        linkColor = MiuixTheme.colorScheme.primary,
                    )
                    if (loading.value) {
                        Spacer(Modifier.height(240.dp))
                    }
                }
            }
        }
        if (loading.value) {
            MiuixLoadingBox()
        }
    }
}

@Composable
fun MiuixReleasesTab(
    module: RepoModule,
    nestedScrollConnection: NestedScrollConnection,
    innerPadding: PaddingValues,
    installState: MiuixRepoInstallState,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
    ) {
        items(items = module.releases, key = { it.tagName }) { release ->
            MiuixReleaseCard(module, release, installState)
        }
    }
}

@Composable
private fun MiuixReleaseCard(
    module: RepoModule,
    release: ReleaseInfo,
    installState: MiuixRepoInstallState,
) {
    val colors = MiuixTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
        colors = miuixCardColors(),
    ) {
        BasicComponent(
            title = release.name,
            summary = release.publishedAt,
            endActions = {
                Icon(
                    imageVector = Icons.TwoTone.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.show_detail_or_hide_detail),
                    tint = colors.onSurfaceVariantSummary,
                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                )
            },
            onClick = { expanded = !expanded },
        )
        if (expanded) {
            Box(Modifier.padding(horizontal = 12.dp)) {
                GithubMarkdown(
                    content = release.descriptionHTML,
                    backgroundColor = Color.Transparent,
                    callerProvideLoadingIndicator = true,
                    linkColor = colors.primary,
                )
            }
        }
        if (release.assets.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            release.assets.forEach { asset ->
                BasicComponent(
                    title = asset.name,
                    summary = stringResource(R.string.assert_support_content).format(
                        formatFileSize(asset.size),
                        asset.downloadCount,
                    ),
                    endActions = {
                        Icon(
                            imageVector = Icons.TwoTone.Download,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    onClick = { installState.request(module, release, asset) },
                )
            }
        }
    }
}

@Composable
fun MiuixInfoTab(
    module: RepoModule,
    nestedScrollConnection: NestedScrollConnection,
    innerPadding: PaddingValues,
) {
    val uriHandler = LocalUriHandler.current
    val colors = MiuixTheme.colorScheme
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
    ) {
        item {
            SmallTitle(text = stringResource(R.string.author))
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
                    .fillMaxWidth(),
                colors = miuixCardColors(),
            ) {
                module.authorList.forEach { author ->
                    ArrowPreference(
                        title = author.name,
                        startAction = {
                            Icon(
                                imageVector = Icons.TwoTone.Person,
                                contentDescription = null,
                                tint = colors.onSurfaceVariantSummary,
                                modifier = Modifier
                                    .padding(end = 14.dp)
                                    .size(22.dp),
                            )
                        },
                        endActions = {
                            Icon(
                                imageVector = Icons.TwoTone.Link,
                                contentDescription = stringResource(R.string.author_link),
                                tint = colors.onSurfaceVariantSummary,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        onClick = { uriHandler.openUri(author.link) },
                    )
                }
            }
        }
        if (module.sourceUrl.isNotEmpty() && module.sourceUrl != "null") {
            item {
                SmallTitle(text = stringResource(R.string.source_code))
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(),
                    colors = miuixCardColors(),
                ) {
                    ArrowPreference(
                        title = module.sourceUrl,
                        startAction = {
                            Icon(
                                imageVector = Icons.TwoTone.Code,
                                contentDescription = null,
                                tint = colors.onSurfaceVariantSummary,
                                modifier = Modifier
                                    .padding(end = 14.dp)
                                    .size(22.dp),
                            )
                        },
                        onClick = { uriHandler.openUri(module.sourceUrl) },
                    )
                }
            }
        }
    }
}
