package com.practicum.playlistmaker.search.domain.impl

import com.practicum.playlistmaker.search.domain.api.HistoryRepository
import com.practicum.playlistmaker.search.domain.api.SearchRepository
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import java.util.concurrent.Executors

class TracksInteractorImpl(
    //private val repository: TracksRepository) : TracksInteractor {
    private val searchRepository: SearchRepository,
    private val historyRepository: HistoryRepository
) : TracksInteractor {

    override suspend fun searchTracks(query: String): List<Track> {
        // Прямой вызов без callback
        return searchRepository.searchTracks(query)
    }

    override suspend fun getSearchHistory(): List<Track> {
        // Прямой вызов без callback
        return historyRepository.getSearchHistory()
    }

    override suspend fun addTrackToHistory(track: Track) {

        // логика
        val currentHistory = historyRepository.getSearchHistory().toMutableList()

        // 1. Удаляем дубликаты
        currentHistory.removeAll { it.trackId == track.trackId }

        // 2. Добавляем в начало
        currentHistory.add(0, track)

        // 3. Ограничиваем размер
        if (currentHistory.size > SearchInteractorConstants.MAX_HISTORY_SIZE) {
            currentHistory.subList(
                SearchInteractorConstants.MAX_HISTORY_SIZE, currentHistory.size).clear()
        }

        // 4. Сохраняем
        historyRepository.saveSearchHistory(currentHistory)

    }

    override suspend fun clearSearchHistory() {
        historyRepository.clearSearchHistory()
    }
}