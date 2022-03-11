package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.TrackModifyItemBinding
import com.beat.util.idsFromTrackList
import com.beat.util.listener.PlaylistTrackEditListener

class EditPlaylistTrackAdapter(
    private val context: Context,
    private val title: String,
    private val playlistTrackEditListener: PlaylistTrackEditListener
) :
    RecyclerView.Adapter<EditPlaylistTrackAdapter.ViewHolder>() {

    private val list: ArrayList<Track> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val trackModifyItemBinding: TrackModifyItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.track_modify_item,
            parent,
            false
        )
        return ViewHolder(trackModifyItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.remove.setOnClickListener {
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyDataSetChanged()
            playlistTrackEditListener.onPlaylistTrackRemove()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(trackModifyItemBinding: TrackModifyItemBinding) :
        RecyclerView.ViewHolder(trackModifyItemBinding.root) {
        val binding = trackModifyItemBinding
    }

    fun getTrackIds(): String {
        return idsFromTrackList(list)
    }

    fun addAll(list: ArrayList<Track>) {
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

}