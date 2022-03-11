package com.beat.view.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.ContinuePlayingSongMenuLayoutBinding
import com.beat.util.listener.ContinuePlayingMenuListener

class ContinuePlayingMenuDialog constructor(
    private val context: Context,
    private val track: Track,
    private val continuePlayingMenuListener: ContinuePlayingMenuListener
) {

    private lateinit var dialog: AlertDialog
    private lateinit var binding: ContinuePlayingSongMenuLayoutBinding

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.continue_playing_song_menu_layout,
            null,
            false
        )

        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(true)

        binding.data = track.trackTitle

        binding.playNowLayout.setOnClickListener {
            continuePlayingMenuListener.onPlayNow(track)
            dialog.dismiss()
        }

        binding.playNextLayout.setOnClickListener {
            continuePlayingMenuListener.onPlayNext(track)
            dialog.dismiss()
        }

        binding.addToQueueLayout.setOnClickListener {
            continuePlayingMenuListener.addToQueue(track)
            dialog.dismiss()
        }

        dialog.show()
    }

}