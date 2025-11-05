package com.practicum.playlistmaker.player.ui.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.player.ui.view_model.PlayerConstants
import com.practicum.playlistmaker.player.domain.models.PlaybackState
import com.practicum.playlistmaker.player.ui.view_model.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlayerActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by viewModel()

    // UI элементы
    private lateinit var buttonPlayPause: ImageButton // Кнопка play/pause
    private lateinit var currentTimeView: TextView // Текст с текущим временем воспроизведения

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_activity) // Устанавливаем макет из XML

        // Инициализация UI элементов
        buttonPlayPause = findViewById(R.id.buttonPlayPause)
        currentTimeView = findViewById(R.id.currentTime)

        // Получаем трек с использованием функции Compat
        val track = getTrackCompat()
        // Проверяем, не равен ли трек null
        if (track == null) {
            Log.e("PlayerActivity", "Track is null!")
            finish()
            return
        }
        Log.d("PlayerActivity",
            "Opening player with track: ${track.trackName}, URL: ${track.previewUrl}")

        setupToolbar() // Настраиваем верхнюю панель
        setupViews(track) // Заполняем интерфейс данными

        observeViewModel()
        setupPlaybackControls()
        viewModel.preparePlayer(track)
    }

    // Получаем трек из Intent с учетом версии Android
    private fun getTrackCompat(): Track? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Современный безопасный способ (Android 13+)
            intent.getSerializableExtra(PlayerConstants.TRACK_EXTRA, Track::class.java)
        } else {
            // Устаревший способ (для совместимости)
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(PlayerConstants.TRACK_EXTRA) as? Track
        }
    }

    // Настройка верхней панели (Toolbar)
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
                finish() // Закрываем Activity
        }
    }

    // Заполнение экрана данными о треке
    private fun setupViews(track: Track?) {
        // Проверка на null
        track ?: return
        // Проверка URL аудио
        Log.d("PlayerActivity", "Track preview URL: ${track.previewUrl}")
        // Находим все view один раз и сохраняем в переменные
        val albumArt = findViewById<ImageView>(R.id.albumArt)
        val trackNameView = findViewById<TextView>(R.id.trackName)
        val artistNameView = findViewById<TextView>(R.id.artistName)

        // Загружаем обложку через Glide
        Glide.with(this)
            .load(track.getCoverArtwork()) // URL в высоком качестве
            .placeholder(R.drawable.placeholder_track_poster) // Заглушка
            .into(albumArt) // ImageView для отображения

        // Основная информация о треке
        trackNameView.text = track.trackName
        artistNameView.text = track.artistName

        //currentTimeView.text = "00:00"
        currentTimeView.text = viewModel.getFormattedTime(0L)// Начальное

        // Блок информации о продолжительности
        setupOptionalField(
            value = SimpleDateFormat("mm:ss",
                Locale.getDefault()).format(track.trackTimeMillis),
            labelView = findViewById(R.id.durationLabel),
            valueView = findViewById(R.id.durationValue),
            labelText = getString(R.string.duration_label)
        )

        // Блок информации об альбоме
        setupOptionalField(
            value = track.collectionName,
            labelView = findViewById(R.id.albumLabel),
            valueView = findViewById(R.id.albumValue),
            labelText = getString(R.string.album_label)
        )

        // Блок информации о годе релиза
        setupOptionalField(
            value = track.releaseDate?.take(4), // Берем только год из даты
            labelView = findViewById(R.id.yearLabel),
            valueView = findViewById(R.id.yearValue),
            labelText = getString(R.string.year_label)
        )

        // Блок информации о жанре
        setupOptionalField(
            value = track.primaryGenreName,
            labelView = findViewById(R.id.genreLabel),
            valueView = findViewById(R.id.genreValue),
            labelText = getString(R.string.genre_label)
        )

        // Блок информации о стране
        setupOptionalField(
            value = track.country,
            labelView = findViewById(R.id.countryLabel),
            valueView = findViewById(R.id.countryValue),
            labelText = getString(R.string.country_label)
        )
    }

    // метод для отображения опциональных полей v3
    private fun setupOptionalField(
        value: String?,
        labelView: TextView,
        valueView: TextView,
        labelText: String
    ) {
        if (!value.isNullOrEmpty()) {
            // Если значение есть - показываем оба TextView
            labelView.text = labelText
            valueView.text = value
        } else {
            // Если значения нет - скрываем оба TextView
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
        viewModel.playbackState.observe(this) { state ->
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
                    currentTimeView.text = "00:00"
                }
                else -> {}
            }
        }

        viewModel.currentPosition.observe(this) { position ->
            currentTimeView.text = viewModel.getFormattedTime(position)
        }
    }

    // Приостановка Activity - ставим воспроизведение на паузу
    override fun onPause() {
        super.onPause()
        if (viewModel.playbackState.value is PlaybackState.Playing) {
            viewModel.pausePlayback()
        }
    }

    // Уничтожение Activity - освобождаем ресурсы
    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopPlayback()
    }

    // Константа для ключа передачи трека между экранами
    companion object {

        // Статический метод для запуска плеера
        fun start(context: Context, track: Track) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(PlayerConstants.TRACK_EXTRA, track)  // Упаковываем трек в Intent
            }
            context.startActivity(intent)  // Запускаем Activity
        }
    }
}