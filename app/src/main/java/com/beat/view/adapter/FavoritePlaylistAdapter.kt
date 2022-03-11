package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Playlist
import com.beat.databinding.PlaylistItemBinding
import com.beat.util.listener.PlaylistMenuClickListener
import com.beat.util.moveToPlaylistFragment

class FavoritePlaylistAdapter(
    private val context: Context,
    private val list: ArrayList<Playlist>,
    private val playlistMenuClickListener: PlaylistMenuClickListener
) : RecyclerView.Adapter<FavoritePlaylistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val playlistItemBinding: PlaylistItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.playlist_item,
            parent,
            false
        )
        return ViewHolder(playlistItemBinding)
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
                offline = false
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(playlistItemBinding: PlaylistItemBinding) :
        RecyclerView.ViewHolder(playlistItemBinding.root) {
        val binding = playlistItemBinding
    }

}