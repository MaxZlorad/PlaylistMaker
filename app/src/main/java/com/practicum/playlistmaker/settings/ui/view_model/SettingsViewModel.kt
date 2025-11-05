package com.practicum.playlistmaker.settings.ui.view_model

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.api.SharingInteractor
import com.practicum.playlistmaker.settings.domain.models.ExternalNavigationEvent
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _navigationEvent = MutableLiveData<ExternalNavigationEvent?>()
    val navigationEvent: LiveData<ExternalNavigationEvent?> = _navigationEvent

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    init {
        loadTheme()
        Log.d("SettingsViewModel", "SharingInteractor: $sharingInteractor")
    }

    fun shareApp() {
        Log.d("SettingsViewModel", "shareApp called")
        _navigationEvent.value = sharingInteractor.getShareAppEvent()
    }

    fun openTerms() {
        _navigationEvent.value = sharingInteractor.getOpenTermsEvent()
    }

    fun openSupport() {
        _navigationEvent.value = sharingInteractor.getOpenSupportEvent()
    }

    fun navigationHandled() {
        _navigationEvent.value = null
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
            //applyTheme(enabled) // Применяем тему сразу
        }
    }
}