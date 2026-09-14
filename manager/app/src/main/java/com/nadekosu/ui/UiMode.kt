package com.nadekosu.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Global UI style: Material (Nadeko's native Material 3 look) or Miuix (MIUI/HyperOS-inspired,
 * ported from KernelSU). Read via [LocalUiMode] from anywhere in the composition; individual
 * screens can branch on it to pick a Material or Miuix implementation, the same way KernelSU's
 * manager does with its own `LocalUiMode`.
 *
 * Only components that have an actual Miuix variant honor this - anything without one keeps
 * rendering as Material regardless of this setting.
 */
enum class UiMode(val value: String) {
    Material("material"),
    Miuix("miuix");

    companion object {
        fun fromValue(value: String): UiMode = when (value) {
            Miuix.value -> Miuix
            else -> Material
        }

        val DEFAULT_VALUE = Material.value
    }
}

val LocalUiMode = staticCompositionLocalOf { UiMode.Material }
