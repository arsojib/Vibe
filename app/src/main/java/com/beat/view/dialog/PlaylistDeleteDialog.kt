package com.beat.view.dialog

import android.app.AlertDialog
import android.content.Context
import com.beat.R
import com.beat.data.model.Playlist
import com.beat.util.listener.DeletePlaylistListener

class PlaylistDeleteDialog constructor(
    private val context: Context,
    private val playlist: Playlist,
    private val deletePlaylistListener: DeletePlaylistListener
) {

    private lateinit var dialog: AlertDialog

    init {
        initialDialog()
    }

    private fun initialDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.warning))
        builder.setMessage(context.getString(R.string.are_you_sure_you_want_to_delete_this_playlist))

        builder.setPositiveButton(android.R.string.yes) { dialogInterface, which ->
            deletePlaylistListener.onPlaylistDelete(playlist.playlistId)
        }
        builder.setNegativeButton(android.R.string.no) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()
    }

}