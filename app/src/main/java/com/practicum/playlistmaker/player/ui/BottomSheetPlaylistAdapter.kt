package com.practicum.playlistmaker.player.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.domain.models.Playlist

// Adapter для списка плейлистов в Bottom Sheet плеера
class BottomSheetPlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<BottomSheetPlaylistViewHolder>() {

    private var playlists: List<Playlist> = emptyList()

    fun setPlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bottom_sheet_playlist, parent, false)
        return BottomSheetPlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: BottomSheetPlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size
}
