package com.pavlovalexey.pleinair.settings.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pavlovalexey.pleinair.databinding.FragmentSettingsBinding
import com.pavlovalexey.pleinair.utils.ui.setDebouncedClickListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDarkModeSwitch()
        setupShareButton()
        setupSupportButton()
        setupUserAgreementButton()
        setupPrivacyPolicy()
        donat()
        setupDeleteAccountButton()

        viewModel.accountDeleted.observe(viewLifecycleOwner) { accountDeleted ->
            if (accountDeleted) {
                // Закрыть активити независимо от результата
                activity?.finish()
            }
        }

        // Добавьте наблюдателя для отслеживания состояния загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDarkModeSwitch() {
        viewModel.isNightMode.observe(viewLifecycleOwner) { isNightMode ->
            binding.switchDarkMode.isChecked = isNightMode
        }
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.changeNightMode(isChecked)
        }
    }

    private fun setupShareButton() {
        binding.buttonSettingsShare.setDebouncedClickListener {
            viewModel.shareApp()
        }
    }

    private fun setupSupportButton() {
        binding.buttonSettingsWriteToSupp.setDebouncedClickListener {
            viewModel.goToHelp()
        }
    }

    private fun setupUserAgreementButton() {
        binding.buttonSettingsUserAgreement.setDebouncedClickListener {
            viewModel.seeUserAgreement()
        }
    }

    private fun setupPrivacyPolicy() {
        binding.buttonSettingsPrivacyPolicy.setDebouncedClickListener {
            viewModel.seePrivacyPolicy()
        }
    }

    private fun donat() {
        binding.donats.setDebouncedClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Подтверждение")
            builder.setMessage("Вы перейдете на платежную систему для пожертвований. Продолжить?")
            builder.setPositiveButton("✔️") { dialog, which ->
                viewModel.seeDonat()
            }
            builder.setNegativeButton("❌") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun setupDeleteAccountButton() {
        binding.buttonDeleteAccount.setDebouncedClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Подтверждение удаления")
        builder.setMessage("Будут удалены все данные пользователя и аккаунт в этом приложении. Вы уверены, что хотите продолжить?")
        builder.setPositiveButton("✔️") { dialog, _ ->
            viewModel.deleteUserAccount()
            dialog.dismiss()
        }
        builder.setNegativeButton("❌") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}