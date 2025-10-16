package com.practicum.playlistmaker.player.domain.models

import com.practicum.playlistmaker.core.domain.models.Track

sealed interface PlaybackState {
    data object Default : PlaybackState
    data class Prepared(val track: Track) : PlaybackState
    data object Playing : PlaybackState
    data object Paused : PlaybackState
    data object Stopped : PlaybackState
    data object Completed : PlaybackState
    data class Error(val message: String) : PlaybackState
}