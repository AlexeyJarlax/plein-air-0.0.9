package com.pavlovalexey.pleinair.profile.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentProfileBinding
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import com.pavlovalexey.pleinair.utils.image.CircleTransform
import com.pavlovalexey.pleinair.utils.image.setupImageResultLaunchers
import com.pavlovalexey.pleinair.utils.image.showImageSelectionDialog
import com.pavlovalexey.pleinair.utils.ui.DialogUtils
import com.pavlovalexey.pleinair.utils.ui.IconStateUtils
import com.pavlovalexey.pleinair.utils.ui.showSnackbar
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(), UserMapFragment.OnLocationSelectedListener {

    @Inject
    lateinit var loginAndUserUtils: LoginAndUserUtils

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
        IconStateUtils.loadSavedIconStates(requireContext(), binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginAndUserUtils.setupUserProfile { name: String ->
            binding.userName.text = name
        }
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
                loginAndUserUtils.logout()
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
                loginAndUserUtils.updateUserNameOnFirebase(newName)
                binding.userName.text = newName
                showSnackbar("Имя обновлено!")
            }
        )
    }

    private fun showEditDescriptionDialog() {
        DialogUtils.showInputDialog(
            context = requireContext(),
            title = "Редактировать описание",
            initialText = "",
            onConfirm = { newDescription ->
                viewModel.updateUserDescription(newDescription) {
                    IconStateUtils.updateIconToChecked(binding.txtEditDescription)
                }
            }
        )
    }

    private fun showTechnicDialog() {
        val artStyles = resources.getStringArray(R.array.art_styles)
        val selectedStyles = viewModel.selectedArtStyles.value?.toTypedArray() ?: emptyArray()
        val checkedItems = BooleanArray(artStyles.size) { index ->
            selectedStyles.contains(artStyles[index])
        }

        DialogUtils.showMultiChoiceDialog(
            context = requireContext(),
            title = "Выберите технику",
            items = artStyles,
            checkedItems = checkedItems,
            onSelectionChanged = { index, isChecked ->
                checkedItems[index] = isChecked
            },
            onConfirm = {
                val selectedItems = artStyles.filterIndexed { index, _ -> checkedItems[index] }
                viewModel.updateSelectedStyles(selectedItems.toSet()) {
                    binding.txtTechnic.text = selectedItems.joinToString(", ")
                    IconStateUtils.updateIconToChecked(binding.txtTechnic)
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
            showSnackbar("Местоположение сохранено!")
            parentFragmentManager.popBackStack()
            IconStateUtils.saveIconState(requireContext(), "location", true)
            IconStateUtils.updateIconToChecked(binding.txtChooseLocation)
        }
    }
}
