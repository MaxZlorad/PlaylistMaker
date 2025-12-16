package com.practicum.playlistmaker.library.domain.models

import com.practicum.playlistmaker.search.domain.models.Track

//Sealed class для состояний экрана "Избранные треки" (пуст да/не)
sealed class FavoriteTracksState {

    object Empty : FavoriteTracksState()

    data class Content(val tracks: List<Track>) : FavoriteTracksState()
}
