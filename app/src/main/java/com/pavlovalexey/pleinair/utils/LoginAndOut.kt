package com.pavlovalexey.pleinair.utils

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth

class LoginAndOut(application: Application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val sharedPreferences = application.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)

    fun logout() {
        auth.signOut()
        val prefs = sharedPreferences.all
        val editor = sharedPreferences.edit()
        for (key in prefs.keys) {
            if (key != "all_terms_accepted") {
                editor.remove(key)
            }
        }
        editor.apply()
    }

    companion object {
        fun logout(application: Application) {
            val loginAndOut = LoginAndOut(application)
            loginAndOut.logout()
        }
    }
}