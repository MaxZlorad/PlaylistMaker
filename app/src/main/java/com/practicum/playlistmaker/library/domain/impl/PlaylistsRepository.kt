package com.practicum.playlistmaker.library.domain.impl

import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

// Интерфейс репозитория для работы с плейлистами.
interface PlaylistsRepository {

    // Создать новый плейлист- название,описание,путь, возврат ID созданного плейлиста
    suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImagePath: String?
    ): Long

    // Получить все плейлисты пользователя.
    fun getAllPlaylists(): Flow<List<Playlist>>

    // Добавить трек в плейлист.
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)

    // Удалить трек из плейлиста.
    suspend fun removeTrackFromPlaylist(trackId: String, playlistId: Int)

    // Получить треки плейлиста по списку ID.
    suspend fun getPlaylistTracks(trackIds: List<String>): List<Track>
}
