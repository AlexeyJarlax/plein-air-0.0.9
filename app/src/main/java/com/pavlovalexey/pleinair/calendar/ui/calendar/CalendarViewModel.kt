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
                Log.d("CalendarViewModel", "Events loaded: $eventList")
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = "Ошибка загрузки событий: ${exception.message}"
                Log.e("CalendarViewModel", "Error loading events", exception)
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

//    fun createEvent(event: Event, callback: (Boolean) -> Unit) {
//        db.collection("events")
//            .add(event)
//            .addOnSuccessListener {
//                callback(true)
//            }
//            .addOnFailureListener { exception ->
//                _errorMessage.value = "Ошибка создания события: ${exception.message}"
//                callback(false)
//            }
//    }
//
//    fun deleteExpiredEvents() {
//        val expirationTime = System.currentTimeMillis() - 72 * 60 * 60 * 1000
//        db.collection("events")
//            .whereLessThan("timestamp", expirationTime)
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    db.collection("events").document(document.id).delete()
//                }
//            }
//            .addOnFailureListener { exception ->
//                _errorMessage.value = "Ошибка удаления просроченных событий: ${exception.message}"
//            }
//    }
}
