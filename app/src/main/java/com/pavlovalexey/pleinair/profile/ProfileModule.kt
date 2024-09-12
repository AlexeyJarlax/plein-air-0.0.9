package com.pavlovalexey.pleinair.profile

import android.app.Application
import android.content.SharedPreferences
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object ProfileModule {

    @Provides
    @FragmentScoped
    fun provideProfileViewModel(
        @ApplicationContext appContext: Application,
        firebaseUserManager: FirebaseUserManager,
        sharedPreferences: SharedPreferences
    ): ProfileViewModel {
        return ProfileViewModel(appContext, firebaseUserManager, sharedPreferences)
    }

    @Provides
    @FragmentScoped
    fun provideSharedPreferences(@ApplicationContext appContext: Application): SharedPreferences {
        return appContext.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Application.MODE_PRIVATE)
    }

    @Provides
    @FragmentScoped
    fun provideFirebaseUserManager(@ApplicationContext appContext: Application): FirebaseUserManager {
        return FirebaseUserManager(appContext)
    }
}