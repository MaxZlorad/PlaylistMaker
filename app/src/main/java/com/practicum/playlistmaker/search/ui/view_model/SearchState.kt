package com.practicum.playlistmaker.search.ui.view_model

import com.practicum.playlistmaker.search.domain.models.Track

sealed class SearchState {
    object Empty : SearchState()
    object Loading : SearchState()
    object Error : SearchState()
    object EmptyResults : SearchState()
    data class Success(val tracks: List<Track>) : SearchState()
}