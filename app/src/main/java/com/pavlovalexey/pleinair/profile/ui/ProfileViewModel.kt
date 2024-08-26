package com.pavlovalexey.pleinair.profile.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _user = MutableLiveData<FirebaseUser?>().apply {
        value = auth.currentUser
    }
    val user: LiveData<FirebaseUser?> = _user

    fun logout() {
        auth.signOut()
        _user.value = null
    }

    fun uploadImageToFirebase(bitmap: Bitmap, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = storage.reference.child("avatars/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener(onSuccess)
        }.addOnFailureListener(onFailure)
    }

    fun uploadImageToFirebase(uri: Uri, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = storage.reference.child("avatars/${UUID.randomUUID()}.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener(onFailure)
    }
}