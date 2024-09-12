package com.pavlovalexey.pleinair.utils.ui

//****************************************** объекты заглушки на ошибки или остутствии результата поиска
//INTERNET_EMPTY = проблемы с интернетом при поиске по АПИ
//RESULTS_EMPTY = ничего не нашли в АПИ
//FAVORITES_EMPTY = ваша медиатека пуста
//PLAYLISTS_EMPTY = вы не создали ни одного плейлиста
//LOADING = загружаем
//HIDE = прячем заглушку

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys

object ErrorUtils {

    fun Fragment.ifSearchErrorShowPlug(
        problemTipo: String,
        sendRequestForDoReserch: () -> Unit
    ) {
        val utilErrorBox = view?.findViewById<LinearLayout>(R.id.utilErrorBox)
        val errorIcon = view?.findViewById<ImageView>(R.id.error_icon)
        val errorTextWeb = view?.findViewById<TextView>(R.id.error_text_web)
        val retryButton = view?.findViewById<Button>(R.id.retry_button)

        utilErrorBox?.visibility = View.VISIBLE

        when (problemTipo) {
            AppPreferencesKeys.INTERNET_EMPTY -> {
                errorIcon?.setImageResource(R.drawable.ic_error_internet)
                errorTextWeb?.text = resources.getString(R.string.error_text_web)
                retryButton?.visibility = View.VISIBLE
                retryButton?.setDebouncedClickListener {
                    sendRequestForDoReserch() // тут отправляем на повторный поиск
                    utilErrorBox?.visibility = View.GONE
                }
                utilErrorBox?.setDebouncedClickListener {
                    utilErrorBox.visibility = View.GONE
                }
            }

            AppPreferencesKeys.RESULTS_EMPTY -> {
                errorIcon?.setImageResource(R.drawable.defaut_avatar_120dp)
                errorTextWeb?.text = resources.getString(R.string.nothing_was_found)
                retryButton?.visibility = View.GONE
                utilErrorBox?.setDebouncedClickListener {
                    utilErrorBox?.visibility = View.GONE
                }
            }

            else -> {
                retryButton?.visibility = View.GONE
            }
        }
    }

    fun Fragment.inMedialibraryShowPlug(
        context: Context,
        problemTipo: String
    ) {

        val errorTextWeb = view?.findViewById<TextView>(R.id.error_text_web)
        val retryButton = view?.findViewById<Button>(R.id.retry_button)


        when (problemTipo) {

            AppPreferencesKeys.FAVORITES_EMPTY -> {
                errorTextWeb?.text = context.resources.getString(R.string.app_name)
                retryButton?.visibility = View.GONE
            }

            AppPreferencesKeys.PLAYLISTS_EMPTY -> {
                errorTextWeb?.text =
                    context.resources.getString(R.string.app_name)
                retryButton?.visibility = View.GONE
            }

            AppPreferencesKeys.LOADING -> {
                errorTextWeb?.text = ""
                retryButton?.visibility = View.GONE
            }

            else -> {
                retryButton?.visibility = View.GONE
            }
        }
    }
}