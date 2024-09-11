package com.pavlovalexey.pleinair.auth.ui

/** Приложение построено как синглактивити на фрагментах с отправной точкой MainActivity
 * TermsActivity и AuthActivity выделены как отдельные активити чтобы безопасно изолировать
 * от основной структуры фрагментов.
 * 1 Этап - подписание соглашений в TermsActivity
 * 2 Этап - авторизация в AuthActivity
 * 3 Этап - MainActivity и фрагменты по всему функционалу приложения с с навигацией через НавГраф
*/

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.ActivityAuthBinding
import com.pavlovalexey.pleinair.main.ui.MainActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityAuthBinding
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnSignInWithGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.exitButton.setOnClickListener {
            finishAffinity()
        }

        checkAuthState()

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Пользователь аутентифицирован, обновляем токен
                user.getIdToken(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result?.token
                        // Здесь можно сохранить или использовать обновленный токен
                        Log.d("AuthActivity", "Token обновлен: $idToken")
                    } else {
                        Log.w("AuthActivity", "Не удалось обновить токен", task.exception)
                    }
                }
            } else {
                // Пользователь не аутентифицирован
                Log.d("AuthActivity", "Пользователь не аутентифицирован")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Добавляем слушателя состояния аутентификации
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        // Убираем слушателя состояния аутентификации
        auth.removeAuthStateListener(authStateListener)
    }

    private fun signInWithGoogle() {
        // Сначала выполняем выход пользователя из текущей учетной записи Google
        googleSignInClient.signOut().addOnCompleteListener {
            // Очищаем данные пользователя в Firebase Auth
            auth.signOut()

            // После выхода запускаем процесс входа
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("AuthActivity", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w("AuthActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun checkAuthState() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}
