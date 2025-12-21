package com.practicum.playlistmaker.library.domain.api

import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

// Интерфейс интерактора для работы с избранными треками
interface FavoriteTracksInteractor {

    suspend fun addTrackToFavorites(track: Track)

    suspend fun removeTrackFromFavorites(track: Track)

    fun getFavoriteTracks(): Flow<List<Track>>
}
