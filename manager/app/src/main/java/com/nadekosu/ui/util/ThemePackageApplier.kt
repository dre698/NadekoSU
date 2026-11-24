package com.nadekosu.ui.util

import android.content.Context
import android.net.Uri
import com.nadekosu.ui.theme.BackgroundManager
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.viewmodel.HomeUiState
import com.nadekosu.ui.viewmodel.HomeViewModel
import com.nadekosu.ui.viewmodel.SettingsUiState
import com.nadekosu.ui.viewmodel.SettingsViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Reads and writes the theme through the same handlers the settings screen uses, so every
 * state, preference and preview stays in sync with a normal change.
 */
object ThemePackageApplier {

    fun snapshot(settings: SettingsUiState, home: HomeUiState): ThemePackage = ThemePackage(
        themeMode = settings.themeMode,
        useDynamicColor = settings.useDynamicColor,
        seedColor = ThemeConfig.seedColor,
        paletteStyle = settings.dynamicPaletteStyle,
        colorSpec = settings.dynamicColorSpec,
        cardAlpha = CardConfig.cardAlpha,
        moduleBannerAlpha = CardConfig.moduleBannerAlpha,
        backgroundDim = ThemeConfig.backgroundDim,
        enableBlur = ThemeConfig.isEnableBlur,
        enableBlurExp = ThemeConfig.isEnableBlurExp,
        enableLiquidGlass = ThemeConfig.isEnableLiquidGlass,
        useBackgroundSeedColor = ThemeConfig.isUseBackgroundSeedColor,
        highContrast = ThemeConfig.isHighContrastMode,
        homeLayoutStyle = home.homeLayoutStyle,
        showHomeCardIcons = home.showHomeCardIcons,
        showNavigationBarBadge = home.showNavigationBarBadge,
    )

    /** Image the export should carry, or null when no custom background is set. */
    fun currentBackgroundUri(): Uri? = ThemeConfig.customBackgroundUri

    /**
     * Applies [data]. [background] / [banner] are the package's images; null means "none", so an
     * existing background or banner is removed. [isReset] restores automatic card alpha.
     */
    suspend fun apply(
        context: Context,
        data: ThemePackage,
        background: java.io.File?,
        banner: java.io.File?,
        settingsViewModel: SettingsViewModel,
        homeViewModel: HomeViewModel,
        isReset: Boolean = false,
    ) {
        // Colors
        settingsViewModel.handleThemeModeChange(context, data.themeMode)
        settingsViewModel.handleDynamicColorChange(context, data.useDynamicColor)
        settingsViewModel.handleThemeColorChange(context, data.seedColor)
        settingsViewModel.handleDynamicPaletteStyleChange(context, data.paletteStyle)
        settingsViewModel.handleDynamicColorSpecChange(context, data.colorSpec)

        // Background image first: setting it resets blur / dim / alpha, which are restored below.
        if (background != null) {
            settingsViewModel.handleCustomBackground(context, Uri.fromFile(background))
        } else if (ThemeConfig.customBackgroundUri != null) {
            settingsViewModel.handleRemoveCustomBackground(context)
        }

        settingsViewModel.handleBackgroundDimChange(context, data.backgroundDim)
        BackgroundManager.saveEnableBlur(context, data.enableBlur)
        BackgroundManager.saveEnableLiquidGlass(context, data.enableLiquidGlass)
        BackgroundManager.saveEnableBlurExp(context, data.enableBlurExp)
        BackgroundManager.saveUseBackgroundSeedColor(context, data.useBackgroundSeedColor)
        BackgroundManager.saveEnableHighContrastMode(context, data.highContrast)
        settingsViewModel.handleCardAlphaChange(context, data.cardAlpha)
        settingsViewModel.handleModuleBannerAlphaChange(context, data.moduleBannerAlpha)
        if (isReset) CardConfig.isCustomAlphaSet = false
        CardConfig.save(context)

        // Home
        homeViewModel.handleHomeLayoutStyleChange(context, data.homeLayoutStyle)
        homeViewModel.handleHomeCardIconsChange(context, data.showHomeCardIcons)
        homeViewModel.handleNavigationBarBadgeChange(context, data.showNavigationBarBadge)
        if (banner != null) {
            suspendCoroutine<Boolean> { continuation ->
                homeViewModel.handleHomeBannerPicked(context, Uri.fromFile(banner)) {
                    continuation.resume(it)
                }
            }
        } else {
            homeViewModel.handleHomeBannerReset(context)
        }
    }
}
