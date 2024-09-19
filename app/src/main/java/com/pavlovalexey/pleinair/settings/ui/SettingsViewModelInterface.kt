package com.pavlovalexey.pleinair.settings.ui

import androidx.lifecycle.LiveData

interface SettingsViewModelInterface {
    val isNightMode: LiveData<Boolean>
    fun changeNightMode(value: Boolean)
    fun shareApp()
    fun goToHelp()
    fun seeUserAgreement()
    fun seePrivacyPolicy()
    fun seeDonat()
    fun deleteUserAccount()
}