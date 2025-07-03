package com.practicum.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale
import java.text.SimpleDateFormat

class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Устанавливаем макет из XML
        setContentView(R.layout.player_activity)

        // Получаем трек из Intent с учетом версии Android
        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Современный безопасный способ (Android 13+)
            intent.getSerializableExtra(TRACK_EXTRA, Track::class.java)
        } else {
            // Устаревший способ (для совместимости)
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(TRACK_EXTRA) as? Track
        }
        Log.d("PlayerActivity", "Received track: ${track?.trackName ?: "null"}")
        // Настраиваем верхнюю панель
        setupToolbar()
        // Заполняем интерфейс данными
        setupViews(track)
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
            // Загрузка обложки альбома
            Glide.with(this)
                .load(it.getCoverArtwork()) // URL в высоком качестве
                .placeholder(R.drawable.placeholder_track_poster) // Заглушка
                .into(findViewById(R.id.albumArt)) // ImageView для отображения

            // Основная информация о треке
            findViewById<TextView>(R.id.trackName).text = it.trackName
            findViewById<TextView>(R.id.artistName).text = it.artistName

            // Форматирование времени трека (минуты:секунды)
            findViewById<TextView>(R.id.currentTime).text =
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(it.trackTimeMillis)

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

    // метод для отображения опциональных полей v3
    private fun setupOptionalField(
        value: String?,
        labelView: TextView,
        valueView: TextView,
        labelText: String
    ) {
        if (!value.isNullOrEmpty()) {
            labelView.text = labelText
            valueView.text = value
        } else {
            labelView.visibility = View.GONE
            valueView.visibility = View.GONE
        }
    }

    // Константа для ключа передачи трека между экранами
    companion object {
        private const val TRACK_EXTRA = "track_extra"

        // Статический метод для запуска плеера
        fun start(context: Context, track: Track) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(TRACK_EXTRA, track)  // Упаковываем трек в Intent
            }
            context.startActivity(intent)  // Запускаем Activity
        }
    }
}