package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.data.network.ItunesApiService
import com.practicum.playlistmaker.search.domain.api.SearchRepository
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.data.mapper.toTracks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepositoryImpl(
    private val apiService: ItunesApiService
) : SearchRepository {

    override fun searchTracks(query: String): Flow<Result<List<Track>>> = flow {
        try {
            // Выполняем suspend функцию в IO потоке
            val response = withContext(Dispatchers.IO) {
                apiService.search(query) // suspend функция
            }

            val tracks = response.results.toTracks()
            emit(Result.success(tracks))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
