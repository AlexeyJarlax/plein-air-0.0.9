package com.pavlovalexey.pleinair.presentation

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var isLoggedIn = mutableStateOf(false)
    var errorMessage = mutableStateOf("")

    fun onEmailChanged(newEmail: String) {
        email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        password.value = newPassword
    }

    fun signIn() {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email.value, password.value).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isLoggedIn.value = true
                    } else {
                        errorMessage.value = task.exception?.message ?: "Authentication failed."
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Unknown error occurred."
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email.value, password.value).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isLoggedIn.value = true
                    } else {
                        errorMessage.value = task.exception?.message ?: "Registration failed."
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Unknown error occurred."
            }
        }
    }

    fun signOut() {
        auth.signOut()
        isLoggedIn.value = false
    }
}