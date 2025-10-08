package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.network.ItunesApiService
import com.practicum.playlistmaker.domain.api.SearchRepository
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.data.toTrack

class SearchRepositoryImpl(
    private val apiService: ItunesApiService
) : SearchRepository {

    override fun searchTracks(query: String): List<Track> {
        val response = apiService.search(query).execute()
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!.results.map { it.toTrack() } //{ trackDto -> trackDto.toTrack() }
        } else {
            emptyList()
        }
    }

    /*
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
    }*/
}