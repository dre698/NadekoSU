package com.nadekosu.ui.screen.themeSettings.component

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import android.net.Uri
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

/**
 * Everything the preview mirrors from the real app. The preview itself holds no settings logic:
 * the screen builds this from the same states the app reads, so any change shows up immediately.
 */
data class ThemePreviewState(
    val dashboardLayout: Boolean = false,
    val hasBanner: Boolean = false,
    val simpleMode: Boolean = false,
    val showHomeCardIcons: Boolean = false,
    val hideLinkCard: Boolean = false,
    val hideOtherInfo: Boolean = false,
    val showNavBadge: Boolean = true,
    val liquidGlass: Boolean = false,
    val topBarBlur: Boolean = false,
    val cardAlpha: Float = 1f,
    val backgroundUri: Uri? = null,
    val backgroundDim: Float = 0f,
)

/** Compact preview: mini phone mock on the left, mode label + palette dots on the right. */
@Composable
fun ThemePreviewCard(
    modeLabel: String,
    previewState: ThemePreviewState,
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
            MiniPhonePreview(state = previewState, modifier = Modifier.width(88.dp))
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
fun ThemePreviewDialog(
    show: Boolean,
    previewState: ThemePreviewState,
    onDismiss: () -> Unit,
) {
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
                MiniPhonePreview(state = previewState, modifier = Modifier.width(220.dp))
            }
        }
    }
}

/**
 * A phone-shaped mock of the real home screen, drawn from the live color scheme and
 * [ThemePreviewState]: layout (circle / dashboard), banner, simple mode, card icons, link card,
 * badge, top bar blur, liquid glass bottom bar, card alpha and custom background all show up here.
 */
@Composable
private fun MiniPhonePreview(state: ThemePreviewState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val cardAlpha = state.cardAlpha.coerceIn(0.1f, 1f)
    val hasBackground = state.backgroundUri != null
    Surface(
        modifier = modifier.aspectRatio(9f / 19f),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(2.dp, colors.outlineVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasBackground) {
                AsyncImage(
                    model = state.backgroundUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = state.backgroundDim.coerceIn(0f, 1f)))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .padding(top = 30.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (state.dashboardLayout) {
                    // Hero with banner image, then the counters
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (state.hasBanner) Brush.linearGradient(
                                    listOf(colors.primary, colors.tertiary)
                                ) else Brush.linearGradient(
                                    listOf(colors.primaryContainer, colors.primaryContainer)
                                )
                            )
                            .padding(6.dp)
                    ) {
                        PreviewBar(
                            width = 0.45f,
                            height = 5.dp,
                            color = if (state.hasBanner) colors.onPrimary else colors.onPrimaryContainer,
                            modifier = Modifier.align(Alignment.BottomStart),
                        )
                    }
                    if (!state.hideOtherInfo) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(colors.surfaceContainerHighest.copy(alpha = cardAlpha))
                                )
                            }
                        }
                    }
                } else {
                    // Circle UI: one status card with a ring on the left
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primaryContainer.copy(alpha = cardAlpha))
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(3.dp, colors.onPrimaryContainer, CircleShape)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            PreviewBar(width = 1f, height = 5.dp, color = colors.onPrimaryContainer,
                                modifier = Modifier.width(34.dp))
                            PreviewBar(width = 1f, height = 4.dp,
                                color = colors.onPrimaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.width(22.dp))
                        }
                    }
                }

                // Info card: fewer rows in simple mode, optional leading icons
                val rows = if (state.simpleMode) 2 else 4
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceContainer.copy(alpha = cardAlpha))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    repeat(rows) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (state.showHomeCardIcons) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                )
                            }
                            PreviewBar(width = 1f, height = 4.dp,
                                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Link card
                if (!state.simpleMode && !state.hideLinkCard) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.tertiaryContainer.copy(alpha = cardAlpha))
                    )
                }
            }

            // Top bar: translucent when blur is on, solid otherwise
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        colors.surfaceContainer.copy(alpha = if (state.topBarBlur) 0.45f else cardAlpha)
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                PreviewBar(width = 0.4f, height = 5.dp, color = colors.onSurface)
            }

            // Bottom bar: glass capsule (translucent + highlight edge) or standard solid capsule
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.liquidGlass) colors.surfaceContainerHighest.copy(alpha = 0.4f)
                        else colors.surfaceContainerHighest
                    )
                    .then(
                        if (state.liquidGlass) Modifier.border(
                            1.dp,
                            colors.onSurface.copy(alpha = 0.25f),
                            CircleShape,
                        ) else Modifier
                    )
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.7f)
                        .width(24.dp)
                        .clip(CircleShape)
                        .background(colors.secondaryContainer)
                )
                repeat(3) { index ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.onSurfaceVariant)
                        )
                        if (index == 0 && state.showNavBadge && !state.hideOtherInfo) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewBar(
    width: Float,
    height: Dp,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .then(if (width < 1f) Modifier.fillMaxWidth(width) else Modifier)
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
