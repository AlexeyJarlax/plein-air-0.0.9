package com.pavlovalexey.pleinair.profile.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.pavlovalexey.pleinair.profile.UserRepository
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import com.pavlovalexey.pleinair.utils.image.ImageUtils
import com.pavlovalexey.pleinair.utils.ui.DialogUtils
import com.pavlovalexey.pleinair.utils.ui.showSnackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseUserManager: FirebaseUserManager,
    private val loginAndUserUtils: LoginAndUserUtils,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user
    private val _selectedArtStyles = MutableLiveData<Set<String>>(emptySet())
    val selectedArtStyles: LiveData<Set<String>> get() = _selectedArtStyles

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                _user.value = userRepository.getUserById(userId)
            }
        }
    }

    fun updateUserName(newName: String, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        loginAndUserUtils.updateUserNameOnFirebase(newName)
        onComplete()
    }

    fun updateUserDescription(newDescription: String, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateUserDescription(
            userId,
            newDescription,
            onSuccess = {
                _user.value = _user.value?.copy(description = newDescription)
                onComplete()
            },
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating user description", e)
            }
        )

    }

    fun signOut() {
        auth.signOut()
    }


    fun checkAndGenerateAvatar(onComplete: () -> Unit) {
        val currentUser = _user.value ?: return
        if (currentUser.profileImageUrl.isEmpty()) {
            val generatedAvatar = ImageUtils.generateRandomAvatar()
            uploadAvatarImageToFirebase(
                imageBitmap = generatedAvatar,
                onSuccess = { uri ->
                    updateProfileImageUrl(uri.toString())
                    onComplete()
                },
                onFailure = {
                    Log.w("ProfileViewModel", "Error uploading generated avatar", it)
                }
            )
        } else {
            onComplete()
        }
    }

    fun uploadAvatarImageToFirebase(
        imageBitmap: Bitmap,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.uploadImageToFirebase(
            userId,
            imageBitmap,
            "profile_images",
            onSuccess = { uri ->
                val updatedUser = _user.value?.copy(profileImageUrl = uri.toString())
                _user.value = updatedUser
                saveProfileImageUrl(uri.toString())
                updateProfileImageUrl(uri.toString())
                onSuccess(uri)
            },
            onFailure = {
                Log.w("ProfileViewModel", "Error uploading image", it)
                onFailure(it)
            }
        )
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateImageUrl(
            userId,
            imageUrl,
            "users",
            onSuccess = {
                _user.value = _user.value?.copy(profileImageUrl = imageUrl)
                saveProfileImageUrl(imageUrl)
            },
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating profile image URL", e)
            }
        )
    }

    fun loadProfileImageFromStorage(onSuccess: (Bitmap) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.loadProfileImageFromStorage(
            userId,
            "profile_images",
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun updateUserLocation(location: LatLng, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateUserLocation(
            userId,
            location,
            "users",
            onSuccess = onSuccess,
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating user location", e)
            }
        )
    }


    fun updateSelectedStyles(styles: Set<String>, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateSelectedStyles(
            userId,
            styles,
            onSuccess = {
                _selectedArtStyles.value = styles
                onSuccess()
            },
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating art styles", e)
            }
        )
    }

    private fun saveProfileImageUrl(url: String) {
        with(sharedPreferences.edit()) {
            putString("profileImageUrl", url)
            apply()
        }
    }
}