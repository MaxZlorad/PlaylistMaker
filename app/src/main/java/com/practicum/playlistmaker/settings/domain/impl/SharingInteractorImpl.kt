package com.practicum.playlistmaker.settings.domain.impl

import com.practicum.playlistmaker.settings.data.repository.ExternalNavigator
import com.practicum.playlistmaker.settings.domain.api.SharingInteractor

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator
) : SharingInteractor {

    override fun shareApp() {
        externalNavigator.shareLink()
    }

    override fun openTerms() {
        externalNavigator.openLink()
    }

    override fun openSupport() {
        externalNavigator.openEmail()
    }
}