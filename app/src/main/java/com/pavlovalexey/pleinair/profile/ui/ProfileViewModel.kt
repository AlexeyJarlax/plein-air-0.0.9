package com.pavlovalexey.pleinair.profile.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
                            saveImageToLocalStorage(imageBitmap, userId) // Save image to local storage
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
            .update("artStyles", styles.toList()) // Save as a list in Firestore
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

    fun saveImageToLocalStorage(imageBitmap: Bitmap, userId: String) {
        val filename = "profile_image_$userId.jpg"
        val file = File(getApplication<Application>().filesDir, filename)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: IOException) {
            Log.e(TAG, "Error saving image to local storage", e)
        } finally {
            fos?.flush()
            fos?.close()
        }
    }

    private fun loadImageFromLocalStorage(userId: String): Bitmap? {
        val filename = "profile_image_$userId.jpg"
        val file = File(getApplication<Application>().filesDir, filename)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    fun loadProfileImageFromStorage(onSuccess: (Bitmap) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val localImage = loadImageFromLocalStorage(userId)
        if (localImage != null) {
            onSuccess(localImage)
        } else {
            val storageRef: StorageReference = storage.reference.child("profile_images/$userId/")
            storageRef.listAll().addOnSuccessListener { listResult ->
                if (listResult.items.isNotEmpty()) {
                    // Load the first image if available
                    val firstItem = listResult.items[0]
                    firstItem.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        saveImageToLocalStorage(bitmap, userId) // Save to local storage
                        onSuccess(bitmap)
                    }.addOnFailureListener { e ->
                        Log.w(TAG, "Error downloading image from Firebase", e)
                        onFailure(e)
                    }
                } else {
                    // No images found in Firebase
                    onFailure(Exception("No profile image found"))
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error getting image list from Firebase", e)
                onFailure(e)
            }
        }
    }
}