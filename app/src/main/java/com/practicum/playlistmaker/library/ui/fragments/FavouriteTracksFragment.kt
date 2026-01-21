package com.practicum.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.databinding.FragmentFavouriteTracksBinding
import com.practicum.playlistmaker.library.ui.view_model.FavouriteTracksViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.domain.models.FavoriteTracksState
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.ui.track.TrackAdapter

class FavouriteTracksFragment : Fragment() {

    private var _binding: FragmentFavouriteTracksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavouriteTracksViewModel by viewModel()

    // Переиспользуем адаптер из экрана поиска
    private val trackAdapter = TrackAdapter(
        tracks = emptyList(),
        onItemClick = { track -> onTrackClick(track) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка RecyclerView
        setupRecyclerView()

        // Подписываемся на изменения состояния
        observeViewModel()
    }

    // Настройка RecyclerView для списка треков
    private fun setupRecyclerView() {
        binding.recyclerViewFavoriteTracks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    // Подписка на изменения состояния
    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoriteTracksState.Empty -> {
                    // Показываем заглушку "Нет избранных треков"
                    binding.placeholderImage.visibility = View.VISIBLE
                    binding.placeholderMessage.visibility = View.VISIBLE
                    binding.recyclerViewFavoriteTracks.visibility = View.GONE
                }
                is FavoriteTracksState.Content -> {
                    // Показываем список треков
                    binding.placeholderImage.visibility = View.GONE
                    binding.placeholderMessage.visibility = View.GONE
                    binding.recyclerViewFavoriteTracks.visibility = View.VISIBLE

                    // Обновляем данные в адаптере
                    //trackAdapter.tracks = state.tracks
                    trackAdapter.updateTracks(state.tracks)
                }
            }
        }
    }

    // бработка клика по треку
    private fun onTrackClick(track: Track) {

        val bundle = Bundle().apply {
            putSerializable("track", track)
        }
        findNavController().navigate(R.id.playerFragment, bundle)
    }

    // Обновляем список при возвращении на экран
    override fun onResume() {
        super.onResume()
        // Перезагружаем треки, чтобы обновить isFavorite
        viewModel.loadFavoriteTracks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavouriteTracksFragment()
    }
}
