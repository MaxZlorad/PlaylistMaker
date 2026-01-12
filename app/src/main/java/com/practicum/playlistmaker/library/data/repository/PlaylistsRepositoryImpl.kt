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
            trackIds = "",
            trackCount = 0, // Пока нет треков
            totalDuration = 0L
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

    // Получить конкретный плейлист
    override suspend fun getPlaylistById(playlistId: Int): Playlist? {
        // Получаем Entity плейлиста из базы данных
        val entity = playlistDao.getPlaylistById(playlistId) ?: return null

        // Конвертируем Entity в Domain модель через converter
        return playlistConverter.map(entity)
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
        playlistDao.updatePlaylist(playlistConverter.map(updatedPlaylist))

        // 6. Сохраняем трек в таблицу playlist_tracks
        // (если трек уже есть, OnConflictStrategy.IGNORE проигнорирует вставку)
        val trackEntity = trackConverter.map(track)
        playlistTrackDao.insertTrack(trackEntity)
    }

    // Удалить трек из плейлиста.
    override suspend fun removeTrackFromPlaylist(trackId: String, playlistId: Int) {
        // 1. Получаем Entity плейлиста
        val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return

        // 2. Конвертируем в Domain модель (там trackIds уже List<String>)
        val playlist = playlistConverter.map(playlistEntity)

        // 3. Удаляем трек из списка
        val updatedTrackIds = playlist.trackIds.toMutableList().apply {
            remove(trackId)
        }

        // 4. Получаем информацию об удаляемом треке для пересчёта длительности
        val trackToRemove = playlistTrackDao.getTrackById(trackId)
        val newTotalDuration = if (trackToRemove != null) {
            playlist.totalDuration - trackToRemove.trackTimeMillis
        } else {
            playlist.totalDuration
        }

        // 5. Создаём обновлённый плейлист
        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size,
            totalDuration = newTotalDuration
        )

        // 6. Сохраняем через updatePlaylist (не updatePlaylistTracks!)
        playlistDao.updatePlaylist(playlistConverter.map(updatedPlaylist))

        // 7. Проверяем, остался ли трек в других плейлистах
        cleanupUnusedTrack(trackId)
    }

    // Вспомогательная функция: удалить трек из таблицы, если он не используется
    private suspend fun cleanupUnusedTrack(trackId: String) {
        // Проверяем, есть ли трек в других плейлистах
        val playlistsCount = playlistTrackDao.countPlaylistsContainingTrack(trackId)

        // Если трек не используется ни в одном плейлисте — удаляем из таблицы
        if (playlistsCount == 0) {
            playlistTrackDao.deleteTrack(trackId)
        }
    }

    // Удаление плейлиста
    override suspend fun deletePlaylist(playlistId: Int) {
        // Получаем плейлист для доступа к списку треков
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val trackIds = playlistConverter.map(playlist).trackIds

        // Удаляем плейлист из БД
        playlistDao.deletePlaylist(playlistId)

        // Очищаем неиспользуемые треки
        trackIds.forEach { trackId ->
            cleanupUnusedTrack(trackId)
        }
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

    // Обновить плейлист с сохранением треков
    override suspend fun updatePlaylist(
        playlistId: Int,
        title: String,
        description: String,
        coverImagePath: String?
    ) {
        val currentPlaylist = playlistDao.getPlaylistById(playlistId) ?: return

        // Обработка нового Uri (если это content://)
        val oldCoverPath = currentPlaylist.coverImagePath

        val newCoverPath = if (coverImagePath != null && coverImagePath.startsWith("content://")) {
            // Новая картинка выбрана - копируем в постоянное хранилище
            imageStorage.saveImage(Uri.parse(coverImagePath))
        } else {
            // Старый путь или null
            coverImagePath
        }

        val playlistEntity = PlaylistEntity(
            playlistId = playlistId,
            name = title,
            description = description,
            coverImagePath = newCoverPath, //coverImagePath,
            trackIds = currentPlaylist.trackIds,
            trackCount = currentPlaylist.trackCount,
            totalDuration = currentPlaylist.totalDuration
        )

        playlistDao.updatePlaylist(playlistEntity)

        if (oldCoverPath != null && oldCoverPath != newCoverPath &&
            !oldCoverPath.startsWith("content://") && oldCoverPath.startsWith("/storage/")) {
            try {
                val oldFile = java.io.File(oldCoverPath)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            } catch (e: Exception) {
                // Игнор ошибки удаления
            }
        }
    }
}
