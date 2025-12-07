package com.practicum.playlistmaker.search.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>(SearchState.Empty)
    val searchState: LiveData<SearchState> = _searchState

    private val _historyState = MutableLiveData<List<Track>>(emptyList())
    val historyState: LiveData<List<Track>> = _historyState

    // Собираем Flow
    fun searchTracks(query: String) {
        if (query.trim().isEmpty()) {
            _searchState.value = SearchState.Empty
            return
        }

        // Показываем индикатор загрузки
        _searchState.value = SearchState.Loading

        // Запуск корутины в viewModelScope
        viewModelScope.launch {
            tracksInteractor.searchTracks(query).collect { result ->
                _searchState.value = when {
                    result.isSuccess -> {
                        val tracks = result.getOrNull() ?: emptyList()
                        if (tracks.isEmpty()) {
                            SearchState.EmptyResults
                        } else {
                            SearchState.Success(tracks)
                        }
                    }
                    result.isFailure -> SearchState.Error
                    else -> SearchState.Error
                }
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
