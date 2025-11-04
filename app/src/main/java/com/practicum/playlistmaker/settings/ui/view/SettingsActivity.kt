package com.practicum.playlistmaker.settings.ui.view

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.settings.ui.view_model.SettingsViewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.getFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.themeState.observe(this) { isDarkTheme ->
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        setContentView(R.layout.activity_settings)
        Log.d("SettingsActivity", "onCreate started")

        setupToolbar()
        setupThemeSwitcher()
        setupShareButton()
        setupSupportButton()
        setupTermsButton()

        Log.d("SettingsActivity", "onCreate completed")
    }

    private fun setupToolbar() {
        Log.d("SettingsActivity", "setupToolbar started")
        findViewById<MaterialToolbar>(R.id.settings_toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupThemeSwitcher() {
        Log.d("SettingsActivity", "setupThemeSwitcher started")
        try {
            val themeSwitch = findViewById<SwitchMaterial>(R.id.themeSwitch)

            // Устанавливаем текущее состояние темы
            //themeSwitch.isChecked = (application as PlaylistMakerApp).darkTheme

            // Подписываемся на состояние темы из ViewModel
            viewModel.themeState.observe(this) { isDarkTheme ->
                themeSwitch.isChecked = isDarkTheme
            }

            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                // Меняем тему сразу через Application
                //(application as PlaylistMakerApp).switchTheme(isChecked)
                // Меняем тему через ViewModel
                viewModel.setDarkTheme(isChecked)
            }

            Log.d("SettingsActivity", "setupThemeSwitcher completed")
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error in setupThemeSwitcher", e)
        }
    }

    private fun setupShareButton() {
        Log.d("SettingsActivity", "setupShareButton started")
        findViewById<MaterialTextView>(R.id.shareTextView).setOnClickListener {
            // Используем интерактор для приложения
            viewModel.shareApp()
        }
    }

    private fun setupSupportButton() {
        Log.d("SettingsActivity", "setupSupportButton started")
        findViewById<MaterialTextView>(R.id.supportTextView).setOnClickListener {
            // Используем интерактор для связи с поддержкой
            viewModel.support()
        }
    }

    private fun setupTermsButton() {
        Log.d("SettingsActivity", "setupTermsButton started")
        findViewById<MaterialTextView>(R.id.termsTextView).setOnClickListener {
            // Используем интерактор для открытия условий использования
            viewModel.openTerms()
        }
    }
}
