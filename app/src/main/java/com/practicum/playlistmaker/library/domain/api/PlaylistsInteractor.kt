package com.practicum.playlistmaker.library.domain.api

import com.practicum.playlistmaker.library.domain.impl.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

// Interactor для работы с плейлистами.
class PlaylistsInteractor(
    private val repository: PlaylistsRepository
) {

    // Создать новый плейлист.
    suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImagePath: String?
    ): Long {
        return repository.createPlaylist(name, description, coverImagePath)
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
}
