package com.practicum.playlistmaker.settings.data.repository

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.settings.domain.api.StringRepository

class StringRepositoryImpl(private val context: Context) : StringRepository {

    override fun getShareMessage(): String {
        return context.getString(R.string.share_message)
    }

    override fun getTermsUrl(): String {
        return context.getString(R.string.terms_url)
    }

    override fun getSupportEmail(): String {
        return context.getString(R.string.support_email)
    }

    override fun getSupportSubject(): String {
        return context.getString(R.string.support_subject)
    }

    override fun getSupportMessage(): String {
        return context.getString(R.string.support_message)
    }

    override fun getChooseEmailClient(): String {
        return context.getString(R.string.choose_email_client)
    }
}