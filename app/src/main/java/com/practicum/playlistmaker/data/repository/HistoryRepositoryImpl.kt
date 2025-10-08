package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.dto.TrackDto
import com.practicum.playlistmaker.domain.api.HistoryRepository
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.data.toTrack
import com.practicum.playlistmaker.data.toTrackDto

class HistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences) : HistoryRepository {

    private val gson = Gson()
    private val historyList = mutableListOf<TrackDto>()

    init {
        loadHistory()
    }

    override fun getSearchHistory(): List<Track> {
        return historyList.map { it.toTrack() }
        //return searchHistory.getHistory().map { it.toTrack() }
    }

    override fun saveSearchHistory(tracks: List<Track>) {
        //searchHistory.saveHistory(tracks.map { it.toTrackDto() })
        historyList.clear()
        historyList.addAll(tracks.map { it.toTrackDto() })
        saveHistory()
    }

    override fun clearSearchHistory() {
        historyList.clear()
        saveHistory()
        //searchHistory.clearHistory()
    }

    fun loadHistory() {
        val json = sharedPreferences.getString(HISTORY_KEY, null) ?: return // Получаем JSON-строку из хранилища
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
            .putString(HISTORY_KEY, json) // Сохраняем как одну строку
            .apply()
    }


    companion object {
        // Ключ для хранения истории в SharedPreferences
        private const val HISTORY_KEY = "search_history_key"
    }
}