package com.practicum.playlistmaker

import android.app.Application
import com.practicum.playlistmaker.di.dataModule
import com.practicum.playlistmaker.di.interactorModule
import com.practicum.playlistmaker.di.repositoryModule
import com.practicum.playlistmaker.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PlaylistMakerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        //Creator.setContext(this)

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@PlaylistMakerApp)
            printLogger(Level.DEBUG)
            modules(
                dataModule,
                repositoryModule,
                interactorModule,
                viewModelModule
            )
        }
    }
}
