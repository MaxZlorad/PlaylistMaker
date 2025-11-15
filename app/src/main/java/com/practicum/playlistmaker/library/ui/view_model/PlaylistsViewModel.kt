package com.practicum.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaylistsViewModel : ViewModel() {

    private val _playlists = MutableLiveData<List<String>>(emptyList())
    val playlists: LiveData<List<String>> = _playlists
}