package com.pavlovalexey.pleinair.event.ui.eventList

import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.event.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> get() = _events

    private var allEvents: List<Event> = emptyList()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                val querySnapshot = firebaseFirestore.collection("events").get().await()
                val eventsList = querySnapshot.documents.mapNotNull { it.toObject(Event::class.java) }
                allEvents = eventsList
                _events.value = eventsList
            } catch (e: Exception) {
            }
        }
    }

    fun searchEvents(query: String) {
        val filteredList = if (query.isEmpty()) {
            allEvents
        } else {
            allEvents.filter {
                it.city.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
        _events.value = filteredList
    }
}