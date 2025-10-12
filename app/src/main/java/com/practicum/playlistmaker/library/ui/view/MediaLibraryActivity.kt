package com.practicum.playlistmaker.library.ui.view
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.ui.view_model.MediaLibraryViewModel

class MediaLibraryActivity : AppCompatActivity() {

    private val viewModel: MediaLibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_library)
    }
}