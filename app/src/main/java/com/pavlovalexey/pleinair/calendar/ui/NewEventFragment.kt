package com.pavlovalexey.pleinair.calendar.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.pavlovalexey.pleinair.databinding.FragmentNewEventBinding

class NewEventFragment : Fragment() {

    private lateinit var viewModel: NewEventViewModel
    private lateinit var binding: FragmentNewEventBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(NewEventViewModel::class.java)

        // Наблюдение за состоянием ввода
        viewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.createPlaylist.isEnabled = isValid
        }

        // Наблюдение за процессом создания события
        viewModel.creationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is CreationStatus.Loading -> showLoading(true)
                is CreationStatus.Success -> {
                    showLoading(false)
                    findNavController().navigateUp()  // Возврат назад
                }

                is CreationStatus.Error -> {
                    showLoading(false)
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }

        // Установка текстовых изменений для обновления формы
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onFieldChanged(
                    city = binding.inputCity.text.toString(),
                    place = binding.inputLocation.text.toString(),
                    date = binding.inputDay.text.toString(),
                    time = binding.inputTime.text.toString(),
                    description = binding.inputDetails.text.toString()
                )
            }
        }
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.inputCity.addTextChangedListener(afterTextChangedListener)
        binding.inputLocation.addTextChangedListener(afterTextChangedListener)
        binding.inputDay.addTextChangedListener(afterTextChangedListener)
        binding.inputTime.addTextChangedListener(afterTextChangedListener)
        binding.inputDetails.addTextChangedListener(afterTextChangedListener)

        // Кнопка создания события
        binding.createPlaylist.setOnClickListener {
            val userId = "User ID"  // Здесь нужно использовать реальный ID пользователя
            val profileImageUrl =
                "User Avatar URL"  // Здесь нужно использовать реальный URL аватара пользователя

            viewModel.createEvent(
                userId = userId,
                profileImageUrl = profileImageUrl,
                city = binding.inputCity.text.toString(),
                place = binding.inputLocation.text.toString(),
                date = binding.inputDay.text.toString(),
                time = binding.inputTime.text.toString(),
                description = binding.inputDetails.text.toString()
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}