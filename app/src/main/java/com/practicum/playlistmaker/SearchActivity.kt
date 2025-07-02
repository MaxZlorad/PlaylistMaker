package com.practicum.playlistmaker

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

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

    // Адаптеры
    private lateinit var adapter: TrackAdapter // Для результатов поиска
    private lateinit var historyAdapter: TrackAdapter // Для истории поиска

    // Логика
    private lateinit var searchHistory: SearchHistory // Менеджер истории (читает/сохраняет треки)
    private var searchQuery: String = "" // Текущий поисковый запрос

    private val iTunesApiService = ItunesApiClient.apiService // Сервис для поиска треков

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews() // Инициализация всех View элементов

        // Инициализация менеджера истории
        searchHistory = SearchHistory(getSharedPreferences("app_prefs", MODE_PRIVATE))
        searchHistory.loadHistory() // Загружаем сохранённые треки

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

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            searchInput.text.clear()
            hideKeyboard()

            // После очистки показать историю, если она есть
            if (searchHistory.getHistory().isNotEmpty()) {
                searchInput.requestFocus() // Фокус полю ввода
                updateHistoryVisibility() // Обновляем видимость истории
            } else {
                showEmptyState()
            }
        }
    }

    private fun setupTextWatcher() {
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchQuery = s?.toString() ?: ""
                clearButton.isVisible = !s.isNullOrEmpty()

                if (s.isNullOrEmpty()) {
                    showEmptyState()
                }
                updateHistoryVisibility() // Добавлен вызов обновления видимости истории

            }
        })

        // Новый слушатель фокуса
        searchInput.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery) // Сохраняем запрос
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        searchInput.setText(searchQuery)
        clearButton.isVisible = searchQuery.isNotEmpty()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(emptyList()) { track ->
            searchHistory.addTrack(track) // Добавляем трек в историю
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

    private fun performSearch(query: String) {
        if (query.trim().isEmpty()) {
            showEmptyState()
            return
        }

        showLoadingState()
        hideKeyboard()

        iTunesApiService.search(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val tracks = response.body()!!.results
                    if (tracks.isEmpty()) {
                        showEmptyResultsState()
                    } else {
                        showTracks(tracks)
                    }
                } else {
                    showErrorState()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                showErrorState()
            }
        })
    }

    private fun showLoadingState() {
        searchHistoryView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
    }

    private fun showTracks(tracks: List<Track>) {
        searchHistoryView.visibility = View.GONE // История скрыта
        recyclerView.visibility = View.VISIBLE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
        adapter.updateTracks(tracks)
    }

    private fun showEmptyResultsState() {
        searchHistoryView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.VISIBLE
        searchHistoryView.visibility = View.GONE
    }

    private fun showErrorState() {
        searchHistoryView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.VISIBLE
        errorSearchView.visibility = View.GONE
        searchHistoryView.visibility = View.VISIBLE
        searchHistoryView.setOnClickListener { performSearch(searchQuery) }
    }

    private fun showEmptyState() {
        searchHistoryView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
        adapter.updateTracks(emptyList()) // Обновляем видимость истории (если поле в фокусе)
    }

    private fun setupHistoryViews() {
        // Адаптер для истории (такой же, как для результатов)
        historyAdapter = TrackAdapter(emptyList()) { track ->
            // Клик по треку в истории:
            searchInput.setText("${track.trackName} ${track.artistName}")
            searchHistory.addTrack(track) // Обновляем позицию трека в истории
            updateHistoryVisibility()
        }

        // Настройка RecyclerView
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

    }

    private fun updateHistoryVisibility() {
        val showHistory = searchInput.text.isEmpty() && // Поле пустое
                searchInput.hasFocus() && // Поле в фокусе
                searchHistory.getHistory().isNotEmpty() // Есть сохранённые треки

        // Всегда показываем кнопку, если есть история
        clearHistoryButton.visibility = if (searchHistory.getHistory().isNotEmpty()) View.VISIBLE else View.GONE

        // Управляем видимостью только контейнера с историей
        searchHistoryView.visibility = if (showHistory) View.VISIBLE else View.GONE

        // Обновляем список
        if (showHistory) {
            historyAdapter.updateTracks(searchHistory.getHistory())
        } else {
            historyAdapter.updateTracks(emptyList()) // Если истории нет, передаем пустой список
        }
        Log.d("SearchActivity", "Clear button enabled: ${clearHistoryButton.isEnabled}")
        Log.d("SearchActivity", "Clear button visibility: ${clearHistoryButton.visibility}")
        clearHistoryButton.post {
            Log.d("SearchActivity", "Clear button width: ${clearHistoryButton.width}px")
        }

        clearHistoryButton.setOnClickListener {
            Log.d("SearchActivity", "Нажатие кнопки очистки истории 2")
            searchHistory.clearHistory()
            Log.d("SearchActivity", "История очищена. Новый размер 2: ${searchHistory.getHistory().size}")

            historyAdapter.updateTracks(emptyList()) // Обновляем адаптер
            clearHistoryButton.visibility = View.GONE
            searchHistoryView.visibility = View.GONE

            updateHistoryVisibility()
        }
    }
}
