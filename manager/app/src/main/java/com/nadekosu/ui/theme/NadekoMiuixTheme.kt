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
 * Miuix theme. Light/dark follows the app setting. With "Enable Monet" off it is the plain Miuix
 * palette; with it on, the page is tinted from the app's theme color, palette style and spec.
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

    val monet = ThemeConfig.miuixMonet
    val mode = when (ThemeConfig.forceDarkMode) {
        null -> if (monet) ColorSchemeMode.MonetSystem else ColorSchemeMode.System
        false -> if (monet) ColorSchemeMode.MonetLight else ColorSchemeMode.Light
        true -> if (monet) ColorSchemeMode.MonetDark else ColorSchemeMode.Dark
    }
    val controller = ThemeController(
        mode,
        keyColor = if (monet) keyColor else null,
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
