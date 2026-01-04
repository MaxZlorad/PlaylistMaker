package com.practicum.playlistmaker.library.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity для хранения треков, добавленных хотя бы в один плейлист.
@Entity(tableName = "playlist_tracks")
data class PlaylistTrackEntity(
    @PrimaryKey
    val trackId: String, // Уникальный ID трека

    val artworkUrl100: String, // URL обложки трека
    val trackName: String, // Название трека
    val artistName: String, // Имя исполнителя
    val collectionName: String?, // Название альбома (может быть null)
    val releaseDate: String?, // Дата релиза (может быть null)
    val primaryGenreName: String, // Жанр
    val country: String, // Страна
    val trackTimeMillis: Long, // Длительность в миллисекундах
    val previewUrl: String?, // URL для прослушивания 30-сек отрывка
    val insertTime: Long = System.currentTimeMillis() // Время добавления
)
