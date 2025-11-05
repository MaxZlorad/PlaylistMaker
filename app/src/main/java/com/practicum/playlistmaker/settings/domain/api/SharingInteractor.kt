package com.practicum.playlistmaker.settings.domain.api

import com.practicum.playlistmaker.settings.domain.models.ExternalNavigationEvent

interface SharingInteractor {
    fun getShareAppEvent(): ExternalNavigationEvent
    fun getOpenTermsEvent(): ExternalNavigationEvent
    fun getOpenSupportEvent(): ExternalNavigationEvent

}