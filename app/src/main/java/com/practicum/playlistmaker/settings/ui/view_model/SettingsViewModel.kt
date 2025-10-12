package com.practicum.playlistmaker.settings.ui.view_model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.core.di.Creator
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    private val _needRecreate = MutableLiveData<Boolean>()
    val needRecreate: LiveData<Boolean> = _needRecreate

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
            Log.d("SettingsViewModel", "Setting dark theme to: $enabled")
            settingsInteractor.setDarkTheme(enabled)
            _themeState.value = enabled
            //onThemeChanged() // Вызываем callback для пересоздания активности
            //_needRecreate.value = true // Сигнал для пересоздания
        }
    }

    fun onRecreated() {
        _needRecreate.value = false // Сбрасываем после пересоздания
    }

    fun shareApp() {
        viewModelScope.launch {
            settingsInteractor.shareApp()
        }
    }

    fun support() {
        viewModelScope.launch {
            settingsInteractor.support()
        }
    }

    fun openTerms() {
        viewModelScope.launch {
            settingsInteractor.openTerms()
        }
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsInteractor = Creator.provideSettingsInteractor(context)
                )
            }
        }
    }
}