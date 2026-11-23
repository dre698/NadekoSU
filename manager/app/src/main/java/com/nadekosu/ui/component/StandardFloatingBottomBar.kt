package com.nadekosu.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadekosu.ui.screen.BottomBarDestination
import kotlin.math.roundToInt

/**
 * Floating capsule used whenever the liquid glass bar is not available (blur off, or Android 11
 * and older): a raised surface with a selection pill that slides between destinations.
 *
 * The pill position is a single animated float read in the layout phase, so quickly tapping
 * through the tabs only retargets the animation and never recomposes the bar per frame.
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

    val itemWidth = if (showLabels) 64.dp else 56.dp
    val itemHeight = 56.dp
    val itemSpacing = 4.dp
    val containerPadding = 7.dp
    val itemShape = MaterialTheme.shapes.large
    val density = LocalDensity.current

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "standardBarIndicator",
    )
    val stepPx = with(density) { (itemWidth + itemSpacing).toPx() }
    val barColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Surface(
        modifier = modifier,
        shape = itemShape,
        color = barColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
    ) {
        Box(modifier = Modifier.padding(containerPadding)) {
            Box(
                modifier = Modifier
                    .offset { IntOffset((stepPx * animatedIndex).roundToInt(), 0) }
                    .size(itemWidth, itemHeight)
                    .background(MaterialTheme.colorScheme.secondaryContainer, itemShape)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEachIndexed { index, destination ->
                    val selected = index == selectedIndex
                    val tint = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val count = badgeCount(destination)
                    Column(
                        modifier = Modifier
                            .size(itemWidth, itemHeight)
                            .clip(itemShape)
                            .selectable(
                                selected = selected,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Tab,
                                onClick = { if (!selected) onSelect(index) },
                            ),
                        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
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
                                            shape = RoundedCornerShape(50),
                                        ),
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ) { Text(count.toString()) }
                                }
                            ) { icon() }
                        } else {
                            icon()
                        }
                        if (showLabels) {
                            Text(
                                text = stringResource(destination.label),
                                color = tint,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Visible,
                            )
                        }
                    }
                }
            }
        }
    }
}
