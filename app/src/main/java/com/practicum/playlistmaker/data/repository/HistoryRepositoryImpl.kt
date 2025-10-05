package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.TrackDto
import com.practicum.playlistmaker.data.storage.SearchHistory
import com.practicum.playlistmaker.domain.api.HistoryRepository
import com.practicum.playlistmaker.domain.models.Track

class HistoryRepositoryImpl(
    private val searchHistory: SearchHistory
) : HistoryRepository {

    override fun getSearchHistory(): List<Track> {
        return searchHistory.getHistory().map { it.toTrack() }
    }

    override fun saveSearchHistory(tracks: List<Track>) {
        searchHistory.saveHistory(tracks.map { it.toTrackDto() })
    }

    override fun clearSearchHistory() {
        searchHistory.clearHistory()
    }

    private fun TrackDto.toTrack(): Track {
        return Track(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl
        )
    }

    private fun Track.toTrackDto(): TrackDto {
        return TrackDto(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl
        )
    }
}