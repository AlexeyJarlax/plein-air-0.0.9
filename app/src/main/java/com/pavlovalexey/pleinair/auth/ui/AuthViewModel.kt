package com.pavlovalexey.pleinair.auth.ui

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        // Вызовите этот Intent в Activity или соответствующем месте, чтобы запустить процесс аутентификации
        // Здесь предполагается, что обработка будет произведена в MainActivity или другом месте
    }

    fun handleSignInResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("AuthViewModel", "Google sign in failed", e)
                _authState.value = AuthState(isAuthenticated = false)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState(isAuthenticated = true)
                } else {
                    Log.w("AuthViewModel", "signInWithCredential:failure", task.exception)
                    _authState.value = AuthState(isAuthenticated = false)
                }
            }
    }

    data class AuthState(val isAuthenticated: Boolean = false)

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
