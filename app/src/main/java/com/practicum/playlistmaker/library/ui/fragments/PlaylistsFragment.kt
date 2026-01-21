package com.practicum.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import com.practicum.playlistmaker.library.ui.adapters.PlaylistAdapter
import com.practicum.playlistmaker.library.ui.view_model.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.practicum.playlistmaker.library.ui.adapters.GridSpacingItemDecoration

// Fragment для отображения списка плейлистов пользователя
class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupRecyclerView()
        setupCreatePlaylistButton()
        observeViewModel()
    }

    // Настройка кнопки "Новый плейлист"
    private fun setupCreatePlaylistButton() {
        binding.createPlaylistButton.setOnClickListener {
            // Переход на экран создания плейлиста

            requireActivity().findNavController(R.id.fragmentContainerView).navigate(
                R.id.action_mediaLibraryFragment_to_newPlaylistFragment
            )
        }
    }

    // Подписка на изменения в ViewModel
    private fun observeViewModel() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                showEmptyState()
            } else {
                showPlaylists(playlists)
            }
        }
    }

    private fun showEmptyState() {
        binding.emptyStateGroup.visibility = View.VISIBLE
        binding.playlistsRecyclerView.visibility = View.GONE
    }

    private fun showPlaylists(playlists: List<com.practicum.playlistmaker.library.domain.models.Playlist>) {
        binding.emptyStateGroup.visibility = View.GONE
        binding.playlistsRecyclerView.visibility = View.VISIBLE
        playlistAdapter.setPlaylists(playlists)
    }

    // Настройка RecyclerView с GridLayoutManager на 2 колонки
    private fun setupRecyclerView() {

        binding.playlistsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 колонки
            adapter = playlistAdapter

            // Добавляем отступы между элементами
            addItemDecoration(GridSpacingItemDecoration(
                spanCount = 2,           // 2 колонки
                horizontalSpacing = 8,   // 8dp между колонками
                verticalSpacing = 16     // 16dp между строками
            ))
        }
    }

    private fun setupAdapter() {
        playlistAdapter = PlaylistAdapter { playlist ->
            // Переход на экран просмотра плейлиста
            findNavController().navigate(
                R.id.action_mediaLibraryFragment_to_playlistDetailFragment,
                PlaylistDetailFragment.createArgs(playlist.playlistId)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}
