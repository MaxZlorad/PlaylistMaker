package com.practicum.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.player.domain.models.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.practicum.playlistmaker.player.domain.api.AudioPlayer
import kotlinx.coroutines.Job

class PlayerViewModel(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private var progressJob: Job? = null

    private val _playbackState = MutableLiveData<PlaybackState>(PlaybackState.Default)
    val playbackState: LiveData<PlaybackState> = _playbackState

    private val _currentPosition = MutableLiveData<Long>(0L)
    val currentPosition: LiveData<Long> = _currentPosition

    private var currentTrack: Track? = null

    fun preparePlayer(track: Track) {
        currentTrack = track
        track.previewUrl?.let { url ->
            // Используем единый метод prepare с колбэками
            audioPlayer.prepare(
                url = url,
                onPrepared = {
                    _playbackState.value = PlaybackState.Prepared(track)
                    _currentPosition.value = 0L
                },
                onError = { errorMessage ->
                    _playbackState.value = PlaybackState.Error(errorMessage)
                }
            )

            // Устанавливаем слушатель завершения
            audioPlayer.setOnCompletionListener {
                playbackCompleted()
            }
        } ?: run {
            _playbackState.value = PlaybackState.Error("No preview URL available")
        }
    }

    fun startPlayback() {
        if (!audioPlayer.isPlaying()) {
            try {
                audioPlayer.start()
                _playbackState.value = PlaybackState.Playing
                startProgressUpdates()
            } catch (e: IllegalStateException) {
                _playbackState.value = PlaybackState.Error("Player not prepared")
            }
        }
    }

    fun pausePlayback() {
        audioPlayer.pause()
        _playbackState.value = PlaybackState.Paused
        stopProgressUpdates()
    }

    fun stopPlayback() {
        audioPlayer.stop()
        _playbackState.value = PlaybackState.Stopped
        _currentPosition.value = 0L
        stopProgressUpdates()
    }

    private fun startProgressUpdates() {
        progressJob = viewModelScope.launch {
            while (audioPlayer.isPlaying()) {
                _currentPosition.value = audioPlayer.getCurrentPosition().toLong()
                delay(PlayerConstants.PROGRESS_UPDATE_DELAY)
            }
            if (!audioPlayer.isPlaying()) {
                _currentPosition.value = audioPlayer.getCurrentPosition().toLong()
            }
        }
    }

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
        audioPlayer.release()
        stopProgressUpdates()
    }
}