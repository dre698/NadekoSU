package com.nadekosu.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.nadekosu.ui.screen.BottomBarDestination
import kotlin.math.roundToInt

private val BarHeight = 64.dp
private val BarPadding = 4.dp
private val BarMaxWidth = 400.dp
private val IconSize = 24.dp
private val LabelGap = 8.dp

/** The spring may overshoot a little past 1 for a soft bounce, but never below 0. */
private const val MaxExpansion = 1.1f

/**
 * Floating capsule used whenever the liquid glass bar is not available (blur off, or Android 11
 * and older).
 *
 * The selected item is a pill that hugs its content (icon + label) with even padding around it.
 * Whatever width is left over is shared equally as padding by every item, so the other icons slide
 * a little sideways to make room instead of staying in fixed slots. With labels off every item has
 * the same width.
 *
 * Widths are worked out in the layout pass from the animated expansion of each item, and the label
 * widths are measured up front, so nothing here recomposes per frame and quick taps through the
 * tabs just retarget the running animations.
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

    val labels = destinations.map { stringResource(it.label) }
    val labelStyle = MaterialTheme.typography.labelLarge
    val textMeasurer = rememberTextMeasurer()
    val labelWidths = remember(labels, labelStyle, textMeasurer) {
        labels.map { textMeasurer.measure(text = it, style = labelStyle, softWrap = false, maxLines = 1).size.width }
    }

    val expansions = destinations.indices.map { index ->
        animateFloatAsState(
            targetValue = if (index == selectedIndex && showLabels) 1f else 0f,
            animationSpec = spec,
            label = "navItemExpansion",
        )
    }
    val pillAlphas = destinations.indices.map { index ->
        animateFloatAsState(
            targetValue = if (index == selectedIndex) 1f else 0f,
            animationSpec = spec,
            label = "navItemPill",
        )
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
            Layout(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BarHeight)
                    .padding(BarPadding)
                    .selectableGroup(),
                content = {
                    destinations.forEachIndexed { index, destination ->
                        NavItem(
                            destination = destination,
                            label = labels[index],
                            selected = index == selectedIndex,
                            showLabels = showLabels,
                            count = badgeCount(destination),
                            expansion = expansions[index],
                            pillAlpha = pillAlphas[index],
                            pillColor = pillColor,
                            barColor = barColor,
                            onClick = { if (index != selectedIndex) onSelect(index) },
                        )
                    }
                },
            ) { measurables, constraints ->
                val n = measurables.size
                val total = constraints.maxWidth
                val height = constraints.maxHeight
                val iconPx = IconSize.roundToPx()
                val gapPx = LabelGap.roundToPx()

                // What each item needs for its icon and (while it is open) its label.
                val contents = FloatArray(n) { i ->
                    val e = expansions[i].value.coerceIn(0f, MaxExpansion)
                    iconPx + (labelWidths[i] + gapPx) * e
                }
                // The leftover width is shared as equal side padding by every item.
                val pad = ((total - contents.sum()) / (2f * n)).coerceAtLeast(0f)
                val widths = FloatArray(n) { contents[it] + 2f * pad }
                val widthsSum = widths.sum()
                val fit = if (widthsSum > total) total / widthsSum else 1f

                val bounds = IntArray(n + 1)
                var acc = 0f
                for (i in 0 until n) {
                    acc += widths[i] * fit
                    bounds[i + 1] = if (i == n - 1) total else acc.roundToInt()
                }

                val placeables = measurables.mapIndexed { i, measurable ->
                    measurable.measure(Constraints.fixed((bounds[i + 1] - bounds[i]).coerceAtLeast(0), height))
                }
                layout(total, height) {
                    placeables.forEachIndexed { i, placeable -> placeable.placeRelative(bounds[i], 0) }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    destination: BottomBarDestination,
    label: String,
    selected: Boolean,
    showLabels: Boolean,
    count: Int,
    expansion: State<Float>,
    pillAlpha: State<Float>,
    pillColor: Color,
    barColor: Color,
    onClick: () -> Unit,
) {
    val tint = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRoundRect(
                    color = pillColor,
                    cornerRadius = CornerRadius(size.height / 2f),
                    alpha = pillAlpha.value.coerceIn(0f, 1f),
                )
            }
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon: @Composable () -> Unit = {
            Icon(
                imageVector = if (selected) destination.iconSelected else destination.iconNotSelected,
                contentDescription = label,
                modifier = Modifier.size(IconSize),
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
            // The label's width follows the expansion, so the icon glides to its place instead of
            // jumping when the label appears or disappears. Never negative: a negative width
            // crashes the layout.
            Text(
                text = label,
                modifier = Modifier
                    .clipToBounds()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(minWidth = 0))
                        val e = expansion.value.coerceIn(0f, 1f)
                        val width = (placeable.width * e).roundToInt().coerceAtLeast(0)
                        layout(width, placeable.height) { placeable.placeRelative(0, 0) }
                    }
                    .graphicsLayer { alpha = expansion.value.coerceIn(0f, 1f) }
                    .padding(start = LabelGap),
                color = tint,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
