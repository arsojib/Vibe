package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Release
import com.beat.databinding.VideoItemBinding
import com.beat.util.listener.VideoClickListener

class HomeVideoAdapter(
    private val context: Context,
    private val list: ArrayList<Release>,
    private val videoClickListener: VideoClickListener
) : RecyclerView.Adapter<HomeVideoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val videoItemBinding: VideoItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.video_item,
            parent,
            false
        )
        return ViewHolder(videoItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.root.setOnClickListener {
            videoClickListener.onVideoClick(list[position], position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(videoItemBinding: VideoItemBinding) :
        RecyclerView.ViewHolder(videoItemBinding.root) {
        val binding = videoItemBinding
    }

}