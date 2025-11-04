package com.practicum.playlistmaker.settings.domain.impl

import com.practicum.playlistmaker.settings.domain.api.SharingInteractor
import com.practicum.playlistmaker.settings.domain.models.ExternalNavigationEvent
import com.practicum.playlistmaker.settings.domain.api.StringRepository

class SharingInteractorImpl(
    private val stringRepository: StringRepository
) : SharingInteractor {

    override fun getShareAppEvent(): ExternalNavigationEvent {
        return ExternalNavigationEvent.ShareApp(stringRepository.getShareMessage())
    }

    override fun getOpenTermsEvent(): ExternalNavigationEvent {
        return ExternalNavigationEvent.OpenTerms(stringRepository.getTermsUrl())
    }

    override fun getOpenSupportEvent(): ExternalNavigationEvent {
        return ExternalNavigationEvent.OpenSupport(
            supportEmail = stringRepository.getSupportEmail(),
            supportSubject = stringRepository.getSupportSubject(),
            supportMessage = stringRepository.getSupportMessage(),
            chooseEmailClient = stringRepository.getChooseEmailClient()
        )
    }
}