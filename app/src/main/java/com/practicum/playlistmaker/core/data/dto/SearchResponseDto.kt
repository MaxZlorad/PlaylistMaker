package com.practicum.playlistmaker.core.data.dto

data class SearchResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
)