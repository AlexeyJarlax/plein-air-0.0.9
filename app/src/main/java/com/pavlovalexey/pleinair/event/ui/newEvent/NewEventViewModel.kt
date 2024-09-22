package com.pavlovalexey.pleinair.event.ui.newEvent

import android.app.Application
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
            _existingEvent.value = eventRepository.getEventByUserId(userId)
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
            } catch (e: Exception) {
                _creationStatus.value = CreationStatus.Error(e.localizedMessage ?: "Error creating event")
            }
        }
    }

    fun deleteExistingEventAndCreateNew() {
        viewModelScope.launch {
            _existingEvent.value?.let { existingEvent ->
                try {
                    eventRepository.deleteEvent(existingEvent.id)
                    createEvent()
                } catch (e: Exception) {
                    _creationStatus.value = CreationStatus.Error(e.localizedMessage ?: "Error deleting existing event")
                }
            }
        }
    }

    fun updateUiState(newState: NewEventUiState) {
        uiState.value = newState
    }
}