package com.practicum.playlistmaker.library.data.db

import com.practicum.playlistmaker.library.data.db.entity.TrackEntity
import com.practicum.playlistmaker.search.domain.models.Track

// Конвертер между доменной моделью Track и сущностью БД TrackEntity
object TrackDbConverter {

    // Преобразование Track -> TrackEntity (для сохранения в БД)
    fun map(track: Track): TrackEntity {
        return TrackEntity(
            trackId = track.trackId,
            artworkUrl100 = track.artworkUrl100,
            trackName = track.trackName,
            artistName = track.artistName,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName ?: "", // Если null, сохраняем пустую строку
            country = track.country ?: "",
            trackTimeMillis = track.trackTimeMillis,
            previewUrl = track.previewUrl,
            insertTime = System.currentTimeMillis() // Время добавления в БД (для сортировки)
        )
    }

    //Преобразование TrackEntity -> Track (при чтении из БД)
    fun map(entity: TrackEntity): Track {
        return Track(
            trackId = entity.trackId,
            trackName = entity.trackName,
            artistName = entity.artistName,
            trackTimeMillis = entity.trackTimeMillis,
            artworkUrl100 = entity.artworkUrl100,
            collectionName = entity.collectionName,
            releaseDate = entity.releaseDate,
            primaryGenreName = entity.primaryGenreName,
            country = entity.country,
            previewUrl = entity.previewUrl,
            isFavorite = true  // Все треки из БД избранные
        )
    }
}
