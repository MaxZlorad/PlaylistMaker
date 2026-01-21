package com.practicum.playlistmaker.library.ui.fragments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MediaLibraryViewPagerAdapter(
    fragment: Fragment // Fragment вместо FragmentActivity
) : FragmentStateAdapter(
    fragment.childFragmentManager, // Используем childFragmentManager
    fragment.viewLifecycleOwner.lifecycle // lifecycle от viewLifecycleOwner
) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavouriteTracksFragment.newInstance()
            else -> PlaylistsFragment.newInstance()
        }
    }

    // Для восстановлении состояния:
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return itemId in 0 until itemCount
    }
}