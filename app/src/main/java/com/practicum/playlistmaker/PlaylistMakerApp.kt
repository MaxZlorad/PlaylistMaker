package com.practicum.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.di.dataModule
import com.practicum.playlistmaker.di.interactorModule
import com.practicum.playlistmaker.di.repositoryModule
import com.practicum.playlistmaker.di.viewModelModule
import com.practicum.playlistmaker.settings.domain.api.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.get

class PlaylistMakerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Сначала инициализируем Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@PlaylistMakerApp)
            modules(
                dataModule,
                repositoryModule,
                interactorModule,
                viewModelModule
            )
        }

        // Теперь получаем SettingsRepository и применяем тему
        applySavedTheme()
    }

    private fun applySavedTheme() {
        // Получаем SettingsRepository через Koin после его инициализации
        val settingsRepository: SettingsRepository = get()

        // Получаем сохраненную тему и применяем ее
        val isDarkTheme = settingsRepository.getDarkTheme()
        val mode = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
