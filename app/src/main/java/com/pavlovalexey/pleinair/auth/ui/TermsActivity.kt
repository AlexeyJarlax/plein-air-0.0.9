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
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.SharedPreferences
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

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
            loginAndUserUtils.logout() // Чистим аутентификацию при переустановке
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

@Composable
fun TermsScreen(onContinue: () -> Unit, viewModel: TermsViewModel) {
    var isAgreementChecked by rememberSaveable { mutableStateOf(false) }
    var isPrivacyPolicyChecked by rememberSaveable { mutableStateOf(false) }

    val isButtonEnabled = isAgreementChecked && isPrivacyPolicyChecked
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = viewModel.termsOfPrivacy,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = viewModel.privacyPolicyContent,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Checkbox(
                checked = isPrivacyPolicyChecked,
                onCheckedChange = { isPrivacyPolicyChecked = it },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = viewModel.termsOfAgreement,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = viewModel.userAgreementContent,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Checkbox(
                checked = isAgreementChecked,
                onCheckedChange = { isAgreementChecked = it },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = onContinue,
            enabled = isButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Продолжить")
        }
    }
}

class TermsViewModel @Inject constructor() : ViewModel() {
    val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    val termsOfPrivacy = "Политика конфиденциальности на $currentDate"
    val termsOfAgreement = "Условия использования на $currentDate"

    var privacyPolicyContent by mutableStateOf("Политика конфиденциальности загружается...")
        private set
    var userAgreementContent by mutableStateOf("Пользовательское соглашение загружается...")
        private set

    init {
        loadTextFromUrl("https://ваш_сайт.com/privacy_policy") { text ->
            privacyPolicyContent = text
        }
        loadTextFromUrl("https://ваш_сайт.com/user_agreement") { text ->
            userAgreementContent = text
        }
    }

    private fun loadTextFromUrl(url: String, onTextLoaded: (String) -> Unit) {
        try {
            val text = URL(url).readText()
            onTextLoaded(text)
        } catch (e: Exception) {
            Log.e("TermsViewModel", "Ошибка загрузки текста из URL: $url", e)
            onTextLoaded("Ошибка загрузки текста.")
        }
    }
}