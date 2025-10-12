package com.practicum.playlistmaker.settings.domain.api

interface SettingsRepository {

    // Получить текущую тему (true - темная, false - светлая)
    fun getDarkTheme(): Boolean

    // Установить тему
    fun setDarkTheme(enabled: Boolean)
}