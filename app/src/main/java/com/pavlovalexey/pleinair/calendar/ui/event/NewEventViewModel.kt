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

    private val _event = MutableLiveData<Event?>()
    val event: MutableLiveData<Event?> get() = _event

    private val _user = MutableLiveData<User?>()
    val user: MutableLiveData<User?> get() = _user

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

    fun checkAndGenerateEventAvatar(onSuccess: () -> Unit) {
        val currentEvent = _event.value ?: return

        if (currentEvent.profileImageUrl.isEmpty()) {
            val generatedAvatar = ImageUtils.generateRandomAvatar()
            uploadEventImageToFirebase(
                imageBitmap = generatedAvatar,
                onSuccess = { uri ->
                    updateEventProfileImageUrl(uri.toString())
                    onSuccess()
                },
                onFailure = {
                    Log.w("NewEventViewModel", "Error uploading generated avatar", it)
                }
            )
        } else {
            onSuccess()
        }
    }

    fun uploadEventImageToFirebase(
        imageBitmap: Bitmap,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val eventId = _event.value?.id ?: return
        firebaseUserManager.uploadImageToFirebase(
            eventId,
            imageBitmap,
            onSuccess = { uri ->
                val updatedEvent = _event.value?.copy(profileImageUrl = uri.toString())
                _event.value = updatedEvent
                saveEventProfileImageUrl(uri.toString())
                onSuccess(uri)
            },
            onFailure = {
                Log.w("NewEventViewModel", "Error uploading image", it)
                onFailure(it)
            }
        )
    }

    private fun updateEventProfileImageUrl(imageUrl: String) {
        val eventId = _event.value?.id ?: return
        firebaseUserManager.updateProfileImageUrl(
            eventId,
            imageUrl,
            onSuccess = {
                _event.value = _event.value?.copy(profileImageUrl = imageUrl)
                saveEventProfileImageUrl(imageUrl)
            },
            onFailure = { e ->
                Log.w("NewEventViewModel", "Error updating event image URL", e)
            }
        )
    }

    fun loadEventImageFromStorage(onSuccess: (Bitmap) -> Unit, onFailure: (Exception) -> Unit) {
        val eventId = _event.value?.id ?: return
        firebaseUserManager.loadProfileImageFromStorage(
            eventId,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun saveEventProfileImageUrl(url: String) {
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