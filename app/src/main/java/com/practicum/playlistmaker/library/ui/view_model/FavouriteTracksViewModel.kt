package com.practicum.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FavouriteTracksViewModel : ViewModel() {

    private val _favouriteTracks = MutableLiveData<List<String>>(emptyList())
    val favouriteTracks: LiveData<List<String>> = _favouriteTracks
}