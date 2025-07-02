package com.practicum.playlistmaker

import java.io.Serializable

data class Track(
    val trackId: String,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String
): Serializable