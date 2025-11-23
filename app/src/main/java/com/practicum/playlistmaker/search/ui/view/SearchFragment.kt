package com.practicum.playlistmaker.search.ui.view

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.ui.track.TrackAdapter
import com.practicum.playlistmaker.search.ui.view_model.SearchState
import com.practicum.playlistmaker.search.ui.view_model.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.search.ui.view.SearchActivityConstants.SEARCH_QUERY_KEY
import com.practicum.playlistmaker.search.ui.view.SearchActivityConstants.CLICK_DEBOUNCE_DELAY
import com.practicum.playlistmaker.search.ui.view.SearchActivityConstants.SEARCH_DEBOUNCE_DELAY

class SearchFragment : Fragment() {

    // ViewBinding переменные - безопасный способ доступа к View элементам
    // _binding используется только внутри класса, binding - для внешнего доступа
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    // ViewModel через Koin - обеспечивает связь с бизнес-логикой
    private val viewModel: SearchViewModel by viewModel()

    // Адаптеры для RecyclerView
    private lateinit var adapter: TrackAdapter          // Для отображения результатов поиска
    private lateinit var historyAdapter: TrackAdapter   // Для отображения истории поиска

    // Переменные для реализации debounce (задержки)
    private var isClickAllowed = true                   // Флаг для ограничения частых кликов
    private val handler = Handler(Looper.getMainLooper()) // Handler для работы с UI потоком
    private lateinit var searchRunnable: Runnable       // Задача для отложенного поиска

    // Создание View фрагмента - вызывается системой при создании UI
    // (надуваем макет из XML и возвращаем корневое View)
    override fun onCreateView(
        inflater: LayoutInflater,      // Объект для создания View из XML
        container: ViewGroup?,         // Родительский контейнер
        savedInstanceState: Bundle?    // Сохраненное состояние (при повороте экрана и т.д.)
    ): View {
        // Инициализируем Binding - создаем экземпляр FragmentSearchBinding
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Возвращаем корневое View (root) из Binding
        return binding.root
    }

    // Настраиваем UI элементы (после создания View)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем Runnable для отложенного поиска
        // Выполнится через 2 секунды после последнего ввода символа
        searchRunnable = Runnable {
            performSearch(binding.searchInput.text.toString())
        }

        // Настраиваем все компоненты UI
        setupHistoryViews()    // История поиска
        setupRecyclerView()    // Список результатов
        setupToolbar()         // Верхняя панель
        setupClearButton()     // Кнопка очистки
        setupTextWatcher()     // Слушатель ввода текста
        setupSearchListener()  // Слушатель кнопки "Поиск"
        observeViewModel()     // Наблюдатели за ViewModel

        // Восстанавливаем состояние при повороте экрана
        if (savedInstanceState != null) {
            val searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            binding.searchInput.setText(searchQuery)
            binding.clearButton.isVisible = searchQuery.isNotEmpty()
        }

        // Загружаем историю поиска при создании фрагмента
        viewModel.getSearchHistory()
    }

    // Вызывается при уничтожении View фрагмента - здесь очищаем ресурсы
    override fun onDestroyView() {
        super.onDestroyView()

        // Удаляем все pending Runnable из Handler чтобы избежать утечек памяти
        handler.removeCallbacks(searchRunnable)

        // Очищаем Binding для предотвращения утечек памяти
        _binding = null
    }

    // Сохранение состояния при уничтожении фрагмента (поворот экрана и т.д.)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем текущий текст поиска
        outState.putString(SEARCH_QUERY_KEY, binding.searchInput.text.toString())
    }

    // Настройка верхней панели (Toolbar)
    private fun setupToolbar() {
        // Устанавливаем обработчик клика на кнопку "Назад"
        // Используем Navigation Component вместо onBackPressedDispatcher
        binding.searchToolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // Переходим вверх по back stack
        }
    }

    // Настройка кнопки очистки поля поиска
    private fun setupClearButton() {
        binding.clearButton.setOnClickListener {
            // Очищаем поле ввода
            binding.searchInput.text.clear()

            // Скрываем клавиатуру
            hideKeyboard()

            // Устанавливаем пустое состояние в ViewModel
            viewModel.setEmptyState()

            // Обновляем видимость истории поиска
            updateHistoryVisibility(viewModel.historyState.value ?: emptyList())

            // Устанавливаем фокус на поле ввода
            binding.searchInput.requestFocus()
        }
    }

    // Настройка слушателя изменений текста в поле поиска
    private fun setupTextWatcher() {
        binding.searchInput.addTextChangedListener(object : android.text.TextWatcher {
            // Вызывается до изменения текста
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // Вызывается во время изменения текста
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            // Вызывается после изменения текста - здесь основная логика
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString() ?: ""

                // Показываем/скрываем кнопку очистки в зависимости от наличия текста
                binding.clearButton.isVisible = query.isNotEmpty()

                if (query.isEmpty()) {
                    // Если поле пустое - показываем пустое состояние
                    viewModel.setEmptyState()
                } else {
                    // Если есть текст - запускаем отложенный поиск
                    searchDebounce()
                }

                // Обновляем видимость истории поиска
                updateHistoryVisibility(viewModel.historyState.value ?: emptyList())
            }
        })

        // Слушатель фокуса на поле ввода
        binding.searchInput.setOnFocusChangeListener { _, hasFocus ->
            // При получении/потере фокуса обновляем видимость истории
            updateHistoryVisibility(viewModel.historyState.value ?: emptyList())
        }
    }

    // Настройка RecyclerView для отображения результатов поиска
    private fun setupRecyclerView() {
        // Создаем адаптер для списка результатов
        adapter = TrackAdapter(emptyList()) { track ->
            // Обработчик клика на трек с защитой от частых кликов
            if (clickDebounce()) {
                // Добавляем трек в историю поиска
                viewModel.addTrackToHistory(track)
                // Открываем фрагмент плеера
                //openPlayerFragment(track)
                // ??? Navigation Component вместо FragmentManager
                openPlayerFragment(track)
            }
        }

        // Настраиваем RecyclerView
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = adapter
    }

    // Настройка слушателя кнопки "Поиск" на клавиатуре
    private fun setupSearchListener() {
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            // Проверяем, что нажата кнопка "Готово" (Done)
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    // Выполняем поиск и скрываем клавиатуру
                    performSearch(query)
                    hideKeyboard()
                }
                true // Обработка события завершена
            } else {
                false // Событие не обработано
            }
        }
    }

    // Настройка компонентов истории поиска
    private fun setupHistoryViews() {
        // Создаем адаптер для истории поиска
        historyAdapter = TrackAdapter(emptyList()) { track ->
            // Обработчик клика на трек в истории
            if (clickDebounce()) {
                // Добавляем трек в историю (обновляем время)
                viewModel.addTrackToHistory(track)
                // Открываем фрагмент плеера
                //openPlayerFragment(track)
                // ??? Navigation Component вместо FragmentManager
                openPlayerFragment(track)
            }
        }

        // Настраиваем RecyclerView для истории
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter

        // Обработчик кнопки очистки истории
        binding.clearHistoryButton.setOnClickListener {
            if (clickDebounce()) {
                // Очищаем историю поиска через ViewModel
                viewModel.clearSearchHistory()
            }
        }
    }

    // Настройка наблюдателей за состоянием ViewModel
    private fun observeViewModel() {
        // Наблюдаем за состоянием поиска
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            // В зависимости от состояния показываем соответствующий UI
            when (state) {
                is SearchState.Loading -> showLoadingState()        // Загрузка
                is SearchState.Success -> showTracks(state.tracks)  // Успех
                is SearchState.EmptyResults -> showEmptyResultsState() // Пустые результаты
                is SearchState.Error -> showErrorState()            // Ошибка
                is SearchState.Empty -> showEmptyState()            // Пустое состояние
            }
        }

        // Наблюдаем за историей поиска
        viewModel.historyState.observe(viewLifecycleOwner) { history ->
            // Обновляем отображение истории
            updateHistoryVisibility(history)
        }
    }

    // Показ состояния загрузки
    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE      // Показываем индикатор загрузки
        binding.historyContainer.visibility = View.GONE    // Скрываем историю
        binding.tracksRecyclerView.visibility = View.GONE  // Скрываем результаты
        binding.errorInternet.visibility = View.GONE       // Скрываем ошибку интернета
        binding.errorSearch.visibility = View.GONE         // Скрываем ошибку поиска
    }

    // Показ найденных треков
    private fun showTracks(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE         // Скрываем индикатор загрузки
        binding.historyContainer.visibility = View.GONE    // Скрываем историю
        binding.tracksRecyclerView.visibility = View.VISIBLE // Показываем результаты
        binding.errorInternet.visibility = View.GONE       // Скрываем ошибку интернета
        binding.errorSearch.visibility = View.GONE         // Скрываем ошибку поиска

        // Обновляем данные в адаптере
        adapter.updateTracks(tracks)
    }

   // Показ состояния "ничего не найдено"
    private fun showEmptyResultsState() {
        binding.progressBar.visibility = View.GONE         // Скрываем индикатор загрузки
        binding.historyContainer.visibility = View.GONE    // Скрываем историю
        binding.tracksRecyclerView.visibility = View.GONE  // Скрываем результаты
        binding.errorInternet.visibility = View.GONE       // Скрываем ошибку интернета
        binding.errorSearch.visibility = View.VISIBLE    // Показываем сообщение "ничего не найдено"
    }

    // Показ состояния ошибки (проблемы с интернетом)
    private fun showErrorState() {
        binding.progressBar.visibility = View.GONE         // Скрываем индикатор загрузки
        binding.historyContainer.visibility = View.GONE    // Скрываем историю
        binding.tracksRecyclerView.visibility = View.GONE  // Скрываем результаты
        binding.errorInternet.visibility = View.VISIBLE    // Показываем ошибку интернета
        binding.errorSearch.visibility = View.GONE         // Скрываем ошибку поиска

        // Настраиваем кнопку "Повторить"
        binding.refreshButton.setOnClickListener {
            if (clickDebounce()) {
                // При клике повторяем поиск с текущим запросом
                performSearch(binding.searchInput.text.toString())
            }
        }
    }

    // Показ пустого состояния (когда поле поиска пустое)
    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE         // Скрываем индикатор загрузки
        binding.historyContainer.visibility = View.GONE    // Скрываем историю (обновление отдельно)
        binding.tracksRecyclerView.visibility = View.GONE  // Скрываем результаты
        binding.errorInternet.visibility = View.GONE       // Скрываем ошибку интернета
        binding.errorSearch.visibility = View.GONE         // Скрываем ошибку поиска

        // Очищаем адаптер
        adapter.updateTracks(emptyList())
    }

    // Обновление видимости истории поиска
    private fun updateHistoryVisibility(history: List<Track>) {
        // Показываем историю только если:
        // 1. История не пустая
        // 2. Поле поиска пустое
        if (history.isNotEmpty() && binding.searchInput.text.isEmpty()) {
            binding.historyContainer.visibility = View.VISIBLE    // Показываем контейнер истории
            historyAdapter.updateTracks(history)                  // Обновляем данные
            binding.clearHistoryButton.visibility = View.VISIBLE  // Показываем кнопку очистки
        } else {
            binding.historyContainer.visibility = View.GONE       // Скрываем контейнер истории
            binding.clearHistoryButton.visibility = View.GONE     // Скрываем кнопку очистки
        }
    }

    // Выполнение поиска с проверкой подключения к интернету
    private fun performSearch(query: String) {
        // Проверяем доступность интернета перед поиском
        if (!isNetworkAvailable()) {
            viewModel.setErrorState() // Показываем состояние ошибки
            return
        }

        // Выполняем поиск через ViewModel
        viewModel.searchTracks(query)
    }

    // Проверка доступности интернет-соединения
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        // Проверяем наличие активного подключения (WiFi, мобильные данные, Ethernet)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    // Защита от частых кликов (debounce), true если клик разрешен, false если нужно игнорировать
    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            // Блокируем дальнейшие клики
            isClickAllowed = false
            // Разблокируем через указанное время
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    // Отложенный поиск (debounce для поля ввода), поиск через 2 сек. после последнего ввода символа
    private fun searchDebounce() {
        // Удаляем предыдущие задачи поиска
        handler.removeCallbacks(searchRunnable)
        // Запускаем новую задачу с задержкой
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    // Скрытие клавиатуры
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(
            Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }

    // Вместо FragmentManager используем NavController для навигации
    private fun openPlayerFragment(track: Track) {
        // Создаем действие навигации с передачей трека в качестве аргумента
        val action = SearchFragmentDirections.actionSearchFragmentToPlayerFragment(track)

        // Выполняем навигацию через NavController
        findNavController().navigate(action)
    }
}
