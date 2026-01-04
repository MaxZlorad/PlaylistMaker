package com.practicum.playlistmaker.library.data.repository

import android.net.Uri
import com.practicum.playlistmaker.library.data.db.AppDatabase
import com.practicum.playlistmaker.library.data.db.PlaylistDbConverter
import com.practicum.playlistmaker.library.data.db.PlaylistTrackDbConverter
import com.practicum.playlistmaker.library.data.db.dao.PlaylistDao
import com.practicum.playlistmaker.library.data.db.dao.PlaylistTrackDao
import com.practicum.playlistmaker.library.data.db.entity.PlaylistEntity
import com.practicum.playlistmaker.library.domain.impl.ImageStorage
import com.practicum.playlistmaker.library.domain.impl.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// Реализация репозитория для работы с плейлистами.
class PlaylistsRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao,
    private val playlistConverter: PlaylistDbConverter,
    private val trackConverter: PlaylistTrackDbConverter,
    private val imageStorage: ImageStorage
) : PlaylistsRepository {

    // Создать новый плейлист.
    override suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImageUri: Uri?
    ): Long {
        // Сохраняем изображение через ImageStorage
        val coverImagePath = coverImageUri?.let { uri ->
            imageStorage.saveImage(uri)
        }

        // Создаём новый плейлист (ID будет сгенерирован автоматически)
        val playlistEntity = PlaylistEntity(
            playlistId = 0, // autoGenerate = true, Room сам присвоит ID
            name = name,
            description = description,
            coverImagePath = coverImagePath,
            trackIds = "[]", // Пустой список треков в формате JSON
            trackCount = 0 // Пока нет треков
        )

        // Сохраняем в БД и получаем ID
        return playlistDao.insertPlaylist(playlistEntity)
    }

    // Получить все плейлисты пользователя.
    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists()
            .map { playlistEntities ->
                // Конвертируем список Entity в список Domain моделей
                playlistEntities.map { entity ->
                    playlistConverter.map(entity)
                }
            }
            .distinctUntilChanged()
    }

    // Добавить трек в плейлист.
    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        // 1. Получаем актуальную версию плейлиста из БД
        val playlistEntity = playlistDao
            .getPlaylistById(playlist.playlistId) ?: return

        // 2. Конвертируем в Domain модель
        val currentPlaylist = playlistConverter.map(playlistEntity)

        // 3. Создаём обновлённый список trackIds (добавляем новый трек в НАЧАЛО!)
        val updatedTrackIds = mutableListOf(track.trackId).apply {
            addAll(currentPlaylist.trackIds) // Добавляем остальные треки
        }

        // 4. Создаём обновлённый плейлист
        val updatedPlaylist = currentPlaylist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size // Обновляем счётчик
        )

        // 5. Конвертируем обратно в Entity и сохраняем в БД
        playlistDao.updatePlaylist(
            playlistConverter.map(updatedPlaylist)
        )

        // 6. Сохраняем трек в таблицу playlist_tracks
        // (если трек уже есть, OnConflictStrategy.IGNORE проигнорирует вставку)
        val trackEntity = trackConverter.map(track)
        playlistTrackDao.insertTrack(trackEntity)
    }

    // Удалить трек из плейлиста.
    override suspend fun removeTrackFromPlaylist(trackId: String, playlistId: Int) {
        // Мемтечко про запас
    }

    // Получить треки плейлиста по списку ID.
    override suspend fun getPlaylistTracks(trackIds: List<String>): List<Track> {
        if (trackIds.isEmpty()) return emptyList()

        // Получаем треки из БД
        val trackEntities = playlistTrackDao
            .getTracksByIds(trackIds)

        // Конвертируем в Domain модели
        return trackEntities.map { entity ->
            trackConverter.map(entity)
        }
    }
}
