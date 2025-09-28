package com.practicum.playlistmaker.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.dto.TrackDto

class SearchHistory(
    // SharedPreferences - хранилище ключ-значение в Android
    private val sharedPreferences: SharedPreferences,

    // Максимальный размер истории (по умолчанию 10 треков)
    private val maxHistorySize: Int = 10
) {
    // Gson для преобразования объектов в JSON и обратно
    private val gson = Gson()

    // Временное хранилище треков в памяти
    private val historyList = mutableListOf<TrackDto>()

    companion object {
        // Ключ для хранения истории в SharedPreferences
        private const val HISTORY_KEY = "search_history_key"
    }

    fun addTrack(track: TrackDto) {
        // Удаляем трек, если он уже есть в истории (по trackId)
        historyList.removeAll { it.trackId == track.trackId }

        // Добавляем новый трек в начало списка
        historyList.add(0, track)

        // Если превышен лимит - удаляем самые старые треки
        if (historyList.size > maxHistorySize) {
            historyList.subList(maxHistorySize, historyList.size).clear()
        }

        // Сохраняем изменения
        saveHistory()
    }

    fun getHistory(): List<TrackDto> {
        return historyList.toList() // Возвращаем копию списка, чтобы избежать внешних изменений
    }

    fun clearHistory() {
        // Очищаем список в памяти
        historyList.clear()

        // Удаляем данные из SharedPreferences
        sharedPreferences.edit()
            .remove(HISTORY_KEY)
            .apply()
    }

    fun loadHistory() {
        val json = sharedPreferences.getString(HISTORY_KEY, null) ?: return // Получаем JSON-строку из хранилища
        val type = object : TypeToken<List<TrackDto>>() {}.type // Создаем тип для десериализации List<Track>

        try {
            val loaded = gson.fromJson<List<TrackDto>>(
                json,
                type
            ) // Преобразуем строку обратно в List<TrackDto>

            historyList.clear() //  Очищаем весь список
            historyList.addAll(loaded) // Загружаем весь список
        } catch (e: Exception) {
            // err
        }
    }

    /**
     * Сохраняет текущую историю в SharedPreferences.
     */
    private fun saveHistory() {
        // Преобразуем список треков в JSON
        val json = gson.toJson(historyList) // Весь список

        // Сохраняем в SharedPreferences
        sharedPreferences.edit()
            .putString(HISTORY_KEY, json) // Сохраняем как одну строку
            .apply()
    }
}