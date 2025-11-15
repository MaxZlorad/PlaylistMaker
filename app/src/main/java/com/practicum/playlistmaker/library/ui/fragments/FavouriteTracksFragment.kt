package com.practicum.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.ui.view_model.FavouriteTracksViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavouriteTracksFragment : Fragment() {

    private val viewModel: FavouriteTracksViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favourite_tracks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.favouriteTracks.observe(viewLifecycleOwner) { tracks ->
            // Реализация
        }
    }

    companion object {
        fun newInstance() = FavouriteTracksFragment()
    }
}