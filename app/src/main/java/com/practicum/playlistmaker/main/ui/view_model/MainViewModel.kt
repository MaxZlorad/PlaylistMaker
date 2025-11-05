package com.practicum.playlistmaker.main.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor


class MainViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    fun getThemeState(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            //val darkTheme = Creator.provideSettingsInteractor(context).getDarkTheme()
            val darkTheme = settingsInteractor.getDarkTheme()
            callback(darkTheme)
        }
    }
}