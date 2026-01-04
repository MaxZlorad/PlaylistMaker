package com.practicum.playlistmaker.library.domain.models

import android.net.Uri

data class NewPlaylistState(
    val playlistName: String = "",
    val playlistDescription: String = "",
    val coverImageUri: Uri? = null,
    val isCreateButtonEnabled: Boolean = false
)