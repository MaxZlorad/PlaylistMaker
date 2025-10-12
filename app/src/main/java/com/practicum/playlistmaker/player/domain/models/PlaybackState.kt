package com.practicum.playlistmaker.player.domain.models

import com.practicum.playlistmaker.core.domain.models.Track

sealed class PlaybackState {
    object Default : PlaybackState()
    data class Prepared(val track: Track) : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Stopped : PlaybackState()
    object Completed : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}