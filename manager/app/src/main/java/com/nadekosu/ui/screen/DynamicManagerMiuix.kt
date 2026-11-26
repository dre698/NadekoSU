package com.nadekosu.ui.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.nadekosu.R
import com.nadekosu.ui.component.miuix.MiuixGroupCard
import com.nadekosu.ui.component.miuix.MiuixListPage
import com.nadekosu.ui.component.miuix.MiuixLoadingBox
import com.nadekosu.ui.component.miuix.MiuixSearchField
import com.nadekosu.ui.viewmodel.DynamicManagerAppItem
import com.nadekosu.ui.viewmodel.DynamicManagerViewModel
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import androidx.compose.foundation.layout.fillMaxWidth
import com.nadekosu.ui.component.miuix.miuixCardColors

@Composable
internal fun MiuixDynamicManagerContent(
    viewModel: DynamicManagerViewModel,
    onManualConfig: () -> Unit,
    onClearConfig: () -> Unit,
    onAppClick: (DynamicManagerAppItem) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val config = uiState.config
    val enabled = !uiState.isSubmitting

    MiuixListPage(
        title = stringResource(R.string.dynamic_manager_title),
        isRefreshing = uiState.isRefreshing,
        onRefresh = { scope.launch { viewModel.refresh() } },
    ) {
        item {
            MiuixSearchField(
                query = uiState.search,
                onQueryChange = viewModel::updateSearch,
                label = stringResource(R.string.search_apps),
            )
        }
        if (uiState.isLoading) {
            item { MiuixLoadingBox(Modifier.heightIn(min = 320.dp)) }
            return@MiuixListPage
        }
        item {
            MiuixGroupCard {
                BasicComponent(
                    title = stringResource(R.string.dynamic_manager_current_status),
                    summary = if (config?.isValid() == true) {
                        stringResource(R.string.dynamic_manager_enabled_summary, config.size.toString())
                    } else {
                        stringResource(R.string.dynamic_manager_disabled)
                    },
                )
                if (config?.isValid() == true) {
                    BasicComponent(
                        title = stringResource(R.string.signature_hash),
                        summary = config.hash,
                    )
                }
                ArrowPreference(
                    title = stringResource(R.string.dynamic_manager_manual_config),
                    summary = stringResource(R.string.dynamic_manager_manual_config_summary),
                    enabled = enabled,
                    onClick = onManualConfig,
                )
                ArrowPreference(
                    title = stringResource(R.string.dynamic_manager_clear_config),
                    summary = stringResource(R.string.dynamic_manager_clear_config_summary),
                    enabled = enabled,
                    onClick = onClearConfig,
                )
            }
        }
        item { SmallTitle(text = stringResource(R.string.manage_managers)) }
        if (uiState.apps.isEmpty()) {
            item { MiuixLoadingBox(Modifier.heightIn(min = 160.dp)) }
        } else {
            items(uiState.apps, key = { "${it.uid}-${it.packageName}" }) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp),
                    colors = miuixCardColors(),
                ) {
                    MiuixDynamicManagerAppRow(app = app, onClick = { onAppClick(app) })
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun MiuixDynamicManagerAppRow(
    app: DynamicManagerAppItem,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val checked = app.isSelected || !app.isChangeable
    BasicComponent(
        title = app.label,
        summary = if (!app.isChangeable) {
            stringResource(
                R.string.dynamic_manager_fixed_manager_summary,
                app.packageName,
                when (app.managerSignatureIndex ?: 0) {
                    254 -> "Debug"
                    253 -> "KernelSU Toolkit"
                    else -> "Kernel"
                },
            )
        } else {
            app.packageName
        },
        enabled = app.isChangeable,
        onClick = onClick,
        startAction = {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(app.packageInfo)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = app.label,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(44.dp),
            )
        },
        endActions = {
            Checkbox(
                state = ToggleableState(checked),
                onClick = null,
                enabled = app.isChangeable,
            )
        },
    )
}
