package com.practicum.playlistmaker.presentation.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SettingsInteractor

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        Log.d("SettingsActivity", "onCreate started")

        //settingsInteractor = Creator.provideSettingsInteractor(this)
        settingsInteractor = Creator.provideSettingsInteractor()
        Log.d("SettingsActivity", "Interactor created")

        setupToolbar()
        setupThemeSwitcher()
        setupShareButton()
        setupSupportButton()
        setupTermsButton()

        Log.d("SettingsActivity", "onCreate completed")
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.settings_toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupThemeSwitcher() {
        val themeSwitch = findViewById<SwitchMaterial>(R.id.themeSwitch)

        // Используем интерактор для получения текущей темы
        themeSwitch.isChecked = settingsInteractor.getDarkTheme()

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Используем интерактор для изменения темы
            settingsInteractor.setDarkTheme(isChecked)
        }
    }

    private fun setupShareButton() {
        findViewById<MaterialTextView>(R.id.shareTextView).setOnClickListener {
            // Используем интерактор для приложения
            settingsInteractor.shareApp()
        }
    }

    private fun setupSupportButton() {
        findViewById<MaterialTextView>(R.id.supportTextView).setOnClickListener {
            // Используем интерактор для связи с поддержкой
            settingsInteractor.support()
        }
    }

    private fun setupTermsButton() {
        findViewById<MaterialTextView>(R.id.termsTextView).setOnClickListener {
            // Используем интерактор для открытия условий использования
            settingsInteractor.openTerms()
        }
    }
}
