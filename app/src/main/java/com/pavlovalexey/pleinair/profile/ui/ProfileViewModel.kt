package com.pavlovalexey.pleinair.profile.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pavlovalexey.pleinair.profile.model.User
import java.io.ByteArrayOutputStream
import java.util.UUID

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _user = MutableLiveData<User>().apply {
        val currentUser = auth.currentUser
        value = currentUser?.let {
            it.displayName?.let { it1 ->
                it.photoUrl?.toString()?.let { it2 ->
                    User(
                        name = it1,
                        profileImageUrl = it2,
                        locationName = null.toString()
                    )
                }
            }
        }
    }
    val user: LiveData<User> get() = _user
    private val _selectedArtStyles = MutableLiveData<Set<String>>(emptySet())
    val selectedArtStyles: LiveData<Set<String>> get() = _selectedArtStyles

    fun logout() {
        auth.signOut()
        _user.value = null
    }

    fun updateProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d(TAG, "Profile image URL updated")
                _user.value = _user.value?.copy(profileImageUrl = imageUrl)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating profile image URL", e)
            }
    }

    fun updateUserName(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("name", newName)
            .addOnSuccessListener {
                Log.d(TAG, "User name updated")
                _user.value = _user.value?.copy(name = newName)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating user name", e)
            }
    }

    fun updateUserLocation(location: LatLng, onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("location", GeoPoint(location.latitude, location.longitude))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating user location", e)
            }
    }

    private fun clearProfileImageFolder(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val folderRef = storage.reference.child("profile_images/$userId/")
        folderRef.listAll()
            .addOnSuccessListener { listResult ->
                val deleteTasks = listResult.items.map { it.delete() }
                Tasks.whenAll(deleteTasks)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun uploadImageToFirebase(imageBitmap: Bitmap, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef: StorageReference = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        clearProfileImageFolder(userId,
            onSuccess = {
                storageRef.putBytes(data)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            onSuccess(uri)
                            updateProfileImageUrl(uri.toString())
                        }
                    }
                    .addOnFailureListener { onFailure(it) }
            },
            onFailure = {
                Log.w(TAG, "Error clearing profile image folder", it)
                onFailure(it)
            }
        )
    }

    fun updateUserDescription(newDescription: String, onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("description", newDescription)
            .addOnSuccessListener {
                Log.d(TAG, "User description updated")
                _user.value = _user.value?.copy(description = newDescription)
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating user description", e)
            }
    }

    fun updateSelectedStyles(styles: Set<String>, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("artStyles", styles.toList()) // Сохраняем как список в Firestore
            .addOnSuccessListener {
                Log.d(TAG, "Art styles updated")
                _selectedArtStyles.value = styles
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating art styles", e)
            }
    }

    fun loadSelectedStyles() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val styles = document.get("artStyles") as? List<String> ?: emptyList()
                    _selectedArtStyles.value = styles.toSet()
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error loading art styles", e)
            }
    }
}
