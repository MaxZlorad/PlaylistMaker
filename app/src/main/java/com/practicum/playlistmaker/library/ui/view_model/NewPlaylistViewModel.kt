package com.practicum.playlistmaker.library.ui.view_model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.models.NewPlaylistState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.io.File

// ViewModel для экрана создания плейлиста
class NewPlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    /// Переменные:

    // Единое состояние экрана (вместо 4 отдельных LiveData)
    private val _state = MutableLiveData(NewPlaylistState())
    val state: LiveData<NewPlaylistState> = _state

    // Событие успешного создания плейлиста
    private val _playlistCreated = MutableLiveData<String>() // Название созданного плейлиста
    val playlistCreated: LiveData<String> = _playlistCreated

    // Режим редактирования
    private var playlistId: Int? = null
    private var isEditMode = false
    private var originalCoverPath: String? = null
    private var coverWasChanged = false

    /// Функции:

    // Загрузить плейлист для редактирования
    fun loadPlaylist(id: Int) {
        playlistId = id
        isEditMode = true
        coverWasChanged = false

        viewModelScope.launch {
            val (playlist, _) = playlistsInteractor.getPlaylistWithTracks(id).first()
            originalCoverPath = playlist.coverImagePath

            Log.d("NewPlaylistVM", "loadPlaylist: originalCoverPath = $originalCoverPath")

            _state.value = NewPlaylistState(
                playlistName = playlist.name,
                playlistDescription = playlist.description ?: "",
                coverImageUri = playlist.coverImagePath?.let { path ->
                    // ← ИСПРАВЬ: используй Uri.fromFile для локальных файлов
                    if (path.startsWith("content://") || path.startsWith("file://")) {
                        Uri.parse(path)
                    } else {
                        Uri.fromFile(File(path))
                    }
                },
                isCreateButtonEnabled = true,
                isEditMode = true
            )
        }
    }

    // Обновить название плейлиста
    fun updatePlaylistName(name: String) {
        _state.value = _state.value?.copy(
            playlistName = name,
            isCreateButtonEnabled = name.trim().isNotEmpty()
        )
    }

    // Обновить описание плейлиста
    fun updatePlaylistDescription(description: String) {
        _state.value = _state.value?.copy(playlistDescription = description)
    }

    // Установить URI выбранного изображения обложки
    fun setCoverImage(uri: Uri?) {
        _state.value = _state.value?.copy(coverImageUri = uri)
        coverWasChanged = true
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
        if (name.trim().isEmpty()) return

        viewModelScope.launch {
            if (isEditMode && playlistId != null) {
                // Режим редактирования
                val coverPath = when { // Определяем путь к обложке
                    !coverWasChanged && originalCoverPath != null -> { originalCoverPath }
                    currentState.coverImageUri != null -> { currentState.coverImageUri.toString() }
                    else -> { null
                    }
                }
                playlistsInteractor.updatePlaylist(
                    playlistId = playlistId!!,
                    title = name,
                    description = currentState.playlistDescription.takeIf { it.isNotEmpty() } ?: "",
                    coverImagePath = coverPath
                )
            } else {

                // Режим создания // Создаём плейлист через Interactor
                playlistsInteractor.createPlaylist(
                    name = name,
                    description = currentState.playlistDescription.takeIf { it.isNotEmpty() },
                    coverImageUri = currentState.coverImageUri // Uri напрямую
                )
            }

            // Отправляем событие об успешном создании
            _playlistCreated.postValue(name)
        }
    }
}
