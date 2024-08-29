package com.pavlovalexey.pleinair.profile.ui

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.UUID

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _user = MutableLiveData<FirebaseUser?>().apply {
        value = auth.currentUser
    }
    val user: LiveData<FirebaseUser?> = _user

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
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating user name", e)
            }
    }

    fun updateUserLocation(location: LatLng) {
        val userId = auth.currentUser?.uid ?: return

        val locationMap = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        firestore.collection("users").document(userId)
            .update("location", locationMap)
            .addOnSuccessListener {
                Log.d(TAG, "User location updated")
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

                // Wait for all delete tasks to complete
                Tasks.whenAll(deleteTasks)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Метод для загрузки изображения в Firebase Storage и обновления URL профиля
    fun uploadImageToFirebase(imageBitmap: Bitmap, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef: StorageReference = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Очищаем папку перед загрузкой нового изображения
        clearProfileImageFolder(userId,
            onSuccess = {
                // Загрузка нового изображения
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

    // Метод для загрузки изображения из URI в Firebase Storage и обновления URL профиля
    fun uploadImageToFirebase(uri: Uri, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef: StorageReference = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")

        // Очищаем папку перед загрузкой нового изображения
        clearProfileImageFolder(userId,
            onSuccess = {
                // Загрузка нового изображения
                storageRef.putFile(uri)
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
}