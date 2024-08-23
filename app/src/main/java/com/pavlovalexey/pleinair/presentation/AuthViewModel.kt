package com.pavlovalexey.pleinair.presentation

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoggedIn by mutableStateOf(false)

    init {
        // Проверяем, сохранены ли данные пользователя
        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            email = savedEmail
            password = savedPassword
            signIn(savedEmail, savedPassword)
        }
    }

    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isLoggedIn = true
                saveCredentials(email, password)
            } else {
                isLoggedIn = false
            }
        }
    }

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isLoggedIn = true
                saveCredentials(email, password)
            } else {
                isLoggedIn = false
            }
        }
    }

    private fun saveCredentials(email: String, password: String) {
        with(sharedPreferences.edit()) {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }
}