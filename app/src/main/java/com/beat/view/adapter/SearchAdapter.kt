package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.core.utils.CoreConstants
import com.beat.data.model.Artist
import com.beat.data.model.Release
import com.beat.data.model.SearchPatch
import com.beat.data.model.Track
import com.beat.databinding.SearchPatchItemBinding
import com.beat.util.Constants
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.util.listener.TrackClickListener
import com.beat.util.listener.TrackPopUpMenuClickListener

class SearchAdapter(
    private val context: Context,
    private val trackClickListener: TrackClickListener,
    private val trackPopUpMenuClickListener: TrackPopUpMenuClickListener,
    private val releasePopUpMenuClickListener: ReleasePopUpMenuClickListener
) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private val list: ArrayList<SearchPatch> = ArrayList()

    private var currentPlayingItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: SearchPatchItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.search_patch_item,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.title.text = list[position].title
        holder.binding.recyclerView.isNestedScrollingEnabled = false
        when (list[position].patch) {
            Constants.MUSIC_PATCH -> {
                holder.binding.recyclerView.layoutManager = LinearLayoutManager(context)
                holder.binding.recyclerView.adapter =
                    SearchTrackAdapter(
                        context,
                        if (list[position].list == null) ArrayList() else
                            list[position].list as ArrayList<Track>,
                        currentPlayingItem,
                        trackClickListener,
                        trackPopUpMenuClickListener
                    )
            }
            Constants.ALBUM_PATCH -> {
                holder.binding.recyclerView.layoutManager = LinearLayoutManager(context)
                holder.binding.recyclerView.adapter =
                    SearchAlbumAdapter(
                        context,
                        if (list[position].list == null) ArrayList() else
                            list[position].list as ArrayList<Release>,
                        releasePopUpMenuClickListener
                    )
            }
            Constants.ARTIST_PATCH -> {
                holder.binding.recyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                holder.binding.recyclerView.adapter =
                    SearchArtistAdapter(
                        context,
                        if (list[position].list == null) ArrayList() else
                            list[position].list as ArrayList<Artist>
                    )
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(searchPatchItemBinding: SearchPatchItemBinding) :
        RecyclerView.ViewHolder(searchPatchItemBinding.root) {
        val binding = searchPatchItemBinding
    }

    fun clearAll() {
        this.list.clear()
        notifyDataSetChanged()
    }

    fun addAll(list: ArrayList<SearchPatch>) {
        this.list.clear()
        notifyDataSetChanged()
        for (searchPatch in list) {
            add(searchPatch)
        }
    }

    private fun add(searchPatch: SearchPatch) {
        list.add(searchPatch)
        notifyItemInserted(list.size - 1)
    }

    fun setFavorite(id: String, type: String, favorite: Boolean) {
        when (type) {
            CoreConstants.FAVORITE_RELEASE -> {
                for (i in 0 until (list.size)) {
                    if (list[i].patch == Constants.ALBUM_PATCH) {
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
                    if (list[i].patch == Constants.MUSIC_PATCH) {
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
            if (it.patch == Constants.MUSIC_PATCH) {
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