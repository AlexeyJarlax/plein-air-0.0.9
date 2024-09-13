package com.pavlovalexey.pleinair.calendar.ui.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentNewEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.google.android.gms.maps.model.LatLng
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import com.pavlovalexey.pleinair.utils.image.CircleTransform
import com.pavlovalexey.pleinair.utils.image.setupImageResultLaunchers
import com.pavlovalexey.pleinair.utils.image.showImageSelectionDialog
import com.pavlovalexey.pleinair.utils.timeAndData.openDatePickerDialog
import com.pavlovalexey.pleinair.utils.timeAndData.openTimePickerDialog
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.BufferedReader
import javax.inject.Inject


@AndroidEntryPoint
class NewEventFragment : Fragment() {

    @Inject
    lateinit var loginAndUserUtils: LoginAndUserUtils
    private val viewModel: NewEventViewModel by viewModels()
    private lateinit var binding: FragmentNewEventBinding
    private var selectedLocation: LatLng? = null
    private var eventId: String? = null

    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewEventBinding.inflate(inflater, container, false)
        val (cameraLauncher, galleryLauncher) = setupImageResultLaunchers { processedBitmap ->
            handleImageSelection(processedBitmap)
        }
        cameraActivityResultLauncher = cameraLauncher
        galleryActivityResultLauncher = galleryLauncher
        setupObservers()
        setupListeners()
        return binding.root
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner, Observer { user ->
            viewModel.checkAndGenerateEventAvatar {
                if (!user?.profileImageUrl.isNullOrEmpty()) {
                    viewModel.loadEventImageFromStorage(
                        { bitmap -> binding.userAvatar.setImageBitmap(bitmap) },
                        {
                            Picasso.get().load(user?.profileImageUrl).transform(CircleTransform())
                                .into(binding.userAvatar)
                        }
                    )
                } else {
                    binding.userAvatar.setImageResource(R.drawable.account_circle_50dp)
                }
            }
        })
        viewModel.creationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is CreationStatus.Loading -> showLoading(true)
                is CreationStatus.Success -> {
                    showLoading(false)
                    eventId = status.eventId
                    findNavController().navigateUp()
                }
                is CreationStatus.Error -> {
                    showLoading(false)
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.userAvatar.setOnClickListener {
            showImageSelectionDialog(cameraActivityResultLauncher, galleryActivityResultLauncher)
        }

        binding.createEvent.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                navigateToNextStep()
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<LatLng>("selectedLocation")
            ?.observe(viewLifecycleOwner) { location ->
                selectedLocation = location
                binding.pointLocation.setText("Широта: ${location.latitude},\nДолгота: ${location.longitude}")
                viewLifecycleOwner.lifecycleScope.launch {
                    navigateToNextStep()
                }
            }
    }

    private fun handleImageSelection(processedBitmap: Bitmap) {
        binding.userAvatar.setImageBitmap(processedBitmap)
        viewModel.uploadEventImageToFirebase(processedBitmap,
            onSuccess = { uri ->
                Picasso.get().load(uri).transform(CircleTransform()).into(binding.userAvatar)
            },
            onFailure = {
                // Обработка ошибки
                Toast.makeText(requireContext(), "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private suspend fun navigateToNextStep() {
        when {
            binding.inputCity.text.isNullOrEmpty() -> findNavController().navigate(R.id.action_newEventFragment_to_citySelectionFragment)
            selectedLocation == null -> findNavController().navigate(R.id.action_newEventFragment_to_eventMapFragment)
            binding.inputDay.text.isNullOrEmpty() -> findNavController().navigate(R.id.action_newEventFragment_to_dateSelectionFragment)
            binding.inputTime.text.isNullOrEmpty() -> findNavController().navigate(R.id.action_newEventFragment_to_timeSelectionFragment)
            else -> {
                createEvent()
            }
        }
    }

    private fun createEvent() {
        if (selectedLocation == null) {
            Toast.makeText(
                requireContext(),
                "Пожалуйста, выберите местоположение",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val userId = loginAndUserUtils.getUserID()
        val profileImageUrl = viewModel.user.value?.profileImageUrl ?: "User Avatar URL"
        val latitude = selectedLocation?.latitude ?: 0.0
        val longitude = selectedLocation?.longitude ?: 0.0

        viewModel.createEvent(
            userId = userId,
            profileImageUrl = profileImageUrl,
            city = binding.inputCity.text.toString(),
            place = binding.pointLocation.text.toString(),
            date = binding.inputDay.text.toString(),
            time = binding.inputTime.text.toString(),
            description = binding.inputDetails.text.toString(),
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
