package com.pavlovalexey.pleinair.calendar.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentNewEventBinding
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel

class NewEventFragment : Fragment() {

    private lateinit var newEventViewModel: NewEventViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var binding: FragmentNewEventBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newEventViewModel = ViewModelProvider(this).get(NewEventViewModel::class.java)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Observe the profile image and set it to the ImageView when loaded
        profileViewModel.loadProfileImageFromStorage(
            onSuccess = { bitmap ->
                binding.addPicture.setImageBitmap(bitmap)
            },
            onFailure = {
                // Handle the case where the image loading fails, e.g., show a default image
                binding.addPicture.setImageResource(R.drawable.default_avatar)
            }
        )

        // Observing the form validation state
        newEventViewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.createPlaylist.isEnabled = isValid
        }

        // Observing the creation status
        newEventViewModel.creationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is CreationStatus.Loading -> showLoading(true)
                is CreationStatus.Success -> {
                    showLoading(false)
                    findNavController().navigateUp()  // Navigate back
                }
                is CreationStatus.Error -> {
                    showLoading(false)
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Set text watchers to monitor form input changes
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                newEventViewModel.onFieldChanged(
                    city = binding.inputCity.text.toString(),
                    place = binding.inputLocation.text.toString(),
                    date = binding.inputDay.text.toString(),
                    time = binding.inputTime.text.toString(),
                    description = binding.inputDetails.text.toString()
                )
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.inputCity.addTextChangedListener(afterTextChangedListener)
        binding.inputLocation.addTextChangedListener(afterTextChangedListener)
        binding.inputDay.addTextChangedListener(afterTextChangedListener)
        binding.inputTime.addTextChangedListener(afterTextChangedListener)
        binding.inputDetails.addTextChangedListener(afterTextChangedListener)

        // Create event button click listener
        binding.createPlaylist.setOnClickListener {
            val userId = profileViewModel.user.value?.userId ?: "User ID"
            val profileImageUrl = profileViewModel.user.value?.profileImageUrl ?: "User Avatar URL"

            newEventViewModel.createEvent(
                userId = userId,
                profileImageUrl = profileImageUrl,
                city = binding.inputCity.text.toString(),
                place = binding.inputLocation.text.toString(),
                date = binding.inputDay.text.toString(),
                time = binding.inputTime.text.toString(),
                description = binding.inputDetails.text.toString()
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
