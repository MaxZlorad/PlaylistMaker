package com.practicum.playlistmaker.settings.domain.api

import com.practicum.playlistmaker.settings.domain.models.ExternalNavigationEvent

interface ExternalNavigator {
    fun navigate(event: ExternalNavigationEvent)
}
