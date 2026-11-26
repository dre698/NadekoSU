package com.nadekosu.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nadekosu.Natives
import com.nadekosu.R
import com.nadekosu.ui.component.miuix.MiuixAppProfileConfig
import com.nadekosu.ui.component.miuix.MiuixGroupCard
import com.nadekosu.ui.component.miuix.MiuixListPage
import com.nadekosu.ui.component.miuix.MiuixRootProfileConfig
import com.nadekosu.ui.component.miuix.MiuixTemplateConfig
import com.nadekosu.ui.util.forceStopApp
import com.nadekosu.ui.util.launchApp
import com.nadekosu.ui.util.restartApp
import com.nadekosu.ui.viewmodel.SuperUserViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val MODE_DEFAULT = 0
private const val MODE_TEMPLATE = 1
private const val MODE_CUSTOM = 2

@Composable
internal fun MiuixAppProfileContent(
    appGroup: SuperUserViewModel.AppGroup,
    profile: Natives.Profile,
    onViewTemplate: (String) -> Unit,
    onManageTemplate: () -> Unit,
    onProfileChange: (Natives.Profile) -> Unit,
) {
    val context = LocalContext.current
    val app = appGroup.mainApp
    MiuixListPage(
        title = app.label,
        subtitle = app.packageName,
    ) {
        item {
            MiuixGroupCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(app.packageInfo).crossfade(true).build(),
                        contentDescription = app.label,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = app.label, fontSize = 18.sp, color = MiuixTheme.colorScheme.onSurface)
                    Text(
                        text = "${app.packageName} · UID ${appGroup.uid}",
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            text = stringResource(R.string.launch_app),
                            onClick = { launchApp(app.packageName) },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = stringResource(R.string.force_stop_app),
                            onClick = { forceStopApp(app.packageName) },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = stringResource(R.string.restart_app),
                            onClick = { restartApp(app.packageName) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            MiuixGroupCard {
                SwitchPreference(
                    title = stringResource(R.string.superuser),
                    checked = profile.allowSu,
                    onCheckedChange = { onProfileChange(profile.copy(allowSu = it)) },
                )
            }
        }

        item {
            if (profile.allowSu) {
                val initialMode = when {
                    profile.rootUseDefault -> MODE_DEFAULT
                    profile.rootTemplate != null -> MODE_TEMPLATE
                    else -> MODE_CUSTOM
                }
                var mode by rememberSaveable { mutableIntStateOf(initialMode) }
                SmallTitle(text = stringResource(R.string.profile))
                MiuixProfileModeTabs(selected = mode, hasTemplate = true) {
                    if (it == MODE_DEFAULT || it == MODE_CUSTOM) {
                        onProfileChange(profile.copy(rootUseDefault = it == MODE_DEFAULT, rootTemplate = null))
                    }
                    mode = it
                }
                when (mode) {
                    MODE_TEMPLATE -> MiuixGroupCard {
                        MiuixTemplateConfig(
                            profile = profile,
                            onViewTemplate = onViewTemplate,
                            onProfileChange = onProfileChange,
                        )
                        ArrowPreference(
                            title = stringResource(R.string.manage_app_profile),
                            summary = stringResource(R.string.settings_profile_template_summary),
                            onClick = onManageTemplate,
                        )
                    }

                    MODE_CUSTOM -> MiuixGroupCard {
                        MiuixRootProfileConfig(profile = profile, onProfileChange = onProfileChange)
                    }
                }
            } else {
                val mode = if (profile.nonRootUseDefault) MODE_DEFAULT else MODE_CUSTOM
                SmallTitle(text = stringResource(R.string.profile))
                MiuixProfileModeTabs(selected = mode, hasTemplate = false) {
                    onProfileChange(profile.copy(nonRootUseDefault = it == MODE_DEFAULT))
                }
                if (mode == MODE_CUSTOM) {
                    MiuixGroupCard {
                        MiuixAppProfileConfig(enabled = true, profile = profile, onProfileChange = onProfileChange)
                    }
                }
            }
        }

        if (appGroup.apps.size > 1) {
            item { SmallTitle(text = stringResource(R.string.affected_applications)) }
            item {
                MiuixGroupCard {
                    appGroup.apps.forEach { affected ->
                        BasicComponent(
                            title = affected.label,
                            summary = affected.packageName,
                            startAction = {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(affected.packageInfo)
                                        .crossfade(true).build(),
                                    contentDescription = affected.label,
                                    modifier = Modifier.padding(end = 12.dp).size(40.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MiuixProfileModeTabs(
    selected: Int,
    hasTemplate: Boolean,
    onSelect: (Int) -> Unit,
) {
    val modes = if (hasTemplate) listOf(MODE_DEFAULT, MODE_TEMPLATE, MODE_CUSTOM) else listOf(MODE_DEFAULT, MODE_CUSTOM)
    val labels = modes.map {
        stringResource(
            when (it) {
                MODE_DEFAULT -> R.string.profile_default
                MODE_TEMPLATE -> R.string.profile_template
                else -> R.string.profile_custom
            }
        )
    }
    TabRow(
        tabs = labels,
        selectedTabIndex = modes.indexOf(selected).coerceAtLeast(0),
        onTabSelected = { onSelect(modes[it]) },
        modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
    )
}
