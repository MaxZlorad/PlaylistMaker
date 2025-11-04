package com.practicum.playlistmaker.settings.ui.view

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.settings.domain.api.ExternalNavigator
import com.practicum.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel  // Koin viewModel
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {

    //private val viewModel: SettingsViewModel by viewModels()
    private val viewModel: SettingsViewModel by viewModel()  // viewModel() от Koin

    private val externalNavigator: ExternalNavigator by inject()

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
        Log.d("SettingsActivity", "ViewModel: $viewModel")
        Log.d("SettingsActivity", "ExternalNavigator: $externalNavigator")

        setupToolbar()
        setupThemeSwitcher()
        setupShareButton()
        setupSupportButton()
        setupTermsButton()
        observeTheme() // наблюдение за изменением темы
        observeNavigation()

        Log.d("SettingsActivity", "onCreate completed")
    }

    private fun observeTheme() {
        viewModel.themeState.observe(this) { isDarkTheme ->
            applyTheme(isDarkTheme)
        }
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
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
            // Подписываемся на состояние темы из ViewModel
            viewModel.themeState.observe(this) { isDarkTheme ->
                themeSwitch.isChecked = isDarkTheme
            }

            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
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
            viewModel.shareApp()
        }
    }
    private fun setupSupportButton() {
        Log.d("SettingsActivity", "setupSupportButton started")
        findViewById<MaterialTextView>(R.id.supportTextView).setOnClickListener {
            viewModel.openSupport()
        }
    }

    private fun setupTermsButton() {
        Log.d("SettingsActivity", "setupTermsButton started")
        findViewById<MaterialTextView>(R.id.termsTextView).setOnClickListener {
            viewModel.openTerms()
        }
    }

    private fun observeNavigation() {
        viewModel.navigationEvent.observe(this) { event ->
            event?.let {
                externalNavigator.navigate(it) // Через DI
                viewModel.navigationHandled()
            }
        }
    }
}
