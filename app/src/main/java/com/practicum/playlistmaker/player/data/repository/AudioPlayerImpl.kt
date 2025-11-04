package com.practicum.playlistmaker.player.data.repository

import android.media.MediaPlayer
import com.practicum.playlistmaker.player.domain.api.AudioPlayer
import java.io.IOException

class AudioPlayerImpl : AudioPlayer {

    private val mediaPlayer = MediaPlayer()

    // Единый метод для подготовки трека
    override fun prepare(url: String, onPrepared: () -> Unit, onError: (String) -> Unit) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()

            // Устанавливаем слушатели
            mediaPlayer.setOnPreparedListener { onPrepared() }
            mediaPlayer.setOnErrorListener { _, what, extra ->
                onError("Playback error: $what, $extra")
                false
            }
        } catch (e: IOException) {
            onError("Failed to load audio: ${e.message}")
        }
    }

    override fun start() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun stop() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        } catch (e: IllegalStateException) {
            // Игнорируем ошибки при остановке
        }
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun reset() {
        mediaPlayer.reset()
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer.setOnCompletionListener { listener() }
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun getDuration(): Int {
        return mediaPlayer.duration
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }
}