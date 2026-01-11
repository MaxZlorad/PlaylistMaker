package com.practicum.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.models.PlaylistDetailState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailViewModel(
    private val playlistsInteractor: PlaylistsInteractor  // Интерактор для работы с плейлистами
) : ViewModel() {

    // LiveData для состояния экрана
    private val _state = MutableLiveData<PlaylistDetailState>()
    // Публичная LiveData для наблюдения из Fragment
    val state: LiveData<PlaylistDetailState> = _state

    private var currentPlaylistId: Int = 0

    // Загружает плейлист по его ID вместе с треками
    fun loadPlaylist(playlistId: Int) {
        // Сохраняем ID
        currentPlaylistId = playlistId
        // Устанавливаем состояние загрузки
        _state.value = PlaylistDetailState.Loading

        // Запускаем корутину в scope ViewModel
        viewModelScope.launch {
            // Подписываемся на Flow с данными плейлиста и треков
            playlistsInteractor.getPlaylistWithTracks(playlistId)
                .collect { (playlist, tracks) ->
                    // Обрабатываем результат
                    when {
                        // Плейлист не найден (ID = 0)
                        playlist.playlistId == 0 -> {
                            _state.value = PlaylistDetailState.Error
                        }
                        // Плейлист найден, но треков нет
                        tracks.isEmpty() -> {
                            _state.value = PlaylistDetailState.Content(playlist, emptyList())
                        }
                        // Плейлист найден и есть треки
                        else -> {
                            _state.value = PlaylistDetailState.Content(playlist, tracks)
                        }
                    }
                }
        }
    }

    fun removeTrack(trackId: String) {
        viewModelScope.launch {
            playlistsInteractor.removeTrackFromPlaylist(trackId, currentPlaylistId)
            // После удаления перезагружаем плейлист
            loadPlaylist(currentPlaylistId)
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            playlistsInteractor.deletePlaylist(playlistId)
        }
    }

    fun formatDuration(durationMs: Long): String {
        val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(durationMs)
        return "$minutes минут"
    }
}
