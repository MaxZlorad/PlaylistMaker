package com.practicum.playlistmaker.presentation.search


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.presentation.player.PlayerActivity
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.track.TrackAdapter
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.domain.api.TracksInteractor

class SearchActivity : AppCompatActivity(), TracksInteractor.TracksConsumer {

    // UI элементы
    private lateinit var searchInput: EditText // Поле ввода поиска
    private lateinit var clearButton: ImageView // Кнопка очистки поля поиска
    private lateinit var recyclerView: RecyclerView // Список результатов поиска

    private lateinit var errorSearchView: TextView // Блок ошибки поиска
    private lateinit var errorInternetView: View // Блок ошибки интернета
    private lateinit var refreshButton: Button // Кнопка обновления

    private lateinit var searchHistoryView: ViewGroup // Контейнер истории (заголовок + список + кнопка)
    private lateinit var historyRecyclerView: RecyclerView // Список истории поиска
    private lateinit var clearHistoryButton: Button // Кнопка очистки истории

    private lateinit var progressBar: ProgressBar // Индикатор загрузки

    // Адаптеры
    private lateinit var adapter: TrackAdapter // Для результатов поиска
    private lateinit var historyAdapter: TrackAdapter // Для истории поиска

    // Интерактор для работы с треками (Domain слой)
    private lateinit var tracksInteractor: TracksInteractor

    // Логика ххде?
    private var searchQuery: String = "" // Текущий поисковый запрос

    // Переменные для debounce private var isClickAllowed = true // Флаг для ограничения кликов
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper()) // Handler для debounce
    private lateinit var searchRunnable: Runnable // Runnable для отложенного поиска


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        // Инициализируем интерактор через Creator (связь с Domain слоем)
        tracksInteractor = Creator.provideTracksInteractor(this)

        // Инициализация Runnable для отложенного поиска
        searchRunnable = Runnable { performSearch(searchQuery) }

        initViews() // Инициализация всех View элементов


        setupHistoryViews() // Настраиваем АДАПТЕР истории
        setupRecyclerView() // Настраиваем АДАПТЕР списка результатов
        setupToolbar() // Кнопка "Назад"
        setupClearButton() // Кнопка очистки поля
        setupTextWatcher() // Следим за вводом текста
        setupSearchListener() // Обработка нажатия "Поиск"

        // Восстановление запроса при повороте экрана
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            searchInput.setText(searchQuery)
            clearButton.isVisible = searchQuery.isNotEmpty() //false
        }

        updateHistoryVisibility() // Первичное обновление видимости
    }

    private fun initViews() {
        // Основные элементы поиска
        searchInput = findViewById(R.id.search_input)
        clearButton = findViewById(R.id.clear_button)
        recyclerView = findViewById(R.id.tracks_recycler_view)
        progressBar = findViewById(R.id.progress_bar)

        // Элементы ошибок
        errorInternetView = findViewById(R.id.error_internet)
        errorSearchView = findViewById(R.id.error_search)
        refreshButton = findViewById(R.id.refresh_button)

        // Элементы истории
        searchHistoryView = findViewById(R.id.history_container) // Контейнер истории
        historyRecyclerView = findViewById(R.id.history_recycler_view) // RecyclerView для истории
        clearHistoryButton = findViewById(R.id.clear_history_button) // Кнопка очистки
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.search_toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    // Настройка кнопки очистки поля (UI логика)
    private fun setupClearButton() {
        clearButton.setOnClickListener {
            searchInput.text.clear()
            hideKeyboard()

            // Используем существующий метод для очистки всего
            showEmptyState()

            // Но убеждаемся, что история показывается при пустом поле
            updateHistoryVisibility()

            searchInput.requestFocus()
        }
    }

    // Слушатель изменений текста (UI логика)
    private fun setupTextWatcher() {
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchQuery = s?.toString() ?: ""
                clearButton.isVisible = !s.isNullOrEmpty()

                if (s.isNullOrEmpty()) {
                    showEmptyState()
                } else {
                    searchDebounce() // Запускаем debounce при изменении текста
                }
                updateHistoryVisibility() // Добавлен вызов обновления видимости истории
            }
        })

        // Новый слушатель фокуса
        searchInput.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility()
        }
    }

    // Скрытие клавиатуры (UI логика)
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(emptyList()) { track ->

            // Используем интерактор для добавления трека в историю
            tracksInteractor.addTrackToHistory(track) // Domain слой

            // Запускаем плеер (UI логика)
            PlayerActivity.start(this, track) // UI слой
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchListener() {
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                    hideKeyboard()
                }
                true
            } else {
                false
            }
        }
    }

    // Выполнение поиска через интерактор (связь с Domain слоем)
    private fun performSearch(query: String) {
        if (query.trim().isEmpty()) {
            showEmptyState()
            return
        }

        showLoadingState()
        hideKeyboard()

        // Используем интерактор для поиска треков
        tracksInteractor.searchTracks(query, this)
    }

    // Callback метод из TracksConsumer (обработка результатов поиска)
    override fun consume(tracks: List<Track>) {
        // Track - доменная модель из domain.models
        runOnUiThread {
            if (tracks.isEmpty()) {
                showEmptyResultsState()
            } else {
                showTracks(tracks) // Передаем доменные модели в UI
            }
        }
    }

    // Debounce для кликов (UI логика)
    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    // Debounce для поиска (UI логика)
    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    // Сохранение состояния (UI логика)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    // Восстановление состояния (UI логика)
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        searchInput.setText(searchQuery)
        clearButton.isVisible = searchQuery.isNotEmpty()
    }

    // Настройка истории поиска (UI логика + связь с Domain слоем)
    private fun setupHistoryViews() {
        // Адаптер для истории (такой же, как для результатов)
        historyAdapter = TrackAdapter(emptyList()) { track ->
            // Клик по треку в истории с debounce
            if (clickDebounce()) {
                // Клик по треку в истории:
                // Используем интерактор для добавления трека в историю
                tracksInteractor.addTrackToHistory(track)

                PlayerActivity.start(this, track) // переход плеер трека
                updateHistoryVisibility()
            }
        }

        // Настройка RecyclerView
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Обработчик для кнопки очистки истории
        clearHistoryButton.setOnClickListener {
            if (clickDebounce()) {

                tracksInteractor.clearSearchHistory()

                updateHistoryVisibility()
            }
        }

    }

    // Методы отображения различных состояний (UI логика)
    private fun showLoadingState() {
        progressBar.visibility = View.VISIBLE
        searchHistoryView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
    }

    private fun showTracks(tracks: List<Track>) {
        progressBar.visibility = View.GONE // Скрываем индикатор загрузки
        searchHistoryView.visibility = View.GONE // История скрыта
        recyclerView.visibility = View.VISIBLE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
        adapter.updateTracks(tracks)
    }

    private fun showEmptyResultsState() {
        progressBar.visibility = View.GONE // Скрываем индикатор загрузки
        searchHistoryView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.VISIBLE
        searchHistoryView.visibility = View.GONE
    }

    private fun showErrorState() {
        progressBar.visibility = View.GONE // Скрываем индикатор загрузки
        searchHistoryView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.VISIBLE
        errorSearchView.visibility = View.GONE

        // Устанавливаем обработчик для кнопки обновления
        refreshButton.setOnClickListener {
            if (clickDebounce()) {
                performSearch(searchQuery)
            }
        }
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE // Скрываем индикатор загрузки
        searchHistoryView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
        adapter.updateTracks(emptyList()) // Обновляем видимость истории (если поле в фокусе)
    }

    // Обновление видимости истории (UI логика + связь с Domain слоем)
    private fun updateHistoryVisibility() {
        // Всегда проверяем есть ли история, независимо от состояния поля
        tracksInteractor.getSearchHistory(object : TracksInteractor.TracksConsumer {
            override fun consume(tracks: List<Track>) {
                runOnUiThread {
                    if (tracks.isNotEmpty()) {
                        // Есть история - показываем всё:
                        searchHistoryView.visibility = View.VISIBLE  // Весь контейнер
                        historyAdapter.updateTracks(tracks)          // Список треков
                        clearHistoryButton.visibility = View.VISIBLE // Кнопку очистки
                    } else {
                        // Нет истории - скрываем всё:
                        searchHistoryView.visibility = View.GONE
                        clearHistoryButton.visibility = View.GONE
                    }
                }
            }
        })
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"

        // Константы для debounce
        private const val CLICK_DEBOUNCE_DELAY = 1000L // 1 секунда для кликов
        private const val SEARCH_DEBOUNCE_DELAY = 2000L // 2 секунды для поиска
    }
}
