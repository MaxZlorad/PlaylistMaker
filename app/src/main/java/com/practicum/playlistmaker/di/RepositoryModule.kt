package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.di.NamedConstants.HISTORY_PREFS
import com.practicum.playlistmaker.di.NamedConstants.SETTINGS_PREFS
import com.practicum.playlistmaker.library.data.db.PlaylistDbConverter
import com.practicum.playlistmaker.library.data.db.PlaylistTrackDbConverter
import com.practicum.playlistmaker.library.domain.impl.FavoriteTracksRepository
import com.practicum.playlistmaker.library.domain.impl.PlaylistsRepository
import com.practicum.playlistmaker.search.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.SearchRepositoryImpl
import com.practicum.playlistmaker.search.domain.api.HistoryRepository
import com.practicum.playlistmaker.search.domain.api.SearchRepository
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.settings.domain.api.SettingsRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.practicum.playlistmaker.library.data.repository.FavoriteTracksRepositoryImpl
import com.practicum.playlistmaker.library.data.repository.PlaylistsRepositoryImpl


val repositoryModule = module {

    // Search репозитории
    single<SearchRepository> {
        SearchRepositoryImpl(
            apiService = get(), // Retrofit-сервис из dataModule
            appDatabase = get()  // Добавление БД для проверки isFavorite
        )
    }

    // HistoryRepository
    single<HistoryRepository> {
        HistoryRepositoryImpl(
            sharedPreferences = get(named(HISTORY_PREFS)), // SP из dataModule
            gson = get(), // Gson для сериализации треков
            appDatabase = get()  // Добавление БД для проверки isFavorite в истории
        )
    }

    // SettingsRepository
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            sharedPreferences = get(named(SETTINGS_PREFS))
        )
    }

    // Управление избранными треками в Room БД
    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(
            appDatabase = get() // Получаем экземпляр БД из dataModule через get()
        )
    }

    // Конвертеры для плейлистов
    single { PlaylistDbConverter() }
    single { PlaylistTrackDbConverter() }

    // Repository для плейлистов
    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(
            appDatabase = get(), // AppDatabase из DataModule
            playlistConverter = get(), // PlaylistDbConverter
            trackConverter = get() // PlaylistTrackDbConverter
        )
    }
}


