package com.pavlovalexey.pleinair.utils.firebase

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.R
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginAndUserUtils @Inject constructor(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences
) {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    init {
        setupGoogleSignInClient()
    }

    private fun setupGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    fun logout() {
        auth.signOut()
        googleSignInClient.signOut()

        // Clear DataStore
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences.clear() // Remove all preferences
            }
        }
    }

    private val defaultLocation = LatLng(59.9500019, 30.3166718)
    private val animals: List<String> by lazy { loadFile(R.raw.animals) }
    private val verbs: List<String> by lazy { loadFile(R.raw.verbs) }
    private val nouns: List<String> by lazy { loadFile(R.raw.nouns) }

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
                    "name" to userName,
                    "location" to hashMapOf(
                        "latitude" to defaultLocation.latitude,
                        "longitude" to defaultLocation.longitude
                    )
                )
                userDocRef.set(userProfile).addOnSuccessListener {
                    updateUserNameOnFirebase(userName)
                    saveUserNameLocally(userName)
                    onProfileSet(userName)
                }
            } else {
                val userName = document.getString("name") ?: generateRandomUserName()
                updateUserNameOnFirebase(userName)
                saveUserNameLocally(userName)
                onProfileSet(userName)
            }
        }
    }


    private fun saveUserNameLocally(userName: String) {
        val editor = sharedPreferences.edit()
        editor.putString("name", userName)
        editor.apply()
    }

    fun updateUserNameOnFirebase(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("name", newName)
            .addOnSuccessListener {
                Log.d("FirebaseUserManager", "User name updated")
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error updating user name", e)
            }
    }
}
