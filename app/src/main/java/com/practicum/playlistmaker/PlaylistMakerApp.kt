package com.practicum.playlistmaker

import android.app.Application
import com.practicum.playlistmaker.core.di.Creator

class PlaylistMakerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Creator.setContext(this)
    }
}
