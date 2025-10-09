package com.practicum.playlistmaker

import android.app.Application

class PlaylistMakerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Сохраняем application context в Creator
        Creator.setContext(applicationContext)
    }
}