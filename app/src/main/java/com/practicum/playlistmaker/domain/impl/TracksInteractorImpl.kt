package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.HistoryRepository
import com.practicum.playlistmaker.domain.api.SearchRepository
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.models.Track
import java.util.concurrent.Executors

class TracksInteractorImpl(
    //private val repository: TracksRepository) : TracksInteractor {
    private val searchRepository: SearchRepository,
    private val historyRepository: HistoryRepository
) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTracks(query: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            val tracks = searchRepository.searchTracks(query)
            consumer.consume(tracks)
        }
    }

    override fun getSearchHistory(consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            val history = historyRepository.getSearchHistory()
            consumer.consume(history)
        }
    }

    override fun addTrackToHistory(track: Track) {
        executor.execute {
            // логика
            val currentHistory = historyRepository.getSearchHistory().toMutableList()

            // 1. Удаляем дубликаты
            currentHistory.removeAll { it.trackId == track.trackId }

            // 2. Добавляем в начало
            currentHistory.add(0, track)

            // 3. Ограничиваем размер
            if (currentHistory.size > MAX_HISTORY_SIZE) {
                currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
            }

            // 4. Сохраняем
            historyRepository.saveSearchHistory(currentHistory)
        }
    }

    override fun clearSearchHistory() {
        executor.execute {
            historyRepository.clearSearchHistory()
        }
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }

}