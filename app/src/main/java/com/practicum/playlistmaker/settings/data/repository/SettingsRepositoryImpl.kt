package com.practicum.playlistmaker.settings.data.repository

import android.content.SharedPreferences
import com.practicum.playlistmaker.settings.domain.api.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences) : SettingsRepository {

    override fun getDarkTheme(): Boolean {
        // Получаем значение темы из SharedPreferences
        return sharedPreferences.getBoolean(SettingsConstants.DARK_THEME_KEY, false)
    }

    override fun setDarkTheme(enabled: Boolean) {
        // Сохраняем значение темы в SharedPreferences
        sharedPreferences.edit().putBoolean(SettingsConstants.DARK_THEME_KEY, enabled).apply()
    }
}