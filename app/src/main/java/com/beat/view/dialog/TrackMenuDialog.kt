package com.beat.view.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.beat.R
import com.beat.core.utils.CoreConstants
import com.beat.data.model.Track
import com.beat.databinding.TrackMenuLayoutBinding
import com.beat.util.idFromTrack
import com.beat.util.listener.TrackPopUpMenuClickListener

class TrackMenuDialog constructor(
    private val context: Context,
    private val track: Track,
    private val trackPopUpMenuClickListener: TrackPopUpMenuClickListener,
    private val fromDownload: Boolean
) {

    private lateinit var dialog: AlertDialog
    private lateinit var binding: TrackMenuLayoutBinding

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.track_menu_layout,
            null,
            false
        )
        binding.data = track

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(true)

        if (track.favorite) {
            binding.favourite.setImageResource(R.drawable.ic_favorite_fill_28)
            binding.addToFavoriteText.text = context.getString(R.string.remove_from_favorite)
        } else {
            binding.favourite.setImageResource(R.drawable.ic_favorite_border_28)
            binding.addToFavoriteText.text = context.getString(R.string.add_to_favourite)
        }

        binding.removeFromDownload.visibility = if (fromDownload) View.VISIBLE else View.GONE

        binding.addToFavorite.setOnClickListener {
            trackPopUpMenuClickListener.onFavorite(
                track.trackId,
                CoreConstants.FAVORITE_TRACK,
                !track.favorite
            )
            dialog.dismiss()
        }
        binding.addToPlayList.setOnClickListener {
            trackPopUpMenuClickListener.onTrackAddToPlaylist(idFromTrack(track.trackId))
            dialog.dismiss()
        }
        binding.gotoArtist.setOnClickListener {
            trackPopUpMenuClickListener.onGoToArtist(track.artistId, track.artistName)
            dialog.dismiss()
        }
        binding.gotoAlbum.setOnClickListener {
            trackPopUpMenuClickListener.onGotoAlbum(
                track.releaseId,
                track.artistId,
                track.artistName
            )
            dialog.dismiss()
        }
        binding.removeFromDownload.setOnClickListener {
            trackPopUpMenuClickListener.onRemoveFromDownload(track.trackId)
        }
        binding.shareOnSocial.setOnClickListener {
            trackPopUpMenuClickListener.onShareOnSocialMedia(track.url)
            dialog.dismiss()
        }

        dialog.show()
    }

}