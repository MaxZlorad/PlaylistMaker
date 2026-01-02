package com.practicum.playlistmaker.library.domain.models

// Domain модель плейлиста для использования в UI и Domain слоях (не Entity)
data class Playlist(
    val playlistId: Int,
    val name: String,
    val description: String?,
    val coverImagePath: String?,
    val trackIds: List<String>, // Список ID треков (не JSON строка!)
    val trackCount: Int
)
