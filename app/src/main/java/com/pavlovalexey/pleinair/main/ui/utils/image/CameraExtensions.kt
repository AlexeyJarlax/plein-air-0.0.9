package com.pavlovalexey.pleinair.main.ui.utils.image

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.IOException

private const val TAG = "FragmentExtensions"

fun Fragment.setupImageResultLaunchers(
    onImageSelected: (Bitmap) -> Unit
): Pair<ActivityResultLauncher<Intent>, ActivityResultLauncher<Intent>> {
    val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                val processedBitmap = ImageUtils.compressAndGetCircularBitmap(it)
                onImageSelected(processedBitmap)
            }
        }
    }

    val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let { uri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    val processedBitmap = ImageUtils.compressAndGetCircularBitmap(imageBitmap)
                    onImageSelected(processedBitmap)
                } catch (e: IOException) {
                    Log.e(TAG, "Ошибка при открытии InputStream для URI", e)
                }
            }
        }
    }
    return Pair(cameraLauncher, galleryLauncher)
}

fun Fragment.showImageSelectionDialog(
    cameraActivityResultLauncher: ActivityResultLauncher<Intent>,
    galleryActivityResultLauncher: ActivityResultLauncher<Intent>
) {
    val options = arrayOf("Сделать фото", "Выбрать из галереи")
    AlertDialog.Builder(requireContext())
        .setTitle("Выберите аватарку")
        .setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
                        cameraActivityResultLauncher.launch(takePictureIntent)
                    }
                }
                1 -> {
                    val pickPhotoIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryActivityResultLauncher.launch(pickPhotoIntent)
                }
            }
        }
        .show()
}


