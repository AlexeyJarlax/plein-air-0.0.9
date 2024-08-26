package com.pavlovalexey.pleinair.profile.ui

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentProfileBinding
import com.pavlovalexey.pleinair.auth.AuthActivity
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Инициализация запуска камеры и галереи
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

        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.userName.text = user?.displayName ?: getString(R.string.default_user_name)
            if (user?.photoUrl != null) {
                Picasso.get().load(user.photoUrl).into(binding.userAvatar)
            } else {
                binding.userAvatar.setImageResource(R.drawable.ic_launcher_foreground) // Предполагается наличие стандартной картинки
            }
            binding.logoutButton.visibility = if (user != null) View.VISIBLE else View.GONE
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.userAvatar.setOnClickListener {
            showAvatarSelectionDialog()
        }

        return binding.root
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { _, _ ->
                viewModel.logout()
                requireActivity().recreate() // Перезапуск активности
            }
            .setNegativeButton("Нет", null)
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

    private fun onUploadSuccess(uri: Uri) {
        Toast.makeText(requireContext(), "Аватарка успешно загружена!", Toast.LENGTH_SHORT).show()
        viewModel.updateProfileImageUrl(uri.toString())
    }

    private fun onUploadFailure(exception: Exception) {
        Toast.makeText(requireContext(), "Ошибка загрузки аватарки: ${exception.message}", Toast.LENGTH_LONG).show()
    }
}