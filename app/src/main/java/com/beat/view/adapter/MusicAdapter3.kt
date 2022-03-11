package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.MusicItem3Binding
import com.beat.util.Logger
import com.beat.util.idsFromTrackList
import com.beat.util.listener.TrackClickListener
import com.beat.view.content.details.ContentDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class MusicAdapter3(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val contentDetailsViewModel: ContentDetailsViewModel,
    private val trackClickListener: TrackClickListener
) :
    RecyclerView.Adapter<MusicAdapter3.ViewHolder>() {

    private val TAG = "MusicAdapter3"

    private val list = ArrayList<Track>()
    private var previousPlayingItem = -1
    private var currentPlayingItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val musicItem3Binding: MusicItem3Binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.music_item_3,
            parent,
            false
        )
        musicItem3Binding.lifecycleOwner = lifecycleOwner
        return ViewHolder(musicItem3Binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        contentDetailsViewModel.getDownloadObserver(list[position].trackId, Observer {
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

    class ViewHolder(musicItem3Binding: MusicItem3Binding) :
        RecyclerView.ViewHolder(musicItem3Binding.root) {
        val binding = musicItem3Binding
    }

    fun getTrackIds(): String {
        return idsFromTrackList(list)
    }

    fun setFavorite(id: String, favorite: Boolean) {
        list.forEachIndexed { index, track ->
            if (track.trackId == id) {
                list[index].favorite = favorite
            }
        }
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

    fun setCurrentPlayingItem(track: Track) {
        list.forEachIndexed { index, it ->
            if (track.trackId == it.trackId) {
                previousPlayingItem = currentPlayingItem
                currentPlayingItem = index
                notifyItemChanged(previousPlayingItem)
                notifyItemChanged(currentPlayingItem)
                return
            }
        }
    }

    fun setDownloadObserverAdded(trackId: String) {
        lifecycleOwner.lifecycleScope.launch {
            list.forEachIndexed { index, track ->
                if (track.trackId == trackId) {
                    withContext(Dispatchers.Main) { notifyItemChanged(index) }
                }
            }
        }
    }

}