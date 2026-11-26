package com.nadekosu.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun LoadingDialogMiuix(
    showDialog: MutableState<Boolean>,
) {
    WindowDialog(
        show = showDialog.value,
        content = {
            // Consume the back gesture before the dialog's own handler
            val navEventState = rememberNavigationEventState(NavigationEventInfo.None)
            NavigationBackHandler(
                state = navEventState,
                isBackEnabled = true,
                onBackCompleted = { },
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                InfiniteProgressIndicator(
                    color = MiuixTheme.colorScheme.onBackground
                )
            }
        }
    )
}

@Composable
fun ConfirmDialogMiuix(
    visuals: ConfirmDialogVisuals,
    confirm: () -> Unit,
    dismiss: () -> Unit,
    showDialog: MutableState<Boolean>
) {
    WindowDialog(
        show = showDialog.value,
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
        title = visuals.title,
        onDismissRequest = {
            dismiss()
            showDialog.value = false
        },
        content = {
            val dismissState = LocalDismissState.current
            Column {
                Column(
                    modifier = Modifier
                        .heightIn(max = 325.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val textColor = MiuixTheme.colorScheme.onSurface
                    when {
                        visuals.isMarkdown -> MarkdownContent(
                            content = visuals.content,
                            contentColor = textColor,
                        )

                        visuals.isHtml -> GithubMarkdown(
                            content = visuals.content,
                            backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
                        )

                        else -> Text(text = visuals.content)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    TextButton(
                        text = visuals.dismiss ?: stringResource(id = android.R.string.cancel),
                        onClick = {
                            dismiss()
                            dismissState?.invoke()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(20.dp))
                    TextButton(
                        text = visuals.confirm ?: stringResource(id = android.R.string.ok),
                        onClick = {
                            confirm()
                            dismissState?.invoke()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    )
}
