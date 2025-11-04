package com.practicum.playlistmaker.di

import org.koin.dsl.module

import android.content.Context
import com.practicum.playlistmaker.search.data.network.ItunesApiService
import org.koin.android.ext.koin.androidContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.practicum.playlistmaker.di.NamedConstants.HISTORY_PREFS
import com.practicum.playlistmaker.di.NamedConstants.SETTINGS_PREFS
import com.practicum.playlistmaker.settings.data.repository.ExternalNavigatorImpl
import com.practicum.playlistmaker.settings.domain.api.ExternalNavigator
import org.koin.core.qualifier.named

val dataModule = module {

    // Network
    single<ItunesApiService> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApiService::class.java)
    }

    // SharedPreferences для истории
    single(named(HISTORY_PREFS)) {
        androidContext().getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
    }

    // SharedPreferences для настроек
    single(named(SETTINGS_PREFS)) {
        androidContext().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
    }

    factory { Gson() }

    // ExternalNavigator
    factory<ExternalNavigator> {
        ExternalNavigatorImpl(androidContext())
    }

}
