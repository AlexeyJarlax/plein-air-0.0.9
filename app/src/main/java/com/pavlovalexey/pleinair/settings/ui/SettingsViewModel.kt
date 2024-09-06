package com.pavlovalexey.pleinair.settings.ui

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor

class SettingsViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    private val _isNightMode = MutableLiveData(false)
    val isNightMode: LiveData<Boolean> = _isNightMode

    init {
        _isNightMode.value = settingsInteractor.loadNightMode()
    }

    fun changeNightMode(value: Boolean) {
        if (_isNightMode.value != value) {
            _isNightMode.value = value
            settingsInteractor.saveNightMode(value)
            if (value) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    fun shareApp() {
        settingsInteractor.buttonToShareApp()
    }

    fun goToHelp() {
        settingsInteractor.buttonToHelp()
    }

    fun seeUserAgreement() {
        settingsInteractor.buttonToSeeUserAgreement()
    }

    fun seePrivacyPolicy() {
        settingsInteractor.buttonToSeePrivacyPolicy()
    }

    fun seeDonat() {
        settingsInteractor.buttonDonat()
    }

    fun deleteUserAccount(onAccountDeleted: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Удаление данных пользователя из Firestore/Realtime Database (если используется)
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener {
                    Log.d("DeleteUser", "Данные пользователя успешно удалены.")
                }
                .addOnFailureListener { e ->
                    Log.w("DeleteUser", "Ошибка при удалении данных пользователя", e)
                }

            // Удаление учетной записи пользователя из Firebase Authentication
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("DeleteUser", "Учетная запись пользователя успешно удалена.")
                        onAccountDeleted() // Вызываем callback после успешного удаления
                    } else {
                        Log.w("DeleteUser", "Ошибка при удалении учетной записи пользователя.", task.exception)
                    }
                }
        }
    }
}
