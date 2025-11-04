package com.practicum.playlistmaker.search.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.search.data.dto.TrackDto
import com.practicum.playlistmaker.search.domain.api.HistoryRepository
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.data.mapper.toTrack
import com.practicum.playlistmaker.search.data.mapper.toTrackDto

class HistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson // Gson через конструктор
) : HistoryRepository {

    private val historyList = mutableListOf<TrackDto>()

    init {
        loadHistory()
    }

    override fun getSearchHistory(): List<Track> {
        return historyList.map { it.toTrack() }
    }

    override fun saveSearchHistory(tracks: List<Track>) {
        historyList.clear()
        historyList.addAll(tracks.map { it.toTrackDto() })
        saveHistory()
    }

    override fun clearSearchHistory() {
        historyList.clear()
        saveHistory()
    }

    fun loadHistory() {
        val json = sharedPreferences.getString(SearchConstants.HISTORY_KEY, null) ?: return // Получаем JSON-строку из хранилища
        val type = object : TypeToken<List<TrackDto>>() {}.type // Создаем тип для десериализации List<Track>

        try {
            val loaded = gson.fromJson<List<TrackDto>>(json,type) // Преобразуем строку обратно в List<TrackDto>

            historyList.clear() //  Очищаем весь список
            historyList.addAll(loaded) // Загружаем весь список
        } catch (e: Exception) {
            // err
        }
    }

    //Сохраняем текущую историю в SharedPreferences
    private fun saveHistory() {
        // Преобразуем список треков в JSON
        val json = gson.toJson(historyList) // Весь список
        // Сохраняем в SharedPreferences
        sharedPreferences.edit()
            .putString(SearchConstants.HISTORY_KEY, json) // Сохраняем как одну строку
            .apply()
    }
}