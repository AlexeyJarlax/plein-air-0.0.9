package com.pavlovalexey.pleinair.utils.timeAndData

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pavlovalexey.pleinair.databinding.FragmentNewEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Fragment.openTimePickerDialog(binding: FragmentNewEventBinding) {
    val timePickerDialog = TimePickerDialog(
        requireContext(),
        { _, hourOfDay, minute ->
            binding.inputTime.setText(
                String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    hourOfDay,
                    minute
                )
            )
        },
        0, 0, true
    )

    timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE, "✔️", timePickerDialog)
    timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "❌", timePickerDialog)
    timePickerDialog.show()
}

fun Fragment.openDatePickerDialog(binding: FragmentNewEventBinding) {
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        requireContext(),
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val today = Calendar.getInstance()
            if (selectedDate.before(today) || selectedDate.timeInMillis - today.timeInMillis > TimeUnit.DAYS.toMillis(3)) {
                Toast.makeText(
                    requireContext(),
                    "Выберите дату в пределах 3 дней от сегодняшнего дня",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.inputDay.setText(
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(selectedDate.time)
                )
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "✔️", datePickerDialog)
    datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "❌", datePickerDialog)

    datePickerDialog.datePicker.minDate = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_MONTH, 3)
    datePickerDialog.datePicker.maxDate = calendar.timeInMillis
    datePickerDialog.show()
}