package com.practicum.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.domain.models.Track
import com.practicum.playlistmaker.player.domain.models.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import kotlinx.coroutines.Job
import java.io.IOException

class PlayerViewModel : ViewModel() {

    private val mediaPlayer = MediaPlayer()
    private var progressJob: Job? = null

    private val _playbackState = MutableLiveData<PlaybackState>(PlaybackState.Default)
    val playbackState: LiveData<PlaybackState> = _playbackState

    private val _currentPosition = MutableLiveData<Long>(0L)
    val currentPosition: LiveData<Long> = _currentPosition

    private var currentTrack: Track? = null

    fun preparePlayer(track: Track) {
        currentTrack = track
        try {
            mediaPlayer.reset() // Очистить предыдущий трек
            track.previewUrl?.let { url ->
                mediaPlayer.setDataSource(url) // Установить URL аудио
                mediaPlayer.prepareAsync() // Подготовить асинхронно

                // Слушатель готовности
                mediaPlayer.setOnPreparedListener {
                    _playbackState.value = PlaybackState.Prepared(track)
                    _currentPosition.value = 0L
                }

                mediaPlayer.setOnCompletionListener {
                    playbackCompleted()
                }

                // Слушатель завершения трека
                mediaPlayer.setOnErrorListener { _, what, extra ->
                    _playbackState.value = PlaybackState.Error("Playback error: $what, $extra")
                    false
                }
            }
        } catch (e: IOException) {
            _playbackState.value = PlaybackState.Error("Failed to load audio: ${e.message}")
        }
    }

    fun startPlayback() {
        // Проверяем, что плеер не играет и подготовлен
        if (!mediaPlayer.isPlaying) {
            try {
                mediaPlayer.start() // воспроизведение
                _playbackState.value = PlaybackState.Playing
                startProgressUpdates() // обновление прогресса
            } catch (e: IllegalStateException) {
                // обработка если плеер не готов
                _playbackState.value = PlaybackState.Error("Player not prepared")
            }
        }
    }

    fun pausePlayback() {
        mediaPlayer.pause() // пауза
        _playbackState.value = PlaybackState.Paused
        stopProgressUpdates() // Остановить обновление прогресса
    }

    fun stopPlayback() {
        try {
            // ДОБАВИТЬ: безопасная остановка
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        } catch (e: IllegalStateException) {
            // Игнорируем ошибки при остановке
        }
        _playbackState.value = PlaybackState.Stopped
        _currentPosition.value = 0L
        stopProgressUpdates()
    }

    // Обновление прогресса воспроизведения
    private fun startProgressUpdates() {
        progressJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                _currentPosition.value = mediaPlayer.currentPosition.toLong() // Взять позицию
                delay(300)
            }
            // Обновляем позицию когда трек закончил играть
            if (!mediaPlayer.isPlaying) {
                _currentPosition.value = mediaPlayer.currentPosition.toLong()
            }
        }
    }

    // Остановка обновления прогресса
    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun playbackCompleted() {
        stopProgressUpdates()
        _currentPosition.value = 0L
        _playbackState.value = PlaybackState.Completed
    }

    fun getFormattedTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release() // Освободить ресурсы плеера
        stopProgressUpdates()
    }
}