package com.practicum.playlistmaker

import android.content.Context
import com.practicum.playlistmaker.data.network.ItunesApiClient
import com.practicum.playlistmaker.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SearchRepositoryImpl
import com.practicum.playlistmaker.domain.api.SettingsInteractor
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.domain.impl.TracksInteractorImpl

object Creator {
    private var appContext: Context? = null

    // Устанавливаем context при запуске приложения
    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    private fun requireContext(): Context {
        return appContext ?: throw IllegalStateException(
            "Context not initialized. Call Creator.setContext() first.")
    }

    // Предоставляем интерактор для работы с треками
    fun provideTracksInteractor(): TracksInteractor {
        /*val searchHistory = SearchHistory(requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
            .apply { loadHistory() }*/

        return TracksInteractorImpl(
            searchRepository = SearchRepositoryImpl(ItunesApiClient.apiService),
            historyRepository = HistoryRepositoryImpl(
                requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            )
        )
    }

    // Предоставляем интерактор для работы с настройками
    fun provideSettingsInteractor(): SettingsInteractor {
        return SettingsInteractorImpl(
            SettingsRepositoryImpl(
                requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            ),
            requireContext()
        )
    }
}
