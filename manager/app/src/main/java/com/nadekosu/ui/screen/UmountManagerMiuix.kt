package com.nadekosu.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nadekosu.R
import com.nadekosu.ui.component.ConfirmResult
import com.nadekosu.ui.component.miuix.MiuixDialogButtons
import com.nadekosu.ui.component.miuix.MiuixGroupCard
import com.nadekosu.ui.component.miuix.MiuixIconFab
import com.nadekosu.ui.component.miuix.MiuixListPage
import com.nadekosu.ui.component.miuix.MiuixLoadingBox
import com.nadekosu.ui.component.miuix.MiuixStatusTag
import com.nadekosu.ui.component.miuix.MiuixWarningCard
import com.nadekosu.ui.component.rememberConfirmDialog
import com.nadekosu.ui.util.LocalSnackbarHost
import com.nadekosu.ui.viewmodel.UmountManagerScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixUmountManagerScreen() {
    val viewModel = viewModel<UmountManagerScreenViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHost.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val confirmDialog = rememberConfirmDialog()
    var showAddDialog by remember { mutableStateOf(false) }
    val confirmDelete = stringResource(R.string.confirm_delete)

    LaunchedEffect(Unit) {
        viewModel.refreshData(context)
    }

    MiuixListPage(
        title = stringResource(R.string.umount_path_manager),
        isRefreshing = uiState.isRefreshing,
        onRefresh = {
            viewModel.markUmountPathDirty()
            viewModel.refreshData(context)
        },
        floatingActionButton = {
            MiuixIconFab(
                icon = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.add_umount_path),
                onClick = { showAddDialog = true },
            )
        },
        overlay = {
            MiuixAddUmountPathDialog(
                show = showAddDialog,
                onDismiss = { showAddDialog = false },
                onConfirm = { path, flags ->
                    showAddDialog = false
                    uiState.umountPaths.filter { it.path == path }.forEach {
                        viewModel.removePath(entry = it, snackBarHost = null, context = null)
                    }
                    viewModel.addPath(
                        path = path,
                        flags = flags,
                        snackBarHost = snackBarHost,
                        context = context,
                    )
                },
            )
        },
    ) {
        if (uiState.isLoading) {
            item { MiuixLoadingBox(Modifier.heightIn(min = 320.dp)) }
            return@MiuixListPage
        }
        item {
            Column(Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                MiuixWarningCard(
                    message = stringResource(R.string.changes_take_effect_immediately),
                    neutral = true,
                )
            }
        }
        if (uiState.umountPaths.isEmpty()) {
            item {
                Column(Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    MiuixWarningCard(message = stringResource(R.string.no_any_umount_path), neutral = true)
                }
            }
        } else {
            item {
                MiuixGroupCard {
                    uiState.umountPaths.forEach { entry ->
                        val confirmDeleteSummary = stringResource(R.string.confirm_delete_umount_path, entry.path)
                        BasicComponent(
                            title = entry.path,
                            bottomAction = {
                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    MiuixStatusTag(
                                        label = stringResource(
                                            if (entry.persistent) R.string.persistent else R.string.temporary
                                        ),
                                        backgroundColor = MiuixTheme.colorScheme.tertiaryContainer,
                                        contentColor = MiuixTheme.colorScheme.onTertiaryContainer,
                                    )
                                    MiuixStatusTag(label = entry.flagName)
                                }
                            },
                            endActions = {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            val result = confirmDialog.awaitConfirm(
                                                title = confirmDelete,
                                                content = confirmDeleteSummary,
                                            )
                                            if (result != ConfirmResult.Confirmed) return@launch
                                            withContext(Dispatchers.IO) {
                                                viewModel.removePath(entry, snackBarHost, context)
                                            }
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = confirmDelete,
                                        tint = MiuixTheme.colorScheme.error,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun MiuixAddUmountPathDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
) {
    var path by remember(show) { mutableStateOf("") }
    var flags by remember(show) { mutableStateOf("0") }
    OverlayDialog(
        show = show,
        title = stringResource(R.string.add_umount_path),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = path,
                onValueChange = { path = it },
                label = stringResource(R.string.mount_path),
                singleLine = true,
            )
            TextField(
                value = flags,
                onValueChange = { flags = it },
                label = stringResource(R.string.umount_flags),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Text(
                text = stringResource(R.string.umount_flags_hint),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            MiuixDialogButtons(
                onDismiss = onDismiss,
                onConfirm = { onConfirm(path, flags.toIntOrNull() ?: 0) },
                confirmEnabled = path.isNotBlank(),
            )
        }
    }
}
