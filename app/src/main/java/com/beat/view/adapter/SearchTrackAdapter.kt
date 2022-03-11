package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.SearchTrackItemBinding
import com.beat.util.listener.TrackClickListener
import com.beat.util.listener.TrackPopUpMenuClickListener
import com.beat.view.dialog.TrackMenuDialog

class SearchTrackAdapter(
    private val context: Context,
    private val list: ArrayList<Track>,
    private val currentPlayingItem: Int,
    private val trackClickListener: TrackClickListener,
    private val trackPopUpMenuClickListener: TrackPopUpMenuClickListener
) :
    RecyclerView.Adapter<SearchTrackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: SearchTrackItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.search_track_item,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.title.setTextColor(
            if (position == currentPlayingItem) ContextCompat.getColor(
                context,
                R.color.colorPrimary
            ) else ContextCompat.getColor(context, R.color.colorBlack)
        )

        holder.binding.menu.setOnClickListener {
            TrackMenuDialog(
                context,
                list[position],
                trackPopUpMenuClickListener,
                false
            )
        }

        holder.binding.root.setOnClickListener {
            trackClickListener.onTrackClick(
                list[position],
                position,
                position == currentPlayingItem
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(searchTrackItemBinding: SearchTrackItemBinding) :
        RecyclerView.ViewHolder(searchTrackItemBinding.root) {
        val binding = searchTrackItemBinding
    }

}