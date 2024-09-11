package com.pavlovalexey.pleinair.profile.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.profile.ui.RandomAvatar
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.LoginAndUserUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val loginAndUserUtils = LoginAndUserUtils(application.applicationContext)

    private val sharedPreferences =
        application.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user
    private val _selectedArtStyles = MutableLiveData<Set<String>>(emptySet())
    val selectedArtStyles: LiveData<Set<String>> get() = _selectedArtStyles

    init {
        loadUser()
    }

    private fun loadUser() {
        val userId = auth.currentUser?.uid ?: return
        val savedName = sharedPreferences.getString("userName", null)
        val savedAvatarUri = sharedPreferences.getString("profileImageUrl", null)

        if (savedName != null && savedAvatarUri != null) {
            _user.value = User(
                name = savedName,
                profileImageUrl = savedAvatarUri ?: "",
                locationName = ""
            )
        } else {
            fetchUserFromServer(userId)
        }
    }

    private fun fetchUserFromServer(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name") ?: loginAndUserUtils.generateRandomUserName()
                    val profileImageUrl =
                        document.getString("profileImageUrl") ?: generateAndSaveRandomAvatar(userId)

                    // Обновляем данные в Firestore
                    val userUpdates = hashMapOf<String, Any>(
                        "name" to name,
                        "profileImageUrl" to profileImageUrl
                    )
                    firestore.collection("users").document(userId)
                        .set(userUpdates, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("ProfileViewModel", "User profile updated in Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.w("ProfileViewModel", "Error updating user profile in Firestore", e)
                        }

                    _user.value = User(
                        name = name,
                        profileImageUrl = profileImageUrl,
                        locationName = ""
                    )

                    // Сохраняем в SharedPreferences
                    with(sharedPreferences.edit()) {
                        putString("userName", name)
                        putString("profileImageUrl", profileImageUrl)
                        apply()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileViewModel", "Error fetching user data", e)
            }
    }

    fun logout() {
        LoginAndUserUtils.logout(getApplication())
    }

    fun uploadImageToFirebase(
        imageBitmap: Bitmap,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
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
                            val serverUrl = uri.toString()
                            saveImageToLocalStorage(
                                imageBitmap,
                                userId
                            ) // Save image to local storage
                            onSuccess(uri)
                            updateProfileImageUrl(serverUrl) // Save server URL to Firestore
                        }
                    }
                    .addOnFailureListener { onFailure(it) }
            },
            onFailure = {
                Log.w("ProfileViewModel", "Error clearing profile image folder", it)
                onFailure(it)
            }
        )
    }

    fun updateProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "Profile image URL updated")
                _user.value = _user.value?.copy(profileImageUrl = imageUrl)

                // Save to SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("profileImageUrl", imageUrl)
                    apply()
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileViewModel", "Error updating profile image URL", e)
            }
    }

    fun updateUserName(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("name", newName)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "User name updated")
                _user.value = _user.value?.copy(name = newName)

                // Save to SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("userName", newName)
                    apply()
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileViewModel", "Error updating user name", e)
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
                Log.w("ProfileViewModel", "Error updating user location", e)
            }
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

    fun updateUserDescription(newDescription: String, onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("description", newDescription)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "User description updated")
                _user.value = _user.value?.copy(description = newDescription)
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("ProfileViewModel", "Error updating user description", e)
            }
    }

    fun updateSelectedStyles(styles: Set<String>, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("artStyles", styles.toList()) // Save as a list in Firestore
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "Art styles updated")
                _selectedArtStyles.value = styles
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("ProfileViewModel", "Error updating art styles", e)
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
                Log.w("ProfileViewModel", "Error loading art styles", e)
            }
    }

    private fun generateAndSaveRandomAvatar(userId: String): String {
        val savedAvatar = loadImageFromLocalStorage(userId)
        if (savedAvatar != null) {
            return avatarBitmapToUri(savedAvatar, userId).toString()
        }

        val avatarBitmap = RandomAvatar().generateRandomAvatar()
        saveImageToLocalStorage(avatarBitmap, userId)
        return avatarBitmapToUri(avatarBitmap, userId).toString()
    }

    private fun avatarBitmapToUri(bitmap: Bitmap, userId: String): Uri {
        val filename = "profile_image_$userId.jpg"
        val file = File(getApplication<Application>().filesDir, filename)
        return Uri.fromFile(file)
    }

    fun saveImageToLocalStorage(imageBitmap: Bitmap, userId: String) {
        val filename = "profile_image_$userId.jpg"
        val file = File(getApplication<Application>().filesDir, filename)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: IOException) {
            Log.e("ProfileViewModel", "Error saving image to local storage", e)
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
                    val firstItem = listResult.items[0]
                    firstItem.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        saveImageToLocalStorage(bitmap, userId) // Save to local storage
                        onSuccess(bitmap)
                    }.addOnFailureListener { e ->
                        Log.w("ProfileViewModel", "Error downloading image from Firebase", e)
                        onFailure(e)
                    }
                } else {
                    onFailure(Exception("No profile image found"))
                }
            }.addOnFailureListener { e ->
                Log.w("ProfileViewModel", "Error getting image list from Firebase", e)
                onFailure(e)
            }
        }
    }
}
