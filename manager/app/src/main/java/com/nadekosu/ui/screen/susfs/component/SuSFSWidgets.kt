package com.nadekosu.ui.screen.susfs.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadekosu.data.UiStyle
import com.nadekosu.ui.component.WarningCard
import com.nadekosu.ui.component.miuix.MiuixWarningCard
import com.nadekosu.ui.component.miuix.miuixCardColors
import com.nadekosu.ui.component.settings.SegmentedColumn
import com.nadekosu.ui.component.settings.SegmentedColumnScope
import com.nadekosu.ui.component.settings.SettingsBaseWidget
import com.nadekosu.ui.component.settings.SettingsDropdownWidget
import com.nadekosu.ui.component.settings.SettingsJumpPageWidget
import com.nadekosu.ui.component.settings.SettingsSwitchWidget
import com.nadekosu.ui.component.settings.SettingsTextFieldWidget
import com.nadekosu.ui.theme.ThemeConfig
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton

/*
 * Style-aware building blocks for the SuSFS tabs. Every widget renders the original Material
 * settings widget by default and a native Miuix component when the Miuix UI style is active,
 * so the tab logic stays shared between both styles.
 */

internal val isSuSFSMiuix: Boolean
    get() = ThemeConfig.uiStyle == UiStyle.MIUIX

@Composable
private fun MiuixStartIcon(icon: ImageVector?) {
    if (icon == null) return
    MiuixIcon(
        imageVector = icon,
        contentDescription = null,
        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier
            .padding(end = 14.dp)
            .size(22.dp),
    )
}

/**
 * Group of settings rows. Material: [SegmentedColumn]. Miuix: a card (or a plain column when
 * [contentPadding] is zero, e.g. inside dialogs) holding the visible rows.
 */
@Composable
internal fun SuSFSGroup(
    modifier: Modifier = Modifier,
    title: String = "",
    contentPadding: PaddingValues? = null,
    content: SegmentedColumnScope.() -> Unit,
) {
    if (!isSuSFSMiuix) {
        if (contentPadding != null) {
            SegmentedColumn(modifier = modifier, title = title, contentPadding = contentPadding, content = content)
        } else {
            SegmentedColumn(modifier = modifier, title = title, content = content)
        }
        return
    }
    val items = SegmentedColumnScope().apply(content).items
    val body: @Composable ColumnScope.() -> Unit = {
        items.forEach { data ->
            key(data.key) {
                AnimatedVisibility(visible = data.visible) {
                    Column { data.content(RectangleShape) }
                }
            }
        }
    }
    Column(modifier = modifier) {
        if (title.isNotEmpty()) SmallTitle(text = title)
        if (contentPadding != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = body,
            )
        } else {
            MiuixCard(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
                    .fillMaxWidth(),
                colors = miuixCardColors(),
                content = body,
            )
        }
    }
}

/** Clickable row with a trailing arrow. */
@Composable
internal fun SuSFSJumpItem(
    title: String,
    icon: ImageVector? = null,
    iconPlaceholder: Boolean = true,
    description: String? = null,
    enabled: Boolean = true,
    trailingIcon: ImageVector = Icons.Filled.ChevronRight,
    onClick: () -> Unit,
) {
    if (!isSuSFSMiuix) {
        SettingsJumpPageWidget(
            icon = icon,
            iconPlaceholder = iconPlaceholder,
            title = title,
            description = description,
            enabled = enabled,
            trailingIcon = trailingIcon,
            onClick = { onClick() },
        )
        return
    }
    if (trailingIcon == Icons.Filled.ChevronRight) {
        ArrowPreference(
            title = title,
            summary = description,
            startAction = icon?.let { { MiuixStartIcon(it) } },
            enabled = enabled,
            onClick = onClick,
        )
    } else {
        BasicComponent(
            title = title,
            summary = description,
            startAction = icon?.let { { MiuixStartIcon(it) } },
            endActions = {
                MiuixIcon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            },
            enabled = enabled,
            onClick = onClick,
        )
    }
}

/** Read-only information row. */
@Composable
internal fun SuSFSInfoItem(
    title: String,
    icon: ImageVector? = null,
    iconPlaceholder: Boolean = true,
    description: String? = null,
    renderBackgroundBlur: Boolean = true,
) {
    if (!isSuSFSMiuix) {
        SettingsBaseWidget(
            icon = icon,
            iconPlaceholder = iconPlaceholder,
            title = title,
            description = description,
            renderBackgroundBlur = renderBackgroundBlur,
        )
        return
    }
    BasicComponent(
        title = title,
        summary = description,
        startAction = icon?.let { { MiuixStartIcon(it) } },
    )
}

/** Switch row. */
@Composable
internal fun SuSFSSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    description: String? = null,
    enabled: Boolean = true,
) {
    if (!isSuSFSMiuix) {
        SettingsSwitchWidget(
            icon = icon,
            title = title,
            description = description,
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )
        return
    }
    SwitchPreference(
        checked = checked,
        onCheckedChange = onCheckedChange,
        title = title,
        summary = description,
        startAction = icon?.let { { MiuixStartIcon(it) } },
        enabled = enabled,
    )
}

/** Text input row. */
@Composable
internal fun SuSFSTextField(
    state: TextFieldState,
    title: String,
    enabled: Boolean = true,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
) {
    if (!isSuSFSMiuix) {
        SettingsTextFieldWidget(
            state = state,
            title = title,
            useLabelAsPlaceholder = true,
            enabled = enabled,
            lineLimits = lineLimits,
            renderBackgroundBlur = false,
        )
        return
    }
    MiuixTextField(
        state = state,
        modifier = Modifier.fillMaxWidth(),
        label = title,
        useLabelAsPlaceholder = true,
        enabled = enabled,
        lineLimits = lineLimits,
    )
}

/** Single-choice dropdown row. */
@Composable
internal fun SuSFSDropdownItem(
    title: String,
    choice: Int,
    data: List<String>,
    onChoiceChange: (Int) -> Unit,
    description: String? = null,
    enabled: Boolean = true,
) {
    if (!isSuSFSMiuix) {
        SettingsDropdownWidget(
            title = title,
            description = description,
            iconPlaceholder = false,
            enabled = enabled,
            choice = choice,
            data = data,
            onChoiceChange = onChoiceChange,
        )
        return
    }
    // Window-based dropdown so it is drawn above overlay dialogs.
    WindowDropdownPreference(
        items = data,
        selectedIndex = choice,
        title = title,
        enabled = enabled,
        onSelectedIndexChange = onChoiceChange,
    )
}

/** Secondary text line used inside [SuSFSDescriptionCard]. */
@Composable
internal fun SuSFSDescriptionText(text: String) {
    if (!isSuSFSMiuix) {
        MaterialText(text = text, style = MaterialTheme.typography.bodySmall)
        return
    }
    MiuixText(
        text = text,
        fontSize = 13.sp,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}

/** Miuix variant of [SuSFSDescriptionCard]. */
@Composable
internal fun MiuixSuSFSDescriptionCard(
    title: String,
    description: String?,
    content: @Composable ColumnScope.() -> Unit,
) {
    MiuixCard(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
        colors = miuixCardColors(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MiuixText(
                text = title,
                fontSize = 16.sp,
                color = MiuixTheme.colorScheme.onSurface,
            )
            description?.let {
                MiuixText(
                    text = it,
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            content()
        }
    }
}

/** Warning banner. */
@Composable
internal fun SuSFSWarningCard(message: String) {
    if (!isSuSFSMiuix) {
        WarningCard(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            message = message,
        )
        return
    }
    Box(Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
        MiuixWarningCard(message = message)
    }
}

/** Full-size loading indicator. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SuSFSLoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isSuSFSMiuix) {
            InfiniteProgressIndicator(color = MiuixTheme.colorScheme.primary)
        } else {
            LoadingIndicator()
        }
    }
}

/**
 * Confirm/cancel dialog. Material: [AlertDialog]. Miuix: [OverlayDialog] with Miuix buttons.
 *
 * @param destructive Paints the confirm action with the error color.
 */
@Composable
internal fun SuSFSDialog(
    show: Boolean,
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    message: String? = null,
    destructive: Boolean = false,
    materialConfirmButton: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    if (!isSuSFSMiuix) {
        if (!show) return
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { MaterialText(title) },
            text = (content ?: message?.let { { MaterialText(it) } }),
            confirmButton = materialConfirmButton ?: {
                MaterialTextButton(onClick = onConfirm) { MaterialText(confirmText) }
            },
            dismissButton = {
                MaterialTextButton(onClick = onDismiss) { MaterialText(dismissText) }
            },
        )
        return
    }
    OverlayDialog(
        show = show,
        title = title,
        summary = message,
        onDismissRequest = onDismiss,
    ) {
        Column {
            if (content != null) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    content()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MiuixTextButton(
                    text = dismissText,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                MiuixTextButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = if (destructive) {
                        MiuixButtonDefaults.textButtonColors(
                            color = MiuixTheme.colorScheme.error,
                            textColor = MiuixTheme.colorScheme.onError,
                        )
                    } else {
                        MiuixButtonDefaults.textButtonColorsPrimary()
                    },
                )
            }
        }
    }
}

/** Background for a row of a Miuix lazy entry group, rounded at the group's ends. */
@Composable
internal fun Modifier.miuixSegment(isFirst: Boolean, isLast: Boolean): Modifier {
    val top = if (isFirst) 16.dp else 0.dp
    val bottom = if (isLast) 16.dp else 0.dp
    return this
        .padding(horizontal = 12.dp)
        .padding(bottom = if (isLast) 12.dp else 0.dp)
        .clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
        .background(miuixCardColors().color)
}
