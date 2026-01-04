package com.practicum.playlistmaker.library.data.repository

import com.practicum.playlistmaker.library.data.db.AppDatabase
import com.practicum.playlistmaker.library.data.db.TrackDbConverter
import com.practicum.playlistmaker.library.domain.impl.FavoriteTracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//Реализация репозитория для работы с избранными треками (используя Room БД)
class FavoriteTracksRepositoryImpl(
    private val appDatabase: AppDatabase // База данных из DataModule
) : FavoriteTracksRepository {

    override suspend fun addTrackToFavorites(track: Track) {
        // 1. Преобразуем Track в TrackEntity
        val trackEntity = TrackDbConverter.map(track)

        // 2. Сохраняем в БД через DAO
        appDatabase.trackDao().insertTrack(trackEntity)
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        // 1. Преобразуем Track в TrackEntity
        val trackEntity = TrackDbConverter.map(track)

        // 2. Удаляем из БД через DAO
        appDatabase.trackDao().deleteTrack(trackEntity)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        // 1. Получаем Flow<List<TrackEntity>> из DAO
        // Room автоматически обновляет Flow при изменениях в БД
        return appDatabase.trackDao().getFavoriteTracks()
            .map { entities ->
                // 2. Преобразуем каждую TrackEntity в Track
                entities.map { entity ->
                    TrackDbConverter.map(entity)
                }
            }
    }

    override suspend fun getFavoriteTrackIds(): List<String> {
        // Возвращаем список ID всех избранных треков, для проверки при поиске
        return appDatabase.trackDao().getFavoriteTrackIds()
    }
}
