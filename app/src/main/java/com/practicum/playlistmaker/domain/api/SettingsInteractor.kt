package com.practicum.playlistmaker.domain.api

interface SettingsInteractor {

    // Получить текущую тему
    fun getDarkTheme(): Boolean

    // Установить тему
    fun setDarkTheme(enabled: Boolean)

    // Поделиться приложением
    fun shareApp()

    // Написать в поддержку
    fun support()

    // Открыть условия использования
    fun openTerms()
}
