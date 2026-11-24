package com.nadekosu.ui.component.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.ThemeConfig
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Card colors for Miuix pages: translucent over a custom background, plain otherwise. */
@Composable
fun miuixCardColors(): CardColors {
    val base = MiuixTheme.colorScheme.surfaceContainer
    val color = if (ThemeConfig.customBackgroundUri != null) {
        base.copy(alpha = CardConfig.cardAlpha)
    } else {
        base
    }
    return CardDefaults.defaultColors(
        color = color,
        contentColor = MiuixTheme.colorScheme.onSurfaceContainer,
    )
}

/** Standard card used for a group of preferences on Miuix pages. */
@Composable
fun MiuixGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
        colors = miuixCardColors(),
        content = content,
    )
}

/**
 * Preference that shows its current value and opens a plain window dialog to pick another one.
 * It deliberately does not use Miuix's overlay dropdown, so it works wherever the page is hosted.
 */
@Composable
fun ChoicePreference(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    startAction: @Composable (() -> Unit)? = null,
) {
    var showDialog by remember { mutableStateOf(false) }
    val colors = MiuixTheme.colorScheme

    ArrowPreference(
        title = title,
        summary = summary,
        startAction = startAction,
        enabled = enabled,
        endActions = {
            Text(
                text = items.getOrElse(selectedIndex) { "" },
                fontSize = 14.sp,
                color = colors.onSurfaceVariantActions,
            )
        },
        onClick = { showDialog = true },
    )

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(
                    color = colors.surfaceContainer,
                    contentColor = colors.onSurfaceContainer,
                ),
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                )
                Column {
                    items.forEachIndexed { index, label ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDialog = false
                                    onSelectedIndexChange(index)
                                }
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                fontSize = 16.sp,
                                color = if (index == selectedIndex) colors.primary else colors.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            if (index == selectedIndex) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
