package com.practicum.playlistmaker.core.domain.api

import com.practicum.playlistmaker.core.domain.models.Track

interface TracksInteractor {
    suspend fun searchTracks(query: String): List<Track>
    suspend fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}