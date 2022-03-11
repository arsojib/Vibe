package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.FeaturedGroupPlaylist
import com.beat.databinding.SpecialPlaylistItemBinding
import com.beat.util.moveToPlaylistFragment

class HomeSpecialPlaylistAdapter(
    private val context: Context,
    private val list: ArrayList<FeaturedGroupPlaylist>
) :
    RecyclerView.Adapter<HomeSpecialPlaylistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val specialPlaylistTemBinding: SpecialPlaylistItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.special_playlist_item,
            parent,
            false
        )
        return ViewHolder(specialPlaylistTemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

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

    class ViewHolder(specialPlaylistTemBinding: SpecialPlaylistItemBinding) :
        RecyclerView.ViewHolder(specialPlaylistTemBinding.root) {
        val binding = specialPlaylistTemBinding
    }

}