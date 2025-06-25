package com.practicum.playlistmaker

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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

    private lateinit var searchInput: EditText
    private lateinit var clearButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    private lateinit var errorInternetView: View  // Контейнер для ошибки интернета
    private lateinit var errorSearchView: TextView  // Текст "Ничего не найдено"
    private lateinit var refrashButton: Button  // Кнопка "Повторить"

    //private val tracks = mutableListOf<Track>()
    private var searchQuery: String = ""
    private val iTunesApiService = ItunesApiClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupToolbar()
        setupClearButton()
        setupTextWatcher()
        setupRecyclerView()
        setupSearchListener() //loadTracks()

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            searchInput.setText(searchQuery)
            clearButton.isVisible = searchQuery.isNotEmpty() //false
        }
    }

    private fun initViews() {
        searchInput = findViewById(R.id.search_input)
        clearButton = findViewById(R.id.clear_button)
        recyclerView = findViewById(R.id.tracks_recycler_view)
        errorInternetView = findViewById(R.id.error_internet)
        errorSearchView = findViewById(R.id.error_search)
        refrashButton = findViewById(R.id.refrash_button)
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
            showEmptyState()
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
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
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
        //recyclerView = findViewById(R.id.tracks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(emptyList()) {}
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
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
    }

    private fun showTracks(tracks: List<Track>) {
        recyclerView.visibility = View.VISIBLE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
        adapter.updateTracks(tracks)
    }

    private fun showEmptyResultsState() {
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.VISIBLE
        refrashButton.visibility = View.GONE
    }

    private fun showErrorState() {
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.VISIBLE
        errorSearchView.visibility = View.GONE
        refrashButton.visibility = View.VISIBLE
        refrashButton.setOnClickListener { performSearch(searchQuery) }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        errorInternetView.visibility = View.GONE
        errorSearchView.visibility = View.GONE
        adapter.updateTracks(emptyList())
    }

    //private fun loadTracks() {}

}
