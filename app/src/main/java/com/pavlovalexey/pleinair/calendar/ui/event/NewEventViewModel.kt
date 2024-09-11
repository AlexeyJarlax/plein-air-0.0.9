package com.pavlovalexey.pleinair.calendar.ui.event

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pavlovalexey.pleinair.calendar.data.EventRepository
import com.pavlovalexey.pleinair.calendar.model.Event
import kotlinx.coroutines.launch

class NewEventViewModel(application: Application) : AndroidViewModel(application) {

    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> get() = _isFormValid

    private val _creationStatus = MutableLiveData<CreationStatus>()
    val creationStatus: LiveData<CreationStatus> get() = _creationStatus

    private val eventRepository = EventRepository(application)

    init {
        _isFormValid.value = false
    }

    fun onFieldChanged(city: String, place: String, date: String, time: String, description: String) {
        _isFormValid.value = validateForm(city, place, date, time, description)
    }

    fun updateEventImageUrl(eventId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                eventRepository.updateEventImageUrl(eventId, imageUrl)
            } catch (e: Exception) {
                Log.e("NewEventViewModel", "Failed to update event image URL", e)
            }
        }
    }

    private fun validateForm(city: String, place: String, date: String, time: String, description: String): Boolean {
        return city.isNotEmpty() && place.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && description.isNotEmpty()
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