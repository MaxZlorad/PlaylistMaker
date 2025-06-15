package com.practicum.playlistmaker

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupToolbar()
        setupClearButton()
        setupTextWatcher()
        setupRecyclerView()
        loadTracks()

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            searchInput.setText(searchQuery)
            clearButton.isVisible = false
        }
    }

    private fun initViews() {
        searchInput = findViewById(R.id.search_input)
        clearButton = findViewById(R.id.clear_button)
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
        }
    }

    private fun setupTextWatcher() {
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchQuery = s?.toString() ?: ""
                clearButton.isVisible = !s.isNullOrEmpty()
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
        recyclerView = findViewById(R.id.tracks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    private fun loadTracks() {
        val tracks = listOf(
            Track(
                "Smells Like Teen Spirit",
                "Nirvana",
                "5:01",
                "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                "Billie Jean",
                "Michael Jackson",
                "4:35",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
            ),
            Track(
                "Stayin' Alive",
                "Bee Gees",
                "4:10",
                "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                "Whole Lotta Love",
                "Led Zeppelin",
                "5:33",
                "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
            ),
            Track(
                "Sweet Child O'Mine",
                "Guns N' Roses",
                "5:03",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
            )
        )
        adapter.updateTracks(tracks)
    }
}