package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.network.ItunesApiService
import com.practicum.playlistmaker.data.storage.SearchHistory
import com.practicum.playlistmaker.data.dto.TrackDto
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.models.Track

class TracksRepositoryImpl(
    // Сервис для работы с iTunes API (сетевой слой)
    private val apiService: ItunesApiService,
    // Хранилище для истории поиска (локальное хранилище)
    private val searchHistory: SearchHistory
) : TracksRepository { // Реализуем интерфейс из domain слоя

    override fun searchTracks(query: String): List<Track> {
        // Выполняем синхронный запрос к API
        val response = apiService.search(query).execute()

        return if (response.isSuccessful && response.body() != null) {
            // Если запрос успешен, преобразуем DTO в доменные модели
            response.body()!!.results.map { trackDto: TrackDto ->
                trackDto.toTrack() // Вызываем функцию-расширение для преобразования
            }
        } else {
            // В случае ошибки возвращаем пустой список
            emptyList()
        }
    }

    override fun getSearchHistory(): List<Track> {
        // Получаем историю из хранилища и преобразуем DTO в доменные модели
        return searchHistory.getHistory().map { trackDto: TrackDto ->
            trackDto.toTrack() // Вызываем функцию-расширение для преобразования
        }
    }

    // Преобразуем доменную модель в DTO и добавляем в хранилище
    override fun addTrackToHistory(track: Track) {
        searchHistory.addTrack(track.toTrackDto())
    }

    // Очистка истории поиска.
    override fun clearSearchHistory() {
        searchHistory.clearHistory()
    }

    // Преобразование DTO в Domain модель
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