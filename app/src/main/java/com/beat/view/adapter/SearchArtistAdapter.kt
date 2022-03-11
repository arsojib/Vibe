package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Artist
import com.beat.databinding.SearchArtistItemBinding
import com.beat.util.moveToArtistFragment

class SearchArtistAdapter(private val context: Context, private val list: ArrayList<Artist>) :
    RecyclerView.Adapter<SearchArtistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: SearchArtistItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.search_artist_item,
            parent,
            false
        )
        return ViewHolder(binding)
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

    class ViewHolder(searchArtistItemBinding: SearchArtistItemBinding) :
        RecyclerView.ViewHolder(searchArtistItemBinding.root) {
        val binding = searchArtistItemBinding
    }

}