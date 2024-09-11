package com.pavlovalexey.pleinair.settings.data

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.settings.domain.SettingsRepository
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys.KEY_NIGHT_MODE
import dagger.hilt.android.internal.Contexts.getApplication
import de.cketti.mailto.EmailIntentBuilder
import java.io.File

class SettingsRepositoryImpl(private val context: Context, private val sharedPreferences: SharedPreferences) :
    SettingsRepository {

    override fun loadNightMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_NIGHT_MODE, false)
    }

    override fun saveNightMode(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NIGHT_MODE, value).apply()
    }

    override fun applyTheme() { // функция, применяющая настройки день/ночь на все приложение, передаю в Application()
        val nightModeEnabled = loadNightMode()
        if (nightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun buttonToShareApp() {
        val appId = context.getString(R.string.app_id)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            context.getString(R.string.share_app_text) + appId // идентификатор приложения
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val chooserIntent = Intent.createChooser(intent, context.getString(R.string.share_app_title))
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }

    override fun buttonToHelp() {
        val subject = context.getString(R.string.support_email_subject)
        val body = context.getString(R.string.support_email_text)
        val email = context.getString(R.string.support_email)
        val emailIntent = EmailIntentBuilder.from(context)
            .to(email)
            .subject(subject)
            .body(body)
            .start()
    }
    override fun buttonToSeePrivacyPolicy() {
        val url = context.getString(R.string.privacy_policy_url)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun buttonToSeeUserAgreement() {
        val url = context.getString(R.string.user_agreement_url)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun buttonDonat() {
        val url = context.getString(R.string.donat)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun sharePlaylist(message: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun deleteUserAccount(onAccountDeleted: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("profile_images/$userId/")

            // Удаляем файлы из Firebase Storage
            storageRef.listAll().addOnSuccessListener { listResult ->
                val items = listResult.items
                for (item in items) {
                    item.delete().addOnSuccessListener {
                        Log.d("DeleteUser", "Файл успешно удален из Firebase Storage: ${item.path}")
                    }.addOnFailureListener { e ->
                        Log.w("DeleteUser", "Ошибка при удалении файла из Firebase Storage: ${item.path}", e)
                    }
                }
            }.addOnFailureListener { e ->
                Log.w("DeleteUser", "Ошибка при получении списка файлов для удаления из Firebase Storage", e)
            }

            // Удаление данных из Firestore
            db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener {
                    Log.d("DeleteUser", "Данные пользователя успешно удалены из Firestore.")

                    // Удаление файлов из локального хранилища
                    val filesDir = context.filesDir
                    val file = File(filesDir, "profile_image_$userId.jpg") // Замените на соответствующее имя файла
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.d("DeleteUser", "Файл успешно удален из локального хранилища: ${file.path}")
                        } else {
                            Log.w("DeleteUser", "Ошибка при удалении файла из локального хранилища: ${file.path}")
                        }
                    } else {
                        Log.d("DeleteUser", "Файл не найден в локальном хранилище: ${file.path}")
                    }

                    // Удаляем данные из SharedPreferences
                    sharedPreferences.edit().clear().apply()
                    Log.d("DeleteUser", "Данные пользователя успешно удалены из SharedPreferences.")

                    // Удаляем учетную запись пользователя из Firebase Authentication
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("DeleteUser", "Учетная запись пользователя успешно удалена.")
                                onAccountDeleted() // Вызываем callback после успешного удаления
                            } else {
                                Log.w("DeleteUser", "Ошибка при удалении учетной записи пользователя.", task.exception)
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("DeleteUser", "Ошибка при удалении данных пользователя из Firestore", e)
                }
        }
    }
}
