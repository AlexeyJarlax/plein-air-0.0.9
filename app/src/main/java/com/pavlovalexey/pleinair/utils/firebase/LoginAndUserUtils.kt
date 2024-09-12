package com.pavlovalexey.pleinair.utils.firebase

import android.app.Application
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import javax.inject.Inject

class LoginAndUserUtils(private val context: Context) {

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    @Inject
    lateinit var loginAndUserUtils: LoginAndUserUtils

    private val defaultLocation = LatLng(59.9500019, 30.3166718)
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

    fun setupUserProfile(onProfileSet: (String) -> Unit) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val userName = generateRandomUserName()
                val userProfile = hashMapOf(
                    "profileImageUrl" to "",
                    "userName" to userName,
                    "location" to hashMapOf(
                        "latitude" to defaultLocation.latitude,
                        "longitude" to defaultLocation.longitude
                    )
                )
                userDocRef.set(userProfile).addOnSuccessListener {
                    saveUserNameLocally(userName)
                    onProfileSet(userName)
                }
            } else {
                val userName = document.getString("userName") ?: generateRandomUserName()
                saveUserNameLocally(userName)
                onProfileSet(userName)
            }
        }
    }

    private fun saveUserNameLocally(userName: String) {
        val editor = sharedPreferences.edit()
        editor.putString("userName", userName)
        editor.apply()
    }

    companion object {
        fun logout(application: Application) {
            val loginAndUserUtils = LoginAndUserUtils(application.applicationContext)
            loginAndUserUtils.logout()
        }
    }
}
