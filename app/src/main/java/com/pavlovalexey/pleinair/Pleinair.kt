package com.pavlovalexey.pleinair

import android.app.Application
import com.google.firebase.FirebaseApp
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Pleinair : Application() {

    @Inject
    lateinit var settings: SettingsInteractor

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Убедитесь, что SettingsInteractor инициализирован
        if (::settings.isInitialized) {
            applyDayNightTheme()
        } else {
            // Обработайте случай, если SettingsInteractor не инициализирован
        }
    }

    private fun applyDayNightTheme() {
        settings.applyTheme()
    }
}