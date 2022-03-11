package com.beat.view.dialog

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.beat.R
import com.beat.core.data.model.CreateUserPlaylistResponse
import com.beat.core.data.model.Event
import com.beat.core.data.model.Resource
import com.beat.data.model.Playlist
import com.beat.databinding.CreatePlaylistLayoutBinding
import com.beat.util.listener.PlaylistCreateListener
import com.beat.view.common.userPlaylist.UserPlaylistViewModel

class CreatePlaylistDialog constructor(
    private val fragment: Fragment,
    private val userPlaylistViewModel: UserPlaylistViewModel,
    private val playlistCreateListener: PlaylistCreateListener
) {

    private lateinit var dialog: AlertDialog
    private lateinit var binding: CreatePlaylistLayoutBinding

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(fragment.context),
            R.layout.create_playlist_layout,
            null,
            false
        )

        val builder: AlertDialog.Builder =
            AlertDialog.Builder(fragment.context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(false)

        userPlaylistViewModel.getCreatePlaylistResponse().observe(
            fragment.viewLifecycleOwner,
            Observer<Event<Resource<CreateUserPlaylistResponse>>> {
                it?.let { resource ->
                    when (resource.getContentIfNotHandled()?.status) {
                        Resource.Status.SUCCESS -> {
                            binding.uiFreezer.visibility = View.INVISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            //Need to Process data
                            val playlist: Playlist? =
                                userPlaylistViewModel.getPlaylistFromResponse(resource.peekContent().data!!)
                            if (playlist != null) {
                                playlistCreateListener.onPlaylistCreated(playlist)
                                Toast.makeText(
                                    fragment.context,
                                    fragment.getText(R.string.playlist_created_successfully),
                                    Toast.LENGTH_LONG
                                ).show()
                                dismissDialog()
                            } else Toast.makeText(
                                fragment.context,
                                fragment.getText(R.string.something_went_wrong_please_try_again),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Resource.Status.ERROR -> {
                            binding.uiFreezer.visibility = View.INVISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            Toast.makeText(
                                fragment.context,
                                fragment.getText(R.string.something_went_wrong_please_try_again),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Resource.Status.LOADING -> {
                            binding.uiFreezer.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })

        binding.ok.setOnClickListener {
            if (binding.title.text.toString().isNotEmpty())
                userPlaylistViewModel.createPlaylist(binding.title.text.toString())
            else
                binding.title.error = fragment.context!!.getString(R.string.title_is_empty)
        }

        binding.cancel.setOnClickListener {
            dismissDialog()
        }

        dialog.show()
    }

    private fun dismissDialog() {
        userPlaylistViewModel.getCreatePlaylistResponse()
            .removeObservers(fragment.viewLifecycleOwner)
        dialog.dismiss()
    }

}