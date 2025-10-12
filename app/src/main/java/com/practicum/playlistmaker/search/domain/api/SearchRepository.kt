package com.practicum.playlistmaker.search.domain.api

import com.practicum.playlistmaker.core.domain.models.Track

interface SearchRepository {
    suspend fun searchTracks(query: String): List<Track>
}