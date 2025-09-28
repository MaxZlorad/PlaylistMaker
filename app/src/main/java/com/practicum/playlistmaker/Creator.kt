package com.practicum.playlistmaker

import android.content.Context
import android.util.Log
import com.practicum.playlistmaker.data.network.ItunesApiClient
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.storage.SearchHistory
import com.practicum.playlistmaker.domain.api.SettingsInteractor
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.domain.impl.TracksInteractorImpl

object Creator {

    // Предоставляем интерактор для работы с треками
    fun provideTracksInteractor(context: Context): TracksInteractor {
        return TracksInteractorImpl(
            TracksRepositoryImpl(
                ItunesApiClient.apiService,
                SearchHistory(context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
                    .apply { loadHistory() } // Загружаем историю при создании
            )
        )
    }

    // Предоставляем интерактор для работы с настройками
    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        Log.d("Creator", "Creating SettingsInteractor")
        return try {
            val interactor = SettingsInteractorImpl(
                SettingsRepositoryImpl(context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)),
                context
            )
            Log.d("Creator", "SettingsInteractor created successfully")
            interactor
        } catch (e: Exception) {
            Log.e("Creator", "Error creating SettingsInteractor", e)
            throw e
        }
    }
}