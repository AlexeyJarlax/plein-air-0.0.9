package com.pavlovalexey.pleinair.profile.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val firebaseUserManager: FirebaseUserManager,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {

    private val auth = firebaseUserManager.auth
    private val _user = MutableLiveData<User?>()
    val user: MutableLiveData<User?> get() = _user
    private val _selectedArtStyles = MutableLiveData<Set<String>>(emptySet())
    val selectedArtStyles: LiveData<Set<String>> get() = _selectedArtStyles

    init {
        loadUser()
    }

    private fun loadUser() {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.fetchUserFromServer(
            userId,
            onSuccess = { name, profileImageUrl ->
                _user.value = User(name = name, profileImageUrl = profileImageUrl, locationName = "")
            },
            onFailure = {
                Log.w("ProfileViewModel", "Error fetching user data", it)
            }
        )
    }

    fun uploadImageToFirebase(
        imageBitmap: Bitmap,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.uploadImageToFirebase(
            userId,
            imageBitmap,
            onSuccess = { uri ->
                val updatedUser = _user.value?.copy(profileImageUrl = uri.toString())
                _user.value = updatedUser
                with(sharedPreferences.edit()) {
                    putString("profileImageUrl", uri.toString())
                    apply()
                }
                onSuccess(uri)
            },
            onFailure = {
                Log.w("ProfileViewModel", "Error uploading image", it)
                onFailure(it)
            }
        )
    }

    fun updateUserName(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateUserName(
            userId,
            newName,
            onSuccess = {
                _user.value = _user.value?.copy(name = newName)
            },
            onFailure = {
                Log.w("ProfileViewModel", "Error updating user name", it)
            }
        )
    }

    fun logout() {
        LoginAndUserUtils.logout(getApplication())
    }

    fun updateProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateProfileImageUrl(
            userId,
            imageUrl,
            onSuccess = {
                _user.value = _user.value?.copy(profileImageUrl = imageUrl)
                with(sharedPreferences.edit()) {
                    putString("profileImageUrl", imageUrl)
                    apply()
                }
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
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun updateUserLocation(location: LatLng, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateUserLocation(
            userId,
            location,
            onSuccess = onSuccess,
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating user location", e)
            }
        )
    }

    fun updateUserDescription(newDescription: String, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateUserDescription(
            userId,
            newDescription,
            onSuccess = {
                _user.value = _user.value?.copy(description = newDescription)
                onSuccess()
            },
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating user description", e)
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

    fun loadSelectedStyles() {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.loadSelectedStyles(
            userId,
            onSuccess = { styles ->
                _selectedArtStyles.value = styles
            },
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error loading art styles", e)
            }
        )
    }
}
