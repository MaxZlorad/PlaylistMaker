package com.practicum.playlistmaker.main.ui.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.ui.view.MediaLibraryActivity
import com.practicum.playlistmaker.search.ui.view.SearchActivity
import com.practicum.playlistmaker.settings.ui.view.SettingsActivity
import com.practicum.playlistmaker.main.ui.view_model.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.getFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getThemeState { isDarkTheme ->
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        setContentView(R.layout.activity_main)

        // Глобальный обработчик ошибок
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("APP_CRASH", "Crash in thread: ${thread.name}", exception)
            exception.printStackTrace()
        }

        val searchButton: Button = findViewById(R.id.searchButton)
        val mediaLibraryButton: Button = findViewById(R.id.mediaLibraryButton)
        val settingsButton: Button = findViewById(R.id.settingsButton)

        setupClickListeners(searchButton, mediaLibraryButton, settingsButton)
    }

    private fun setupClickListeners(
        searchButton: Button,
        mediaLibraryButton: Button,
        settingsButton: Button
    ) {
        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        mediaLibraryButton.setOnClickListener {
            startActivity(Intent(this, MediaLibraryActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}