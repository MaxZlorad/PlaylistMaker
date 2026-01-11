package com.practicum.playlistmaker.library.domain.models

// Domain модель плейлиста для использования в UI и Domain слоях (не Entity)
data class Playlist(
    val playlistId: Int,
    val name: String,
    val description: String?,// Описание плейлиста (может отсутствовать)
    val coverImagePath: String?, // Путь к обложке плейлиста (может отсутствовать)
    val trackIds: List<String>, // Список ID треков (не JSON строка!)
    val trackCount: Int, // Количество треков в плейлисте
    val totalDuration: Long = 0L // Общая длительность всех треков
)
