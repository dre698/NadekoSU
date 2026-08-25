package com.nadekosu.ui.component

import androidx.compose.runtime.Composable
import com.nadekosu.domain.model.KernelStatus

@Composable
inline fun KsuIsValid(
    status: KernelStatus,
    content: @Composable () -> Unit
) {
    if (status.isValid)
        content()
}
