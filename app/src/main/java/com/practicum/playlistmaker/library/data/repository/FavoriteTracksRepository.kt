package com.practicum.playlistmaker.library.data.repository

import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

//Интерфейс репозитория для работы с избранными треками
interface FavoriteTracksRepository {

    suspend fun addTrackToFavorites(track: Track)

    suspend fun removeTrackFromFavorites(track: Track)

    fun getFavoriteTracks(): Flow<List<Track>>

    suspend fun getFavoriteTrackIds(): List<String> // список ID
}
