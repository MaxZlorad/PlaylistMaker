package com.practicum.playlistmaker.settings.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.practicum.playlistmaker.settings.domain.api.ExternalNavigator
import com.practicum.playlistmaker.settings.domain.models.ExternalNavigationEvent

class ExternalNavigatorImpl(private val context: Context) : ExternalNavigator {

    override fun navigate(event: ExternalNavigationEvent) {
        Log.d("ExternalNavigator", "navigate called with: $event")
        try {
            when (event) {
                is ExternalNavigationEvent.ShareApp -> shareLink(event.shareText)
                is ExternalNavigationEvent.OpenTerms -> openLink(event.termsUrl)
                is ExternalNavigationEvent.OpenSupport -> openEmail(
                    event.supportEmail,
                    event.supportSubject,
                    event.supportMessage,
                    event.chooseEmailClient
                )
            }
        } catch (e: Exception) {
            Log.e("ExternalNavigator", "Navigation failed", e)
        }
    }

    private fun shareLink(shareText: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserIntent = Intent.createChooser(intent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }

    private fun openLink(termsUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(termsUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openEmail(
        supportEmail: String,
        supportSubject: String,
        supportMessage: String,
        chooseEmailClient: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$supportEmail")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
            putExtra(Intent.EXTRA_SUBJECT, supportSubject)
            putExtra(Intent.EXTRA_TEXT, supportMessage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserIntent = Intent.createChooser(intent, chooseEmailClient).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }
}