package com.practicum.playlistmaker

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.isVisible

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var clearButton: ImageButton
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupToolbar()
        setupClearButton()
        setupTextWatcher()

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
}