package com.pavlovalexey.pleinair

import android.content.Context
import android.content.SharedPreferences
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideFirebaseUserManager(@ApplicationContext appContext: Context): FirebaseUserManager {
        return FirebaseUserManager(appContext)
    }

    @Provides
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
    }
}