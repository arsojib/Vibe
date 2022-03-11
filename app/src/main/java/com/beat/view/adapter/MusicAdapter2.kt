package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.MusicItem2Binding
import com.beat.util.listener.TrackClickListener

class MusicAdapter2(
    private val context: Context,
    private val trackClickListener: TrackClickListener
) :
    RecyclerView.Adapter<MusicAdapter2.ViewHolder>() {

    private val list = ArrayList<Track>()
    private var previousPlayingItem = -1
    private var currentPlayingItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val musicItem2Binding: MusicItem2Binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.music_item_2,
            parent,
            false
        )
        return ViewHolder(musicItem2Binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.id.text = context.getString(R.string.position_format, position + 1)
        holder.binding.data = list[position]

        holder.binding.title.setTextColor(
            if (position == currentPlayingItem) ContextCompat.getColor(
                context,
                R.color.colorPrimary
            ) else ContextCompat.getColor(context, R.color.colorBlack)
        )

        holder.binding.menu.setOnClickListener {
            trackClickListener.onTrackMenuClick(list[position])
        }

        holder.binding.root.setOnClickListener {
            trackClickListener.onTrackClick(
                list[position],
                position,
                position == currentPlayingItem
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(musicItem2Binding: MusicItem2Binding) :
        RecyclerView.ViewHolder(musicItem2Binding.root) {
        val binding = musicItem2Binding
    }

    fun setFavorite(id: String, favorite: Boolean) {
        list.forEachIndexed { index, track ->
            if (track.trackId == id) {
                list[index].favorite = favorite
            }
        }
    }

    fun addAll(list: List<Track>) {
        this.list.clear()
        notifyDataSetChanged()
        for (track in list) {
            add(track)
        }
    }

    private fun add(track: Track) {
        list.add(track)
        notifyItemInserted(list.size - 1)
    }

    fun getAll(): ArrayList<Track> = list

    fun setCurrentPlayingItem(track: Track) {
        list.forEachIndexed { index, it ->
            if (track.trackId == it.trackId) {
                previousPlayingItem = currentPlayingItem
                currentPlayingItem = index
                notifyItemChanged(previousPlayingItem)
                notifyItemChanged(currentPlayingItem)
                return
            }
        }
    }

}