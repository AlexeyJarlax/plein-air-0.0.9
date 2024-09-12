package com.pavlovalexey.pleinair.auth.ui

/** Приложение построено как синглактивити на фрагментах с отправной точкой MainActivity
 * TermsActivity и AuthActivity выделены как отдельные активности чтобы изолировать
 * от основной структуры фрагментов.
 * 1 Этап - подписание соглашений в TermsActivity
 * 2 Этап - авторизация в AuthActivity
 * 3 Этап - MainActivity и фрагменты по всему функционалу приложения с навигацией через НавГраф
 */

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pavlovalexey.pleinair.R
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread
import android.content.SharedPreferences
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TermsActivity : AppCompatActivity() {

    @Inject
    lateinit var loginAndUserUtils: LoginAndUserUtils
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var checkAgreement: CheckBox
    private lateinit var checkPrivacyPolicy: CheckBox
    private lateinit var btnContinue: Button
    private lateinit var tvBeforeAgreement: TextView
    private lateinit var tvAgreement: TextView
    private lateinit var tvBeforePolicy: TextView
    private lateinit var tvPrivacyPolicy: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        sharedPreferences = getSharedPreferences(AppPreferencesKeys.PREFS_NAME, MODE_PRIVATE)

        // Проверяем состояние согласия
        if (isTermsAccepted()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        } else {
            loginAndUserUtils.logout() // если пользователь удалил и поставил заново приложень, чистим его аутентификацию
        }

        checkAgreement = findViewById(R.id.checkAgreement)
        checkPrivacyPolicy = findViewById(R.id.checkPrivacyPolicy)
        btnContinue = findViewById(R.id.btnContinue)
        tvBeforeAgreement = findViewById(R.id.tvBeforeAgreement)
        tvAgreement = findViewById(R.id.tvAgreement)
        tvBeforePolicy = findViewById(R.id.tvBeforePolicy)
        tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy)

        // Получаем текущую дату
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        // Устанавливаем текст Пользовательского соглашения с динамической датой
        val beforeAgreementText =
            getString(R.string.terms_of_agreement, getString(R.string.app_name), currentDate)
        tvBeforeAgreement.text = beforeAgreementText
        val beforePolicyText =
            getString(R.string.terms_of_privacy, getString(R.string.app_name), currentDate)
        tvBeforePolicy.text = beforePolicyText

        // Получаем URL из строковых ресурсов
        val userAgreementUrl = getString(R.string.user_agreement_url)
        val privacyPolicyUrl = getString(R.string.privacy_policy_url)

        // Загрузка Пользовательского соглашения
        loadTextFromUrl(userAgreementUrl, tvAgreement, R.string.error_loading_agreement)

        // Загрузка Политики конфиденциальности
        loadTextFromUrl(privacyPolicyUrl, tvPrivacyPolicy, R.string.error_loading_policy)

        // Проверяем состояние чекбоксов
        btnContinue.isEnabled = checkAgreement.isChecked && checkPrivacyPolicy.isChecked

        checkAgreement.setOnCheckedChangeListener { _, _ ->
            btnContinue.isEnabled = checkAgreement.isChecked && checkPrivacyPolicy.isChecked
        }

        checkPrivacyPolicy.setOnCheckedChangeListener { _, _ ->
            btnContinue.isEnabled = checkAgreement.isChecked && checkPrivacyPolicy.isChecked
        }

        btnContinue.setOnClickListener {
            // Сохраняем состояние согласия
            saveTermsAccepted(true)
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadTextFromUrl(url: String, textView: TextView, errorResId: Int) {
        thread {
            try {
                val text = URL(url).readText()

                runOnUiThread {
                    textView.text = text
                }
            } catch (e: Exception) {
                Log.e("TermsActivity", "Ошибка загрузки текста из URL: $url", e)
                runOnUiThread {
                    textView.text = getString(errorResId)
                }
            }
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