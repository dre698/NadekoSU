package com.nadekosu.data

/** Which component set the theme settings page is drawn with. */
object UiStyle {
    const val PREF_KEY = "ui_style"
    const val MATERIAL = "material"
    const val MIUIX = "miuix"
    const val DEFAULT = MATERIAL

    val supported: List<String> = listOf(MATERIAL, MIUIX)

    fun fromValue(value: String?): String = if (value == MIUIX) MIUIX else MATERIAL
}
