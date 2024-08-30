package com.pavlovalexey.pleinair.di

import android.content.Context
import android.media.MediaPlayer
import com.google.gson.Gson
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import org.koin.dsl.module
import com.pavlovalexey.pleinair.settings.data.SettingsRepositoryImpl
import com.pavlovalexey.pleinair.settings.domain.SettingsRepository
import org.koin.android.ext.koin.androidContext

    val dataModule = module {

        single {
            androidContext()
                .getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
        }

        single<SettingsRepository> {
            SettingsRepositoryImpl(get(), get())
        }

        factory { Gson() }
        factory { MediaPlayer() }
    }
