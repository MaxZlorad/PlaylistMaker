package com.practicum.playlistmaker.library.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityMediaLibraryBinding
import com.practicum.playlistmaker.library.ui.fragments.MediaLibraryViewPagerAdapter
import com.practicum.playlistmaker.library.ui.view_model.MediaLibraryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaLibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaLibraryBinding
    private val viewModel: MediaLibraryViewModel by viewModel()
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = MediaLibraryViewPagerAdapter(this)

        tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.playlists)
                1 -> getString(R.string.favourite_tracks)
                else -> ""
            }
        }
        tabLayoutMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}