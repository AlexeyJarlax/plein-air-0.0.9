package com.pavlovalexey.pleinair.event.ui.newEvent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlovalexey.pleinair.event.data.EventRepository
import com.pavlovalexey.pleinair.event.model.Event
import com.pavlovalexey.pleinair.event.model.NewEventUiState
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewEventViewModel @Inject constructor(
    private val firebaseUserManager: FirebaseUserManager,
    private val eventRepository: EventRepository,
) : ViewModel() {

    val uiState: MutableStateFlow<NewEventUiState> = MutableStateFlow(NewEventUiState())

    private val _creationStatus = MutableStateFlow<CreationStatus>(CreationStatus.Idle)
    val creationStatus: StateFlow<CreationStatus> get() = _creationStatus

    private val _existingEvent = MutableLiveData<Event?>()
    val existingEvent: LiveData<Event?> get() = _existingEvent

    init {
        viewModelScope.launch {
            val userId = firebaseUserManager.getCurrentUserId()
            val event = eventRepository.getEventByUserId(userId)
            _existingEvent.value = event
            Log.d("NewEventViewModel", "=== Existing event for user $userId: $event")
        }
    }

    fun createEvent() {
        _creationStatus.value = CreationStatus.Loading

        viewModelScope.launch {
            val userId = firebaseUserManager.getCurrentUserId()
            val userProfileImageUrl = firebaseUserManager.getCurrentUserProfileImageUrl()

            val event = Event(
                id = UUID.randomUUID().toString(),
                userId = userId,
                city = uiState.value.city,
                date = uiState.value.date,
                time = uiState.value.time,
                description = uiState.value.description,
                latitude = uiState.value.latitude ?: 0.0,
                longitude = uiState.value.longitude ?: 0.0,
                profileImageUrl = userProfileImageUrl
            )

            try {
                val eventId = eventRepository.createEvent(event)
                _creationStatus.value = CreationStatus.Success(eventId)
                Log.d("NewEventViewModel", "=== Event created with id $eventId")
            } catch (e: Exception) {
                _creationStatus.value = CreationStatus.Error(e.localizedMessage ?: "Error creating event")
                Log.e("NewEventViewModel", "=== Failed to create event: ${e.localizedMessage}")
            }
        }
    }

    fun deleteExistingEventAndCreateNew() {
        viewModelScope.launch {
            val userId = firebaseUserManager.getCurrentUserId()
            val eventId = _existingEvent.value?.id
                Log.d("NewEventViewModel", "=== Attempting to delete event with id $userId")
                try {
                    eventRepository.deleteEvent(userId, eventId?: "")
//                    createEvent()
                } catch (e: Exception) {
                    _creationStatus.value = CreationStatus.Error(e.localizedMessage ?: "Error deleting existing event")
                    Log.e("NewEventViewModel", "=== Failed to delete event with id $userId: ${e.localizedMessage}")
                }
        }
    }

    fun updateUiState(newState: NewEventUiState) {
        uiState.value = newState
    }
}
