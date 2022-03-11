package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Playlist
import com.beat.databinding.PlaylistItem4Binding
import com.beat.util.listener.PlaylistMenuClickListener
import com.beat.util.moveToPlaylistFragment

class DownloadedPlaylistAdapter(
    private val context: Context,
    private val playlistMenuClickListener: PlaylistMenuClickListener
) :
    RecyclerView.Adapter<DownloadedPlaylistAdapter.ViewHolder>() {

    private val list = ArrayList<Playlist>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val playlistItem4Binding: PlaylistItem4Binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.playlist_item_4,
            parent,
            false
        )
        return ViewHolder(playlistItem4Binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.menu.setOnClickListener {
            playlistMenuClickListener.onMenuClick(list[position])
        }

        holder.binding.root.setOnClickListener {
            moveToPlaylistFragment(
                context,
                list[position].playlistId,
                addToPlaylist = false,
                offline = true
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(playlistItem4Binding: PlaylistItem4Binding) :
        RecyclerView.ViewHolder(playlistItem4Binding.root) {
        val binding = playlistItem4Binding
    }

    fun addAll(list: List<Playlist>) {
        this.list.clear()
        notifyDataSetChanged()
        for (playlist in list) {
            add(playlist)
        }
    }

    private fun add(playlist: Playlist) {
        list.add(playlist)
        notifyItemInserted(list.size - 1)
    }

    fun removePlaylist(playlistId: String) {
        list.forEachIndexed { index, playlist ->
            if (playlist.playlistId == playlistId) {
                list.removeAt(index)
                notifyItemRemoved(index)
                notifyItemRangeChanged(index, list.size)
                return
            }
        }
    }

}