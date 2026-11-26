package com.nadekosu.ui.component.miuix

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Horizontally scrollable tab row in the Miuix look: the selected tab is a filled capsule, the
 * others are outlined. Tabs keep their full label (no ellipsis), and the row scrolls the selected
 * tab into view. Unlike the stock TabRow, the selection is drawn on the tab itself, so it can't
 * drift from the tab when the row is scrolled.
 */
@Composable
fun MiuixScrollableTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    val listState = rememberLazyListState()
    val shape = RoundedCornerShape(12.dp)

    LaunchedEffect(selectedTabIndex, tabs.size) {
        if (selectedTabIndex !in tabs.indices) return@LaunchedEffect
        val info = listState.layoutInfo
        val item = info.visibleItemsInfo.firstOrNull { it.index == selectedTabIndex }
        if (item == null) {
            listState.animateScrollToItem(selectedTabIndex)
        } else {
            val viewport = info.viewportEndOffset - info.viewportStartOffset
            val delta = item.offset - (viewport - item.size) / 2
            listState.animateScrollBy(delta.toFloat())
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(tabs) { index, title ->
            val selected = index == selectedTabIndex
            val background by animateColorAsState(
                targetValue = if (selected) colors.surfaceContainer else Color.Transparent,
                label = "tabBackground",
            )
            val borderColor by animateColorAsState(
                targetValue = if (selected) Color.Transparent else colors.outline.copy(alpha = 0.6f),
                label = "tabBorder",
            )
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(shape)
                    .background(background)
                    .border(BorderStroke(1.dp, borderColor), shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) { onTabSelected(index) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    color = if (selected) colors.onSurface else colors.onSurfaceVariantSummary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}
