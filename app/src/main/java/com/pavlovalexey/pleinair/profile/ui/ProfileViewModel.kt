package com.pavlovalexey.pleinair.profile.ui
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

        val user = hashMapOf(
            "profileImageUrl" to imageUrl,
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "Profile image URL updated")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating profile image URL", e)
            }
    }

    fun updateUserName(newName: String) {
        val userId = auth.currentUser?.uid ?: return

        val user = hashMapOf(
            "name" to newName,
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User name updated")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating user name", e)
            }
    }

    fun uploadImageToFirebase(imageBitmap: Bitmap, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef: StorageReference = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri)
                    updateProfileImageUrl(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun uploadImageToFirebase(uri: Uri, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef: StorageReference = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri)
                    updateProfileImageUrl(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}