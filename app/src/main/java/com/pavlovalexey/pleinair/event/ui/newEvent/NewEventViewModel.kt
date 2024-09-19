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
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewEventViewModel @Inject constructor(
    application: Application,
    private val firebaseUserManager: FirebaseUserManager,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _creationStatus = MutableLiveData<CreationStatus>()
    val creationStatus: LiveData<CreationStatus> get() = _creationStatus

    val event = MutableLiveData<Event?>()

    fun createEvent(uiState: NewEventUiState) {
        _creationStatus.value = CreationStatus.Loading

        viewModelScope.launch {
            val userId = firebaseUserManager.getCurrentUserId()
            val event = Event(
                id = UUID.randomUUID().toString(),
                userId = userId,
                city = uiState.city,
                date = uiState.date,
                time = uiState.time,
                description = uiState.description,
                latitude = uiState.latitude ?: 0.0,
                longitude = uiState.longitude ?: 0.0
            )
            try {
                val eventId = eventRepository.createEvent(event)
                _creationStatus.value = CreationStatus.Success(eventId)
            } catch (e: Exception) {
                _creationStatus.value = CreationStatus.Error(e.localizedMessage ?: "Error creating event")
            }
        }
    }
}
