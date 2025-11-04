package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.data.network.ItunesApiService
import com.practicum.playlistmaker.search.domain.api.SearchRepository
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.data.mapper.toTrack
import com.practicum.playlistmaker.search.data.mapper.toTracks
import retrofit2.awaitResponse

class SearchRepositoryImpl(
    private val apiService: ItunesApiService
) : SearchRepository {

    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            // Асинхронный вызов
            val response = apiService.search(query).awaitResponse()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.results.toTracks()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // Возвращаем пустой список при любой ошибке
            emptyList()
        }
    }



}