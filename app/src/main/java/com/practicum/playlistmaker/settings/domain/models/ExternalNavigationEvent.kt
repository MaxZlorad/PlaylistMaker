package com.practicum.playlistmaker.settings.domain.models

sealed class ExternalNavigationEvent {
    data class ShareApp(val shareText: String) : ExternalNavigationEvent()
    data class OpenTerms(val termsUrl: String) : ExternalNavigationEvent()
    data class OpenSupport(
        val supportEmail: String,
        val supportSubject: String,
        val supportMessage: String,
        val chooseEmailClient: String
    ) : ExternalNavigationEvent()
}
