package com.practicum.playlistmaker.player.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlayerBinding
import com.practicum.playlistmaker.player.domain.models.PlaybackState
import com.practicum.playlistmaker.player.ui.view_model.PlayerViewModel
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.player.ui.BottomSheetPlaylistAdapter

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()

    // Получаем аргументы через Navigation Component
    private val args: PlayerFragmentArgs by navArgs()

    // Сохраняем все UI элементы как в оригинальной Activity
    private lateinit var buttonPlayPause: ImageButton
    private lateinit var currentTimeView: TextView
    private lateinit var albumArt: ImageView
    private lateinit var trackNameView: TextView
    private lateinit var artistNameView: TextView

    // Кнопка "Нравится" (сердечко)
    private lateinit var buttonAddToFavorites: ImageButton

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var bottomSheetAdapter: BottomSheetPlaylistAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация UI элементов через binding
        buttonPlayPause = binding.buttonPlayPause
        currentTimeView = binding.currentTime
        albumArt = binding.albumArt
        trackNameView = binding.trackName
        artistNameView = binding.artistName
        buttonAddToFavorites = binding.favoriteButton

        // Получаем трек из аргументов навигации
        val track = args.track
        if (track == null) {
            Log.e(TAG, "Track is null!")
            findNavController().navigateUp() // Возвращаемся назад если трек не передан
            return
        }

        setupToolbar()
        setupViews(track)
        observeViewModel()
        setupPlaybackControls()
        setupFavoriteButton()
        setupBottomSheet()

        // Подготавливаем плеер с треком
        viewModel.preparePlayer(track)
    }

    private fun setupToolbar() {
        // Используем Navigation Component вместо popBackStack
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // Навигируем вверх по back stack
        }
    }

    // Полностью сохраняем логику setupViews из Activity
    private fun setupViews(track: Track) {
        Log.d(TAG, "Track preview URL: ${track.previewUrl}")

        // Загружаем обложку через Glide
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.placeholder_track_poster)
            .into(albumArt)

        // Основная информация о треке
        trackNameView.text = track.trackName
        artistNameView.text = track.artistName
        currentTimeView.text = viewModel.getFormattedTime(0L)

        // Блок информации о продолжительности
        setupOptionalField(
            value = SimpleDateFormat(
                "mm:ss", Locale.getDefault()).format(track.trackTimeMillis),
            labelView = binding.durationLabel,
            valueView = binding.durationValue,
            labelText = getString(R.string.duration_label)
        )

        // Блок информации об альбоме
        setupOptionalField(
            value = track.collectionName,
            labelView = binding.albumLabel,
            valueView = binding.albumValue,
            labelText = getString(R.string.album_label)
        )

        // Блок информации о годе релиза
        setupOptionalField(
            value = track.releaseDate?.take(4),
            labelView = binding.yearLabel,
            valueView = binding.yearValue,
            labelText = getString(R.string.year_label)
        )

        // Блок информации о жанре
        setupOptionalField(
            value = track.primaryGenreName,
            labelView = binding.genreLabel,
            valueView = binding.genreValue,
            labelText = getString(R.string.genre_label)
        )

        // Блок информации о стране
        setupOptionalField(
            value = track.country,
            labelView = binding.countryLabel,
            valueView = binding.countryValue,
            labelText = getString(R.string.country_label)
        )
    }

    private fun setupOptionalField(
        value: String?,
        labelView: TextView,
        valueView: TextView,
        labelText: String
    ) {
        if (!value.isNullOrEmpty()) {
            labelView.text = labelText
            valueView.text = value
            labelView.visibility = View.VISIBLE
            valueView.visibility = View.VISIBLE
        } else {
            labelView.visibility = View.GONE
            valueView.visibility = View.GONE
        }
    }

    private fun setupPlaybackControls() {
        buttonPlayPause.setOnClickListener {
            when (viewModel.screenState.value?.playbackState) {
                is PlaybackState.Playing -> viewModel.pausePlayback()
                is PlaybackState.Paused,
                is PlaybackState.Prepared,
                is PlaybackState.Stopped,
                is PlaybackState.Completed -> viewModel.startPlayback()
                else -> {}
            }
        }
    }

    // Настройка кнопки "Нравится"
    private fun setupFavoriteButton() {
        // При нажатии на кнопку вызываем метод ViewModel
        buttonAddToFavorites.setOnClickListener {
            viewModel.onFavoriteClicked()
        }
    }

    private fun observeViewModel() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            // Обновляем UI плеера
            updatePlaybackUI(state.playbackState)

            // Обновляем позицию
            currentTimeView.text = viewModel.getFormattedTime(state.currentPosition)

            // Обновляем кнопку избранного
            updateFavoriteButton(state.isFavorite)
        }

        // Список плейлистов
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            bottomSheetAdapter.setPlaylists(playlists)
        }

        // Статус добавления трека
        viewModel.addTrackStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is PlayerViewModel.AddTrackStatus.Success -> {
                    // Закрывать Bottom Sheet ТОЛЬКО при успехе
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    Toast.makeText(
                        requireContext(),
                        "Добавлено в плейлист ${status.playlistName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PlayerViewModel.AddTrackStatus.AlreadyExists -> {
                    // Не закрываем Bottom Sheet
                    Toast.makeText(
                        requireContext(),
                        "Трек уже добавлен в плейлист ${status.playlistName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updatePlaybackUI(playbackState: PlaybackState) {
        when (playbackState) {
            is PlaybackState.Prepared -> {
                buttonPlayPause.isEnabled = true
                buttonPlayPause.setImageResource(R.drawable.ic_play_100)
            }
            is PlaybackState.Playing -> {
                buttonPlayPause.setImageResource(R.drawable.ic_pause_100)
            }
            is PlaybackState.Paused -> {
                buttonPlayPause.setImageResource(R.drawable.ic_play_100)
            }
            is PlaybackState.Completed -> {
                buttonPlayPause.setImageResource(R.drawable.ic_play_100)
                currentTimeView.text = viewModel.getFormattedTime(0L)
            }
            else -> {}
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            buttonAddToFavorites.setImageResource(R.drawable.ic_favorite_select)
        } else {
            buttonAddToFavorites.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    // Аналоги onPause и onDestroy из Activity
    override fun onPause() {
        super.onPause()
        // Ставим на паузу при скрытии фрагмента
        if (viewModel.screenState.value?.playbackState is PlaybackState.Playing) {
            viewModel.pausePlayback()
        }
    }

    // Настройка Bottom Sheet
    private fun setupBottomSheet() {
        // Инициализация BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistsBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Adapter для списка плейлистов
        bottomSheetAdapter = BottomSheetPlaylistAdapter { playlist ->
            // Клик на плейлист - добавляем трек
            val track = getCurrentTrack() // Получение текущего трека
            viewModel.addTrackToPlaylist(track, playlist)
        }

        binding.bottomSheetRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bottomSheetAdapter
        }

        // Слушатель изменения состояния Bottom Sheet
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.visibility = View.GONE
                    }
                    else -> {
                        binding.overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Плавное затемнение
                _binding?.let { binding ->
                    binding.overlay.alpha = (slideOffset + 1f) / 2f
                }
            }
        })

        binding.playlistButton.setOnClickListener {
            viewModel.loadPlaylists()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // Кнопка "Новый плейлист" в Bottom Sheet
        binding.newPlaylistButton.setOnClickListener {
            // Переход на экран создания плейлиста
            findNavController().navigate(
                R.id.action_playerFragment_to_newPlaylistFragment
            )
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    // Метод для получения текущего трека
    private fun getCurrentTrack(): Track {
        return args.track
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Останавливаем воспроизведение при уничтожении фрагмента
        viewModel.stopPlayback()
        _binding = null
    }

    companion object {
        private const val TAG = "PlayerFragment"
    }
}
