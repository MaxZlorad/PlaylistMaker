package com.practicum.playlistmaker.data.dto

data class SearchResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
)