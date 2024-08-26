package com.pavlovalexey.pleinair

import android.app.Application
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import com.pavlovalexey.pleinair.di.dataModule
import com.pavlovalexey.pleinair.di.interactorModule
import com.pavlovalexey.pleinair.di.repositoryModule
import com.pavlovalexey.pleinair.di.viewModelModule
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor
import org.koin.android.ext.android.inject
import org.koin.core.context.startKoin

class Pleinair : Application()  {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        runKoinDependencies()
        applyDayNightTheme()
    }

    private fun runKoinDependencies() {
        startKoin {
            androidContext(this@Pleinair)
            modules(dataModule, interactorModule, repositoryModule, viewModelModule)
        }}

    private fun applyDayNightTheme() {
        val settings: SettingsInteractor by inject()
        settings.applyTheme()
    }
}