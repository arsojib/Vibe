package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.core.utils.CoreConstants
import com.beat.data.model.Patch
import com.beat.data.model.Playlist
import com.beat.data.model.Release
import com.beat.data.model.Track
import com.beat.databinding.FavoriteHeaderItemBinding
import com.beat.util.Constants
import com.beat.util.listener.*

class FavoriteAudioTabAdapter(
    private val context: Context,
    private val trackClickListener: TrackClickListener,
    private val trackPopUpMenuClickListener: TrackPopUpMenuClickListener,
    private val releasePopUpMenuClickListener: ReleasePopUpMenuClickListener,
    private val playlistMenuClickListener: PlaylistMenuClickListener
) :
    RecyclerView.Adapter<FavoriteAudioTabAdapter.ViewHolder>() {

    private val list: ArrayList<Patch> = ArrayList()

    private var currentPlayingItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val favoriteHeaderItemBinding: FavoriteHeaderItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.favorite_header_item, parent, false
        )
        return ViewHolder(favoriteHeaderItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.title.text = list[position].title
        when (list[position].patch) {
            Constants.TRACK_PATCH -> {
                holder.binding.recyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                holder.binding.recyclerView.adapter =
                    FavoriteTrackAdapter(
                        context,
                        if (list[position].list == null) ArrayList() else
                            list[position].list as ArrayList<Track>,
                        currentPlayingItem,
                        trackClickListener,
                        trackPopUpMenuClickListener
                    )
            }
            Constants.RELEASE_PATCH -> {
                holder.binding.recyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                holder.binding.recyclerView.adapter =
                    FavoriteAlbumAdapter(
                        context,
                        if (list[position].list == null) ArrayList() else
                            list[position].list as ArrayList<Release>,
                        releasePopUpMenuClickListener
                    )
            }
            Constants.PLAYLIST_PATCH -> {
                holder.binding.recyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                holder.binding.recyclerView.adapter =
                    FavoritePlaylistAdapter(
                        context,
                        if (list[position].list == null) ArrayList() else
                            list[position].list as ArrayList<Playlist>,
                        playlistMenuClickListener
                    )
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(favoriteHeaderItemBinding: FavoriteHeaderItemBinding) :
        RecyclerView.ViewHolder(favoriteHeaderItemBinding.root) {
        val binding = favoriteHeaderItemBinding
    }

    fun addAll(list: ArrayList<Patch>) {
        this.list.clear()
        notifyDataSetChanged()
        for (patch in list) {
            add(patch)
        }
    }

    private fun add(patch: Patch) {
        list.add(patch)
        notifyItemInserted(list.size - 1)
    }

    fun setFavorite(id: String, type: String, favorite: Boolean) {
        when (type) {
            CoreConstants.FAVORITE_RELEASE -> {
                for (i in 0 until (list.size)) {
                    if (list[i].patch == Constants.RELEASE_PATCH) {
                        val releaseList =
                            if (list[i].list == null) break else list[i].list as ArrayList<Release>
                        releaseList.forEachIndexed { index, release ->
                            if (release.releaseId == id) {
                                releaseList[index].favorite = favorite
                            }
                        }
                    }
                }
            }
            CoreConstants.FAVORITE_TRACK -> {
                for (i in 0 until (list.size)) {
                    if (list[i].patch == Constants.TRACK_PATCH) {
                        val trackList =
                            if (list[i].list == null) break else list[i].list as ArrayList<Track>
                        trackList.forEachIndexed { index, track ->
                            if (track.trackId == id) {
                                trackList[index].favorite = favorite
                            }
                        }
                    }
                }
            }
        }
    }

    fun setCurrentPlayingItem(track: Track) {
        list.forEachIndexed { index, it ->
            if (it.patch == Constants.TRACK_PATCH) {
                if (it.list != null) {
                    (it.list as ArrayList<Track>).forEachIndexed { position, itTrack ->
                        if (track.trackId == itTrack.trackId) {
                            currentPlayingItem = position
                            notifyItemChanged(index)
                            return
                        }
                    }
                    return
                }
            }
        }
    }

}