package com.practicum.playlistmaker.settings.domain.impl

import com.practicum.playlistmaker.settings.domain.api.ExternalNavigator
import com.practicum.playlistmaker.settings.domain.api.SharingInteractor
import com.practicum.playlistmaker.settings.domain.models.ExternalNavigationEvent

class SharingInteractorImpl : SharingInteractor {

    override fun getShareAppEvent(): ExternalNavigationEvent {
        return ExternalNavigationEvent.ShareApp("Скачайте крутое приложение для создания плейлистов!")
    }

    override fun getOpenTermsEvent(): ExternalNavigationEvent {
        return ExternalNavigationEvent.OpenTerms("https://example.com/terms")
    }

    override fun getOpenSupportEvent(): ExternalNavigationEvent {
        return ExternalNavigationEvent.OpenSupport(
            supportEmail = "support@playlistmaker.com",
            supportSubject = "Поддержка PlaylistMaker",
            supportMessage = "Здравствуйте, у меня вопрос по приложению...",
            chooseEmailClient = "Выберите email клиент"
        )
    }
}