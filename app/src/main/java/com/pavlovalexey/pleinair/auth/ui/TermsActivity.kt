package com.pavlovalexey.pleinair.auth.ui

/** Приложение построено как синглактивити на фрагментах с отправной точкой MainActivity.
 * TermsActivity и AuthActivity выделены как отдельные активности чтобы изолировать
 * от основной структуры фрагментов и навигации через НавХостКонтроллер.
 * Вместо xml применил Jetpack Compose — фреймворк для создания UI на Android, основанный на декларативном подходе.
 *
 * 1 Этап - подписание соглашений в TermsActivity
 * 2 Этап - авторизация в AuthActivity
 * 3 Этап - MainActivity и фрагменты по всему функционалу приложения с навигацией через НавГраф
 */

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.SharedPreferences

@AndroidEntryPoint
class TermsActivity : AppCompatActivity() {

    @Inject
    lateinit var loginAndUserUtils: LoginAndUserUtils

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val viewModel: TermsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isTermsAccepted()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        } else {
            loginAndUserUtils.logout()
        }

        setContent {
            TermsScreen(
                onContinue = {
                    saveTermsAccepted(true)
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                },
                viewModel = viewModel
            )
        }
    }

    private fun saveTermsAccepted(accepted: Boolean) {
        Log.d("TermsActivity", "Saving terms accepted: $accepted")
        with(sharedPreferences.edit()) {
            putBoolean("all_terms_accepted", accepted)
            apply()
        }
    }

    private fun isTermsAccepted(): Boolean {
        return sharedPreferences.getBoolean("all_terms_accepted", false)
    }
}