package com.pavlovalexey.pleinair.settings.domain

interface SettingsRepository {
    fun loadNightMode(): Boolean
    fun saveNightMode(value: Boolean)
    fun buttonToShareApp()
    fun buttonToHelp()
    fun buttonToSeeUserAgreement()
    fun buttonToSeePrivacyPolicy()
    fun buttonDonat()
    fun applyTheme()
    fun sharePlaylist(message: String)
    fun deleteUserAccount(onAccountDeleted: () -> Unit)
}