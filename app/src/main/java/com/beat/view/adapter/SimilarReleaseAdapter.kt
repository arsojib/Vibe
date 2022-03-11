package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Release
import com.beat.databinding.SimilarReleaseItemBinding
import com.beat.util.listener.ReleaseClickListener

class SimilarReleaseAdapter(
    private val context: Context,
    private val list: ArrayList<Release>,
    private val releaseClickListener: ReleaseClickListener
) : RecyclerView.Adapter<SimilarReleaseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val similarReleaseItemBinding: SimilarReleaseItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.similar_release_item,
            parent,
            false
        )
        return ViewHolder(similarReleaseItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.root.setOnClickListener {
            releaseClickListener.onReleaseClick(list[position], position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(similarReleaseItemBinding: SimilarReleaseItemBinding) :
        RecyclerView.ViewHolder(similarReleaseItemBinding.root) {
        val binding = similarReleaseItemBinding
    }

}