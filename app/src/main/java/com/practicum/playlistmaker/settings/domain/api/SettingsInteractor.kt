package com.practicum.playlistmaker.settings.domain.api

interface SettingsInteractor {

    // Получить текущую тему
    suspend fun getDarkTheme(): Boolean

    // Установить тему
    suspend fun setDarkTheme(enabled: Boolean)

    // Поделиться приложением
    suspend fun shareApp()

    // Написать в поддержку
    suspend fun support()

    // Открыть условия использования
    suspend fun openTerms()
}
