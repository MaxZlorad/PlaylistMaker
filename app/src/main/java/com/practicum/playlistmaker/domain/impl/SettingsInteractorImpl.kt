package com.practicum.playlistmaker.domain.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SettingsInteractor
import com.practicum.playlistmaker.domain.api.SettingsRepository

class SettingsInteractorImpl(
    private val repository: SettingsRepository,
    private val context: Context
) : SettingsInteractor {

    override fun getDarkTheme(): Boolean {
        // Получаем тему через репозиторий
        return repository.getDarkTheme()
    }

    override fun setDarkTheme(enabled: Boolean) {
        // Устанавливаем тему через репозиторий
        repository.setDarkTheme(enabled)
        applyTheme(enabled)
    }

    private fun applyTheme(darkThemeEnabled: Boolean) {
        // Применяем тему напрямую
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun shareApp() {
        // Создаем интент для приложения
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message))
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    override fun support() {
        // Создаем интент для отправки email в поддержку
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.support_subject))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.support_message))
        }
        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.choose_email_client)))
    }

    override fun openTerms() {
        // Создаем интент для открытия условий использования в браузере
        val termsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.terms_url)))
        context.startActivity(termsIntent)
    }
}