package com.nadekosu.ui.component.miuix

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadekosu.R
import com.nadekosu.ui.theme.monospaceFontFamily
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Miuix page that streams shell output into a card, used by the flash and
 * module action screens. Item 2 of [listState] is the bottom spacer, so callers
 * can keep scrolling to it while output arrives.
 */
@Composable
fun MiuixLogPage(
    title: String,
    text: String,
    listState: LazyListState,
    running: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    subtitle: String = "",
    saveLabel: String = stringResource(R.string.save_log),
    fabIcon: ImageVector = Icons.Rounded.Close,
    fabLabel: String = stringResource(R.string.close),
    showFab: Boolean = !running,
    onFabClick: () -> Unit = onBack,
    header: @Composable () -> Unit = {},
) {
    MiuixListPage(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        listState = listState,
        actions = {
            MiuixBarAction(
                icon = Icons.Rounded.Save,
                contentDescription = saveLabel,
                enabled = !running,
                onClick = onSave,
            )
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    modifier = Modifier.padding(end = 4.dp, bottom = 8.dp),
                    onClick = onFabClick,
                    containerColor = MiuixTheme.colorScheme.primary,
                    minWidth = 0.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = fabIcon,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = fabLabel, color = MiuixTheme.colorScheme.onPrimary)
                    }
                }
            }
        },
    ) {
        item { header() }
        item {
            MiuixGroupCard {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = monospaceFontFamily(),
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}
