package com.practicum.playlistmaker.settings.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.practicum.playlistmaker.settings.domain.models.EmailData

class ExternalNavigator(private val context: Context) {

    fun shareLink(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        context.startActivity(intent)
    }

    fun openEmail(emailData: EmailData) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${emailData.email}")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData.email))
            putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
            putExtra(Intent.EXTRA_TEXT, emailData.message)
        }
        context.startActivity(Intent.createChooser(intent, SharingConstants.CHOOSE_EMAIL_CLIENT))
    }
}