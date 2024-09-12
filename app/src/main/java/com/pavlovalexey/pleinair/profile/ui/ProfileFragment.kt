package com.pavlovalexey.pleinair.profile.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentProfileBinding
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys.PREFS_NAME
import com.pavlovalexey.pleinair.utils.image.CircleTransform
import com.pavlovalexey.pleinair.utils.image.setupImageResultLaunchers
import com.pavlovalexey.pleinair.utils.image.showImageSelectionDialog
import com.pavlovalexey.pleinair.utils.ui.DialogUtils
import com.pavlovalexey.pleinair.utils.ui.showSnackbar
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(), UserMapFragment.OnLocationSelectedListener {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    private val TAG = ProfileFragment::class.java.simpleName
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val (cameraLauncher, galleryLauncher) = setupImageResultLaunchers { processedBitmap ->
            handleImageSelection(processedBitmap)
        }
        cameraActivityResultLauncher = cameraLauncher
        galleryActivityResultLauncher = galleryLauncher
        setupObservers()
        setupListeners()
        loadSavedIconStates()
        return binding.root
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            binding.userName.text = user?.name ?: getString(R.string.default_user_name)
            if (!user?.profileImageUrl.isNullOrEmpty()) {
                viewModel.loadProfileImageFromStorage(
                    { bitmap -> binding.userAvatar.setImageBitmap(bitmap) },
                    { Picasso.get().load(user?.profileImageUrl).transform(CircleTransform()).into(binding.userAvatar) }
                )
            } else {
                binding.userAvatar.setImageResource(R.drawable.account_circle_50dp)
            }
            binding.txtChooseLocation.text = user?.locationName ?: getString(R.string.location)
            updateIconIfLocationExists(user?.locationName)
        })

        viewModel.selectedArtStyles.observe(viewLifecycleOwner, Observer { selectedStyles ->

        })
    }

    private fun setupListeners() {
        binding.logoutButton.setOnClickListener { showLogoutConfirmationDialog() }
        binding.userAvatar.setOnClickListener {
            showImageSelectionDialog(cameraActivityResultLauncher, galleryActivityResultLauncher)
        }
        binding.userName.setOnClickListener { showEditNameDialog() }
        binding.btnChooseLocation.setOnClickListener { openUserMapFragment() }
        binding.editDescription.setOnClickListener { showEditDescriptionDialog() }
        binding.exitButton.setOnClickListener { showExitConfirmationDialog() }
        binding.btnTechnic.setOnClickListener { showTechnicDialog() }
    }

    private fun handleImageSelection(processedBitmap: Bitmap) {
        binding.userAvatar.setImageBitmap(processedBitmap)
        viewModel.uploadImageToFirebase(processedBitmap,
            onSuccess = { uri ->
                Picasso.get().load(uri).transform(CircleTransform()).into(binding.userAvatar)
            },
            onFailure = {
            }
        )
    }

    private fun showLogoutConfirmationDialog() {
        DialogUtils.showConfirmationDialog(
            context = requireContext(),
            title = "Log out",
            message = "Разлогинить пользователя?",
            onConfirm = {
                viewModel.logout()
                requireActivity().recreate()
            }
        )
    }

    private fun showEditNameDialog() {
        val currentName = binding.userName.text.toString()
        DialogUtils.showInputDialog(
            context = requireContext(),
            title = "Изменить имя",
            initialText = currentName,
            onConfirm = { newName ->
                viewModel.updateUserName(newName)
                showSnackbar("Имя обновлено!")
            }
        )
    }

    private fun showEditDescriptionDialog() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val currentDescription = documentSnapshot.getString("description") ?: ""
                DialogUtils.showInputDialog(
                    context = requireContext(),
                    title = "Изменить описание",
                    initialText = currentDescription,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
                    onConfirm = { newDescription ->
                        viewModel.updateUserDescription(newDescription) {
                            updateIconToChecked(binding.txtEditDescription)
                            saveIconState("description", true)
                            showSnackbar("Описание обновлено!")
                        }
                    }
                )
            }
            .addOnFailureListener { e ->
                showSnackbar("Ошибка загрузки описания")
                Log.w(TAG, "Ошибка при получении описания пользователя", e)
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

        DialogUtils.showMultiChoiceDialog(
            context = requireContext(),
            title = "Выберите технику",
            items = artStyles,
            checkedItems = checkedItems,
            onSelectionChanged = { which, isChecked ->
                if (isChecked) {
                    selectedStyles.add(artStyles[which])
                } else {
                    selectedStyles.remove(artStyles[which])
                }
            },
            onConfirm = {
                viewModel.updateSelectedStyles(selectedStyles) {
                    binding.txtTechnic.text = selectedStyles.joinToString(", ")
                    updateIconToChecked(binding.txtTechnic)
                    saveIconState("technic", true)
                    showSnackbar("Техники сохранены!")
                }
            }
        )
    }

    private fun showExitConfirmationDialog() {
        DialogUtils.showConfirmationDialog(
            context = requireContext(),
            title = "Exit",
            message = "Закрыть приложение?",
            onConfirm = { activity?.finishAffinity() }
        )
    }

    private fun openUserMapFragment() {
        val userMapFragment = UserMapFragment()
        userMapFragment.setOnLocationSelectedListener(this)
        findNavController().navigate(R.id.action_profileFragment_to_UserMapFragment)
    }

    override fun onLocationSelected(location: LatLng) {
        viewModel.updateUserLocation(location) {
            updateIconToChecked(binding.txtChooseLocation)
            saveIconState("location", true)
            showSnackbar("Местоположение сохранено!")
            parentFragmentManager.popBackStack()
        }
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

    private fun saveIconState(key: String, state: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(key, state)
            apply()
        }
    }

    private fun loadSavedIconStates() {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
