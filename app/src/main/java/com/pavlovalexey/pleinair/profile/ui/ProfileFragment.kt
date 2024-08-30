package com.pavlovalexey.pleinair.profile.ui

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.model.LatLng
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentProfileBinding
import com.pavlovalexey.pleinair.map.ui.MapFragment
import com.squareup.picasso.Picasso
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import android.location.Geocoder
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import java.io.IOException
import java.util.Locale

class ProfileFragment : Fragment(), MapFragment.OnLocationSelectedListener {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>
    private val TAG = ProfileFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                binding.userAvatar.setImageBitmap(imageBitmap)
                viewModel.uploadImageToFirebase(imageBitmap, ::onUploadSuccess, ::onUploadFailure)
            }
        }

        galleryActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                binding.userAvatar.setImageURI(selectedImageUri)
                selectedImageUri?.let { viewModel.uploadImageToFirebase(it, ::onUploadSuccess, ::onUploadFailure) }
            }
        }

        // Наблюдаем за изменениями данных пользователя
        viewModel.user.observe(viewLifecycleOwner) { user ->
            // Обновляем имя пользователя
            binding.userName.text = user?.displayName ?: getString(R.string.default_user_name)

            // Обновляем аватар пользователя
            if (user?.photoUrl != null) {
                Picasso.get().load(user.photoUrl).into(binding.userAvatar)
            } else {
                binding.userAvatar.setImageResource(R.drawable.default_avatar)
            }

            // Обновляем текст с текущим местоположением
            binding.txtChooseLocation.text = user?.locationName ?: getString(R.string.location)

            // Отображаем кнопку выхода, если пользователь авторизован
            binding.logoutButton.visibility = if (user != null) View.VISIBLE else View.GONE
        }

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

        return binding.root
    }

    private fun openMapFragment() {
        val mapFragment = MapFragment()
        // Передаем текущий фрагмент как OnLocationSelectedListener
        mapFragment.setOnLocationSelectedListener(this)
        findNavController().navigate(R.id.action_profileFragment_to_mapFragment)
    }

    override fun onLocationSelected(location: LatLng) {
        viewModel.updateUserLocation(location)

        // Выполнение обратного геокодирования с обработкой ошибок
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val cityName: String = try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.locality ?: "Неизвестное место"
        } catch (e: IOException) {
            Log.e("LocationError", "Ошибка при выполнении геокодирования", e)
            "Координаты: ${location.latitude}, ${location.longitude}"
        } catch (e: IllegalArgumentException) {
            Log.e("LocationError", "Неверные координаты", e)
            "Координаты: ${location.latitude}, ${location.longitude}"
        }

        // Устанавливаем название населенного пункта в TextView
        binding.txtChooseLocation.text = cityName

        Toast.makeText(requireContext(), "Местоположение сохранено!", Toast.LENGTH_SHORT).show()

        // Возвращаемся на предыдущий экран после выбора местоположения
        parentFragmentManager.popBackStack()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти?")
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
                        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
            Toast.makeText(requireContext(), "Аватарка успешно загружена!", Toast.LENGTH_SHORT).show()
            viewModel.updateProfileImageUrl(uri.toString())
        } else {
            Log.w(TAG, "Фрагмент не был присоединён, не удалось показать Toast")
        }
    }

    private fun onUploadFailure(exception: Exception) {
        Toast.makeText(requireContext(), "Ошибка загрузки аватарки: ${exception.message}", Toast.LENGTH_LONG).show()
    }
}
