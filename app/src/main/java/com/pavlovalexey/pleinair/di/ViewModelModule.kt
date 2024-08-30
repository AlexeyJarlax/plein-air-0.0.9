package com.pavlovalexey.pleinair.di

import com.pavlovalexey.pleinair.settings.ui.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SettingsViewModel(get())
    }
}