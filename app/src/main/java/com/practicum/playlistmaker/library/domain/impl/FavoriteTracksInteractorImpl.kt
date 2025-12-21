package com.practicum.playlistmaker.library.domain.impl

import com.practicum.playlistmaker.library.domain.api.FavoriteTracksInteractor
import com.practicum.playlistmaker.library.data.repository.FavoriteTracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

// Интерактор для работы с избранными треками
class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository // Репозиторий из data-слоя
) : FavoriteTracksInteractor {

    override suspend fun addTrackToFavorites(track: Track) {
        // Просто делегируем вызов в репозиторий
        repository.addTrackToFavorites(track)
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        repository.removeTrackFromFavorites(track)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        // Возвращаем Flow из репозитория
        // Приэтом сортировка в SQL-запросе (ORDER BY insertTime DESC)
        return repository.getFavoriteTracks()
    }
}
