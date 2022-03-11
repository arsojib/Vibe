package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Artist
import com.beat.databinding.ArtistItemBinding
import com.beat.util.moveToArtistFragment

class HomeArtistAdapter(
    private val context: Context,
    private val list: ArrayList<Artist>
) : RecyclerView.Adapter<HomeArtistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val artistItemBinding: ArtistItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.artist_item,
            parent,
            false
        )
        return ViewHolder(artistItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.root.setOnClickListener {
            moveToArtistFragment(context, list[position].artistId)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(artistItemBinding: ArtistItemBinding) :
        RecyclerView.ViewHolder(artistItemBinding.root) {
        val binding = artistItemBinding
    }

}