package com.pavlovalexey.pleinair

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pavlovalexey.pleinair.calendar.adapter.EventAdapter
import com.pavlovalexey.pleinair.calendar.data.EventRepository
import com.pavlovalexey.pleinair.calendar.ui.calendar.CalendarViewModel
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import com.pavlovalexey.pleinair.settings.data.SettingsRepositoryImpl
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractorImpl
import com.pavlovalexey.pleinair.settings.domain.SettingsRepository
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import com.pavlovalexey.pleinair.utils.ui.IconStateUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Provides
//    @ApplicationContext
//    fun provideApplicationContext(application: Application): Context {
//        return application.applicationContext
//    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
    }

////////// Firebase
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, googleSignInOptions)
    }

    ////////// Repository
    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(context, sharedPreferences)
    }
    @Provides
    fun provideEventRepository(
        firebase: FirebaseFirestore,
        sharedPreferences: SharedPreferences
    ): EventRepository {
        return EventRepository(firebase, sharedPreferences)
    }

    ////////// Interactor
    @Provides
    @Singleton
    fun provideSettingsInteractor(settingsRepository: SettingsRepository): SettingsInteractor {
        return SettingsInteractorImpl(settingsRepository)
    }

    ////////// Adapter
    @Provides
    @Singleton
    fun provideEventAdapter(): EventAdapter {
        return EventAdapter()
    }

    ////////// ViewModel
    @Provides
    @Singleton
    fun provideProfileViewModel(
        firebaseUserManager: FirebaseUserManager,
        auth: FirebaseAuth,
        sharedPreferences: SharedPreferences
    ): ProfileViewModel {
        return ProfileViewModel(firebaseUserManager, auth, sharedPreferences)
    }
    @Provides
    @Singleton
    fun provideCalendarViewModel(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ): CalendarViewModel {
        return CalendarViewModel(firebaseAuth, firebaseFirestore)
    }

////////// Utils
    @Provides
    @Singleton
    fun provideLoginAndUserUtils(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        sharedPreferences: SharedPreferences
    ): LoginAndUserUtils {
        return LoginAndUserUtils(context, firebaseAuth, firebaseFirestore, sharedPreferences)
    }
    @Provides
    @Singleton
    fun provideFirebaseUserManager(
        @ApplicationContext appContext: Context,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        sharedPreferences: SharedPreferences
    ): FirebaseUserManager {
        return FirebaseUserManager(appContext, auth, firestore, storage, sharedPreferences)
    }
    @Provides
    @Singleton
    fun provideIconStateUtils(sharedPreferences: SharedPreferences): IconStateUtils {
        return IconStateUtils(sharedPreferences)
    }
}