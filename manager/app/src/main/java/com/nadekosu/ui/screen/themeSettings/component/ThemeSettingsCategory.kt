package com.nadekosu.ui.screen.themeSettings.component

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nadekosu.R
import com.nadekosu.ui.theme.CardConfig

/**
 * Categories of the theme settings page. Only the selected category's settings are composed;
 * the category grid under the preview card switches between them.
 */
enum class ThemeSettingsCategory(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    COLORS(R.string.theme_category_colors, Icons.Rounded.Colorize),
    BACKGROUND(R.string.theme_category_background, Icons.Rounded.Wallpaper),
    ICON_TEXT(R.string.theme_category_icon_text, Icons.Rounded.Edit),
    HOME_CARD(R.string.theme_category_home_card, Icons.Rounded.Home),
    TOP_BAR(R.string.theme_category_top_bar, Icons.Rounded.VerticalAlignTop),
    BOTTOM_BAR(R.string.theme_category_bottom_bar, Icons.Rounded.CallToAction),
    GESTURES(R.string.theme_category_gestures, Icons.Rounded.Swipe),
    DETAILS(R.string.theme_category_details, Icons.Rounded.Description),
    LANGUAGE(R.string.theme_category_language, Icons.Rounded.Translate);
}

/** Compact preview: mini phone mock on the left, mode label + palette dots on the right. */
@Composable
fun ThemePreviewCard(
    modeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.surfaceContainer.copy(alpha = CardConfig.cardAlpha),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniPhonePreview(modifier = Modifier.width(88.dp))
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.theme_preview_caption),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                )
                Text(
                    text = modeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        colors.primary,
                        colors.secondaryContainer,
                        colors.tertiaryContainer,
                        colors.surfaceContainerHighest,
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.theme_preview_compact_summary),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** Larger preview shown when the compact card is tapped. */
@Composable
fun ThemePreviewDialog(show: Boolean, onDismiss: () -> Unit) {
    if (!show) return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = onDismiss,
        ) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                MiniPhonePreview(modifier = Modifier.width(220.dp))
            }
        }
    }
}

/**
 * A phone-shaped mock drawn from the live [MaterialTheme.colorScheme], so it follows every
 * setting that changes the theme (mode, seed color, palette style, color spec) with no extra state.
 */
@Composable
private fun MiniPhonePreview(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val phoneShape = RoundedCornerShape(14.dp)
    Surface(
        modifier = modifier.aspectRatio(9f / 19f),
        shape = phoneShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(2.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Title
            PreviewBar(width = 0.4f, height = 5.dp, color = colors.onSurface)
            // Hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primaryContainer)
                    .padding(6.dp)
            ) {
                PreviewBar(width = 0.45f, height = 5.dp, color = colors.onPrimaryContainer)
            }
            // Stat tiles
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(colors.surfaceContainerHighest)
                    )
                }
            }
            // Highlight pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.tertiaryContainer)
            )
            // List rows
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(colors.surfaceContainer)
                )
            }
            Spacer(Modifier.weight(1f))
            // Bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceContainerHighest)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .width(22.dp)
                        .clip(CircleShape)
                        .background(colors.secondaryContainer)
                )
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(colors.onSurfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewBar(width: Float, height: Dp, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth(width)
            .height(height)
            .clip(CircleShape)
            .background(color)
    )
}

/** 3-column connected toggle-button grid used to switch between categories. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeCategoryGrid(
    selected: ThemeSettingsCategory,
    onSelect: (ThemeSettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.surfaceContainer.copy(alpha = CardConfig.cardAlpha),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            ThemeSettingsCategory.entries.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    row.forEachIndexed { index, category ->
                        ToggleButton(
                            checked = category == selected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    onSelect(category)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                row.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            colors = ToggleButtonDefaults.colors(
                                checkedContainerColor = colors.primaryContainer,
                                checkedContentColor = colors.onPrimaryContainer,
                                containerColor = colors.surfaceContainerHighest,
                                contentColor = colors.onSurface,
                            ),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                )
                                Text(
                                    text = stringResource(category.labelRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
