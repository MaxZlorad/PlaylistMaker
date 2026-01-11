package com.practicum.playlistmaker.library.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.practicum.playlistmaker.library.data.db.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

// DAO интерфейс для работы с таблицей playlists.
@Dao
interface PlaylistDao {

    // Вставить новый плейлист в БД. @return Long - ID вставленной записи (автоинкремент)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    // Обновить существующий плейлист
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    // Получить все плейлисты из БД. Flow автоматически обновляет UI, новые плейлисты по убыванию ID
    @Query("SELECT * FROM playlists ORDER BY playlistId DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    // Получить конкретный плейлист по ID.
    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    suspend fun getPlaylistById(playlistId: Int): PlaylistEntity?

    // Удалить плейлист по ID
    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)
}
