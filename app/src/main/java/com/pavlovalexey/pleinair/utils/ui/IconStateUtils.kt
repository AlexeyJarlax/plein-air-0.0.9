package com.pavlovalexey.pleinair.utils.ui

import android.content.Context
import android.view.View
import android.widget.TextView
import com.pavlovalexey.pleinair.databinding.FragmentProfileBinding
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys.PREFS_NAME

class IconStateUtils {

    companion object {

        fun updateIconToChecked(view: View) {
            if (view is TextView) {
                view.text = "✔️"
            }
        }

        fun updateIconToUnchecked(view: View) {
            if (view is TextView) {
                view.text = "✏️"
            }
        }

        fun saveIconState(context: Context, key: String, state: Boolean) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean(key, state)
                apply()
            }
        }

        fun loadSavedIconStates(context: Context, binding: FragmentProfileBinding) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val locationState = sharedPreferences.getBoolean("location", false)
            val descriptionState = sharedPreferences.getBoolean("description", false)
            val technicState = sharedPreferences.getBoolean("technic", false)

            if (locationState) {
                updateIconToChecked(binding.txtChooseLocation)
            } else {
                updateIconToUnchecked(binding.txtChooseLocation)
            }

            if (descriptionState) {
                updateIconToChecked(binding.txtEditDescription)
            } else {
                updateIconToUnchecked(binding.txtEditDescription)
            }

            if (technicState) {
                updateIconToChecked(binding.txtTechnic)
            } else {
                updateIconToUnchecked(binding.txtTechnic)
            }
        }
    }
}