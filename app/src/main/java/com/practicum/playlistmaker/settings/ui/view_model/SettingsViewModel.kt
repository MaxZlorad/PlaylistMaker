package com.practicum.playlistmaker.settings.ui.view_model

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.core.di.Creator
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.api.SharingInteractor
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    init {
        loadTheme()
    }

    fun loadTheme() {
        viewModelScope.launch {
            _themeState.value = settingsInteractor.getDarkTheme()
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsInteractor.setDarkTheme(enabled)
            _themeState.value = enabled
            applyTheme(enabled) // Применяем тему сразу
        }
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        // Применяем тему через AppCompatDelegate
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun shareApp() {
        sharingInteractor.shareApp()
    }

    fun support() {
        sharingInteractor.openSupport()
    }

    fun openTerms() {
        sharingInteractor.openTerms()
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    sharingInteractor = Creator.provideSharingInteractor(context),
                    settingsInteractor = Creator.provideSettingsInteractor(context)
                )
            }
        }
    }
}