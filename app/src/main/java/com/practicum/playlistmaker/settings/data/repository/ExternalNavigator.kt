package com.practicum.playlistmaker.settings.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.practicum.playlistmaker.R

class ExternalNavigator(private val context: Context) {

    fun shareLink() {
        val shareText = context.getString(R.string.share_message)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    fun openLink() {
        val termsUrl = context.getString(R.string.terms_url)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(termsUrl)
        }
        context.startActivity(intent)
    }

    fun openEmail() {
        val supportEmail = context.getString(R.string.support_email)
        val supportSubject = context.getString(R.string.support_subject)
        val supportMessage = context.getString(R.string.support_message)
        val chooseEmailClient = context.getString(R.string.choose_email_client)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$supportEmail")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
            putExtra(Intent.EXTRA_SUBJECT, supportSubject)
            putExtra(Intent.EXTRA_TEXT, supportMessage)
        }
        context.startActivity(Intent.createChooser(intent, chooseEmailClient))
    }
}