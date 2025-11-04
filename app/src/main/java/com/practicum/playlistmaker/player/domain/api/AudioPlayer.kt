package com.practicum.playlistmaker.player.domain.api

interface AudioPlayer {
    fun prepare(url: String, onPrepared: () -> Unit, onError: (String) -> Unit)
    fun start()
    fun pause()
    fun stop()
    fun release()
    fun reset()
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    fun isPlaying(): Boolean
    fun setOnCompletionListener(listener: () -> Unit)
    /* Что б не забыть -эти методы внутри prepare:
    fun setDataSource(url: String)
    fun prepareAsync()
    fun setOnPreparedListener(listener: () -> Unit)
    fun setOnErrorListener(listener: (what: Int, extra: Int) -> Boolean)*/
}