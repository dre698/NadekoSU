package com.nadekosu.ui

import com.nadekosu.domain.model.StartupState

internal fun shouldKeepStartupSplash(
    startupState: StartupState,
    homeInitialDataLoaded: Boolean,
): Boolean = when (startupState) {
    StartupState.Loading -> true
    StartupState.Ready -> !homeInitialDataLoaded
    is StartupState.Failed -> false
}
