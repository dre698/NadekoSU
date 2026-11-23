package com.nadekosu.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.nadekosu.ui.screen.BottomBarDestination

private val ItemMinWidth = 48.dp
private val ItemSpacing = 4.dp
private val BarPadding = 8.dp
private val BarMaxWidth = 400.dp

/**
 * Floating capsule used whenever the liquid glass bar is not available (blur off, or Android 11
 * and older). The bar spans the screen width; the selected item grows into a pill that holds its
 * label, every other item stays an icon.
 *
 * Each item animates its own expansion and pill fade from where it currently is, so tapping
 * quickly through the tabs retargets the running animations instead of restarting them, and
 * nothing here recomposes per frame (the weight and the pill alpha are read in layout and draw).
 */
@Composable
fun StandardFloatingBottomBar(
    destinations: List<BottomBarDestination>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    showLabels: Boolean,
    badgeCount: (BottomBarDestination) -> Int,
    modifier: Modifier = Modifier,
) {
    if (destinations.isEmpty()) return

    val barColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val pillColor = MaterialTheme.colorScheme.secondaryContainer
    val spec = remember {
        spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val barWidth: Dp = maxWidth.coerceAtMost(BarMaxWidth)
        // Space left for the selected item once every other item has its minimum width.
        val extraWidth = (barWidth - BarPadding * 2 - ItemSpacing * (destinations.size - 1) -
            ItemMinWidth * destinations.size).coerceAtLeast(0.dp)
        val openWeight = if (showLabels) 1f + extraWidth / ItemMinWidth else 1f

        Surface(
            modifier = Modifier.width(barWidth),
            shape = CircleShape,
            color = barColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .padding(BarPadding)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(ItemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEachIndexed { index, destination ->
                    val selected = index == selectedIndex
                    val expansion by animateFloatAsState(
                        targetValue = if (selected && showLabels) 1f else 0f,
                        animationSpec = spec,
                        label = "navItemExpansion",
                    )
                    val pillAlpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = spec,
                        label = "navItemPill",
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.9f,
                        animationSpec = spec,
                        label = "navItemIconScale",
                    )
                    val tint = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val count = badgeCount(destination)

                    Row(
                        modifier = Modifier
                            .weight(1f + (openWeight - 1f) * expansion)
                            .height(44.dp)
                            .clip(CircleShape)
                            .drawBehind { drawRect(pillColor, alpha = pillAlpha) }
                            .selectable(
                                selected = selected,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Tab,
                                onClick = { if (!selected) onSelect(index) },
                            )
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val icon: @Composable () -> Unit = {
                            Icon(
                                imageVector = if (selected) destination.iconSelected else destination.iconNotSelected,
                                contentDescription = stringResource(destination.label),
                                modifier = Modifier.graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                },
                                tint = tint,
                            )
                        }
                        if (count > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        modifier = Modifier.border(
                                            width = 1.5.dp,
                                            color = barColor,
                                            shape = CircleShape,
                                        ),
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ) { Text(count.toString()) }
                                }
                            ) { icon() }
                        } else {
                            icon()
                        }
                        if (expansion > 0f && extraWidth > 0.dp) {
                            Text(
                                text = stringResource(destination.label),
                                modifier = Modifier
                                    .padding(start = 8.dp * expansion)
                                    .graphicsLayer { alpha = expansion },
                                color = tint,
                                style = MaterialTheme.typography.labelLarge,
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
