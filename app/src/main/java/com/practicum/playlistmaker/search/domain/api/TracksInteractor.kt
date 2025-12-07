package com.practicum.playlistmaker.search.domain.api

import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface TracksInteractor {
    fun searchTracks(query: String): Flow<Result<List<Track>>>
    suspend fun getSearchHistory(): List<Track>
    suspend fun addTrackToHistory(track: Track)
    suspend fun clearSearchHistory()
}
