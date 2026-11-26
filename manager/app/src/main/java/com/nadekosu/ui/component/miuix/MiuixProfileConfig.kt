package com.nadekosu.ui.component.miuix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReadMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.nadekosu.Natives
import com.nadekosu.R
import com.nadekosu.profile.Capabilities
import com.nadekosu.profile.Groups
import com.nadekosu.toRawFlags
import com.nadekosu.toRootProfileFlags
import com.nadekosu.ui.util.isSepolicyValid
import com.nadekosu.ui.util.listAppProfileTemplates
import com.nadekosu.ui.util.setSepolicy
import com.nadekosu.ui.viewmodel.getTemplateInfoById
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight

/**
 * Preference that opens a dialog with a checkbox per item and reports the
 * chosen indices once the user confirms.
 */
@Composable
fun MiuixMultiChoicePreference(
    title: String,
    items: List<String>,
    selectedIndices: Set<Int>,
    onSelectedIndicesChange: (Set<Int>) -> Unit,
    itemDescriptions: List<String?> = emptyList(),
    maxSelected: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
) {
    var show by remember { mutableStateOf(false) }
    var pending by remember(show, selectedIndices) { mutableStateOf(selectedIndices) }
    val selectedLabels = selectedIndices.sorted().mapNotNull { items.getOrNull(it) }

    BasicComponent(
        title = title,
        enabled = enabled,
        onClick = { show = true },
        endActions = {
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        },
        bottomAction = if (selectedLabels.isEmpty()) null else {
            {
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    selectedLabels.forEach { MiuixStatusTag(label = it) }
                }
            }
        },
    )

    OverlayDialog(
        show = show,
        title = title,
        onDismissRequest = { show = false },
    ) {
        Column {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                itemsIndexed(items) { index, item ->
                    val checked = index in pending
                    CheckboxPreference(
                        title = item,
                        summary = itemDescriptions.getOrNull(index),
                        checked = checked,
                        enabled = checked || pending.size < maxSelected,
                        onCheckedChange = {
                            pending = if (it) pending + index else pending - index
                        },
                    )
                }
            }
            MiuixDialogButtons(
                onDismiss = { show = false },
                onConfirm = {
                    show = false
                    onSelectedIndicesChange(pending)
                },
            )
        }
    }
}

/** Miuix counterpart of `rootProfileConfig`, drawn as rows inside a group card. */
@Composable
fun MiuixRootProfileConfig(
    profile: Natives.Profile,
    onProfileChange: (Natives.Profile) -> Unit,
) {
    MiuixUidField(value = profile.uid, label = "uid") {
        onProfileChange(profile.copy(uid = it, rootUseDefault = false))
    }
    MiuixUidField(value = profile.gid, label = "gid") {
        onProfileChange(profile.copy(gid = it, rootUseDefault = false))
    }

    val selectedGroups = profile.groups.ifEmpty { listOf(0) }.mapNotNull { g ->
        Groups.entries.find { it.gid == g }
    }
    val groups = remember(selectedGroups) {
        Groups.entries.sortedWith(
            compareBy<Groups> { if (it in selectedGroups) 0 else 1 }
                .thenBy {
                    when (it) {
                        Groups.ROOT -> 0
                        Groups.SYSTEM -> 1
                        Groups.SHELL -> 2
                        else -> Int.MAX_VALUE
                    }
                }
                .thenBy { it.name }
        )
    }
    MiuixMultiChoicePreference(
        title = stringResource(R.string.profile_groups),
        items = groups.map { it.display },
        itemDescriptions = groups.map { it.desc },
        selectedIndices = groups.indices.filter { groups[it] in selectedGroups }.toSet(),
        maxSelected = 32,
        onSelectedIndicesChange = { indices ->
            onProfileChange(
                profile.copy(
                    groups = indices.mapNotNull { groups.getOrNull(it)?.gid }.ifEmpty { listOf(0) },
                    rootUseDefault = false,
                )
            )
        },
    )

    val selectedCaps = profile.capabilities.mapNotNull { c -> Capabilities.entries.find { it.cap == c } }
    val caps = remember(selectedCaps) {
        Capabilities.entries.sortedWith(
            compareBy<Capabilities> { if (it in selectedCaps) 0 else 1 }.thenBy { it.name }
        )
    }
    MiuixMultiChoicePreference(
        title = stringResource(R.string.profile_capabilities),
        items = caps.map { it.display },
        itemDescriptions = caps.map { it.desc },
        selectedIndices = caps.indices.filter { caps[it] in selectedCaps }.toSet(),
        onSelectedIndicesChange = { indices ->
            onProfileChange(
                profile.copy(
                    capabilities = indices.mapNotNull { caps.getOrNull(it)?.cap },
                    rootUseDefault = false,
                )
            )
        },
    )

    ChoicePreference(
        title = stringResource(R.string.profile_namespace),
        items = listOf(
            stringResource(R.string.profile_namespace_inherited),
            stringResource(R.string.profile_namespace_global),
            stringResource(R.string.profile_namespace_individual),
        ),
        selectedIndex = profile.namespace,
        onSelectedIndexChange = {
            onProfileChange(profile.copy(namespace = it, rootUseDefault = false))
        },
    )

    val selectedFlags = profile.flags.toRootProfileFlags()
    val flags = Natives.Profile.RootProfileFlag.entries
    MiuixMultiChoicePreference(
        title = stringResource(R.string.profile_flags),
        items = flags.map { it.display },
        itemDescriptions = flags.map { stringResource(it.desc) },
        selectedIndices = flags.indices.filter { flags[it] in selectedFlags }.toSet(),
        onSelectedIndicesChange = { indices ->
            onProfileChange(profile.copy(flags = indices.mapNotNull { flags.getOrNull(it) }.toRawFlags()))
        },
    )

    MiuixSELinuxPreference(profile = profile) { domain, rules ->
        onProfileChange(profile.copy(context = domain, rules = rules, rootUseDefault = false))
    }
}

@Composable
private fun MiuixUidField(value: Int, label: String, onChange: (Int) -> Unit) {
    var text by remember { mutableStateOf(value.toString()) }
    LaunchedEffect(value) {
        if (text.toIntOrNull() != value && text.isNotEmpty()) text = value.toString()
    }
    TextField(
        value = text,
        onValueChange = { input ->
            if (input.isEmpty()) {
                text = ""
                onChange(0)
            } else if (input.isDigitsOnly() && input.toIntOrNull() != null) {
                text = input
                onChange(input.toInt())
            }
        },
        label = label,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun MiuixSELinuxPreference(
    profile: Natives.Profile,
    onChange: (domain: String, rules: String) -> Unit,
) {
    var show by remember { mutableStateOf(false) }
    ArrowPreference(
        title = stringResource(R.string.profile_selinux_context),
        summary = profile.context,
        onClick = { show = true },
    )
    var domain by remember(show, profile.context) { mutableStateOf(profile.context) }
    var rules by remember(show, profile.rules) { mutableStateOf(profile.rules) }
    val domainValid = domain.matches(Regex("^[a-z_]+:[a-z0-9_]+:[a-z0-9_]+(:[a-z0-9_]+)?$"))
    OverlayDialog(
        show = show,
        title = stringResource(R.string.profile_selinux_context),
        onDismissRequest = { show = false },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = domain,
                onValueChange = { domain = it },
                label = stringResource(R.string.profile_selinux_domain),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            )
            TextField(
                value = rules,
                onValueChange = { rules = it },
                label = stringResource(R.string.profile_selinux_rules),
                minLines = 4,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            )
            MiuixDialogButtons(
                onDismiss = { show = false },
                onConfirm = {
                    show = false
                    onChange(domain, rules)
                },
                confirmEnabled = domainValid && isSepolicyValid(rules),
            )
        }
    }
}

/** Miuix counterpart of `AppProfileConfig` (umount modules switch). */
@Composable
fun MiuixAppProfileConfig(
    enabled: Boolean,
    profile: Natives.Profile,
    onProfileChange: (Natives.Profile) -> Unit,
) {
    SwitchPreference(
        title = stringResource(R.string.profile_umount_modules),
        summary = stringResource(R.string.profile_umount_modules_summary),
        checked = if (enabled) profile.umountModules else Natives.isDefaultUmountModules(),
        enabled = enabled,
        onCheckedChange = {
            onProfileChange(profile.copy(umountModules = it, nonRootUseDefault = false))
        },
    )
}

/** Miuix counterpart of `TemplateConfig`: pick a root profile template. */
@Composable
fun MiuixTemplateConfig(
    profile: Natives.Profile,
    onViewTemplate: (id: String) -> Unit = {},
    onProfileChange: (Natives.Profile) -> Unit,
) {
    var template by rememberSaveable { mutableStateOf(profile.rootTemplate ?: "") }
    val profileTemplates = listOf("None") + listAppProfileTemplates()
    val currentIndex = profileTemplates.indexOf(template).let { if (it == -1) 0 else it }

    ChoicePreference(
        title = stringResource(R.string.profile_template),
        items = profileTemplates,
        selectedIndex = currentIndex,
        onSelectedIndexChange = { index ->
            if (index == 0) {
                template = ""
                return@ChoicePreference
            }
            template = profileTemplates[index]
            val info = getTemplateInfoById(template) ?: return@ChoicePreference
            if (setSepolicy(template, info.rules.joinToString("\n"))) {
                onProfileChange(
                    profile.copy(
                        rootTemplate = template,
                        rootUseDefault = false,
                        uid = info.uid,
                        gid = info.gid,
                        groups = info.groups,
                        capabilities = info.capabilities,
                        context = info.context,
                        namespace = info.namespace,
                    )
                )
            }
        },
    )
    if (currentIndex != 0) {
        BasicComponent(
            title = stringResource(R.string.app_profile_template_view),
            summary = profileTemplates[currentIndex],
            onClick = { onViewTemplate(profileTemplates[currentIndex]) },
            endActions = {
                IconButton(onClick = { onViewTemplate(profileTemplates[currentIndex]) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReadMore,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    )
                }
            },
        )
    }
}
