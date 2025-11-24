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
            value = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis),
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
            when (viewModel.playbackState.value) {
                is PlaybackState.Playing -> viewModel.pausePlayback()
                is PlaybackState.Paused,
                is PlaybackState.Prepared,
                is PlaybackState.Stopped,
                is PlaybackState.Completed -> viewModel.startPlayback()
                else -> {}
            }
        }
    }

    private fun observeViewModel() {
        viewModel.playbackState.observe(viewLifecycleOwner) { state ->
            when (state) {
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

        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            currentTimeView.text = viewModel.getFormattedTime(position)
        }
    }

    // Аналоги onPause и onDestroy из Activity
    override fun onPause() {
        super.onPause()
        // Ставим на паузу при скрытии фрагмента
        if (viewModel.playbackState.value is PlaybackState.Playing) {
            viewModel.pausePlayback()
        }
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