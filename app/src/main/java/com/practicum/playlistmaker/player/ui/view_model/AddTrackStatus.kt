package com.practicum.playlistmaker.player.ui.view_model

sealed class AddTrackStatus {
    data class Success(val playlistName: String) : AddTrackStatus()
    data class AlreadyExists(val playlistName: String) : AddTrackStatus()
}