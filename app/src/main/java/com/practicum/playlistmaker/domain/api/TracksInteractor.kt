package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface TracksInteractor {
    fun searchTracks(query: String, consumer: TracksConsumer)
    fun getSearchHistory(consumer: TracksConsumer) // Асинхронный с callback
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()

    interface TracksConsumer {
        fun consume(tracks: List<Track>)
    }
}