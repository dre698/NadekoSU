package com.nadekosu.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Extension
import androidx.compose.material.icons.twotone.Group
import androidx.compose.material.icons.twotone.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nadekosu.Natives
import com.nadekosu.R
import com.nadekosu.ui.screen.LabelText
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.util.HomeBannerStorage
import com.nadekosu.ui.util.LocalHandlePageChange
import com.nadekosu.ui.viewmodel.HomeUiState

/** Page indices of the Superuser and Module tabs while the kernel module is present. */
private const val PAGE_SUPERUSER = 1
private const val PAGE_MODULE = 2

/**
 * Dashboard UI status area: a hero banner (optionally with a custom image) over the
 * Superuser and Module counters. The info cards below are shared with the legacy layout.
 */
@Composable
internal fun DashboardStatusSection(
    uiState: HomeUiState,
    moduleUpdateCount: Int,
    onClickHero: () -> Unit,
) {
    val onPageChange = LocalHandlePageChange.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DashboardHero(
            uiState = uiState,
            onClick = onClickHero,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardCountCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.TwoTone.Group,
                label = stringResource(R.string.superuser),
                value = uiState.systemInfo.superuserCount.toString(),
                onClick = { onPageChange(PAGE_SUPERUSER) },
            )
            DashboardCountCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.TwoTone.Extension,
                label = stringResource(R.string.module),
                value = uiState.systemInfo.moduleCount.toString(),
                badge = moduleUpdateCount.takeIf { it > 0 }?.let { "↑$it" },
                onClick = { onPageChange(PAGE_MODULE) },
            )
        }
    }
}

@Composable
private fun DashboardHero(
    uiState: HomeUiState,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val status = uiState.systemStatus
    val bannerFile = remember(uiState.homeBannerFile) {
        HomeBannerStorage.fileOf(context, uiState.homeBannerFile)
    }
    val hasImage = bannerFile != null
    val shape = RoundedCornerShape(28.dp)
    val baseColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (hasImage) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
    val subColor = contentColor.copy(alpha = 0.8f)

    val title = if (Natives.isSafeMode) {
        stringResource(R.string.safe_mode)
    } else {
        stringResource(R.string.home_working)
    }
    val versionText = buildString {
        append(status.ksuVersion ?: 0)
        status.kernelUAPIVersion?.let { append("-").append(it) }
    }
    val runMode = if (status.lkmMode == true) "LKM" else "Built-in"

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = baseColor.copy(alpha = CardConfig.cardAlpha),
        contentColor = contentColor,
    ) {
        Box(modifier = Modifier.heightIn(min = 190.dp)) {
            if (bannerFile != null) {
                AsyncImage(
                    model = bannerFile,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
                // Scrim keeps the white text readable on any picture.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.TwoTone.TaskAlt,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (hasImage) Color.White else MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Version: $versionText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = subColor,
                        )
                    }
                    LabelText(
                        label = runMode,
                        containerColor = MaterialTheme.colorScheme.primary,
                    )
                    if (Natives.isLateLoadMode) {
                        Spacer(Modifier.width(6.dp))
                        LabelText(
                            label = stringResource(R.string.jailbreak_mode),
                            containerColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HeroFact(
                        modifier = Modifier.weight(1f),
                        label = "Version",
                        value = versionText,
                        labelColor = subColor,
                    )
                    HeroFact(
                        modifier = Modifier.weight(1.4f),
                        label = stringResource(R.string.home_kernel),
                        value = uiState.systemInfo.kernelRelease,
                        labelColor = subColor,
                    )
                    HeroFact(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.home_selinux_status),
                        value = uiState.systemInfo.selinuxStatus,
                        labelColor = subColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroFact(
    label: String,
    value: String,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DashboardCountCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = CardConfig.cardAlpha),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (badge != null) {
                        Spacer(Modifier.width(6.dp))
                        LabelText(label = badge)
                    }
                }
            }
        }
    }
}
