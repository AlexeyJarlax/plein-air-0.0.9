package com.pavlovalexey.pleinair.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class FirebaseUserManager(private val context: Context) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val sharedPreferences =
        context.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)

    fun fetchUserFromServer(
        userId: String,
        onSuccess: (String, String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name") ?: "Unknown"
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    onSuccess(name, profileImageUrl)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun updateProfileImageUrl(userId: String, imageUrl: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d("FirebaseUserManager", "Profile image URL updated")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error updating profile image URL", e)
                onFailure(e)
            }
    }

    fun updateUserName(userId: String, newName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .update("name", newName)
            .addOnSuccessListener {
                Log.d("FirebaseUserManager", "User name updated")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error updating user name", e)
                onFailure(e)
            }
    }

    fun updateUserLocation(userId: String, location: LatLng, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .update("location", GeoPoint(location.latitude, location.longitude))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error updating user location", e)
                onFailure(e)
            }
    }

    fun uploadImageToFirebase(
        userId: String,
        imageBitmap: Bitmap,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageRef: StorageReference =
            storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        clearProfileImageFolder(userId,
            onSuccess = {
                storageRef.putBytes(data)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            onSuccess(uri)
                        }
                    }
                    .addOnFailureListener { onFailure(it) }
            },
            onFailure = {
                Log.w("FirebaseUserManager", "Error clearing profile image folder", it)
                onFailure(it)
            }
        )
    }

    private fun clearProfileImageFolder(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
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

    fun updateUserDescription(userId: String, newDescription: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .update("description", newDescription)
            .addOnSuccessListener {
                Log.d("FirebaseUserManager", "User description updated")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error updating user description", e)
                onFailure(e)
            }
    }

    fun updateSelectedStyles(userId: String, styles: Set<String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .update("artStyles", styles.toList())
            .addOnSuccessListener {
                Log.d("FirebaseUserManager", "Art styles updated")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error updating art styles", e)
                onFailure(e)
            }
    }

    fun loadSelectedStyles(userId: String, onSuccess: (Set<String>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val styles = document.get("artStyles") as? List<String> ?: emptyList()
                    onSuccess(styles.toSet())
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error loading art styles", e)
                onFailure(e)
            }
    }

    fun loadProfileImageFromStorage(
        userId: String,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val localImage = loadImageFromLocalStorage(userId)
        if (localImage != null) {
            onSuccess(localImage)
        } else {
            val storageRef: StorageReference = storage.reference.child("profile_images/$userId/")
            storageRef.listAll().addOnSuccessListener { listResult ->
                if (listResult.items.isNotEmpty()) {
                    val firstItem = listResult.items[0]
                    firstItem.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        saveImageToLocalStorage(bitmap, userId) // Сохраняем в локальное хранилище
                        onSuccess(bitmap)
                    }.addOnFailureListener { e ->
                        Log.w("FirebaseUserManager", "Error downloading image from Firebase", e)
                        onFailure(e)
                    }
                } else {
                    onFailure(Exception("No profile image found"))
                }
            }.addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error getting image list from Firebase", e)
                onFailure(e)
            }
        }
    }

    private fun loadImageFromLocalStorage(userId: String): Bitmap? {
        val filename = "profile_image_$userId.jpg"
        val file = File(context.filesDir, filename)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    private fun saveImageToLocalStorage(imageBitmap: Bitmap, userId: String) {
        val filename = "profile_image_$userId.jpg"
        val file = File(context.filesDir, filename)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: IOException) {
            Log.e("FirebaseUserManager", "Error saving image to local storage", e)
        } finally {
            fos?.flush()
            fos?.close()
        }
    }
}

