package com.nadekosu.data

/**
 * Tokens persisted under [PREF_KEY]. Unknown or missing values fall back to [CIRCLE].
 */
object HomeLayoutStyle {
    const val PREF_KEY = "home_layout_style"
    const val BANNER_PREF_KEY = "home_banner_file"

    /** The original single-column home (Circle UI / Legacy). */
    const val CIRCLE = "circle"

    /** Hero banner with a custom image, counters and the info cards. */
    const val DASHBOARD = "dashboard_ui"

    const val DEFAULT = CIRCLE

    val supported: List<String> = listOf(CIRCLE, DASHBOARD)

    fun fromValue(value: String?): String = if (value == DASHBOARD) DASHBOARD else CIRCLE
}
