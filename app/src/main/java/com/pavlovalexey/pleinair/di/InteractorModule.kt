package com.pavlovalexey.pleinair.di

import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    factory<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }
}

