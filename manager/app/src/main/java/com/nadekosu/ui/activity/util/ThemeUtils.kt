package com.nadekosu.ui.activity.util

import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings
import com.nadekosu.data.appPreferences
import com.nadekosu.ui.MainActivity
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.viewmodel.SettingsViewModel

class ThemeChangeContentObserver(
    handler: Handler,
    private val onThemeChanged: () -> Unit
) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        onThemeChanged()
    }
}

object ThemeUtils {

    fun initializeThemeSettings(activity: MainActivity, settingsViewModel: SettingsViewModel) {
        settingsViewModel.initialize(activity)
        settingsViewModel.initializeFirstRunSettings(activity)

        loadThemeMode()
        loadThemeSeedColor()
        loadDynamicColorState()
        CardConfig.load(activity.applicationContext)
    }

    fun registerThemeChangeObserver(activity: MainActivity): ThemeChangeContentObserver {
        val contentObserver = ThemeChangeContentObserver(Handler(activity.mainLooper)) {
            activity.runOnUiThread {
                if (!ThemeConfig.preventBackgroundRefresh) {
                    ThemeConfig.backgroundImageLoaded = false
                    loadCustomBackground()
                }
            }
        }

        activity.contentResolver.registerContentObserver(
            Settings.System.getUriFor("ui_night_mode"),
            false,
            contentObserver
        )

        return contentObserver
    }

    fun unregisterThemeChangeObserver(activity: MainActivity, observer: ThemeChangeContentObserver) {
        activity.contentResolver.unregisterContentObserver(observer)
    }

    fun onActivityPause(activity: MainActivity) {
        CardConfig.save(activity.applicationContext)
        activity.appPreferences.putBoolean("prevent_background_refresh", true)
        ThemeConfig.preventBackgroundRefresh = true
    }

    fun onActivityResume() {
        if (!ThemeConfig.backgroundImageLoaded && !ThemeConfig.preventBackgroundRefresh) {
            loadCustomBackground()
        }
    }

    private fun loadThemeMode() {
    }

    private fun loadThemeSeedColor() {
    }

    private fun loadDynamicColorState() {
    }

    private fun loadCustomBackground() {
    }
}
