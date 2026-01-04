package com.practicum.playlistmaker.library.data.db

import com.practicum.playlistmaker.library.data.db.entity.PlaylistTrackEntity
import com.practicum.playlistmaker.search.domain.models.Track

// Конвертер для преобразования между PlaylistTrackEntity (БД) и Track (Domain).
class PlaylistTrackDbConverter {

    // Преобразует PlaylistTrackEntity (из БД) в Track (Domain модель).
    fun map(entity: PlaylistTrackEntity): Track {
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
            isFavorite = false // По умолчанию false (обновим позже)
        )
    }

    // Преобразует Track (Domain модель) в PlaylistTrackEntity (для БД).
    fun map(track: Track): PlaylistTrackEntity {
        return PlaylistTrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName ?: "", // Обязательное поле
            country = track.country ?: "", // Обязательное поле
            previewUrl = track.previewUrl,
            insertTime = System.currentTimeMillis()
        )
    }
}
