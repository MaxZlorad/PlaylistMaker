package com.practicum.playlistmaker.settings.domain.impl

import com.practicum.playlistmaker.settings.domain.models.EmailData
import com.practicum.playlistmaker.settings.data.repository.ExternalNavigator
import com.practicum.playlistmaker.settings.data.repository.SharingConstants
import com.practicum.playlistmaker.settings.domain.api.SharingInteractor

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator
) : SharingInteractor {

    override fun shareApp() {
        externalNavigator.shareLink(SharingConstants.SHARE_TEXT)
    }

    override fun openTerms() {
        externalNavigator.openLink(SharingConstants.TERMS_URL)
    }

    override fun openSupport() {
        externalNavigator.openEmail(EmailData(
            email = SharingConstants.SUPPORT_EMAIL,
            subject = SharingConstants.SUPPORT_SUBJECT,
            message = SharingConstants.SUPPORT_MESSAGE
        ))
    }
}