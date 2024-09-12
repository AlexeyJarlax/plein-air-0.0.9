package com.pavlovalexey.pleinair.utils.ui

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout

object DialogUtils {

    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("✔️") { _, _ -> onConfirm() }
            .setNegativeButton("❌", null)
            .show()
    }

    fun showInputDialog(
        context: Context,
        title: String,
        initialText: String,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        onConfirm: (String) -> Unit
    ) {
        val editText = EditText(context).apply {
            setText(initialText)
            this.inputType = inputType
            minLines = 5
            maxLines = 10
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = (context.resources.displayMetrics.density * 300).toInt()
            }
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("✔️") { _, _ -> onConfirm(editText.text.toString()) }
            .setNegativeButton("❌", null)
            .show()
    }

    fun showOptionsDialog(
        context: Context,
        title: String,
        options: Array<String>,
        onOptionSelected: (Int) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setItems(options) { _, which -> onOptionSelected(which) }
            .show()
    }

    fun showMultiChoiceDialog(
        context: Context,
        title: String,
        items: Array<String>,
        checkedItems: BooleanArray,
        onSelectionChanged: (Int, Boolean) -> Unit,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                onSelectionChanged(which, isChecked)
            }
            .setPositiveButton("✔️") { _, _ -> onConfirm() }
            .setNegativeButton("❌", null)
            .show()
    }
}