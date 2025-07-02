package com.practicum.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val PREFS_NAME = "app_settings"
    }

    var darkTheme = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        loadPreferences()
        switchTheme(darkTheme)
    }

    private fun loadPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        darkTheme = sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        sharedPreferences.edit()
            .putBoolean(KEY_DARK_THEME, darkThemeEnabled)
            .apply()
    }
}
