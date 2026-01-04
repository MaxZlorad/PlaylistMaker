package com.practicum.playlistmaker.library.ui.view_model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.models.NewPlaylistState
import kotlinx.coroutines.launch

// ViewModel для экрана создания плейлиста
class NewPlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    // ===== STATE =====

    // Единое состояние экрана (вместо 4 отдельных LiveData)
    private val _state = MutableLiveData(NewPlaylistState())
    val state: LiveData<NewPlaylistState> = _state

    // Событие успешного создания плейлиста
    private val _playlistCreated = MutableLiveData<String>() // Название созданного плейлиста
    val playlistCreated: LiveData<String> = _playlistCreated

    // ===== МЕТОДЫ =====

    // Обновить название плейлиста
    fun updatePlaylistName(name: String) {
        _state.value = _state.value?.copy(
            playlistName = name,
            isCreateButtonEnabled = name.isNotEmpty()
        )
    }

    // Обновить описание плейлиста
    fun updatePlaylistDescription(description: String) {
        _state.value = _state.value?.copy(playlistDescription = description)
    }

    // Установить URI выбранного изображения обложки
    fun setCoverImage(uri: Uri?) {
        _state.value = _state.value?.copy(coverImageUri = uri)
    }

    // Проверить, есть ли несохранённые данные
    fun hasUnsavedData(): Boolean {
        val currentState = _state.value ?: return false
        return currentState.playlistName.isNotEmpty() ||
                currentState.playlistDescription.isNotEmpty() ||
                currentState.coverImageUri != null
    }

    // Создать плейлист
    fun createPlaylist() {
        val currentState = _state.value ?: return
        val name = currentState.playlistName
        if (name.isEmpty()) return

        viewModelScope.launch {

            // Создаём плейлист через Interactor
            playlistsInteractor.createPlaylist(
                name = name,
                description = currentState.playlistDescription.takeIf { it.isNotEmpty() },
                coverImageUri = currentState.coverImageUri // Uri напрямую
            )

            // Отправляем событие об успешном создании
            _playlistCreated.postValue(name)
        }
    }
}
