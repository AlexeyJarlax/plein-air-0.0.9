package com.pavlovalexey.pleinair.calendar.ui.event

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pavlovalexey.pleinair.calendar.data.EventRepository
import com.pavlovalexey.pleinair.calendar.model.Event
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import com.pavlovalexey.pleinair.utils.image.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewEventViewModel @Inject constructor(
    application: Application,
    private val firebaseUserManager: FirebaseUserManager,
    private val auth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences,
    private val eventRepository: EventRepository
) : AndroidViewModel(application) {

    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> get() = _isFormValid

    private val _user = MutableLiveData<User?>()
    val user: MutableLiveData<User?> get() = _user

    private val _event = MutableLiveData<Event?>()
    val event: MutableLiveData<Event?> get() = _event

    private val _creationStatus = MutableLiveData<CreationStatus>()
    val creationStatus: LiveData<CreationStatus> get() = _creationStatus

    init {
        _isFormValid.value = false
    }

    fun onFieldChanged(city: String, place: String, date: String, time: String, description: String) {
        _isFormValid.value = validateForm(city, place, date, time, description)
    }

    private fun validateForm(city: String, place: String, date: String, time: String, description: String): Boolean {
        return city.isNotEmpty() && place.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && description.isNotEmpty()
    }

    fun checkAndGenerateAvatar(onSuccess: () -> Unit) {
        val currentUser = _user.value ?: return
        val currentEvent = _event.value ?: return

        if (currentEvent.profileImageUrl.isEmpty()) {
            val generatedAvatar = ImageUtils.generateRandomAvatar()
            uploadImageToFirebase(
                imageBitmap = generatedAvatar,
                onSuccess = { uri ->
                    updateProfileImageUrl(uri.toString()) // Обновляем URL аватара в Firestore
                    onSuccess()
                },
                onFailure = {
                    Log.w("ProfileViewModel", "Error uploading generated avatar", it)
                }
            )
        } else {
            onSuccess()
        }
    }

    fun uploadImageToFirebase( //заменить на Event: val id: String = "", val profileImageUrl: String = ""
        imageBitmap: Bitmap,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val id = auth.currentUser?.uid ?: return
        firebaseUserManager.uploadImageToFirebase(
            userId,
            imageBitmap,
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

    private fun updateProfileImageUrl(imageUrl: String) { //заменить на Event: val id: String = "", val profileImageUrl: String = ""
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateProfileImageUrl(
            userId,
            imageUrl,
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
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun saveProfileImageUrl(url: String) { //заменить на Event: val profileImageUrl: String = ""
        with(sharedPreferences.edit()) {
            putString("profileEventImageUrl", url)
            apply()
        }
    }

    fun createEvent(
        userId: String,
        profileImageUrl: String,
        city: String,
        place: String,
        date: String,
        time: String,
        description: String,
        latitude: Double,
        longitude: Double
    ) {
        _creationStatus.value = CreationStatus.Loading

        val event = Event(
            userId = userId,
            profileImageUrl = profileImageUrl,
            city = city,
            place = place,
            date = date,
            time = time,
            description = description,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                val eventId = eventRepository.addEvent(event)
                _creationStatus.value = CreationStatus.Success(eventId.toString())
            } catch (e: Exception) {
                _creationStatus.value = CreationStatus.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}