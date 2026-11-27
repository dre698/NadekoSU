package com.nadekosu.ui.screen.themeSettings.component

import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.layout.layout
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Palette
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
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.data.UiStyle
import androidx.compose.runtime.Immutable
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
    THEME(R.string.theme_category_theme, Icons.Rounded.Palette);
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
            color = if (ThemeConfig.uiStyle == UiStyle.MIUIX) {
                MiuixTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
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

/** Colors the mini phone is drawn with: the Miuix palette in Miuix mode, Material otherwise. */
@Immutable
internal class PreviewColors(
    val surfaceContainerLow: Color,
    val outlineVariant: Color,
    val surfaceContainer: Color,
    val surfaceContainerHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val tertiary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
)

@Composable
internal fun previewColors(): PreviewColors {
    if (ThemeConfig.uiStyle == UiStyle.MIUIX) {
        val m = MiuixTheme.colorScheme
        return PreviewColors(
            surfaceContainerLow = m.surface,
            outlineVariant = m.onSurfaceVariantActions.copy(alpha = 0.35f),
            surfaceContainer = m.surfaceContainer,
            surfaceContainerHighest = m.surfaceContainerHighest,
            onSurface = m.onSurface,
            onSurfaceVariant = m.onSurfaceVariantSummary,
            primary = m.primary,
            onPrimary = m.onPrimary,
            tertiary = m.primary,
            primaryContainer = m.tertiaryContainer,
            onPrimaryContainer = m.onTertiaryContainer,
            secondaryContainer = m.secondaryContainer,
            onSecondaryContainer = m.onSecondaryContainer,
        )
    }
    val c = MaterialTheme.colorScheme
    return PreviewColors(
        surfaceContainerLow = c.surfaceContainerLow,
        outlineVariant = c.outlineVariant,
        surfaceContainer = c.surfaceContainer,
        surfaceContainerHighest = c.surfaceContainerHighest,
        onSurface = c.onSurface,
        onSurfaceVariant = c.onSurfaceVariant,
        primary = c.primary,
        onPrimary = c.onPrimary,
        tertiary = c.tertiary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.onPrimaryContainer,
        secondaryContainer = c.secondaryContainer,
        onSecondaryContainer = c.onSecondaryContainer,
    )
}

private val PreviewDesignWidth = 88.dp

/**
 * A phone-shaped mock of the real home screen, drawn from the live color scheme and
 * [ThemePreviewState]. It is laid out once at a fixed design size and scaled to whatever room it
 * gets, so the compact card and the enlarged dialog show the same thing.
 */
@Composable
internal fun MiniPhonePreview(state: ThemePreviewState, modifier: Modifier = Modifier) {
    val designHeight = PreviewDesignWidth * 19f / 9f
    Box(
        modifier = modifier
            .aspectRatio(9f / 19f)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints.fixed(PreviewDesignWidth.roundToPx(), designHeight.roundToPx())
                )
                val scale = constraints.maxWidth.toFloat() / placeable.width
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.placeWithLayer(0, 0) {
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = scale
                        scaleY = scale
                    }
                }
            }
    ) {
        PhoneContent(state)
    }
}

@Composable
private fun PhoneContent(state: ThemePreviewState) {
    val colors = previewColors()
    val cardAlpha = state.cardAlpha.coerceIn(0.1f, 1f)
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(2.dp, colors.outlineVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.backgroundUri != null) {
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
                    .padding(horizontal = 7.dp)
                    .padding(top = 28.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (ThemeConfig.uiStyle == UiStyle.MIUIX) {
                    if (state.dashboardLayout) {
                        MiuixDashboardHome(state, cardAlpha)
                    } else {
                        MiuixCircleHome(state, cardAlpha)
                    }
                } else if (state.dashboardLayout) {
                    DashboardHome(state, cardAlpha)
                } else {
                    CircleHome(state, cardAlpha)
                }

                if (!state.simpleMode && !state.hideLinkCard) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceContainer.copy(alpha = cardAlpha))
                    )
                }
            }

            // Top bar: translucent when blur is on, solid otherwise
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(
                        colors.surfaceContainer.copy(alpha = if (state.topBarBlur) 0.45f else cardAlpha)
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Bar(28.dp, 5.dp, colors.onSurface)
                Dot(5.dp, colors.onSurfaceVariant)
            }

            PreviewBottomBar(state, Modifier.align(Alignment.BottomCenter))
        }
    }
}

/** Colors of the Miuix home status card, same rules as the real page. */
private class MiuixPreviewStatusColors(
    val card: Color,
    val accent: Color,
    val lkmChip: Color,
    val title: Color,
    val subtitle: Color,
)

@Composable
private fun miuixPreviewStatusColors(colors: PreviewColors): MiuixPreviewStatusColors {
    val monet = ThemeConfig.miuixMonet
    val isDark = ThemeConfig.forceDarkMode ?: isSystemInDarkTheme()
    val accent = if (monet) colors.primary else Color(0xFF36D167)
    return MiuixPreviewStatusColors(
        card = when {
            monet -> colors.secondaryContainer
            isDark -> Color(0xFF1A3825)
            else -> Color(0xFFDFFAE4)
        },
        accent = accent,
        lkmChip = accent,
        title = colors.onSurface,
        subtitle = colors.onSurfaceVariant.copy(alpha = 0.7f),
    )
}

@Composable
private fun MiuixPreviewChips(status: MiuixPreviewStatusColors) {
    Box(
        modifier = Modifier
            .size(width = 10.dp, height = 4.5.dp)
            .clip(RoundedCornerShape(1.5.dp))
            .background(status.lkmChip)
    )
}

/** One info card of the Miuix home: leading icon dots (optional) plus two text lines per row. */
@Composable
private fun MiuixPreviewInfoCard(
    rows: Int,
    showIcons: Boolean,
    cardAlpha: Float,
    colors: PreviewColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(colors.surfaceContainer.copy(alpha = cardAlpha))
            .padding(horizontal = 5.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(rows) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                if (showIcons) Dot(6.dp, colors.onSurfaceVariant.copy(alpha = 0.7f))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Bar(24.dp, 3.dp, colors.onSurface.copy(alpha = 0.8f))
                    Bar(38.dp, 2.5.dp, colors.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

/** Miuix Dashboard UI: big status card with ring, two counter tiles, grouped info cards. */
@Composable
private fun MiuixDashboardHome(state: ThemePreviewState, cardAlpha: Float) {
    val colors = previewColors()
    val status = miuixPreviewStatusColors(colors)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(status.card.copy(alpha = cardAlpha))
    ) {
        // Ring with a check, partly cut off by the card edge like the real hero
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 5.dp, y = 5.dp)
                .size(24.dp)
                .border(2.5.dp, status.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = status.accent,
                modifier = Modifier.size(14.dp),
            )
        }
        Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            Box(modifier = Modifier.align(Alignment.TopStart)) { MiuixPreviewChips(status) }
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Bar(25.dp, 5.5.dp, status.title)
                Bar(24.dp, 2.5.dp, status.subtitle)
            }
        }
    }

    if (!state.hideOtherInfo) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(2) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surfaceContainer.copy(alpha = cardAlpha))
                        .padding(horizontal = 5.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Bar(9.dp, 5.dp, colors.onSurface)
                    Bar(16.dp, 2.5.dp, colors.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }
    }

    // The dashboard layout always shows leading icons, like the real page
    MiuixPreviewInfoCard(if (state.simpleMode) 2 else 3, true, cardAlpha, colors)
    if (!state.simpleMode) {
        MiuixPreviewInfoCard(2, true, cardAlpha, colors)
    }
}

/** Miuix Circle UI: compact status card (no ring), then grouped info cards. */
@Composable
private fun MiuixCircleHome(state: ThemePreviewState, cardAlpha: Float) {
    val colors = previewColors()
    val status = miuixPreviewStatusColors(colors)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(status.card.copy(alpha = cardAlpha))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MiuixPreviewChips(status)
        Bar(24.dp, 5.dp, status.title)
        Bar(34.dp, 2.5.dp, status.subtitle)
    }

    MiuixPreviewInfoCard(if (state.simpleMode) 2 else 3, state.showHomeCardIcons, cardAlpha, colors)
    if (!state.simpleMode) {
        MiuixPreviewInfoCard(2, state.showHomeCardIcons, cardAlpha, colors)
    }
}

/** Dashboard UI: banner hero with stats, two counters, one info card with icons. */
@Composable
private fun DashboardHome(state: ThemePreviewState, cardAlpha: Float) {
    val colors = previewColors()
    val heroText = if (state.hasBanner) colors.onPrimary else colors.onPrimaryContainer
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                if (state.hasBanner) Brush.linearGradient(listOf(colors.primary, colors.tertiary))
                else Brush.linearGradient(listOf(colors.primaryContainer, colors.primaryContainer))
            )
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(modifier = Modifier.size(9.dp).border(1.5.dp, heroText, CircleShape))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Bar(26.dp, 4.dp, heroText)
                Bar(18.dp, 3.dp, heroText.copy(alpha = 0.7f))
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Bar(14.dp, 2.5.dp, heroText.copy(alpha = 0.7f))
                    Bar(17.dp, 3.5.dp, heroText)
                }
            }
        }
    }

    if (!state.hideOtherInfo) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(2) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(15.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surfaceContainer.copy(alpha = cardAlpha))
                        .padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Dot(7.dp, colors.primary.copy(alpha = 0.8f))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Bar(16.dp, 3.dp, colors.onSurface.copy(alpha = 0.8f))
                        Bar(8.dp, 2.5.dp, colors.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(colors.surfaceContainer.copy(alpha = cardAlpha))
            .padding(horizontal = 5.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(if (state.simpleMode) 2 else 3) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                // The dashboard info card always shows leading icons
                Dot(6.dp, colors.onSurfaceVariant.copy(alpha = 0.7f))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Bar(24.dp, 3.dp, colors.onSurface.copy(alpha = 0.8f))
                    Bar(38.dp, 2.5.dp, colors.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

/** Circle UI: compact status card with chips, then titled groups of separate rows. */
@Composable
private fun CircleHome(state: ThemePreviewState, cardAlpha: Float) {
    val colors = previewColors()
    val onCard = colors.onPrimaryContainer
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.primaryContainer.copy(alpha = cardAlpha))
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(modifier = Modifier.size(10.dp).border(1.5.dp, onCard, CircleShape))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Bar(18.dp, 4.dp, onCard)
                Box(
                    modifier = Modifier
                        .size(width = 9.dp, height = 5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(onCard.copy(alpha = 0.25f))
                )
                Box(
                    modifier = Modifier
                        .size(width = 15.dp, height = 5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(onCard.copy(alpha = 0.25f))
                )
            }
            Bar(28.dp, 3.dp, onCard.copy(alpha = 0.7f))
        }
    }

    repeat(2) { group ->
        Bar(18.dp, 3.dp, colors.primary)
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            repeat(if (state.simpleMode) 1 else 2) { index ->
                val last = if (state.simpleMode) 0 else 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = if (index == 0) 6.dp else 2.dp,
                                topEnd = if (index == 0) 6.dp else 2.dp,
                                bottomStart = if (index == last) 6.dp else 2.dp,
                                bottomEnd = if (index == last) 6.dp else 2.dp,
                            )
                        )
                        .background(colors.surfaceContainer.copy(alpha = cardAlpha))
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (state.showHomeCardIcons) {
                        Dot(6.dp, colors.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Bar(if (group == 0) 22.dp else 26.dp, 3.dp, colors.onSurface.copy(alpha = 0.8f))
                        Bar(34.dp, 2.5.dp, colors.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

/**
 * Standard: solid capsule, selected item is a pill with icon + label.
 * Liquid glass: translucent capsule with a light edge, selected item is just a lit circle.
 */
@Composable
private fun PreviewBottomBar(state: ThemePreviewState, modifier: Modifier = Modifier) {
    val colors = previewColors()
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .fillMaxWidth()
            .height(18.dp)
            .clip(CircleShape)
            .background(
                if (state.liquidGlass) colors.surfaceContainerHighest.copy(alpha = 0.4f)
                else colors.surfaceContainerHighest
            )
            .then(
                if (state.liquidGlass) Modifier.border(
                    1.dp, colors.onSurface.copy(alpha = 0.25f), CircleShape
                ) else Modifier
            )
            .padding(horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (state.liquidGlass) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(colors.onSurface.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Dot(5.dp, colors.primary)
            }
        } else {
            Row(
                modifier = Modifier
                    .height(13.dp)
                    .clip(CircleShape)
                    .background(colors.secondaryContainer)
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Dot(5.dp, colors.onSecondaryContainer)
                Bar(9.dp, 2.5.dp, colors.onSecondaryContainer)
            }
        }
        repeat(3) { index ->
            Box(contentAlignment = Alignment.TopEnd) {
                Dot(6.dp, colors.onSurfaceVariant)
                if (index < 2 && state.showNavBadge && !state.hideOtherInfo) {
                    Dot(4.dp, colors.primary)
                }
            }
        }
    }
}

@Composable
private fun Dot(size: Dp, color: Color) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun Bar(width: Dp, height: Dp, color: Color) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
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
