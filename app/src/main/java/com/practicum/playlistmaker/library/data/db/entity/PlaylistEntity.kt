package com.practicum.playlistmaker.library.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity для хранения плейлистов в базе данных Room.
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Int = 0, // уникальный ID плейлиста (генерируется автоматически)

    val name: String, // Название плейлиста (обязательно!)

    val description: String?, // Описание (опционально)

    val coverImagePath: String?, // Путь к обложке в private storage

    val trackIds: String, // JSON строка списка ID треков ["123", "456"]

    val trackCount: Int = 0, // Количество треков

    val totalDuration: Long
)
