package com.pavlovalexey.pleinair

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализируем Firebase
        FirebaseApp.initializeApp(this)
    }
}