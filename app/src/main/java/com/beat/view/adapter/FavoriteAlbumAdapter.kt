package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Release
import com.beat.databinding.AlbumItemBinding
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.util.moveToReleaseFragment
import com.beat.view.dialog.ReleaseMenuDialog

class FavoriteAlbumAdapter(
    private val context: Context,
    private val list: ArrayList<Release>,
    private val releasePopUpMenuClickListener: ReleasePopUpMenuClickListener
) : RecyclerView.Adapter<FavoriteAlbumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val albumItemBinding: AlbumItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.album_item,
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

    class ViewHolder(albumItemBinding: AlbumItemBinding) :
        RecyclerView.ViewHolder(albumItemBinding.root) {
        val binding = albumItemBinding
    }

}