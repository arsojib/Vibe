package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Genres
import com.beat.databinding.GenresItemBinding
import com.beat.util.moveToPlaylistFragment

class HomeGenresAdapter(
    private val context: Context,
    private val list: ArrayList<Genres>
) :
    RecyclerView.Adapter<HomeGenresAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val genresItemBinding: GenresItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.genres_item,
            parent,
            false
        )
        return ViewHolder(genresItemBinding)
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

    class ViewHolder(genresItemBinding: GenresItemBinding) :
        RecyclerView.ViewHolder(genresItemBinding.root) {
        val binding = genresItemBinding
    }

}