package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.library.data.db.AppDatabase
import com.practicum.playlistmaker.search.data.network.ItunesApiService
import com.practicum.playlistmaker.search.domain.api.SearchRepository
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.data.mapper.toTracks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepositoryImpl(
    private val apiService: ItunesApiService,
    private val appDatabase: AppDatabase // БД
) : SearchRepository {

    override fun searchTracks(query: String): Flow<Result<List<Track>>> = flow {
        try {
            // 1. Выполняем поиск через API
            val response = apiService.search(query)

            // 2. Преобразуем в Track
            val tracks = response.results.toTracks()

            // 3. Получаем ID избранных треков из БД
            val favoriteIds = withContext(Dispatchers.IO) {
                appDatabase.trackDao().getFavoriteTrackIds()
            }

            // 4. Проставляем флаг isFavorite
            val tracksWithFavorites = tracks.map { track ->
                track.copy(
                    isFavorite = favoriteIds.contains(track.trackId)
                )
            }

            // 5. Возвращаем успешный результат
            emit(Result.success(tracksWithFavorites))

        } catch (e: Exception) {
            // 6. Возвращаем ошибку
            emit(Result.failure(e))
        }
    }
}
