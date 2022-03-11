package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Release
import com.beat.databinding.SimilarVideoItemBinding

class SimilarVideoAdapter(
    private val context: Context,
    private val list: ArrayList<Release>
) : RecyclerView.Adapter<SimilarVideoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val similarVideoItemBinding: SimilarVideoItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.similar_video_item,
            parent,
            false
        )
        return ViewHolder(similarVideoItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(similarVideoItemBinding: SimilarVideoItemBinding) :
        RecyclerView.ViewHolder(similarVideoItemBinding.root) {
        val binding = similarVideoItemBinding
    }

}