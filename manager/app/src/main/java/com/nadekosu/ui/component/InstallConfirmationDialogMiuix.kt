package com.nadekosu.ui.component

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nadekosu.R
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun InstallConfirmationDialogMiuix(
    show: Boolean,
    zipFiles: List<ZipFileInfo>,
    onConfirm: (List<ZipFileInfo>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val title = if (zipFiles.size == 1) {
        stringResource(R.string.confirm_installation)
    } else {
        stringResource(R.string.confirm_multiple_installation, zipFiles.size)
    }

    WindowDialog(
        show = show && zipFiles.isNotEmpty(),
        title = title,
        onDismissRequest = onDismiss,
        content = {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    zipFiles.forEach { zipFile ->
                        MiuixInstallItem(context = context, zipFile = zipFile)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    TextButton(
                        text = stringResource(android.R.string.cancel),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(20.dp))
                    TextButton(
                        text = stringResource(R.string.install_confirm),
                        onClick = { onConfirm(zipFiles) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    )
}

@Composable
private fun MiuixInstallItem(context: Context, zipFile: ZipFileInfo) {
    val colors = MiuixTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = zipFile.name.ifEmpty {
                when (zipFile.type) {
                    ZipType.MODULE -> context.getString(R.string.unknown_module)
                    ZipType.KERNEL -> context.getString(R.string.unknown_kernel)
                    else -> context.getString(R.string.unknown_file)
                }
            },
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
        )
        Text(
            text = when (zipFile.type) {
                ZipType.MODULE -> context.getString(R.string.module_package)
                ZipType.KERNEL -> context.getString(R.string.kernel_package)
                else -> context.getString(R.string.unknown_package)
            },
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = colors.onSurfaceVariantSummary,
        )
        if (zipFile.version.isNotEmpty()) {
            MiuixInfoRow(
                label = context.getString(R.string.version),
                value = zipFile.version + if (zipFile.versionCode.isNotEmpty()) " (${zipFile.versionCode})" else ""
            )
        }
        if (zipFile.author.isNotEmpty()) {
            MiuixInfoRow(label = context.getString(R.string.author), value = zipFile.author)
        }
        if (zipFile.description.isNotEmpty() && zipFile.type == ZipType.MODULE) {
            MiuixInfoRow(label = context.getString(R.string.description), value = zipFile.description)
        }
        if (zipFile.supported.isNotEmpty() && zipFile.type == ZipType.KERNEL) {
            MiuixInfoRow(label = context.getString(R.string.supported_devices), value = zipFile.supported)
        }
    }
}

@Composable
private fun MiuixInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(
            text = "$label:",
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}
