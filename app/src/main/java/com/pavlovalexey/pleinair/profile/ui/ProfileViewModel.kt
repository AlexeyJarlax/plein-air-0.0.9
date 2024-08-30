package com.pavlovalexey.pleinair.profile.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.location.Geocoder
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pavlovalexey.pleinair.profile.model.User
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale
import java.util.UUID

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _user = MutableLiveData<User>().apply {
        val currentUser = auth.currentUser
        value = currentUser?.let {
            User(
                displayName = it.displayName,
                photoUrl = it.photoUrl?.toString(),
                locationName = null
            )
        }
    }
    val user: LiveData<User> get() = _user

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
                // Обновляем значение LiveData
                _user.value = _user.value?.copy(photoUrl = imageUrl)
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
                // Обновляем значение LiveData
                _user.value = _user.value?.copy(displayName = newName)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating user name", e)
            }
    }

    fun updateUserLocation(location: LatLng) {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        val locationName = try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.locality ?: "Неизвестное место"
        } catch (e: IOException) {
            Log.e("LocationError", "Ошибка при выполнении геокодирования", e)
            "Координаты: ${location.latitude}, ${location.longitude}"
        } catch (e: IllegalArgumentException) {
            Log.e("LocationError", "Неверные координаты", e)
            "Координаты: ${location.latitude}, ${location.longitude}"
        }

        // Обновляем значение LiveData
        _user.value = _user.value?.copy(locationName = locationName)
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
