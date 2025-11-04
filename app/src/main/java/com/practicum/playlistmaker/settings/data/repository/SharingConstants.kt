package com.practicum.playlistmaker.settings.data.repository

import android.content.Context
import com.practicum.playlistmaker.R

object SharingConstants {
    var SHARE_TEXT: String = ""
    var SUPPORT_EMAIL: String = ""
    var SUPPORT_SUBJECT: String = ""
    var SUPPORT_MESSAGE: String = ""
    var TERMS_URL: String = ""
    var CHOOSE_EMAIL_CLIENT: String = ""

    fun initialize(context: Context) {
        SHARE_TEXT = context.getString(R.string.share_message)
        SUPPORT_EMAIL = context.getString(R.string.support_email)
        SUPPORT_SUBJECT = context.getString(R.string.support_subject)
        SUPPORT_MESSAGE = context.getString(R.string.support_message)
        TERMS_URL = context.getString(R.string.terms_url)
        CHOOSE_EMAIL_CLIENT = context.getString(R.string.choose_email_client)
    }
}