package com.practicum.playlistmaker.domain.api

interface SettingsRepository {

    // Получить текущую тему (true - темная, false - светлая)
    fun getDarkTheme(): Boolean

    // Установить тему
    fun setDarkTheme(enabled: Boolean)
}