package com.practicum.playlistmaker.main.ui.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.core.di.Creator
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import kotlinx.coroutines.launch


class MainViewModel(private val context: Context) : ViewModel() {

    fun getThemeState(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val darkTheme = Creator.provideSettingsInteractor(context).getDarkTheme()
            callback(darkTheme)
        }
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(context)
            }
        }
    }

}