package com.pavlovalexey.pleinair.utils.ui

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import com.pavlovalexey.pleinair.databinding.FragmentProfileBinding
import javax.inject.Inject

class IconStateUtils @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

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
            with(sharedPreferences.edit()) {
                putBoolean(key, state)
                apply()
            }
        }

        fun loadSavedIconStates(context: Context, binding: FragmentProfileBinding) {
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
