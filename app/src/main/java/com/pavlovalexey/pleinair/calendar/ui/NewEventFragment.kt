package com.pavlovalexey.pleinair.calendar.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentNewEventBinding
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.google.android.gms.maps.model.LatLng

class NewEventFragment : Fragment() {

    private lateinit var newEventViewModel: NewEventViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var binding: FragmentNewEventBinding
    private var selectedLocation: LatLng? = null  // Изменение типа на nullable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newEventViewModel = ViewModelProvider(this).get(NewEventViewModel::class.java)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Слушатель для результата выбора местоположения
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")
            selectedLocation = LatLng(latitude, longitude)  // Инициализация переменной
            binding.pointLocation.setText("Широта: $latitude, Долгота: $longitude")
        }

        // Переход на карту для выбора местоположения
        binding.btnChooseLocation.setOnClickListener {
            findNavController().navigate(R.id.action_newEventFragment_to_eventMapFragment)
        }

        // Загрузка изображения профиля
        profileViewModel.loadProfileImageFromStorage(
            onSuccess = { bitmap ->
                binding.addPicture.setImageBitmap(bitmap)
            },
            onFailure = {
                binding.addPicture.setImageResource(R.drawable.default_avatar)
            }
        )

        // Наблюдение за валидностью формы
        newEventViewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.createPlaylist.isEnabled = isValid
        }

        // Наблюдение за статусом создания события
        newEventViewModel.creationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is CreationStatus.Loading -> showLoading(true)
                is CreationStatus.Success -> {
                    showLoading(false)
                    findNavController().navigateUp()  // Навигация назад
                }
                is CreationStatus.Error -> {
                    showLoading(false)
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Установка слушателей для отслеживания изменений в форме
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                newEventViewModel.onFieldChanged(
                    city = binding.inputCity.text.toString(),
                    place = binding.pointLocation.text.toString(),
                    date = binding.inputDay.text.toString(),
                    time = binding.inputTime.text.toString(),
                    description = binding.inputDetails.text.toString()
                )
            }
        }

        // Установка дат и времени
        binding.inputCity.addTextChangedListener(afterTextChangedListener)

        val calendar = Calendar.getInstance()
        binding.inputDay.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val today = Calendar.getInstance()
                    if (selectedDate.before(today) || selectedDate.timeInMillis - today.timeInMillis > TimeUnit.DAYS.toMillis(3)) {
                        Toast.makeText(requireContext(), "Выберите дату в пределах 3 дней от сегодняшнего дня", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.inputDay.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time))
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 3)
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        binding.inputTime.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    binding.inputTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
                },
                0, 0, true
            )
            timePickerDialog.show()
        }

        binding.inputDetails.addTextChangedListener(afterTextChangedListener)

        // Создание события по нажатию кнопки
        binding.createPlaylist.setOnClickListener {
            if (selectedLocation == null) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите местоположение", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = profileViewModel.user.value?.userId ?: "User ID"
            val profileImageUrl = profileViewModel.user.value?.profileImageUrl ?: "User Avatar URL"

            newEventViewModel.createEvent(
                userId = userId,
                profileImageUrl = profileImageUrl,
                city = binding.inputCity.text.toString(),
                place = binding.pointLocation.text.toString(),
//                latitude = selectedLocation?.latitude ?: 0.0,
//                longitude = selectedLocation?.longitude ?: 0.0,
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