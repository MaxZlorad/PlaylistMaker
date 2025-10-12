package com.practicum.playlistmaker.settings.domain.impl


import android.content.Context
import android.content.Intent
import android.net.Uri
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.api.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsInteractorImpl(
    // Принимаем SettingsRepository через конструктор
    // Это соответствует принципу Dependency Injection
    private val settingsRepository: SettingsRepository,
    private val context: Context) : SettingsInteractor {

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

    // Методы для работы с внешними приложениями
    override suspend fun shareApp() = withContext(Dispatchers.Main) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Скачайте крутое приложение для создания плейлистов!")
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться приложением"))
    }

    override suspend fun support() = withContext(Dispatchers.Main) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@playlistmaker.com")
            putExtra(Intent.EXTRA_SUBJECT, "Поддержка PlaylistMaker")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@playlistmaker.com"))
        }
        context.startActivity(intent)
    }

    override suspend fun openTerms() = withContext(Dispatchers.Main) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://example.com/terms")
        }
        context.startActivity(intent)
    }
}