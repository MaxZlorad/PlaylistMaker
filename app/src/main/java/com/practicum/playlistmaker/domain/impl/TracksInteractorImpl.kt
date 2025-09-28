package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.models.Track
import java.util.concurrent.Executors

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTracks(query: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            val tracks = repository.searchTracks(query)
            consumer.consume(tracks)
        }
    }

    override fun getSearchHistory(consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            val history = repository.getSearchHistory()
            consumer.consume(history)
        }
    }

    override fun addTrackToHistory(track: Track) {
        executor.execute {
            repository.addTrackToHistory(track)
        }
    }

    override fun clearSearchHistory() {
        executor.execute {
            repository.clearSearchHistory()
        }
    }
}