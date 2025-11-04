package com.practicum.playlistmaker.settings.domain.impl


import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.api.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsInteractorImpl(
    // Принимаем SettingsRepository через конструктор
    // Это соответствует принципу Dependency Injection
    private val settingsRepository: SettingsRepository
    //, private val context: Context
) : SettingsInteractor {


    // Получаем состояние темной темы через репозиторий
    // Используем корутины для асинхронной работы
    override suspend fun getDarkTheme(): Boolean = withContext(Dispatchers.IO) {
        // Делегируем получение данных репозиторию
        // Это соответствует принципу единственной ответственности
        settingsRepository.getDarkTheme()
    }

    // Устанавливаем состояние темной темы через репозиторий
    override suspend fun setDarkTheme(enabled: Boolean) = withContext(Dispatchers.IO) {
        // Делегируем сохранение данных репозиторию
        // Interactor не должен знать детали реализации хранения
        settingsRepository.setDarkTheme(enabled)
    }
}
