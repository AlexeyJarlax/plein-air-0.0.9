package com.pavlovalexey.pleinair.calendar.ui.event

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.pavlovalexey.pleinair.calendar.data.EventRepository
import com.pavlovalexey.pleinair.calendar.model.Event
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import com.pavlovalexey.pleinair.utils.image.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewEventViewModel @Inject constructor(
    application: Application,
    private val firebaseUserManager: FirebaseUserManager,
    private val sharedPreferences: SharedPreferences,
    private val eventRepository: EventRepository
) : AndroidViewModel(application) {

    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> get() = _isFormValid

    private val _creationStatus = MutableLiveData<CreationStatus>()
    val creationStatus: LiveData<CreationStatus> get() = _creationStatus

    fun onFieldChanged(city: String, place: String, date: String, time: String, description: String, currentStep: Int) {
        _isFormValid.value = when (currentStep) {
            1 -> city.isNotEmpty()
            2 -> place.isNotEmpty()
            3 -> date.isNotEmpty()
            4 -> time.isNotEmpty()
            5 -> true
            else -> false
        }
    }

    fun handleImageSelection(processedBitmap: Bitmap) {
        uploadEventImageToFirebase(processedBitmap, { uri ->
            saveEventProfileImageUrl(uri.toString())
        }, {})
    }

    fun uploadEventImageToFirebase(
        imageBitmap: Bitmap,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firebaseUserManager.uploadImageToFirebase(
            UUID.randomUUID().toString(),
            imageBitmap,
            "event_images",
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun createEvent(location: LatLng) {
        saveEventPreferences(location)
        _creationStatus.value = CreationStatus.Loading
        viewModelScope.launch {
            try {
                val eventId = eventRepository.createEvent()
                _creationStatus.value = CreationStatus.Success(eventId.toString())
            } catch (e: Exception) {
                _creationStatus.value = CreationStatus.Error(e.localizedMessage ?: "Error creating event")
            }
        }
    }

    private fun saveEventPreferences(location: LatLng) {
        with(sharedPreferences.edit()) {
            putString("userId", firebaseUserManager.getCurrentUserId())
            putString("profileEventImageUrl", sharedPreferences.getString("profileEventImageUrl", ""))
            putString("eventCity", sharedPreferences.getString("eventCity", ""))
            putString("eventPlace", sharedPreferences.getString("eventPlace", ""))
            putString("eventDate", sharedPreferences.getString("eventDate", ""))
            putString("eventTime", sharedPreferences.getString("eventTime", ""))
            putString("eventDescription", sharedPreferences.getString("eventDescription", ""))
            putFloat("eventLatitude", location.latitude.toFloat())
            putFloat("eventLongitude", location.longitude.toFloat())
            apply()
        }
    }

    fun validateStep(currentStep: Int): Boolean {
        return when (currentStep) {
            1 -> sharedPreferences.getString("eventCity", "").isNullOrEmpty().not()
            2 -> sharedPreferences.getFloat("eventLatitude", 0f) != 0f
            3 -> sharedPreferences.getString("eventDate", "").isNullOrEmpty().not()
            4 -> sharedPreferences.getString("eventTime", "").isNullOrEmpty().not()
            else -> true
        }
    }

    private fun saveEventProfileImageUrl(url: String) {
        with(sharedPreferences.edit()) {
            putString("profileEventImageUrl", url)
            apply()
        }
    }
}