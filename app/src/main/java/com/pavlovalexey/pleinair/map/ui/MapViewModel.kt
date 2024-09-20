package com.pavlovalexey.pleinair.map.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlovalexey.pleinair.event.model.Event
import com.pavlovalexey.pleinair.profile.data.UserRepository
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.event.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch

class MapViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadUsers(onlyOnline: Boolean) {
        _loading.value = true
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val usersList = result.documents.mapNotNull { it.toObject(User::class.java) }
                    .filter { user -> if (onlyOnline) user.isOnline == true else true }
                _users.value = usersList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
            }
    }

    fun loadEvents() {
        _loading.value = true
        firestore.collection("events")
            .get()
            .addOnSuccessListener { result ->
                val eventsList = result.documents.mapNotNull { it.toObject(Event::class.java) }
                _events.value = eventsList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
            }
    }




}
