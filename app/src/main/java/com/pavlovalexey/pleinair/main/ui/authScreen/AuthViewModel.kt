package com.pavlovalexey.pleinair.main.ui.authScreen

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val loginAndUserUtils: LoginAndUserUtils
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        loginAndUserUtils.signInWithGoogle(launcher)
    }

    fun handleSignInResult(result: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w("AuthViewModel", "Google sign in failed", e)
            _authState.value = AuthState(isAuthenticated = false)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState(isAuthenticated = true)
            } else {
                Log.w("AuthViewModel", "signInWithCredential:failure", task.exception)
                _authState.value = AuthState(isAuthenticated = false)
            }
        }
    }

    data class AuthState(val isAuthenticated: Boolean = false)
}