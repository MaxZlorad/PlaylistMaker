package com.practicum.playlistmaker.search.ui.track

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.models.Track

class TrackAdapter(
    private var tracks: List<Track> = emptyList(),
    private val onItemClick: (Track) -> Unit,
    private val onItemLongClick: ((Track) -> Boolean)? = null
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)

        // Обычное нажатие (переход на Player)
        holder.itemView.setOnClickListener { onItemClick(track) }

        // Длинное нажатие (диалог удаления)
        onItemLongClick?.let { callback ->
            holder.itemView.setOnLongClickListener {
                callback(track)
                true
            }
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        Log.d("TrackAdapter", "Updating tracks: ${newTracks.size}")
        tracks = newTracks
        notifyDataSetChanged()
    }

}