package com.nadekosu.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.dynamiccolor.ColorSpec
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

/**
 * Miuix theme driven by the same settings as the Material one (mode, dynamic color, seed color,
 * palette style, color spec), so both styles always show the same palette.
 */
@Composable
fun NadekoMiuixTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = ThemeConfig.forceDarkMode ?: isSystemInDarkTheme()

    val paletteStyle = try {
        ThemePaletteStyle.valueOf(ThemeConfig.dynamicPaletteStyle.name)
    } catch (_: Exception) {
        ThemePaletteStyle.TonalSpot
    }
    val colorSpec = if (ThemeConfig.dynamicColorSpec == ColorSpec.SpecVersion.SPEC_2025) {
        ThemeColorSpec.Spec2025
    } else {
        ThemeColorSpec.Spec2021
    }

    val keyColor: Color = if (ThemeConfig.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
    } else {
        Color(ThemeConfig.seedColor)
    }

    val controller = ThemeController(
        when (ThemeConfig.forceDarkMode) {
            null -> ColorSchemeMode.MonetSystem
            false -> ColorSchemeMode.MonetLight
            true -> ColorSchemeMode.MonetDark
        },
        keyColor = keyColor,
        isDark = darkTheme,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

    MiuixTheme(
        controller = controller,
        content = {
            CompositionLocalProvider(
                LocalContentColor provides MiuixTheme.colorScheme.onBackground,
            ) {
                content()
            }
        }
    )
}
