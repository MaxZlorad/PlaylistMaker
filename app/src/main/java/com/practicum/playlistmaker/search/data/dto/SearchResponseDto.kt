package com.practicum.playlistmaker.search.data.dto

data class SearchResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
)