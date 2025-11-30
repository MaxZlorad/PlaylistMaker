package com.practicum.playlistmaker.library.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentMediaLibraryBinding
import com.practicum.playlistmaker.library.ui.fragments.MediaLibraryViewPagerAdapter
import com.practicum.playlistmaker.library.ui.view_model.MediaLibraryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController

class MediaLibraryFragment : Fragment() {

    // Binding для безопасного доступа к View элементам
    private var _binding: FragmentMediaLibraryBinding? = null
    private val binding get() = _binding!!

    // ViewModel через Koin для инъекции зависимостей
    private val viewModel: MediaLibraryViewModel by viewModel()

    // Медиатор для связи TabLayout и ViewPager
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate макета фрагмента
        _binding = FragmentMediaLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        // Navigation Component
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // Навигируем вверх по back stack
        }
    }

    private fun setupViewPager() {
        // Создаем адаптер для ViewPager с вкладками
        // requireActivity() передаем вместо this, так как фрагмент не является Context
        binding.viewPager.adapter = MediaLibraryViewPagerAdapter(requireActivity())

        // Связываем TabLayout с ViewPager
        tabLayoutMediator = TabLayoutMediator(
            binding.tabLayout, binding.viewPager) { tab, position -> tab.text = when (position) {
                0 -> getString(R.string.favourite_tracks)
                else -> getString(R.string.playlists)
            }
        }
        tabLayoutMediator.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Важно: отключаем медиатор и очищаем binding при уничтожении View
        tabLayoutMediator.detach()
        _binding = null
    }
}