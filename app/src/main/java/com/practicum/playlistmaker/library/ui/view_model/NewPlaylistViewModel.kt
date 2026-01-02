package com.practicum.playlistmaker.library.ui.view_model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import kotlinx.coroutines.launch

// ViewModel для экрана создания плейлиста
class NewPlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    // ===== STATE =====

    // Название плейлиста
    private val _playlistName = MutableLiveData<String>()
    val playlistName: LiveData<String> = _playlistName

    // Описание плейлиста
    private val _playlistDescription = MutableLiveData<String>()
    val playlistDescription: LiveData<String> = _playlistDescription

    // URI выбранного изображения обложки
    private val _coverImageUri = MutableLiveData<Uri?>()
    val coverImageUri: LiveData<Uri?> = _coverImageUri

    // Активна ли кнопка "Создать" (название должно быть заполнено!)
    private val _isCreateButtonEnabled = MutableLiveData<Boolean>(false)
    val isCreateButtonEnabled: LiveData<Boolean> = _isCreateButtonEnabled

    // Событие успешного создания плейлиста
    private val _playlistCreated = MutableLiveData<String>() // Название созданного плейлиста
    val playlistCreated: LiveData<String> = _playlistCreated

    // ===== МЕТОДЫ =====

    // Обновить название плейлиста
    fun updatePlaylistName(name: String) {
        _playlistName.value = name
        updateCreateButtonState()
    }

    // Обновить описание плейлиста
    fun updatePlaylistDescription(description: String) {
        _playlistDescription.value = description
    }

    // Установить URI выбранного изображения обложки
    fun setCoverImage(uri: Uri?) {
        _coverImageUri.value = uri
    }

    // Проверить, есть ли несохранённые данные
    fun hasUnsavedData(): Boolean {
        return !_playlistName.value.isNullOrEmpty() ||
                !_playlistDescription.value.isNullOrEmpty() ||
                _coverImageUri.value != null
    }

    // Создать плейлист
    fun createPlaylist(saveImageCallback: (Uri) -> String?) {
        val name = _playlistName.value ?: return

        viewModelScope.launch {
            // Сохраняем изображение обложки (если выбрано)
            val coverImagePath = _coverImageUri.value?.let { uri ->
                saveImageCallback(uri) // Функция из Fragment для сохранения
            }

            // Создаём плейлист через Interactor
            playlistsInteractor.createPlaylist(
                name = name,
                description = _playlistDescription.value,
                coverImagePath = coverImagePath
            )

            // Отправляем событие об успешном создании
            _playlistCreated.postValue(name)
        }
    }

    // Обновить состояние кнопки "Создать"
    private fun updateCreateButtonState() {
        _isCreateButtonEnabled.value = !_playlistName.value.isNullOrEmpty()
    }
}
