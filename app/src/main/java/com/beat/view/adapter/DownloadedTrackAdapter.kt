package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.TrackItemBinding
import com.beat.util.Logger
import com.beat.util.listener.TrackClickListener
import com.beat.view.content.download.DownloadViewModel

class DownloadedTrackAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val downloadViewModel: DownloadViewModel,
    private val trackClickListener: TrackClickListener
) :
    RecyclerView.Adapter<DownloadedTrackAdapter.ViewHolder>() {

    private val TAG = "DownloadedTrackAdapter"

    private val list: ArrayList<Track> = ArrayList()

    private val currentPlayingItem: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val trackItemBinding: TrackItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.track_item,
            parent,
            false
        )
        return ViewHolder(trackItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        downloadViewModel.getDownloadObserver(list[position].trackId, Observer {
            Logger.d(TAG, list[position].trackId + " " + it)
            holder.binding.progressBar.progress = it
            holder.binding.progressBar.visibility = if (it < 100) View.VISIBLE else View.INVISIBLE
            holder.binding.downloadDone.visibility = if (it < 100) View.INVISIBLE else View.VISIBLE
        }, lifecycleOwner)

        holder.binding.title.setTextColor(
            if (position == currentPlayingItem) ContextCompat.getColor(
                context,
                R.color.colorPrimary
            ) else ContextCompat.getColor(context, R.color.colorBlack)
        )

        holder.binding.menu.setOnClickListener {
            trackClickListener.onTrackMenuClick(list[position])
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

    class ViewHolder(trackItemBinding: TrackItemBinding) :
        RecyclerView.ViewHolder(trackItemBinding.root) {
        val binding = trackItemBinding
    }

    fun addAll(list: List<Track>) {
        this.list.clear()
        notifyDataSetChanged()
        for (track in list) {
            add(track)
        }
    }

    private fun add(track: Track) {
        list.add(track)
        notifyItemInserted(list.size - 1)
    }

    fun getAll(): ArrayList<Track> = list

    fun setFavorite(id: String, favorite: Boolean) {
        list.forEachIndexed { index, track ->
            if (track.trackId == id) {
                list[index].favorite = favorite
            }
        }
    }

    fun removeById(id: String) {
        list.forEachIndexed { index, track ->
            if (track.trackId == id) {
                list.removeAt(index)
                notifyItemRemoved(index)
                notifyItemRangeChanged(index, list.size)
            }
        }
    }

}