package com.beat.view.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.beat.R
import com.beat.data.model.Playlist
import com.beat.databinding.PlaylistMenuLayoutBinding
import com.beat.util.Constants
import com.beat.util.listener.PlaylistPopUpMenuClickListener

class PlaylistMenuDialog constructor(
    private val context: Context,
    private val playlist: Playlist,
    private val isDownloaded: Boolean,
    private val isOffline: Boolean,
    private val playlistPopUpMenuClickListener: PlaylistPopUpMenuClickListener
) {

    private lateinit var dialog: AlertDialog
    private lateinit var binding: PlaylistMenuLayoutBinding

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.playlist_menu_layout,
            null,
            false
        )
        binding.data = playlist
        binding.downloaded = isDownloaded

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(true)

        binding.editPlaylist.visibility =
            if (playlist.permission == Constants.PERMISSION_WRITE && !isOffline) View.VISIBLE else View.GONE
        binding.deletePlaylist.visibility =
            if (playlist.permission == Constants.PERMISSION_WRITE && !isOffline) View.VISIBLE else View.GONE
        binding.addToPlayList.visibility = if (isOffline) View.GONE else View.VISIBLE

        binding.downloadPlaylist.setOnClickListener {
            playlistPopUpMenuClickListener.onPlaylistDownload(playlist.playlistId, !isDownloaded)
            dialog.dismiss()
        }
        binding.addToPlayList.setOnClickListener {
            playlistPopUpMenuClickListener.onAddToPlaylist(
                playlist.playlistId,
                playlist.playlistTitle
            )
            dialog.dismiss()
        }
        binding.editPlaylist.setOnClickListener {
            playlistPopUpMenuClickListener.onEditPlaylist(
                playlist.playlistId,
                playlist.playlistTitle
            )
            dialog.dismiss()
        }
        binding.deletePlaylist.setOnClickListener {
            playlistPopUpMenuClickListener.onDeletePlaylist(playlist)
            dialog.dismiss()
        }
        binding.shareOnSocial.setOnClickListener {
            playlistPopUpMenuClickListener.onShareOnSocialMedia(playlist.url)
            dialog.dismiss()
        }

        dialog.show()
    }

}