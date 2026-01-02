package com.practicum.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.models.Playlist

// ViewModel для экрана со списком плейлистов (список плейлистов через Interactor)
class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    // LiveData со списком плейлистов
    val playlists: LiveData<List<Playlist>> =
        playlistsInteractor.getAllPlaylists().asLiveData()
}
