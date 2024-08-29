package com.pavlovalexey.pleinair.profile.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.commit
import com.google.android.gms.maps.model.LatLng
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentProfileBinding
import com.pavlovalexey.pleinair.map.MapFragment
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(), MapFragment.OnLocationSelectedListener {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.userName.text = user?.displayName ?: getString(R.string.default_user_name)
            if (user?.photoUrl != null) {
                Picasso.get().load(user.photoUrl).into(binding.userAvatar)
            } else {
                binding.userAvatar.setImageResource(R.drawable.default_avatar)
            }
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
        parentFragmentManager.commit {
            replace(R.id.fragment_container, MapFragment())
            addToBackStack(null)
        }
    }

    override fun onLocationSelected(location: LatLng) {
        viewModel.updateUserLocation(location)
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
        Toast.makeText(requireContext(), "Аватарка успешно загружена!", Toast.LENGTH_SHORT).show()
        viewModel.updateProfileImageUrl(uri.toString())
    }

    private fun onUploadFailure(exception: Exception) {
        Toast.makeText(requireContext(), "Ошибка загрузки аватарки: ${exception.message}", Toast.LENGTH_LONG).show()
    }
}