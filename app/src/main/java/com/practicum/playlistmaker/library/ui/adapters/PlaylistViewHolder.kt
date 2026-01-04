package com.practicum.playlistmaker.library.ui.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.domain.models.Playlist
import java.io.File

// ViewHolder для отображения плейлиста в списке
class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val playlistCover: ImageView = itemView.findViewById(R.id.playlistCover)
    private val playlistName: TextView = itemView.findViewById(R.id.playlistName)
    private val playlistTrackCount: TextView = itemView.findViewById(R.id.playlistTrackCount)

    // Привязать данные плейлиста к элементу списка
    fun bind(playlist: Playlist) {

        // Название плейлиста
        playlistName.text = playlist.name

        // Количество треков (склонение: "1 трек", "2 трека", "5 треков")
        playlistTrackCount.text = itemView.context.resources.getQuantityString(
            R.plurals.track_count,
            playlist.trackCount,
            playlist.trackCount
        )

        // Обложка плейлиста
        if (playlist.coverImagePath != null && File(playlist.coverImagePath).exists()) {
            // Загружаем изображение из файла
            Glide.with(itemView.context)
                .load(playlist.coverImagePath)
                .placeholder(R.drawable.placeholder_album)
                .into(playlistCover)
        } else {
            // Показываем placeholder
            playlistCover.setImageResource(R.drawable.placeholder_album)
        }
    }
}
