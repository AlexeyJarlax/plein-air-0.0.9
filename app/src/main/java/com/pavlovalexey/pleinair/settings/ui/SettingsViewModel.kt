package com.pavlovalexey.pleinair.settings.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _isNightMode = MutableLiveData(false)
    val isNightMode: LiveData<Boolean> = _isNightMode
    private val _accountDeleted = MutableLiveData<Boolean>()
    val accountDeleted: LiveData<Boolean> get() = _accountDeleted
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _eventChannel = Channel<Event>()
    val eventFlow = _eventChannel.receiveAsFlow()

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

    fun deleteUserAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()
            ?.addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) {
                    // Успешное удаление аккаунта
                    viewModelScope.launch {
                        _eventChannel.send(Event.AccountDeleted)
                    }
                } else {
                    // Обрабатывать ошибку удаления
                    viewModelScope.launch {
                        _eventChannel.send(Event.DeleteAccountFailed(deleteTask.exception))
                    }
                }
            }
    }

    sealed class Event {
        object FinishActivity : Event()
        object AccountDeleted : Event()
        data class DeleteAccountFailed(val exception: Exception?) : Event()
        data class ReauthenticationFailed(val exception: Exception?) : Event()
    }
}