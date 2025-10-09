package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.practicum.playlistmaker.domain.api.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun getDarkTheme(): Boolean {
        // Получаем значение темы из SharedPreferences
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }

    override fun setDarkTheme(enabled: Boolean) {
        // Сохраняем значение темы в SharedPreferences
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
    }

}