package com.beat.view.content.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.Resource
import com.beat.data.model.Playlist
import com.beat.databinding.PlaylistFragmentLayoutBinding
import com.beat.util.listener.PlaylistMenuClickListener
import com.beat.util.listener.PlaylistPopUpMenuClickListener
import com.beat.util.shareOnSocial
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.DownloadedPlaylistAdapter
import com.beat.view.dialog.PlaylistMenuDialog
import javax.inject.Inject

class DownloadedPlaylistFragment : BaseDaggerFragment(), PlaylistMenuClickListener,
    PlaylistPopUpMenuClickListener {

    private lateinit var binding: PlaylistFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory
    private lateinit var downloadViewModel: DownloadViewModel
    private lateinit var downloadedPlaylistAdapter: DownloadedPlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.playlist_fragment_layout, container, false)
        downloadViewModel =
            ViewModelProvider(this, providerFactory).get(DownloadViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        downloadedPlaylistAdapter = DownloadedPlaylistAdapter(mContext, this)
        binding.recyclerView.adapter = downloadedPlaylistAdapter

        downloadViewModel.getPlaylistList()
            .observe(viewLifecycleOwner, Observer<Resource<List<Playlist>>> {
                it?.let {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.GONE
                            downloadedPlaylistAdapter.addAll(it.data!!)
                        }
                        Resource.Status.ERROR -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.VISIBLE
                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        downloadViewModel.getDataUpdatedObserver(Observer {
            downloadViewModel.fetchPlaylistList()
        }, viewLifecycleOwner)

        downloadViewModel.fetchPlaylistList()
    }

    override fun onMenuClick(playlist: Playlist) {
        downloadViewModel.isPlaylistDownloaded(playlist.playlistId) {
            PlaylistMenuDialog(mContext, playlist, it, true, this)
        }
    }

    override fun onPlaylistDownload(playlistId: String, download: Boolean) {
        if (!download) {
            downloadViewModel.removePlaylist(playlistId)
            downloadedPlaylistAdapter.removePlaylist(playlistId)
        }
    }

    //Not required for downloaded playlist
    override fun onAddToPlaylist(playlistId: String, playlistTitle: String) {

    }

    //Not required for downloaded playlist
    override fun onEditPlaylist(playlistId: String, playlistTitle: String) {

    }

    override fun onDeletePlaylist(playlist: Playlist) {

    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(mContext, url)
    }


}