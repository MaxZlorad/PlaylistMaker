package com.practicum.playlistmaker.core.di

import android.content.Context
import com.practicum.playlistmaker.core.data.network.ItunesApiClient
import com.practicum.playlistmaker.search.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.SearchRepositoryImpl
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.core.domain.api.TracksInteractor
import com.practicum.playlistmaker.settings.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.search.domain.impl.TracksInteractorImpl
import com.practicum.playlistmaker.settings.data.repository.ExternalNavigator
import com.practicum.playlistmaker.settings.domain.api.SharingInteractor
import com.practicum.playlistmaker.settings.domain.impl.SharingInteractorImpl

object Creator {
    private var appContext: Context? = null

    // Устанавливаем context при запуске приложения
    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    private fun requireContext(): Context {
        return appContext ?: throw IllegalStateException(
            "Context not initialized. Call Creator.setContext() first."
        )
    }

    // Предоставляем интерактор для работы с треками
    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(
            searchRepository = SearchRepositoryImpl(ItunesApiClient.apiService),
            historyRepository = HistoryRepositoryImpl(
                requireContext().getSharedPreferences(
                    SettingsConstants.APP_PREFS_NAME,
                    Context.MODE_PRIVATE
                )
            )
        )
    }

    // Предоставляем интерактор для работы с настройками
    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        val settingsRepository = SettingsRepositoryImpl(
            context.getSharedPreferences(SettingsConstants.APP_SETTINGS_NAME, Context.MODE_PRIVATE)
        )
        return SettingsInteractorImpl(settingsRepository)
    }

    fun provideSharingInteractor(context: Context): SharingInteractor {
        // Инициализировать константы один раз при создании
        val externalNavigator = ExternalNavigator(context)
        return SharingInteractorImpl(externalNavigator)  // Без context!
    }
}