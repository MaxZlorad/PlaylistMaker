package com.practicum.playlistmaker.library.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practicum.playlistmaker.library.data.db.entity.PlaylistTrackEntity

// DAO интерфейс для работы с таблицей playlist_tracks.
@Dao
interface PlaylistTrackDao {

    // Добавить трек в таблицу playlist_tracks.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    // Получить список треков по их ID.
    @Query("SELECT * FROM playlist_tracks WHERE trackId IN (:trackIds)")
    suspend fun getTracksByIds(trackIds: List<String>): List<PlaylistTrackEntity>
}
