package com.pavlovalexey.pleinair.di

import com.pavlovalexey.pleinair.settings.data.SettingsRepositoryImpl
import com.pavlovalexey.pleinair.settings.domain.SettingsRepository
import org.koin.dsl.module

    val repositoryModule = module {

        single<SettingsRepository> {
            SettingsRepositoryImpl(get(), get())
        }

    }
