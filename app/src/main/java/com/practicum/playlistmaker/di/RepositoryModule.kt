package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.di.NamedConstants.HISTORY_PREFS
import com.practicum.playlistmaker.di.NamedConstants.SETTINGS_PREFS
import com.practicum.playlistmaker.search.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.SearchRepositoryImpl
import com.practicum.playlistmaker.search.domain.api.HistoryRepository
import com.practicum.playlistmaker.search.domain.api.SearchRepository
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.settings.domain.api.SettingsRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    /*// Search репозитории
    single<SearchRepository> {
        SearchRepositoryImpl(get())
    }

    // HistoryRepository
    single<HistoryRepository> {
        HistoryRepositoryImpl(get(named(HISTORY_PREFS)))
    }

    // SettingsRepository
    single<SettingsRepository> {
        SettingsRepositoryImpl(get(named(SETTINGS_PREFS)))
    }*/

    // Search репозитории
    single<SearchRepository> {
        SearchRepositoryImpl(get())
    }

    // HistoryRepository
    single<HistoryRepository> {
        HistoryRepositoryImpl(
            sharedPreferences = get(named(HISTORY_PREFS))
        )
    }

    // SettingsRepository
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            sharedPreferences = get(named(SETTINGS_PREFS))
        )
    }
}
