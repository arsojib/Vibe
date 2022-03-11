package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.MusicItemBinding

class HomeMusicAdapter(private val context: Context, private val list: ArrayList<Track>) : RecyclerView.Adapter<HomeMusicAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val musicItemBinding: MusicItemBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.music_item, parent, false)
        return ViewHolder(musicItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.root.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(musicItemBinding: MusicItemBinding) : RecyclerView.ViewHolder(musicItemBinding.root) {
        val binding = musicItemBinding
    }

}