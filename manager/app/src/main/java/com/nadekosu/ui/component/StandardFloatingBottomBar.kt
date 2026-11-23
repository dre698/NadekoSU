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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.nadekosu.ui.screen.BottomBarDestination
import kotlin.math.roundToInt

private val BarHeight = 64.dp
private val ItemHeight = 56.dp
private val BarPadding = 4.dp
private val BarMaxWidth = 400.dp

/** The selected item is this many times wider than the others, which all share the rest evenly. */
private const val SelectedItemWeight = 1.5f

/**
 * Floating capsule used whenever the liquid glass bar is not available (blur off, or Android 11
 * and older). Items share the bar evenly; the selected one is a little wider and becomes a pill
 * holding its label next to the icon.
 *
 * Each item animates its own width and pill from where it currently is, so tapping quickly through
 * the tabs retargets the running animations instead of restarting them. The animated values are
 * read in layout and draw, so nothing here recomposes per frame.
 *
 * Nothing in an item is clipped: a badge with two digits is wider than the icon it hangs on and
 * must be allowed to stick out of the item.
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
        Surface(
            modifier = Modifier.width(maxWidth.coerceAtMost(BarMaxWidth)),
            shape = CircleShape,
            color = barColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .height(BarHeight)
                    .padding(BarPadding)
                    .selectableGroup(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEachIndexed { index, destination ->
                    val selected = index == selectedIndex
                    // The bouncy spring overshoots past 0 and 1; a negative value used to give the
                    // label a negative width and crash, so both are clamped where they are read.
                    val expansionRaw by animateFloatAsState(
                        targetValue = if (selected && showLabels) 1f else 0f,
                        animationSpec = spec,
                        label = "navItemExpansion",
                    )
                    val pillAlphaRaw by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = spec,
                        label = "navItemPill",
                    )
                    val expansion = expansionRaw.coerceIn(0f, 1f)
                    val pillAlpha = pillAlphaRaw.coerceIn(0f, 1f)
                    val tint = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val count = badgeCount(destination)

                    Row(
                        modifier = Modifier
                            .weight(1f + (SelectedItemWeight - 1f) * expansion)
                            .height(ItemHeight)
                            .padding(horizontal = 2.dp)
                            .drawBehind {
                                drawRoundRect(
                                    color = pillColor,
                                    cornerRadius = CornerRadius(size.height / 2f),
                                    alpha = pillAlpha,
                                )
                            }
                            .selectable(
                                selected = selected,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Tab,
                                onClick = { if (!selected) onSelect(index) },
                            ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val icon: @Composable () -> Unit = {
                            Icon(
                                imageVector = if (selected) destination.iconSelected else destination.iconNotSelected,
                                contentDescription = stringResource(destination.label),
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
                                    ) { Text(if (count > 99) "99+" else count.toString()) }
                                }
                            ) { icon() }
                        } else {
                            icon()
                        }
                        if (showLabels) {
                            // The label's width follows the expansion, so the icon glides to its
                            // place instead of jumping when the label appears or disappears.
                            Text(
                                text = stringResource(destination.label),
                                modifier = Modifier
                                    .clipToBounds()
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints.copy(minWidth = 0))
                                        val width = (placeable.width * expansion).roundToInt().coerceAtLeast(0)
                                        layout(width, placeable.height) { placeable.placeRelative(0, 0) }
                                    }
                                    .graphicsLayer { alpha = expansion }
                                    .padding(start = 8.dp),
                                color = tint,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                            )
                        }
                    }
                }
            }
        }
    }
}
