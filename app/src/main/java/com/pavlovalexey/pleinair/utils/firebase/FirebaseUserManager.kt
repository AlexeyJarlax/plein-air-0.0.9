package com.pavlovalexey.pleinair.utils.firebase

import android.content.Context
import android.content.SharedPreferences
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
import com.pavlovalexey.pleinair.utils.image.ImageUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class FirebaseUserManager @Inject constructor(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val sharedPreferences: SharedPreferences
) {

    fun getCurrentUserId() : String {
        val userId = auth.currentUser?.uid
        return userId?: "class FirebaseUserManager не выдал ID"
    }

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

    fun updateImageUrl(id: String, imageUrl: String, collectionName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) { // работает на User и Event
        firestore.collection(collectionName).document(id)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d("=== FirebaseUserManager", "updateImageUrl = V")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("=== FirebaseUserManager", "updateImageUrl = X", e)
                onFailure(e)
            }
    }


    fun updateUserLocation(userId: String, location: LatLng, collectionName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {  // работает на User и Event
        firestore.collection(collectionName).document(userId)
            .update("location", GeoPoint(location.latitude, location.longitude))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseUserManager", "Error updating user location", e)
                onFailure(e)
            }
    }

    fun uploadImageToFirebase(  // работает на User и Event
        id: String,
        imageBitmap: Bitmap,
        bitmapStorage: String,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageRef: StorageReference =
            storage.reference.child("$bitmapStorage/$id/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        clearProfileImageFolder(id, bitmapStorage,
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
                onFailure(it)
            }
        )
        saveImageToLocalStorage(imageBitmap, id, bitmapStorage)
    }

    private fun clearProfileImageFolder(  // работает на User и Event
        id: String,
        bitmapStorage: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val folderRef = storage.reference.child("$bitmapStorage/$id/")
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
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun updateSelectedStyles(userId: String, styles: Set<String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .update("artStyles", styles.toList())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
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
                onFailure(e)
            }
    }

    fun loadProfileImageFromStorage(   // работает на User и Event
        id: String,
        bitmapStorage: String,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val localImage = loadImageFromLocalStorage(id, bitmapStorage)
        if (localImage != null) {
            onSuccess(localImage)
        } else {
            val storageRef: StorageReference = storage.reference.child("$bitmapStorage/$id/")
            storageRef.listAll().addOnSuccessListener { listResult ->
                if (listResult.items.isNotEmpty()) {
                    val firstItem = listResult.items[0]
                    firstItem.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        saveImageToLocalStorage(bitmap, id, bitmapStorage)
                        onSuccess(bitmap)
                    }.addOnFailureListener { e ->
                        onFailure(e)
                    }
                } else {
                    onFailure(Exception("No profile image found"))
                }
            }.addOnFailureListener { e ->
                onFailure(e)
            }
        }
    }

    fun setDefaultAvatarIfEmpty(eventId: String) {
        loadProfileImageFromStorage(eventId, "events",
            onSuccess = { bitmap ->
                saveImageToLocalStorage(bitmap, eventId, "events")
            },
            onFailure = {
                // Если аватар не найден, установить аватар по умолчанию
                val defaultAvatar = ImageUtils.generateRandomAvatar()
                uploadImageToFirebase(eventId, defaultAvatar, "events",
                    onSuccess = { uri ->
                        // Успешное обновление
                        updateImageUrl(eventId, uri.toString(), "events",
                            onSuccess = {},
                            onFailure = { error -> Log.e("Avatar", "Ошибка обновления аватара", error) }
                        )

                    },
                    onFailure = { error -> Log.e("Avatar", "Ошибка загрузки аватара", error) }
                )
            }
        )
    }

    private fun loadImageFromLocalStorage(id: String, bitmapStorage: String): Bitmap? {   // работает на User и Event
        val filename = bitmapStorage + "_" + "$id.jpg"
        val file = File(context.filesDir, filename)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    private fun saveImageToLocalStorage(imageBitmap: Bitmap, id: String, bitmapStorage: String) {   // работает на User и Event
        val filename = bitmapStorage + "_" + "$id.jpg"
        val file = File(context.filesDir, filename)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: IOException) {
        } finally {
            fos?.flush()
            fos?.close()
        }
    }
}

