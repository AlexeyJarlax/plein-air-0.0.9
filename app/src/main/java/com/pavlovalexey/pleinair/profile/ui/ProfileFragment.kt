package com.pavlovalexey.pleinair.profile.ui

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentProfileBinding
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import com.pavlovalexey.pleinair.utils.image.CircleTransform
import com.pavlovalexey.pleinair.utils.image.ImageUtils
import com.squareup.picasso.Picasso
import java.io.IOException

class ProfileFragment : Fragment(), UserMapFragment.OnLocationSelectedListener {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>
    private val TAG = ProfileFragment::class.java.simpleName
    private var logoutListener: LogoutListener? = null

    interface LogoutListener {
        fun onLogout()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel.loadSelectedStyles()

        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                val processedBitmap = ImageUtils.compressAndGetCircularBitmap(imageBitmap)
                binding.userAvatar.setImageBitmap(processedBitmap)
                saveAndUploadProfileImage(processedBitmap)
            }
        }

        galleryActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->
                    try {
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        val imageBitmap = BitmapFactory.decodeStream(inputStream)
                        val processedBitmap =
                            imageBitmap?.let { ImageUtils.compressAndGetCircularBitmap(it) }
                        binding.userAvatar.setImageBitmap(processedBitmap)
                        processedBitmap?.let {
                            viewModel.uploadImageToFirebase(
                                it,
                                ::onUploadSuccess,
                                ::onUploadFailure
                            )
                        }
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

        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.userName.text = user?.name ?: getString(R.string.default_user_name)
            if (!user?.profileImageUrl.isNullOrEmpty()) {
                // Load the image from local storage if it exists, otherwise load from Firebase Storage
                viewModel.loadProfileImageFromStorage(
                    { bitmap ->
                        binding.userAvatar.setImageBitmap(bitmap)
                    },
                    {
                        Picasso.get()
                            .load(user?.profileImageUrl)
                            .transform(CircleTransform()) // Устанавливаем закругленные углы
                            .into(binding.userAvatar)
                    }
                )
            } else {
                binding.userAvatar.setImageResource(R.drawable.account_circle_50dp)
            }
            binding.txtChooseLocation.text = user?.locationName ?: getString(R.string.location)
            updateIconIfLocationExists(user?.locationName)
        }

        // Load saved icon states
        loadSavedIconStates()

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.userAvatar.setOnClickListener {
            showAvatarSelectionDialog()
        }

        binding.userName.setOnClickListener {
            showEditNameDialog()
        }

        binding.btnChooseLocation.setOnClickListener {
            openMapFragment()
        }

        binding.editDescription.setOnClickListener {
            showEditDescriptionDialog()
        }

        binding.exitButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Exit")
                .setMessage("Закрыть приложение?")
                .setPositiveButton("✔️") { _, _ ->
                    activity?.finishAffinity()
                }
                .setNegativeButton("❌", null)
                .show()
        }

        binding.btnTechnic.setOnClickListener {
            showTechnicDialog()
        }

        viewModel.selectedArtStyles.observe(viewLifecycleOwner) { selectedStyles ->

        }

        return binding.root
    }

    private fun openMapFragment() {
        val userMapFragment = UserMapFragment()
        userMapFragment.setOnLocationSelectedListener(this)
        findNavController().navigate(R.id.action_profileFragment_to_UserMapFragment)
    }

    override fun onLocationSelected(location: LatLng) {
        viewModel.updateUserLocation(location) {
            updateIconToChecked(binding.txtChooseLocation)
            saveIconState("location", true)
            Toast.makeText(requireContext(), "Местоположение сохранено!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log out")
            .setMessage("Разлогинить пользователя?")
            .setPositiveButton("✔️") { _, _ ->
                viewModel.logout()
                requireActivity().recreate()
            }
            .setNegativeButton("❌", null)
            .show()
    }

    private fun showAvatarSelectionDialog() {
        val options = arrayOf("Сделать фото", "Выбрать из галереи")
        AlertDialog.Builder(requireContext())
            .setTitle("Выберите аватарку")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                            cameraActivityResultLauncher.launch(takePictureIntent)
                        }
                    }

                    1 -> {
                        val pickPhotoIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryActivityResultLauncher.launch(pickPhotoIntent)
                    }
                }
            }
            .show()
    }

    private fun showEditNameDialog() {
        val currentName = binding.userName.text.toString()
        val editText = EditText(requireContext()).apply {
            setText(currentName)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить имя")
            .setView(editText)
            .setPositiveButton("✔️") { _, _ ->
                val newName = editText.text.toString()
                viewModel.updateUserName(newName)
                Toast.makeText(requireContext(), "Имя обновлено!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("❌", null)
            .show()
    }

    private fun onUploadSuccess(uri: Uri) {
        if (isAdded) {
            Toast.makeText(requireContext(), "Аватарка успешно загружена!", Toast.LENGTH_SHORT)
                .show()
            viewModel.updateProfileImageUrl(uri.toString())
        } else {
            Log.w(TAG, "Фрагмент не был присоединён, не удалось показать Toast")
        }
    }

    private fun onUploadFailure(exception: Exception) {
        Toast.makeText(
            requireContext(),
            "Ошибка загрузки аватарки: ${exception.message}",
            Toast.LENGTH_LONG
        ).show()
    }

    fun setLogoutListener(listener: LogoutListener) {
        logoutListener = listener
    }

    private fun updateIconIfLocationExists(locationName: String?) {
        if (locationName != null && locationName.isNotEmpty()) {
            updateIconToChecked(binding.txtChooseLocation)
            saveIconState("location", true)
        } else {
            updateIconToUnchecked(binding.txtChooseLocation)
            saveIconState("location", false)
        }
    }

    private fun updateIconToChecked(view: View) {
        if (view is TextView) {
            view.text = "✔️"
        }
    }

    private fun updateIconToUnchecked(view: View) {
        if (view is TextView) {
            view.text = "✏️"
        }
    }

    private fun showEditDescriptionDialog() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val currentDescription = documentSnapshot.getString("description") ?: ""

                val editText = EditText(requireContext()).apply {
                    setText(currentDescription)
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    minLines = 5
                    maxLines = 10
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        height = (resources.displayMetrics.density * 300).toInt() // Приблизительная высота для 5 строк
                    }
                }

                // Создаем и отображаем диалоговое окно
                AlertDialog.Builder(requireContext())
                    .setTitle("Изменить описание")
                    .setView(editText)
                    .setPositiveButton("✔️") { _, _ ->
                        val newDescription = editText.text.toString()
                        viewModel.updateUserDescription(newDescription) {
                            updateIconToChecked(binding.txtEditDescription)
                            saveIconState("description", true)
                            Toast.makeText(requireContext(), "Описание обновлено!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("❌", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Ошибка при получении описания пользователя", e)
                Toast.makeText(requireContext(), "Ошибка загрузки описания", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showTechnicDialog() {
        val artStyles = arrayOf(
            "Масляная живопись", "Акварель", "Акриловая живопись", "Темпера",
            "Гуашь", "Фреска", "Пастель", "Энкаустика",
            "Чернила и тушь", "Аэрография", "Графические техники", "Другое"
        )
        val selectedStyles = viewModel.selectedArtStyles.value?.toMutableSet() ?: mutableSetOf()

        val checkedItems = BooleanArray(artStyles.size) { index ->
            selectedStyles.contains(artStyles[index])
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите технику")
            .setMultiChoiceItems(artStyles, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedStyles.add(artStyles[which])
                } else {
                    selectedStyles.remove(artStyles[which])
                }
            }
            .setPositiveButton("✔️") { _, _ ->
                viewModel.updateSelectedStyles(selectedStyles) {
                    binding.txtTechnic.text = selectedStyles.joinToString(", ")
                    updateIconToChecked(binding.txtTechnic)
                    saveIconState("technic", true)
                    Toast.makeText(requireContext(), "Техники сохранены!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("❌", null)
            .show()
    }

    private fun saveIconState(key: String, state: Boolean) {
        val sharedPreferences =
            requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(key, state)
            apply()
        }
    }

    private fun loadSavedIconStates() {
        val sharedPreferences =
            requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
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

    private fun saveAndUploadProfileImage(bitmap: Bitmap) {
        viewModel.uploadImageToFirebase(bitmap, ::onUploadSuccess, ::onUploadFailure)
    }
}
