package com.nadekosu.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Copyright
import androidx.compose.material.icons.rounded.Group
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.nadekosu.BuildConfig
import com.nadekosu.R
import com.nadekosu.ui.component.miuix.MiuixGroupCard
import com.nadekosu.ui.component.miuix.MiuixListPage
import com.nadekosu.ui.component.miuix.MiuixLoadingBox
import com.nadekosu.ui.component.miuix.MiuixStatusTag
import com.nadekosu.ui.component.miuix.MiuixWarningCard
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.navigation.Route
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow

@Composable
private fun MiuixAboutIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.padding(end = 6.dp),
        tint = MiuixTheme.colorScheme.onBackground,
    )
}

@Composable
internal fun MiuixAboutScreen() {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    MiuixListPage(title = stringResource(R.string.about)) {
        item {
            MiuixGroupCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Image(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        painter = rememberDrawablePainter(
                            drawable = ContextCompat.getDrawable(LocalContext.current, R.mipmap.ic_launcher)
                        ),
                        contentDescription = stringResource(R.string.app_name),
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                MiuixWarningCard(
                    message = stringResource(
                        R.string.about_anime_character_sticker,
                        "怡子曰曰",
                        "明风 OuO",
                        "CC BY-NC-SA 4.0",
                    ),
                    neutral = true,
                    onClick = {
                        uriHandler.openUri("https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode.txt")
                    },
                )
            }
        }
        item {
            MiuixGroupCard {
                ArrowPreference(
                    title = stringResource(R.string.get_source_code),
                    summary = stringResource(R.string.get_source_code_detail),
                    startAction = { MiuixAboutIcon(Icons.Rounded.Code) },
                    onClick = { uriHandler.openUri("https://github.com/dre698/NadekoSU") },
                )
                ArrowPreference(
                    title = stringResource(R.string.join_telegram_group),
                    summary = stringResource(R.string.join_telegram_group_detail),
                    startAction = { MiuixAboutIcon(Icons.Rounded.Group) },
                    onClick = { uriHandler.openUri("https://t.me/dre698trashes") },
                )
                ArrowPreference(
                    title = stringResource(R.string.open_source_license),
                    summary = stringResource(R.string.open_source_license_settings_description),
                    startAction = { MiuixAboutIcon(Icons.Rounded.Copyright) },
                    onClick = { navigator.push(Route.OpenSourceLicense) },
                )
            }
        }
    }
}

@Composable
internal fun MiuixOpenSourceLicenseScreen() {
    val libraries by produceLibraries(R.raw.aboutlibraries)
    var selected by remember { mutableStateOf<Library?>(null) }
    val list = libraries?.libraries

    MiuixListPage(
        title = stringResource(R.string.open_source_license),
        overlay = {
            val library = selected
            val uriHandler = LocalUriHandler.current
            OverlayDialog(
                show = library != null,
                title = library?.name,
                summary = library?.let {
                    stringResource(R.string.license, it.licenses.joinToString(", ") { l -> l.name })
                },
                onDismissRequest = { selected = null },
            ) {
                if (library != null) {
                    Column {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            library.licenses.forEach { license ->
                                Text(
                                    text = license.name,
                                    fontWeight = FontWeight.Medium,
                                    color = MiuixTheme.colorScheme.primary,
                                )
                                Text(
                                    text = license.licenseContent ?: stringResource(R.string.no_license_text),
                                    fontSize = 13.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            val website = library.website
                            if (website != null) {
                                TextButton(
                                    text = stringResource(R.string.visit_home_page),
                                    onClick = { uriHandler.openUri(website) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            TextButton(
                                text = stringResource(R.string.close),
                                onClick = { selected = null },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        }
                    }
                }
            }
        },
    ) {
        if (list == null) {
            item { MiuixLoadingBox(Modifier.heightIn(min = 240.dp)) }
        } else {
            item {
                MiuixGroupCard {
                    list.forEach { library ->
                        ArrowPreference(
                            title = library.name,
                            summary = listOfNotNull(
                                library.artifactVersion,
                                library.developers.firstOrNull()?.name,
                            ).joinToString(" · ").ifEmpty { null },
                            onClick = { selected = library },
                        )
                    }
                }
            }
        }
    }

}
