package com.practicum.playlistmaker.search.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.domain.api.TracksInteractor
import com.practicum.playlistmaker.core.domain.models.Track
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>(SearchState.Empty)
    val searchState: LiveData<SearchState> = _searchState

    private val _historyState = MutableLiveData<List<Track>>(emptyList())
    val historyState: LiveData<List<Track>> = _historyState

    fun searchTracks(query: String) {
        if (query.trim().isEmpty()) {
            _searchState.value = SearchState.Empty
            return
        }

        _searchState.value = SearchState.Loading

        viewModelScope.launch {
            try {
                // Прямой вызов suspend функции
                val tracks = tracksInteractor.searchTracks(query)
                _searchState.value = if (tracks.isEmpty()) SearchState.EmptyResults
                else SearchState.Success(tracks)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error
            }
        }
    }

    fun getSearchHistory() {
        viewModelScope.launch {
            try {
                // Прямой вызов suspend функции
                val history = tracksInteractor.getSearchHistory()
                _historyState.value = history
            } catch (e: Exception) {
                // err
                _historyState.value = emptyList()
            }
        }
    }

    fun addTrackToHistory(track: Track) {
        viewModelScope.launch {
            tracksInteractor.addTrackToHistory(track)
            getSearchHistory()
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            tracksInteractor.clearSearchHistory()
            getSearchHistory()
        }
    }

    fun setErrorState() {
        _searchState.value = SearchState.Error
    }

    fun setEmptyState() {
        _searchState.value = SearchState.Empty
    }
}