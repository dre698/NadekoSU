package com.nadekosu.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadekosu.R
import com.nadekosu.toOrdinalList
import com.nadekosu.toRootProfileFlags
import com.nadekosu.ui.component.miuix.MiuixGroupCard
import com.nadekosu.ui.component.miuix.MiuixRootProfileConfig
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.nadekosu.ui.component.miuix.MiuixBarAction
import com.nadekosu.ui.component.miuix.MiuixEmptyState
import com.nadekosu.ui.component.miuix.MiuixIconFab
import com.nadekosu.ui.component.miuix.MiuixListPage
import com.nadekosu.ui.component.miuix.MiuixStatusTag
import com.nadekosu.ui.component.miuix.miuixCardColors
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.navigation.Route
import com.nadekosu.ui.viewmodel.TemplateViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixTemplateList(
    templates: List<TemplateViewModel.TemplateInfo>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSync: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onCreate: () -> Unit,
) {
    val importLabel = stringResource(R.string.app_profile_import_from_clipboard)
    val exportLabel = stringResource(R.string.app_profile_export_to_clipboard)
    MiuixListPage(
        title = stringResource(R.string.settings_profile_template),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        actions = {
            MiuixBarAction(
                icon = Icons.Rounded.Sync,
                contentDescription = stringResource(R.string.app_profile_template_sync),
                onClick = onSync,
            )
            OverlayIconDropdownMenu(
                entry = DropdownEntry(
                    items = listOf(
                        DropdownItem(text = importLabel, onClick = onImport),
                        DropdownItem(text = exportLabel, onClick = onExport),
                    )
                ),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ImportExport,
                    contentDescription = stringResource(R.string.app_profile_import_export),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        },
        floatingActionButton = {
            MiuixIconFab(
                icon = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.app_profile_template_create),
                onClick = onCreate,
            )
        },
    ) {
        if (templates.isEmpty() && !isRefreshing) {
            item {
                MiuixEmptyState(
                    icon = Icons.Rounded.Inventory2,
                    text = stringResource(R.string.settings_profile_template),
                )
            }
        }
        items(templates, key = { it.id }) { template ->
            MiuixTemplateItem(template)
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun MiuixTemplateItem(template: TemplateViewModel.TemplateInfo) {
    val navigator = LocalNavigator.current
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 10.dp),
        colors = miuixCardColors(),
        onClick = {
            navigator.navigateForResult(
                Route.TemplateEditor(
                    templateId = template.id,
                    readOnly = !template.local,
                    isCreation = false,
                ),
                "template_edit"
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = template.name, fontSize = 17.sp, color = colors.onSurface)
            Text(
                text = template.id + if (template.author.isEmpty()) "" else "@${template.author}",
                fontSize = 13.sp,
                color = colors.onSurfaceVariantSummary,
            )
            if (template.description.isNotBlank()) {
                Text(text = template.description, fontSize = 14.sp, color = colors.onSurfaceVariantSummary)
            }
            FlowRow(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MiuixStatusTag(
                    label = "UID: ${template.uid}",
                    backgroundColor = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer,
                )
                MiuixStatusTag(label = "GID: ${template.gid}")
                MiuixStatusTag(label = template.context)
                MiuixStatusTag(
                    label = stringResource(if (template.local) R.string.local else R.string.remote)
                )
            }
        }
    }
}

@Composable
internal fun MiuixTemplateEditor(
    title: String,
    summary: String,
    readOnly: Boolean,
    isCreation: Boolean,
    template: TemplateViewModel.TemplateInfo,
    idError: (String) -> String,
    onIdChange: (String) -> Unit,
    onTemplateChange: (TemplateViewModel.TemplateInfo) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
) {
    androidx.activity.compose.BackHandler(onBack = onBack)
    MiuixListPage(
        title = title,
        subtitle = summary,
        onBack = onBack,
        actions = {
            if (!readOnly) {
                MiuixBarAction(
                    icon = Icons.Rounded.DeleteForever,
                    contentDescription = stringResource(R.string.app_profile_template_delete),
                    onClick = onDelete,
                )
                MiuixBarAction(
                    icon = Icons.Rounded.Save,
                    contentDescription = stringResource(R.string.app_profile_template_save),
                    onClick = onSave,
                )
            }
        },
    ) {
        item {
            Column(
                Modifier.then(
                    if (readOnly) Modifier.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                            }
                        }
                    } else Modifier
                )
            ) {
                MiuixGroupCard {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        if (isCreation) {
                            var error by remember { mutableStateOf("") }
                            MiuixTemplateTextField(
                                label = stringResource(R.string.app_profile_template_id),
                                initial = template.id,
                                error = error,
                            ) {
                                error = idError(it)
                                onIdChange(it)
                            }
                        }
                        MiuixTemplateTextField(
                            label = stringResource(R.string.app_profile_template_name),
                            initial = template.name,
                        ) { onTemplateChange(template.copy(name = it)) }
                        MiuixTemplateTextField(
                            label = stringResource(R.string.app_profile_template_description),
                            initial = template.description,
                        ) { onTemplateChange(template.copy(description = it)) }
                    }
                }
                MiuixGroupCard {
                    MiuixRootProfileConfig(profile = toNativeProfile(template)) {
                        onTemplateChange(
                            template.copy(
                                uid = it.uid,
                                gid = it.gid,
                                groups = it.groups,
                                capabilities = it.capabilities,
                                context = it.context,
                                namespace = it.namespace,
                                rules = it.rules.split("\n"),
                                flags = it.flags.toRootProfileFlags().toOrdinalList(),
                            )
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MiuixTemplateTextField(
    label: String,
    initial: String,
    error: String = "",
    onChange: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        top.yukonga.miuix.kmp.basic.TextField(
            value = text,
            onValueChange = {
                text = it
                onChange(it)
            },
            label = label,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii,
            ),
        )
        if (error.isNotEmpty()) {
            Text(
                text = error,
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp),
            )
        }
    }
}
