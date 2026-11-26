package com.nadekosu.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadekosu.R
import com.nadekosu.ui.component.miuix.ChoicePreference
import com.nadekosu.ui.component.miuix.MiuixBarAction
import com.nadekosu.ui.component.miuix.MiuixEmptyState
import com.nadekosu.ui.component.miuix.MiuixGroupCard
import com.nadekosu.ui.component.miuix.MiuixListPage
import com.nadekosu.ui.component.miuix.MiuixLoadingBox
import com.nadekosu.ui.component.miuix.MiuixSearchField
import com.nadekosu.ui.component.miuix.MiuixStatusTag
import com.nadekosu.ui.component.miuix.MiuixWarningCard
import com.nadekosu.ui.component.miuix.miuixCardColors
import com.nadekosu.ui.theme.monospaceFontFamily
import com.nadekosu.ui.util.SulogEntry
import com.nadekosu.ui.util.SulogEventFilter
import com.nadekosu.ui.viewmodel.SulogActions
import com.nadekosu.ui.viewmodel.SulogScreenState
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixSulogScreenContent(
    state: SulogScreenState,
    actions: SulogActions,
) {
    val fileSelector = buildSulogFileSelector(state.files, state.selectedFilePath)
    var selectedEntry by remember { mutableStateOf<SulogEntry?>(null) }
    val filterLabels = SulogEventFilter.entries.map { sulogFilterLabel(it) }

    MiuixListPage(
        title = stringResource(R.string.settings_sulog),
        onBack = actions.onBack,
        isRefreshing = state.isRefreshing,
        onRefresh = actions.onRefresh,
        actions = {
            MiuixBarAction(
                icon = Icons.Rounded.DeleteSweep,
                contentDescription = stringResource(R.string.sulog_clean_title),
                onClick = actions.onCleanFile,
            )
            OverlayIconDropdownMenu(
                entry = DropdownEntry(
                    items = SulogEventFilter.entries.mapIndexed { index, filter ->
                        DropdownItem(
                            text = filterLabels[index],
                            selected = filter in state.selectedFilters,
                            onClick = { actions.onToggleFilter(filter) },
                        )
                    }
                ),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = stringResource(R.string.sulog_filter_title),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        },
        overlay = {
            val entry = selectedEntry
            OverlayDialog(
                show = entry != null,
                title = entry?.let { sulogEntryTitle(it) },
                onDismissRequest = { selectedEntry = null },
            ) {
                if (entry != null) {
                    Column {
                        Column(
                            Modifier
                                .heightIn(max = 420.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            SelectionContainer {
                                Text(
                                    text = sulogEntryDetailText(entry),
                                    fontFamily = monospaceFontFamily(),
                                    fontSize = 13.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        TextButton(
                            text = stringResource(android.R.string.ok),
                            onClick = { selectedEntry = null },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }
        },
    ) {
        item {
            MiuixSearchField(
                query = state.searchText,
                onQueryChange = actions.onSearchTextChange,
                label = stringResource(R.string.sulog_search_placeholder),
            )
        }
        if (state.isLoading) {
            item { MiuixLoadingBox(Modifier.heightIn(min = 320.dp)) }
            return@MiuixListPage
        }
        item { MiuixSulogStatus(state, actions) }
        item {
            MiuixGroupCard {
                ChoicePreference(
                    title = stringResource(R.string.sulog_log_files),
                    items = fileSelector.items,
                    enabled = fileSelector.items.isNotEmpty(),
                    selectedIndex = fileSelector.selectedIndex.coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        state.files.getOrNull(index)?.let { actions.onSelectFile(it.path) }
                    },
                )
            }
        }
        when {
            state.errorMessage != null -> item {
                MiuixEmptyState(
                    icon = Icons.Rounded.History,
                    text = stringResource(R.string.sulog_failed_to_load) + "\n" + state.errorMessage,
                )
            }

            state.visibleEntries.isEmpty() -> item {
                MiuixEmptyState(icon = Icons.Rounded.History, text = stringResource(R.string.settings_sulog))
            }

            else -> itemsIndexed(
                state.visibleEntries,
                key = { index, entry -> "$index-${entry.key}" },
            ) { _, entry ->
                MiuixSulogEntryCard(entry = entry, onClick = { selectedEntry = entry })
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun MiuixSulogStatus(state: SulogScreenState, actions: SulogActions) {
    val message = when {
        state.sulogStatus == "unsupported" -> stringResource(R.string.sulog_unsupported_title)
        state.sulogStatus == "managed" -> stringResource(R.string.feature_status_managed_summary)
        state.sulogStatus == "supported" && !state.isSulogEnabled -> stringResource(R.string.sulog_disabled_title)
        else -> return
    }
    Column(
        modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MiuixWarningCard(message = message)
        if (state.sulogStatus == "supported" && !state.isSulogEnabled) {
            Button(
                onClick = actions.onEnableSulog,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(
                    text = stringResource(R.string.sulog_enable_action),
                    color = MiuixTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun MiuixSulogEntryCard(entry: SulogEntry, onClick: () -> Unit) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp),
        colors = miuixCardColors(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            androidx.compose.foundation.layout.Row {
                Text(
                    text = sulogEntryTitle(entry),
                    fontSize = 16.sp,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                sulogEntryStatus(entry)?.let {
                    Text(text = it, fontSize = 13.sp, color = colors.onSurfaceVariantSummary)
                }
            }
            sulogEntryDescription(entry)?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            entry.timestampText?.let {
                Text(text = it, fontSize = 12.sp, color = colors.onSurfaceVariantSummary)
            }
            val tags = sulogEntrySummaryTags(entry)
            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tags.forEach { MiuixStatusTag(label = it) }
                }
            }
        }
    }
}
