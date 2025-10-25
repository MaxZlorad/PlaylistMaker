package com.practicum.playlistmaker.settings.domain.api

interface SettingsInteractor {

    // Получить текущую тему
    suspend fun getDarkTheme(): Boolean

    // Установить тему
    suspend fun setDarkTheme(enabled: Boolean)
}
