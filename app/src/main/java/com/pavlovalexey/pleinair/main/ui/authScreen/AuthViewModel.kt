package com.pavlovalexey.pleinair.main.ui.authScreen

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val loginAndUserUtils: LoginAndUserUtils
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _authState.value = AuthState(isAuthenticated = loginAndUserUtils.isUserSignedIn())
    }

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
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                if (authResult.user != null) {
                    if (loginAndUserUtils.isUserSignedIn()) {
                        loginAndUserUtils.setupUserProfile {
                            _authState.value = AuthState(isAuthenticated = true)
                        }
                    } else {
                        _authState.value = AuthState(isAuthenticated = false)
                    }
                } else {
                    _authState.value = AuthState(isAuthenticated = false)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Firebase authentication failed", e)
                _authState.value = AuthState(isAuthenticated = false)
            }
        }
    }

    fun signOut() {
        loginAndUserUtils.logout()
        _authState.value = AuthState(isAuthenticated = false)
    }

    fun resetAuthState() {
        _authState.value = AuthState(isAuthenticated = false)
    }

    data class AuthState(val isAuthenticated: Boolean = false)
}