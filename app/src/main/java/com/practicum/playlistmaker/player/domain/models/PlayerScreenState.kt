package com.practicum.playlistmaker.player.domain.models

//Единый стейт экрана "Аудиоплеер"
data class PlayerScreenState(
    val playbackState: PlaybackState = PlaybackState.Default,
    val currentPosition: Long = 0L,
    val isFavorite: Boolean = false
)
