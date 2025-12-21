package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.library.ui.view_model.MediaLibraryViewModel
import com.practicum.playlistmaker.main.ui.view_model.MainViewModel
import com.practicum.playlistmaker.player.ui.view_model.PlayerViewModel
import com.practicum.playlistmaker.search.ui.view_model.SearchViewModel
import com.practicum.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.practicum.playlistmaker.library.ui.view_model.FavouriteTracksViewModel
import com.practicum.playlistmaker.library.ui.view_model.PlaylistsViewModel

val viewModelModule = module {
    // Main
    viewModel { MainViewModel(get()) } // SettingsInteractor

    // Search
    viewModel { SearchViewModel(get()) }// TracksInteractor

    // Settings
    viewModel { SettingsViewModel(get(), get()) } // Оба интерфактора

    // Player
    viewModel {
        PlayerViewModel(
            audioPlayer = get(), // AudioPlayer из DataModule
            favoriteTracksInteractor = get() // FavoriteTracksInteractor из InteractorModule
        )
    }

    // MediaLibrary
    viewModel { MediaLibraryViewModel() }
    viewModel { PlaylistsViewModel() }
    viewModel {
        FavouriteTracksViewModel(
            favoriteTracksInteractor = get() // FavoriteTracksInteractor из InteractorModule
        )
    }
}