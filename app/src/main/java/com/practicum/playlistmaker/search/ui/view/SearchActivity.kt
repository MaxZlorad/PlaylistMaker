package com.practicum.playlistmaker.search.ui.view


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.player.ui.view.PlayerActivity
import com.practicum.playlistmaker.search.ui.track.TrackAdapter
import com.practicum.playlistmaker.search.ui.view_model.SearchState
import com.practicum.playlistmaker.search.ui.view_model.SearchViewModel
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.data.repository.SearchConstants
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModel()

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

    // Переменные для debounce private var isClickAllowed = true // Флаг для ограничения кликов
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper()) // Handler для debounce
    private lateinit var searchRunnable: Runnable // Runnable для отложенного поиска


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация Runnable для отложенного поиска
        searchRunnable = Runnable { performSearch(searchInput.text.toString()) }

        initViews() // Инициализация всех View элементов

        setupHistoryViews() // Настраиваем АДАПТЕР истории
        setupRecyclerView() // Настраиваем АДАПТЕР списка результатов
        setupToolbar() // Кнопка "Назад"
        setupClearButton() // Кнопка очистки поля
        setupTextWatcher() // Следим за вводом текста
        setupSearchListener() // Обработка нажатия "Поиск"
        observeViewModel()

        // Восстановление запроса при повороте экрана
        if (savedInstanceState != null) {
            val searchQuery = savedInstanceState.
                getString(SearchActivityConstants.SEARCH_QUERY_KEY, "")
            searchInput.setText(searchQuery)
            clearButton.isVisible = searchQuery.isNotEmpty() //false
        }

        // Первичное обновление видимости
        viewModel.getSearchHistory()
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

    private fun observeViewModel() {
        // Наблюдаем за состоянием поиска
        viewModel.searchState.observe(this) { state ->
            when (state) {
                is SearchState.Loading -> showLoadingState()
                is SearchState.Success -> showTracks(state.tracks)
                is SearchState.EmptyResults -> showEmptyResultsState()
                is SearchState.Error -> showErrorState()
                is SearchState.Empty -> showEmptyState()
            }
        }

        // Наблюдаем за историей поиска
        viewModel.historyState.observe(this) { history ->
            updateHistoryVisibility(history)
        }
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
            viewModel.setEmptyState()

            // Но убеждаемся, что история показывается при пустом поле
            updateHistoryVisibility(viewModel.historyState.value ?: emptyList())

            searchInput.requestFocus()
        }
    }

    // Слушатель изменений текста (UI логика)
    private fun setupTextWatcher() {
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString() ?: ""
                clearButton.isVisible = query.isNotEmpty()

                if (query.isEmpty()) {
                    viewModel.setEmptyState()
                } else {
                    searchDebounce() // Запускаем debounce при изменении текста
                }

                // Добавлен вызов обновления видимости истории
                updateHistoryVisibility(viewModel.historyState.value ?: emptyList())
            }
        })

        // Новый слушатель фокуса
        searchInput.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility(viewModel.historyState.value ?: emptyList())
        }
    }

    // Скрытие клавиатуры (UI логика)
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(emptyList()) { track ->

            if (clickDebounce()) {
                viewModel.addTrackToHistory(track)
                PlayerActivity.start(this, track)
            }
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

        if (!isNetworkAvailable()) {
            viewModel.setErrorState()
            return
        }

        // Используем интерактор для поиска треков
        viewModel.searchTracks(query)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    // Debounce для кликов (UI логика)
    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, SearchActivityConstants.CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    // Debounce для поиска (UI логика)
    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SearchActivityConstants.SEARCH_DEBOUNCE_DELAY)
    }

    // Настройка истории поиска (UI логика + связь с Domain слоем)
    private fun setupHistoryViews() {
        // Адаптер для истории (такой же, как для результатов)
        historyAdapter = TrackAdapter(emptyList()) { track ->
            // Клик по треку в истории с debounce
            if (clickDebounce()) {
                // Используем интерактор для добавления трека в историю
                viewModel.addTrackToHistory(track)

                PlayerActivity.start(this, track) // переход плеер трека
            }
        }

        // Настройка RecyclerView
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Обработчик для кнопки очистки истории
        clearHistoryButton.setOnClickListener {
            if (clickDebounce()) {
                viewModel.clearSearchHistory()
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
                performSearch(searchInput.text.toString())
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
    private fun updateHistoryVisibility(history: List<Track>) {
        if (history.isNotEmpty() && searchInput.text.isEmpty()) {
            searchHistoryView.visibility = View.VISIBLE
            historyAdapter.updateTracks(history)
            clearHistoryButton.visibility = View.VISIBLE
        } else {
            searchHistoryView.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SearchActivityConstants.SEARCH_QUERY_KEY, searchInput.text.toString())
    }
}
