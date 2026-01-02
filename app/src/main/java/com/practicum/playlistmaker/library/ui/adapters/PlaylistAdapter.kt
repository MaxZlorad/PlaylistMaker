package com.practicum.playlistmaker.library.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.domain.models.Playlist

// Adapter для отображения списка плейлистов в RecyclerView
class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit // Callback при клике на плейлист
) : RecyclerView.Adapter<PlaylistViewHolder>() {

    // Список плейлистов
    private var playlists: List<Playlist> = emptyList()

    // Обновить список плейлистов
    fun setPlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged() // Обновляем весь список
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)

        // Обработка клика на элемент списка
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size
}
