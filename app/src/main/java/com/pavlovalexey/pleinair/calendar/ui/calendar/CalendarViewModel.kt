package com.pavlovalexey.pleinair.calendar.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.calendar.model.Event

import javax.inject.Inject

class CalendarViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : ViewModel() {

    private val _events = MutableLiveData<List<Event>?>()
    val events: LiveData<List<Event>?> get() = _events
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    fun loadEvents() {
        firebaseFirestore.collection("events")
            .get()
            .addOnSuccessListener { result ->
                val eventList = result.mapNotNull { it.toObject(Event::class.java) }
                _events.value = eventList
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = "Ошибка загрузки событий: ${exception.message}"
            }
    }

    fun searchEvents(query: String) {
        val filteredList = _events.value?.filter {
            it.city.contains(query, true) || it.description.contains(query, true)
        }
        _events.value = filteredList
    }

    fun checkUserEvent(userId: String, callback: (Boolean) -> Unit) {
        firebaseFirestore.collection("events")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                callback(result.isEmpty)
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = "Ошибка проверки событий: ${exception.message}"
            }
    }
}