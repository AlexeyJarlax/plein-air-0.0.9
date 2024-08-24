package com.pavlovalexey.pleinair.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _email = MutableLiveData<String>("")
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData<String>("")
    val password: LiveData<String> get() = _password

    private val _isLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _errorMessage = MutableLiveData<String>("")
    val errorMessage: LiveData<String> get() = _errorMessage

    var isLoginScreen = MutableLiveData<Boolean>(true)

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun signIn() {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email.value!!, password.value!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _isLoggedIn.value = true
                    } else {
                        _errorMessage.value = task.exception?.message ?: "Authentication failed."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred."
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email.value!!, password.value!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _isLoggedIn.value = true
                    } else {
                        _errorMessage.value = task.exception?.message ?: "Registration failed."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred."
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _isLoggedIn.value = false
    }
}