package com.practicum.playlistmaker.library.domain.impl

import android.net.Uri

interface ImageStorage {
    fun saveImage(uri: Uri): String?
}
