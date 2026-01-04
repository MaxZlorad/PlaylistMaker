package com.practicum.playlistmaker.library.data.db

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.library.data.db.entity.PlaylistEntity
import com.practicum.playlistmaker.library.domain.models.Playlist

// Конвертер для преобразования между PlaylistEntity (БД) и Playlist (Domain).
class PlaylistDbConverter {

    private val gson = Gson()

    // Преобразует PlaylistEntity (из БД) в Playlist (Domain модель).
    fun map(entity: PlaylistEntity): Playlist {
        return Playlist(
            playlistId = entity.playlistId,
            name = entity.name,
            description = entity.description,
            coverImagePath = entity.coverImagePath,
            trackIds = parseTrackIds(entity.trackIds), // JSON → List
            trackCount = entity.trackCount
        )
    }

    // Преобразует Playlist (Domain модель) в PlaylistEntity (для БД).
    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            playlistId = playlist.playlistId,
            name = playlist.name,
            description = playlist.description,
            coverImagePath = playlist.coverImagePath,
            trackIds = serializeTrackIds(playlist.trackIds), // List → JSON
            trackCount = playlist.trackCount
        )
    }

    //Парсит JSON строку в список ID треков.
    private fun parseTrackIds(json: String): List<String> {
        if (json.isEmpty()) return emptyList()

        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Конвертирует список ID треков в JSON строку.
    private fun serializeTrackIds(trackIds: List<String>): String {
        return gson.toJson(trackIds)
    }
}
