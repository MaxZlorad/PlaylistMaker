package com.practicum.playlistmaker.player.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.domain.models.Playlist
import java.io.File

// ViewHolder для плейлиста в Bottom Sheet плеера
class BottomSheetPlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val playlistCover: ImageView = itemView.findViewById(R.id.playlistCover)
    private val playlistName: TextView = itemView.findViewById(R.id.playlistName)
    private val playlistTrackCount: TextView = itemView.findViewById(R.id.playlistTrackCount)

    fun bind(playlist: Playlist) {
        playlistName.text = playlist.name
        playlistTrackCount.text = getTrackCountText(playlist.trackCount)

        // Загружаем обложку
        if (playlist.coverImagePath != null && File(playlist.coverImagePath).exists()) {
            Glide.with(itemView.context)
                .load(playlist.coverImagePath)
                .placeholder(R.drawable.placeholder_album)
                .centerCrop()
                .into(playlistCover)
        } else {
            Glide.with(itemView.context)
                .load(R.drawable.placeholder_album)
                .centerCrop()
                .into(playlistCover)
        }
    }

    private fun getTrackCountText(count: Int): String {
        val lastDigit = count % 10
        val lastTwoDigits = count % 100

        return when {
            lastTwoDigits in 11..14 -> "$count треков"
            lastDigit == 1 -> "$count трек"
            lastDigit in 2..4 -> "$count трека"
            else -> "$count треков"
        }
    }
}