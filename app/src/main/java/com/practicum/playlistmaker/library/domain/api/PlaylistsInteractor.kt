package com.practicum.playlistmaker.library.domain.api

import android.net.Uri
import com.practicum.playlistmaker.library.domain.impl.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Interactor для работы с плейлистами.
class PlaylistsInteractor(
    private val repository: PlaylistsRepository
) {

    // Создать новый плейлист.
    suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImageUri: Uri?
    ): Long {
        return repository.createPlaylist(name, description, coverImageUri)
    }

    // Получить все плейлисты пользователя.
    fun getAllPlaylists(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }

    // Добавить трек в плейлист.
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        repository.addTrackToPlaylist(track, playlist)
    }

    // Получить треки плейлиста.
    suspend fun getPlaylistTracks(trackIds: List<String>): List<Track> {
        return repository.getPlaylistTracks(trackIds)
    }

    // Получить плейлист вместе с его треками
    suspend fun getPlaylistWithTracks(playlistId: Int): Flow<Pair<Playlist, List<Track>>> = flow {
        // Получаем плейлист из репозитория
        val playlist = repository.getPlaylistById(playlistId)

        // Если плейлист не найден, возвращаем пустые данные
        if (playlist == null) {
            emit(Pair(Playlist(
                0,
                "",
                null,
                null,
                emptyList(),
                0,
                0L
            ), emptyList()))
            return@flow
        }

        // Получаем треки плейлиста по их ID
        val tracks = repository.getPlaylistTracks(playlist.trackIds)

        // Вычисляем общую длительность всех треков в миллисекундах
        val totalDuration = tracks.sumOf { it.trackTimeMillis }

        // Возвращаем плейлист с обновлённой длительностью и список треков
        emit(Pair(
            playlist.copy(totalDuration = totalDuration),  // Обновляем totalDurationMs
            tracks                                           // Список треков
        ))
    }

    // Удаление трека из плейлиста
    suspend fun removeTrackFromPlaylist(trackId: String, playlistId: Int) {
        repository.removeTrackFromPlaylist(trackId, playlistId)
    }

    suspend fun deletePlaylist(playlistId: Int) {
        repository.deletePlaylist(playlistId)
    }

    // Обновить плейлист
    suspend fun updatePlaylist(
        playlistId: Int, title: String, description: String, coverImagePath: String?
    ) {
        repository.updatePlaylist(
            playlistId, title, description, coverImagePath
        )
    }
}
