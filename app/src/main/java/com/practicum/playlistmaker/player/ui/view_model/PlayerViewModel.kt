package com.practicum.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.FavoriteTracksInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.player.domain.models.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.practicum.playlistmaker.player.domain.api.AudioPlayer
import kotlinx.coroutines.Job
import com.practicum.playlistmaker.player.domain.models.PlayerScreenState

class PlayerViewModel(
    private val audioPlayer: AudioPlayer,
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private var progressJob: Job? = null

    private var currentTrack: Track? = null

    private val _screenState = MutableLiveData<PlayerScreenState>()
    val screenState: LiveData<PlayerScreenState> = _screenState

    init {
        // Инициализация с дефолтным состоянием
        _screenState.value = PlayerScreenState()
    }

    fun preparePlayer(track: Track) {
        currentTrack = track
        updateState(isFavorite = track.isFavorite)

        track.previewUrl?.let { url ->
            // Используем единый метод prepare с колбэками
            audioPlayer.prepare(
                url = url,
                onPrepared = {
                    updateState(
                        playbackState = PlaybackState.Prepared(track),
                        currentPosition = 0L
                    )
                },
                onError = { errorMessage ->
                    updateState(playbackState = PlaybackState.Error(errorMessage))
                }
            )

            // Устанавливаем слушатель завершения
            audioPlayer.setOnCompletionListener {
                playbackCompleted()
            }
        } ?: run {
            updateState(playbackState = PlaybackState.Error("No preview URL available"))
        }
    }

    fun startPlayback() {
        if (!audioPlayer.isPlaying()) {
            try {
                audioPlayer.start()
                updateState(playbackState = PlaybackState.Playing)
                startProgressUpdates()
            } catch (e: IllegalStateException) {
                updateState(playbackState = PlaybackState.Error("Player not prepared"))
            }
        }
    }

    fun pausePlayback() {
        audioPlayer.pause()
        updateState(playbackState = PlaybackState.Paused)
        stopProgressUpdates()
    }

    fun stopPlayback() {
        audioPlayer.stop()
        updateState(
            playbackState = PlaybackState.Stopped,
            currentPosition = 0L
        )
        stopProgressUpdates()
    }

    private fun startProgressUpdates() {
        progressJob = viewModelScope.launch {
            while (audioPlayer.isPlaying()) {
                updateState(currentPosition = audioPlayer.getCurrentPosition().toLong())
                delay(PlayerConstants.PROGRESS_UPDATE_DELAY)
            }
            if (!audioPlayer.isPlaying()) {
                updateState(currentPosition = audioPlayer.getCurrentPosition().toLong())
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun playbackCompleted() {
        stopProgressUpdates()
        updateState(
            currentPosition = 0L,
            playbackState = PlaybackState.Completed
        )
    }

    fun getFormattedTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Вызывается при нажатии на "Лайк"
    fun onFavoriteClicked() {
        val track = currentTrack ?: return // Проверяем, что трек загружен

        // Запускаем корутину в viewModelScope (автоматически отменится при уничтожении ViewModel)
        viewModelScope.launch {
            if (track.isFavorite) { // флаг показывает текущее состояние в избранном да/нет
                // Если трек уже в избранном — удаляем
                favoriteTracksInteractor.removeTrackFromFavorites(track)
                track.isFavorite = false // Обновляем локальный флаг
            } else {
                // Если трека нет в избранном — добавляем
                favoriteTracksInteractor.addTrackToFavorites(track)
                track.isFavorite = true // Обновляем локальный флаг
            }
            // Публикуем новое состояние в UI
            updateState(isFavorite = track.isFavorite) // публикует изменение в UI через LiveData
        }
    }

    // Обновление стейта
    private fun updateState(
        playbackState: PlaybackState? = null,
        currentPosition: Long? = null,
        isFavorite: Boolean? = null
    ) {
        val currentState = _screenState.value ?: PlayerScreenState()

        _screenState.value = currentState.copy(
            playbackState = playbackState ?: currentState.playbackState,
            currentPosition = currentPosition ?: currentState.currentPosition,
            isFavorite = isFavorite ?: currentState.isFavorite
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        stopProgressUpdates()
    }
}
