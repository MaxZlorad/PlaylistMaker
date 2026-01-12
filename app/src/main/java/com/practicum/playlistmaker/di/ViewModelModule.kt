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
import com.practicum.playlistmaker.library.ui.view_model.NewPlaylistViewModel
import com.practicum.playlistmaker.library.ui.view_model.PlaylistDetailViewModel
import org.koin.android.ext.koin.androidApplication

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
            favoriteTracksInteractor = get(), // FavoriteTracksInteractor из InteractorModule
            playlistsInteractor = get()
        )
    }

    // MediaLibrary
    viewModel { MediaLibraryViewModel() }

    // FavouriteTracks
    viewModel {
        FavouriteTracksViewModel(
            favoriteTracksInteractor = get() // FavoriteTracksInteractor из InteractorModule
        )
    }

    // Playlists - список плейлистов
    viewModel {
        PlaylistsViewModel(
            playlistsInteractor = get() // PlaylistsInteractor из InteractorModule
        )
    }

    // ViewModel для списка плейлистов
    viewModel {
        PlaylistsViewModel(
            playlistsInteractor = get() // PlaylistsInteractor из InteractorModule
        )
    }

    // ViewModel для создания плейлиста
    viewModel {
        NewPlaylistViewModel(
            playlistsInteractor = get() // PlaylistsInteractor из InteractorModule
        )
    }

    viewModel {
        PlaylistDetailViewModel(
            application = androidApplication(),
            playlistsInteractor = get()
        )
    }
}