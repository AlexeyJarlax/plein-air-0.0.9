package com.pavlovalexey.pleinair.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.pavlovalexey.pleinair.settings.data.SettingsRepositoryImpl
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractorImpl
import com.pavlovalexey.pleinair.settings.domain.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(context, sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideSettingsInteractor(settingsRepository: SettingsRepository): SettingsInteractor {
        return SettingsInteractorImpl(settingsRepository)
    }
}