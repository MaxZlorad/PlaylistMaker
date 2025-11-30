package com.practicum.playlistmaker.settings.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.databinding.FragmentSettingsBinding
import com.practicum.playlistmaker.settings.domain.api.ExternalNavigator
import com.practicum.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController

class SettingsFragment : Fragment() {

    // Binding для безопасного доступа к View
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // ViewModel через Koin
    private val viewModel: SettingsViewModel by viewModel()

    // Внешний навигатор для открытия ссылок и шаринга
    private val externalNavigator: ExternalNavigator by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SettingsFragment", "onViewCreated started")

        setupToolbar()
        setupThemeSwitcher()
        setupShareButton()
        setupSupportButton()
        setupTermsButton()
        observeNavigation()

        Log.d("SettingsFragment", "onViewCreated completed")
    }

    private fun setupToolbar() {
        // ??? Используем Navigation Component
        binding.settingsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // Переход вверх по back stack
        }
    }

    private fun setupThemeSwitcher() {
        try {
            val themeSwitch = binding.themeSwitch

            // Подписываемся на изменения темы из ViewModel
            viewModel.themeState.observe(viewLifecycleOwner) { isDarkTheme ->
                themeSwitch.isChecked = isDarkTheme
            }

            // Обработчик изменения переключателя темы
            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setDarkTheme(isChecked)
                applyTheme(isChecked) // Применяем тему сразу после изменения
            }

            Log.d("SettingsFragment", "setupThemeSwitcher completed")
        } catch (e: Exception) {
            Log.e("SettingsFragment", "Error in setupThemeSwitcher", e)
        }
    }

    private fun applyTheme(isDark: Boolean) {
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupShareButton() {
        binding.shareTextView.setOnClickListener {
            viewModel.shareApp()
        }
    }

    private fun setupSupportButton() {
        binding.supportTextView.setOnClickListener {
            viewModel.openSupport()
        }
    }

    private fun setupTermsButton() {
        binding.termsTextView.setOnClickListener {
            viewModel.openTerms()
        }
    }

    private fun observeNavigation() {
        // Наблюдаем за событиями навигации (открытие ссылок, шаринг и т.д.)
        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                externalNavigator.navigate(it)
                viewModel.navigationHandled()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}