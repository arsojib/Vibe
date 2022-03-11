package com.beat.view.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.beat.R
import com.beat.core.utils.CoreConstants
import com.beat.data.model.Release
import com.beat.databinding.ReleaseMenuLayoutBinding
import com.beat.util.listener.ReleasePopUpMenuClickListener

class ReleaseMenuDialog constructor(
    private val context: Context,
    private val release: Release,
    private val releasePopUpMenuClickListener: ReleasePopUpMenuClickListener
) {
    private lateinit var dialog: AlertDialog
    private lateinit var binding: ReleaseMenuLayoutBinding

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.release_menu_layout,
            null,
            false
        )
        binding.data = release

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(true)

        if (release.favorite) {
            binding.favourite.setImageResource(R.drawable.ic_favorite_fill_28)
            binding.addToFavoriteText.text = context.getString(R.string.remove_from_favorite)
        } else {
            binding.favourite.setImageResource(R.drawable.ic_favorite_border_28)
            binding.addToFavoriteText.text = context.getString(R.string.add_to_favourite)
        }

        binding.addToFavorite.setOnClickListener {
            releasePopUpMenuClickListener.onFavorite(
                release.releaseId,
                CoreConstants.FAVORITE_RELEASE,
                !release.favorite
            )
            dialog.dismiss()
        }
        binding.gotoArtist.setOnClickListener {
            releasePopUpMenuClickListener.onGoToArtist(release.artistId, release.artistName)
            dialog.dismiss()
        }
        binding.shareOnSocial.setOnClickListener {
            releasePopUpMenuClickListener.onShareOnSocialMedia(release.url)
            dialog.dismiss()
        }

        dialog.show()
    }

}