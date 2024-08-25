package com.pavlovalexey.pleinair.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.main.ui.MainActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        checkAuthState()  // Проверка, залогинен ли пользователь
    }

    private fun checkAuthState() {
        if (auth.currentUser != null) {
            // Если пользователь уже залогинен, переходим на главный экран
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Если нет, показываем экран авторизации
            // Здесь реализуйте ваш UI для входа
        }
    }
}