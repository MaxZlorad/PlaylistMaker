package com.practicum.playlistmaker.library.ui.fragments

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MediaLibraryViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PlaylistsFragment.newInstance()
            1 -> FavouriteTracksFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}