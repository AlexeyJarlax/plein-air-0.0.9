package com.pavlovalexey.pleinair.utils

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.pavlovalexey.pleinair.R

class LoginAndUserUtils(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val sharedPreferences = context.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
    private val animals: List<String> by lazy { loadFile(R.raw.animals) }
    private val verbs: List<String> by lazy { loadFile(R.raw.verbs) }
    private val nouns: List<String> by lazy { loadFile(R.raw.nouns) }

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

    private fun loadFile(resourceId: Int): List<String> {
        val inputStream = context.resources.openRawResource(resourceId)
        return inputStream.bufferedReader().use { it.readLines() }
    }

    fun generateRandomUserName(): String {
        val animal = animals.random()
        val verb = verbs.random()
        val noun = nouns.random()
        return "$animal $verb $noun"
    }

    companion object {
        fun logout(application: Application) {
            val loginAndUserUtils = LoginAndUserUtils(application.applicationContext)
            loginAndUserUtils.logout()
        }
    }
}
