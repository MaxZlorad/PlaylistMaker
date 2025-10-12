package com.practicum.playlistmaker.core.data.mapper

import com.practicum.playlistmaker.core.data.dto.TrackDto
import com.practicum.playlistmaker.core.domain.models.Track

fun TrackDto.toTrack(): Track {
    return Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}

fun Track.toTrackDto(): TrackDto {
    return TrackDto(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}

fun List<TrackDto>.toTracks(): List<Track> = map { it.toTrack() }
fun List<Track>.toTrackDtos(): List<TrackDto> = map { it.toTrackDto() }