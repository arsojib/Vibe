package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Release
import com.beat.databinding.VideoItem2Binding
import com.beat.util.moveToVideoPlayerActivity

class FavoriteVideoAdapter(private val context: Context, private val list: ArrayList<Release>) :
    RecyclerView.Adapter<FavoriteVideoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val videoItemBinding: VideoItem2Binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.video_item_2,
            parent,
            false
        )
        return ViewHolder(videoItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.root.setOnClickListener {
            moveToVideoPlayerActivity(context, list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(videoItemBinding: VideoItem2Binding) :
        RecyclerView.ViewHolder(videoItemBinding.root) {
        val binding = videoItemBinding
    }

}