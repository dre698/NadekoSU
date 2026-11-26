package com.nadekosu.ui.component.miuix

import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.ui.Alignment
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.ThemeConfig
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
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
 * Dropdown preference. Its popup is drawn by the page's `MiuixPopupHost`, so the Scaffold that
 * contains it must not override `popupHost` with an empty one.
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
    OverlayDropdownPreference(
        title = title,
        summary = summary,
        items = items,
        startAction = startAction,
        enabled = enabled,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelectedIndexChange,
    )
}

/** Error-tinted (or neutral) message card used across Miuix pages. */
@Composable
fun MiuixWarningCard(message: String, onClick: (() -> Unit)? = null, neutral: Boolean = false) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (neutral) miuixCardColors() else CardDefaults.defaultColors(
            color = colors.errorContainer,
            contentColor = colors.onErrorContainer,
        ),
        onClick = onClick,
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (neutral) Icons.Rounded.Info else Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = if (neutral) colors.onSurface else colors.onErrorContainer,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = if (neutral) colors.onSurface else colors.onErrorContainer,
            )
        }
    }
}
