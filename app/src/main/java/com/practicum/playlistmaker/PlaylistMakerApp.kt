package com.practicum.playlistmaker

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.core.di.Creator
import com.practicum.playlistmaker.settings.data.repository.SettingsConstants

class PlaylistMakerApp : Application() {

    var darkTheme: Boolean = false
        private set

    private val sharedPreferences by lazy {
        getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        // Сохраняем контекст приложения в Creator, чтобы он мог создавать объекты
        Creator.setContext(this)

        // Загружаем тему при запуске приложения
        darkTheme = sharedPreferences.getBoolean(SettingsConstants.DARK_THEME_KEY, false)
        applyTheme(darkTheme)
    }

    fun switchTheme(isDarkTheme: Boolean) {
        darkTheme = isDarkTheme
        sharedPreferences.edit().putBoolean(SettingsConstants.DARK_THEME_KEY, isDarkTheme).apply()
        applyTheme(isDarkTheme)
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
