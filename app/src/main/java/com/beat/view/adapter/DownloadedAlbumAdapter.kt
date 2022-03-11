package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Release
import com.beat.data.model.Track
import com.beat.databinding.AlbumItem2Binding
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.util.moveToReleaseFragment
import com.beat.view.dialog.ReleaseMenuDialog

class DownloadedAlbumAdapter(
    private val context: Context,
    private val releasePopUpMenuClickListener: ReleasePopUpMenuClickListener
) :
    RecyclerView.Adapter<DownloadedAlbumAdapter.ViewHolder>() {

    private val list = ArrayList<Release>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val albumItemBinding: AlbumItem2Binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.album_item_2,
            parent,
            false
        )
        return ViewHolder(albumItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.menu.setOnClickListener {
            ReleaseMenuDialog(context, list[position], releasePopUpMenuClickListener)
        }

        holder.binding.root.setOnClickListener {
            moveToReleaseFragment(
                context,
                list[position].releaseId
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(albumItemBinding: AlbumItem2Binding) :
        RecyclerView.ViewHolder(albumItemBinding.root) {
        val binding = albumItemBinding
    }

    fun addAll(list: List<Release>) {
        this.list.clear()
        notifyDataSetChanged()
        for (release in list) {
            add(release)
        }
    }

    private fun add(release: Release) {
        list.add(release)
        notifyItemInserted(list.size - 1)
    }

    fun setFavorite(id: String, favorite: Boolean) {
        list.forEachIndexed { index, release ->
            if (release.releaseId == id) {
                list[index].favorite = favorite
            }
        }
    }

}