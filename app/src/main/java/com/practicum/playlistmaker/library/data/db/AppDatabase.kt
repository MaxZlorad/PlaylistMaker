package com.practicum.playlistmaker.library.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practicum.playlistmaker.library.data.db.dao.TrackDao
import com.practicum.playlistmaker.library.data.db.entity.TrackEntity
import com.practicum.playlistmaker.library.data.db.dao.PlaylistDao
import com.practicum.playlistmaker.library.data.db.dao.PlaylistTrackDao
import com.practicum.playlistmaker.library.data.db.entity.PlaylistEntity
import com.practicum.playlistmaker.library.data.db.entity.PlaylistTrackEntity

@Database(
    version = 2, // м.б. новая, т.к. уже с тремя табл.?
    entities = [
        TrackEntity::class,        // 1. Избранные треки
        PlaylistEntity::class,     // 2. Плейлисты
        PlaylistTrackEntity::class // 3. Треки плейлистов
    ],
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO для избранных треков
    abstract fun trackDao(): TrackDao

    // DAO для плейлистов
    abstract fun playlistDao(): PlaylistDao

    // DAO для треков плейлистов
    abstract fun playlistTrackDao(): PlaylistTrackDao
}
