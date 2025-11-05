package com.practicum.playlistmaker.settings.domain.api

interface StringRepository {
    fun getShareMessage(): String
    fun getTermsUrl(): String
    fun getSupportEmail(): String
    fun getSupportSubject(): String
    fun getSupportMessage(): String
    fun getChooseEmailClient(): String
}