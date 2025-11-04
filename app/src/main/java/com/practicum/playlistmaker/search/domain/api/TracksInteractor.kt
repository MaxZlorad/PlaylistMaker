package com.practicum.playlistmaker.search.domain.api

import com.practicum.playlistmaker.search.domain.models.Track

interface TracksInteractor {
    suspend fun searchTracks(query: String): List<Track>
    suspend fun getSearchHistory(): List<Track>
    suspend fun addTrackToHistory(track: Track)
    suspend fun clearSearchHistory()
}