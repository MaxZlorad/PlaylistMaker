package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface SearchRepository {
    fun searchTracks(query: String): List<Track>
}