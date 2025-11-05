package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.impl.TracksInteractorImpl
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.api.SharingInteractor
import com.practicum.playlistmaker.settings.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.settings.domain.impl.SharingInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    factory<TracksInteractor> { TracksInteractorImpl(get(), get()) }

    factory<SharingInteractor> { SharingInteractorImpl(get()) }

    factory<SettingsInteractor> { SettingsInteractorImpl(get()) }

}