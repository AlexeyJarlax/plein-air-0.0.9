package com.pavlovalexey.pleinair.settings.ui

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val loginAndUserUtils: LoginAndUserUtils,
    @ApplicationContext private val context: Context
) : ViewModel(), SettingsViewModelInterface {

    private val contextRef = WeakReference(context)

    private val _isNightMode = MutableLiveData(false)
    override val isNightMode: LiveData<Boolean> get() = _isNightMode
    private val _accountDeleted = MutableLiveData<Boolean>()
    val accountDeleted: LiveData<Boolean> get() = _accountDeleted
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow: SharedFlow<Event> = _eventFlow

    init {
        _isNightMode.value = settingsInteractor.loadNightMode() // Загрузка текущего режима из SharedPreferences
    }

    override fun changeNightMode(value: Boolean) {
        if (_isNightMode.value != value) {
            _isNightMode.value = value
            settingsInteractor.saveNightMode(value)
            settingsInteractor.applyTheme() // Применение новой темы
        }
    }

    override fun shareApp() {
        settingsInteractor.buttonToShareApp()
    }

    override fun goToHelp() {
        settingsInteractor.buttonToHelp()
    }

    override fun seeUserAgreement() {
        settingsInteractor.buttonToSeeUserAgreement()
    }

    override fun seePrivacyPolicy() {
        settingsInteractor.buttonToSeePrivacyPolicy()
    }

    override fun seeDonat() {
        settingsInteractor.buttonDonat()
    }

    override fun deleteUserAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                settingsInteractor.deleteUserAccount { ->
                    _accountDeleted.value = true
                    viewModelScope.launch {
                        _eventFlow.emit(Event.AccountDeleted)
                    }
                }
            } finally {
                loginAndUserUtils.logout()
                exitApplication()
                _isLoading.value = false
            }
        }
    }

    private fun exitApplication() {
        val context = contextRef.get() ?: return
        val activity = context as? Activity
        activity?.finishAffinity()  // закрывает данную активность и все вышестоящие активности
        System.exit(0)  // завершает процесс
    }

    sealed class Event {
        object FinishActivity : Event()
        object AccountDeleted : Event()
        object DeleteAccountFailed : Event()
        object ReauthenticationFailed : Event()
    }
}
