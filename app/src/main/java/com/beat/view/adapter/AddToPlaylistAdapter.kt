package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Playlist
import com.beat.databinding.AddToPlaylistItemFooterBinding
import com.beat.databinding.PlaylistItem2Binding
import com.beat.util.listener.PaginationListener
import com.beat.util.listener.PlaylistClickListener

class AddToPlaylistAdapter(
    private val context: Context,
    private val playlistClickListener: PlaylistClickListener,
    private val paginationListener: PaginationListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = ArrayList<Playlist>()
    private var stopPagination: Boolean = true
    private val FOOTER_VIEW = 0
    private val ITEM_VIEW = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == FOOTER_VIEW) {
            val addToPlaylistItemFooterBinding: AddToPlaylistItemFooterBinding =
                DataBindingUtil.inflate(
                    LayoutInflater.from(context),
                    R.layout.add_to_playlist_item_footer,
                    parent,
                    false
                )
            return FooterViewHolder(addToPlaylistItemFooterBinding)
        } else {
            val playlistItemBinding: PlaylistItem2Binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.playlist_item_2,
                parent,
                false
            )
            return ViewHolder(playlistItemBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.binding.data = list[position]

            holder.binding.root.setOnClickListener {
                playlistClickListener.onPlaylistClick(list[position], position)
            }
        } else if (holder is FooterViewHolder) {
            if (stopPagination) {
                holder.binding.footerProgress.visibility = View.GONE
            } else {
                holder.binding.footerProgress.visibility = View.VISIBLE
            }
            paginationListener.onPagination()
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (list.size == position) return FOOTER_VIEW
        return ITEM_VIEW
    }

    class ViewHolder(playlistItemBinding: PlaylistItem2Binding) :
        RecyclerView.ViewHolder(playlistItemBinding.root) {
        val binding = playlistItemBinding
    }

    class FooterViewHolder(addToPlaylistItemFooterBinding: AddToPlaylistItemFooterBinding) :
        RecyclerView.ViewHolder(addToPlaylistItemFooterBinding.root) {
        val binding = addToPlaylistItemFooterBinding
    }

    fun addAll(list: ArrayList<Playlist>) {
        for (playlist in list) {
            add(playlist)
        }
        if (this.list.size == list.size) notifyDataSetChanged()
    }

    private fun add(playlist: Playlist) {
        list.add(playlist)
        notifyItemInserted(list.size - 1)
    }

    fun addToTop(playlist: Playlist) {
        if (list.isNotEmpty()) {
            list.add(0, playlist)
        } else {
            list.add(playlist)
        }
        notifyDataSetChanged()
    }

    fun stopPagination(stop: Boolean) {
        stopPagination = stop
    }

}