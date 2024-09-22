package com.pavlovalexey.pleinair

/** Приложение построено Jetpack Compose с сингл-активити и отправной точкой MainActivity.
 * Вместо xml и фрагментов применил Jetpack Compose — фреймворк для создания UI на Android, основанный на декларативном подходе.
 *
 * Этапы входа в приложение:
 * 1 Этап - подписание соглашений в TermsScreen
 * 2 Этап - авторизация в AuthScreen
 * 3 Этап - MainActivity и фрагменты по всему функционалу приложения с навигацией через НавГраф и BottomNavBar. Первый: Профиль фрагмент.
 */

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
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
        MapsInitializer.initialize(this)
        applyDayNightTheme()
    }

    private fun applyDayNightTheme() {
        settings.applyTheme()
    }
}