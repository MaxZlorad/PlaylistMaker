package com.practicum.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.presentation.library.MediaLibraryActivity
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.settings.SettingsActivity
import com.practicum.playlistmaker.presentation.search.SearchActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Глобальный обработчик ошибок
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("APP_CRASH", "Crash in thread: ${thread.name}", exception)
            exception.printStackTrace()
        }

        val searchButton: Button = findViewById(R.id.searchButton)
        val mediaLibraryButton: Button = findViewById(R.id.mediaLibraryButton)
        val settingsButton: Button = findViewById(R.id.settingsButton)

        // Применяем сохраненную тему при запуске
        val settingsInteractor = Creator.provideSettingsInteractor(this)
        val darkTheme = settingsInteractor.getDarkTheme()

        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        mediaLibraryButton.setOnClickListener {
            val intent = Intent(this, MediaLibraryActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}