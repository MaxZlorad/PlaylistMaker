package com.practicum.playlistmaker.library.domain.models

import com.practicum.playlistmaker.search.domain.models.Track

// Состояния экрана "Плейлист"
sealed class PlaylistDetailState {

    // Состояние загрузки данных
    object Loading : PlaylistDetailState()

    // Состояние с данными плейлиста и треками
    data class Content(
        val playlist: Playlist,
        val tracks: List<Track>
    ) : PlaylistDetailState()

    // Нет треков
    object Empty : PlaylistDetailState()

    // Пейлист не найден
    object Error : PlaylistDetailState()
}