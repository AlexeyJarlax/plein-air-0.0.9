package com.pavlovalexey.pleinair.calendar.ui.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.calendar.model.Event

class CalendarViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _events = MutableLiveData<List<Event>?>()
    val events: MutableLiveData<List<Event>?> get() = _events
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loadEvents() {
        db.collection("events")
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
            it.city.contains(query, true) || it.place.contains(query, true) || it.description.contains(query, true)
        }
        _events.value = filteredList
    }

    fun checkUserEvent(userId: String, callback: (Boolean) -> Unit) {
        db.collection("events")
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