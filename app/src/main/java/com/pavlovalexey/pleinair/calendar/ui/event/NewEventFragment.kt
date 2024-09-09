package com.pavlovalexey.pleinair.calendar.ui.event

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import com.google.firebase.Firebase
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.CircleTransform
import com.pavlovalexey.pleinair.utils.ImageUtils
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID

class NewEventFragment : Fragment() {

    private lateinit var newEventViewModel: NewEventViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var binding: FragmentNewEventBinding
    private var selectedLocation: LatLng? = null
    private var eventId: String? = null
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<String>

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

        // Загрузка списка городов
        val inputStream = resources.openRawResource(R.raw.cities)
        val cities = inputStream.bufferedReader().use(BufferedReader::readLines)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        binding.inputCity.setAdapter(adapter)
        binding.inputCity.threshold = 1

        // Слушатель для результата выбора местоположения
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")
            selectedLocation = LatLng(latitude, longitude)
            binding.pointLocation.setText("Широта: $latitude,\nДолгота: $longitude")
        }

        // Переход на карту для выбора местоположения
        binding.btnChooseLocation.setOnClickListener {
            findNavController().navigate(R.id.action_newEventFragment_to_eventMapFragment)
        }

        // Загрузка изображения профиля
        profileViewModel.loadProfileImageFromStorage(
            onSuccess = { bitmap ->
                val circleBitmap = CircleTransform().transform(bitmap)
                binding.addPicture.setImageBitmap(circleBitmap)
            },
            onFailure = {
                binding.addPicture.setImageResource(R.drawable.account_circle_50dp)
            }
        )

        // Наблюдение за валидностью формы
        newEventViewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.createEvent.isEnabled = isValid
        }

        // Наблюдение за статусом создания события
        newEventViewModel.creationStatus.observe(viewLifecycleOwner) { status ->
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

        binding.inputCity.addTextChangedListener(afterTextChangedListener)

        binding.addPicture.setOnClickListener {
            val options = arrayOf("Сделать фото", "Выбрать из галереи")
            AlertDialog.Builder(requireContext())
                .setTitle("Выберите изображение")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                                val photoUri = createImageUri()
                                photoUri?.let {
                                    cameraActivityResultLauncher.launch(it)
                                }
                            }
                        }

                        1 -> {
                            galleryActivityResultLauncher.launch("image/*")
                        }
                    }
                }
                .show()
        }
        // Регистрация ActivityResultLauncher для камеры
        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                val photoUri = createImageUri()
                photoUri?.let { uri ->
                    try {
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        val imageBitmap = BitmapFactory.decodeStream(inputStream)
                        val processedBitmap =
                            imageBitmap?.let { ImageUtils.compressAndGetCircularBitmap(it) }
                        processedBitmap?.let { handleImageResult(it) }
                    } catch (e: IOException) {
                        Log.e(TAG, "Ошибка при открытии InputStream для URI", e)
                        Toast.makeText(
                            requireContext(),
                            "Не удалось загрузить изображение",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        // Регистрация ActivityResultLauncher для галереи
        galleryActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    val processedBitmap =
                        imageBitmap?.let { ImageUtils.compressAndGetCircularBitmap(it) }
                    processedBitmap?.let { handleImageResult(it) }
                } catch (e: IOException) {
                    Log.e(TAG, "Ошибка при открытии InputStream для URI", e)
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить изображение",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val calendar = Calendar.getInstance()

        // Установка обработчика для поля даты
        binding.inputDay.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                openDatePickerDialog(calendar)
            }
        }

        // Установка обработчика для поля времени
        binding.inputTime.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                openTimePickerDialog()
            }
        }

        binding.inputDetails.addTextChangedListener(afterTextChangedListener)

        // Создание события по нажатию кнопки
        binding.createEvent.setOnClickListener {
            if (selectedLocation == null) {
                Toast.makeText(
                    requireContext(),
                    "Пожалуйста, выберите местоположение",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val sharedPreferences = requireContext().getSharedPreferences(
                AppPreferencesKeys.PREFS_NAME,
                Context.MODE_PRIVATE
            )
            val userId = sharedPreferences.getString("userId", "") ?: ""
            val profileImageUrl = profileViewModel.user.value?.profileImageUrl ?: "User Avatar URL"
            val latitude = selectedLocation?.latitude ?: 0.0
            val longitude = selectedLocation?.longitude ?: 0.0

            newEventViewModel.createEvent(
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
    }

    private fun openDatePickerDialog(calendar: Calendar) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val today = Calendar.getInstance()
                if (selectedDate.before(today) || selectedDate.timeInMillis - today.timeInMillis > TimeUnit.DAYS.toMillis(
                        3
                    )
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Выберите дату в пределах 3 дней от сегодняшнего дня",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.inputDay.setText(
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(selectedDate.time)
                    )
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "✔️", datePickerDialog)
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "❌", datePickerDialog)

        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun openTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                binding.inputTime.setText(
                    String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        hourOfDay,
                        minute
                    )
                )
            },
            0, 0, true
        )

        timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE, "✔️", timePickerDialog)
        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "❌", timePickerDialog)
        timePickerDialog.show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun createImageUri(): Uri? {
        val contentResolver = requireContext().contentResolver
        val imageFileName = "event_${System.currentTimeMillis()}.jpg"
        val storageDir = requireContext().getExternalFilesDir(null)
        val imageFile = File(storageDir, imageFileName)
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )
    }

    private fun handleImageResult(imageBitmap: Bitmap) {
        val processedBitmap = CircleTransform().transform(imageBitmap)
        uploadImageToFirebase(processedBitmap)
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        val userId = profileViewModel.user.value?.userId ?: return
        val eventId = this.eventId ?: return // Получение eventId
        val storageRef =
            FirebaseStorage.getInstance().reference.child("event_images/$userId/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val serverUrl = uri.toString()
                    // Обновите URL изображения в Firestore для соответствующего события
                    newEventViewModel.updateEventImageUrl(eventId, serverUrl)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NewEventFragment", "Failed to upload image", exception)
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}