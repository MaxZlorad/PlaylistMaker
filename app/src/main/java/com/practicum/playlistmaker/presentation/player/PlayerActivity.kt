package com.practicum.playlistmaker.presentation.player

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
import java.util.Locale
import java.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track


class PlayerActivity : AppCompatActivity() {
    private var playerState = STATE_DEFAULT // Текущее состояние плеера
    private lateinit var mediaPlayer: MediaPlayer // Медиаплеер для воспроизведения аудио
    private lateinit var handler: Handler // Handler для обновления UI в главном потоке
    private lateinit var updateProgressRunnable: Runnable // Runnable для обновления прогресса

    // UI элементы
    private lateinit var buttonPlayPause: ImageButton // Кнопка play/pause
    private lateinit var currentTimeView: TextView // Текст с текущим временем воспроизведения

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_activity) // Устанавливаем макет из XML

        // Инициализация UI элементов
        buttonPlayPause = findViewById(R.id.buttonPlayPause)
        currentTimeView = findViewById(R.id.currentTime)

        // Инициализация Handler для обновления прогресса (главный поток)
        handler = Handler(Looper.getMainLooper())

        // Инициализация MediaPlayer
        mediaPlayer = MediaPlayer()

        // Инициализация Runnable для обновления прогресса воспроизведения
        updateProgressRunnable = object : Runnable {
            override fun run() {
                if (playerState == STATE_PLAYING) {
                    // Обновляем текущее время воспроизведения
                    val currentPosition = mediaPlayer.currentPosition
                    currentTimeView.text = SimpleDateFormat("mm:ss", Locale.getDefault())
                        .format(currentPosition)

                    // Планируем следующее обновление через 300 мс
                    handler.postDelayed(this, 300)
                }
            }
        }

        // Назначаем обработчик клика на кнопку воспроизведения
        buttonPlayPause.setOnClickListener {
            playbackControl() // Управление воспроизведением
        }

        // Получаем трек с использованием функции Compat
        val track = getTrackCompat()
        // Проверяем, не равен ли трек null
        if (track == null) {
            finish() // Закрываем Activity, если трек некорректен
            return
        }

        Log.d("PlayerActivity", "Received track: ${track?.trackName ?: "null"}")

        setupToolbar() // Настраиваем верхнюю панель
        setupViews(track) // Заполняем интерфейс данными
        preparePlayer(track.previewUrl) // Подготавливаем плеер для воспроизведения
    }

    // Получаем трек из Intent с учетом версии Android
    private fun getTrackCompat(): Track? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Современный безопасный способ (Android 13+)
            intent.getSerializableExtra(TRACK_EXTRA, Track::class.java)
        } else {
            // Устаревший способ (для совместимости)
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(TRACK_EXTRA) as? Track
        }
    }

    // Настройка верхней панели (Toolbar)
    private fun setupToolbar() {
        // Находим Toolbar по ID
        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            // Обработчик нажатия на кнопку "Назад"
            setNavigationOnClickListener {
                finish() // Закрываем Activity
            }
        }
    }

    // Заполнение экрана данными о треке
    private fun setupViews(track: Track?) {
        track?.let { // Если трек не null
            // Находим все view один раз и сохраняем в переменные
            val albumArt = findViewById<ImageView>(R.id.albumArt)
            val trackNameView = findViewById<TextView>(R.id.trackName)
            val artistNameView = findViewById<TextView>(R.id.artistName)
            //val currentTimeView = findViewById<TextView>(R.id.currentTime)

            // Загружаем обложку через Glide
            Glide.with(this)
                .load(it.getCoverArtwork()) // URL в высоком качестве
                .placeholder(R.drawable.placeholder_track_poster) // Заглушка
                .into(albumArt) // ImageView для отображения

            // Основная информация о треке
            trackNameView.text = it.trackName
            artistNameView.text = it.artistName

            // Форматирование времени трека (минуты:секунды)
            currentTimeView.text = "00:00"  // Начальное
            //SimpleDateFormat("mm:ss", Locale.getDefault()).format(it.trackTimeMillis)

            // Блок информации о продолжительности
            setupOptionalField(
                value = SimpleDateFormat("mm:ss", Locale.getDefault()).format(it.trackTimeMillis),
                labelView = findViewById(R.id.durationLabel),
                valueView = findViewById(R.id.durationValue),
                labelText = getString(R.string.duration_label)
            )

            // Блок информации об альбоме
            setupOptionalField(
                value = it.collectionName,
                labelView = findViewById(R.id.albumLabel),
                valueView = findViewById(R.id.albumValue),
                labelText = getString(R.string.album_label)
            )

            // Блок информации о годе релиза
            setupOptionalField(
                value = it.releaseDate?.take(4), // Берем только год из даты
                labelView = findViewById(R.id.yearLabel),
                valueView = findViewById(R.id.yearValue),
                labelText = getString(R.string.year_label)
            )

            // Блок информации о жанре
            setupOptionalField(
                value = it.primaryGenreName,
                labelView = findViewById(R.id.genreLabel),
                valueView = findViewById(R.id.genreValue),
                labelText = getString(R.string.genre_label)
            )

            // Блок информации о стране
            setupOptionalField(
                value = it.country,
                labelView = findViewById(R.id.countryLabel),
                valueView = findViewById(R.id.countryValue),
                labelText = getString(R.string.country_label)
            )
        }
    }

    // Подготовка плеера для воспроизведения
    private fun preparePlayer(previewUrl: String?) {
        // Сбросить плеер перед подготовкой нового трека
        mediaPlayer.reset()

        if (previewUrl.isNullOrEmpty()) {
            buttonPlayPause.isEnabled = false // Отключаем кнопку если нет ссылки
            return
        }

        try {
            // Устанавливаем источник данных (ссылка на аудио)
            mediaPlayer.setDataSource(previewUrl)
            // Готовим плеер асинхронно
            mediaPlayer.prepareAsync()

            // Слушатель готовности плеера
            mediaPlayer.setOnPreparedListener {
                buttonPlayPause.isEnabled = true // Включаем кнопку
                playerState = STATE_PREPARED // Меняем состояние на "готов"
                buttonPlayPause.setImageResource(R.drawable.ic_play_100) // Устанавливаем иконку play
            }

            // Слушатель завершения воспроизведения
            mediaPlayer.setOnCompletionListener {
                stopProgressUpdates() // Останавливаем обновление прогресса
                currentTimeView.text = "00:00" // Сбрасываем таймер
                buttonPlayPause.setImageResource(R.drawable.ic_play_100) // Меняем на иконку play
                playerState = STATE_PREPARED // Возвращаем состояние "готов"
            }

        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error preparing media player", e)
            buttonPlayPause.isEnabled = false // Отключаем кнопку при ошибке
        }
    }

    // Управление воспроизведением (play/pause)
    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer() // Если играет - ставим на паузу
            }

            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer() // Если готов или на паузе - запускаем
            }
        }
    }

    // Запуск воспроизведения
    private fun startPlayer() {
        mediaPlayer.start() // Запускаем воспроизведение
        buttonPlayPause.setImageResource(R.drawable.ic_pause_100) // Меняем на иконку паузы
        playerState = STATE_PLAYING // Меняем состояние на "играет"
        startProgressUpdates() // Запускаем обновление прогресса
    }

    // Пауза воспроизведения
    private fun pausePlayer() {
        mediaPlayer.pause() // Ставим на паузу
        buttonPlayPause.setImageResource(R.drawable.ic_play_100) // Меняем на иконку play
        playerState = STATE_PAUSED // Меняем состояние на "на паузе"
        stopProgressUpdates() // Останавливаем обновление прогресса
    }

    // Запуск обновления прогресса воспроизведения
    private fun startProgressUpdates() {
        handler.post(updateProgressRunnable) // Запускаем Runnable
    }

    // Остановка обновления прогресса воспроизведения
    private fun stopProgressUpdates() {
        handler.removeCallbacks(updateProgressRunnable) // Удаляем Runnable из очереди
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

    // Приостановка Activity - ставим воспроизведение на паузу
    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer() // Если играет - ставим на паузу
        }
    }

    // Уничтожение Activity - освобождаем ресурсы
    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdates() // Останавливаем обновление прогресса
        mediaPlayer.release() // Освобождаем MediaPlayer
    }

    // Константа для ключа передачи трека между экранами
    companion object {
        private const val TRACK_EXTRA = "track_extra" // Ключ передачи трека

        // Константы состояний плеера
        private const val STATE_DEFAULT = 0 // Состояние не инициализирован
        private const val STATE_PREPARED = 1 // Готов к воспроизведению
        private const val STATE_PLAYING = 2 // Воспроизведение
        private const val STATE_PAUSED = 3 // На паузе

        // Статический метод для запуска плеера
        fun start(context: Context, track: Track) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(TRACK_EXTRA, track)  // Упаковываем трек в Intent
            }
            context.startActivity(intent)  // Запускаем Activity
        }
    }
}