package com.beat.view.main

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.Resource
import com.beat.data.model.Patch
import com.beat.data.model.Playlist
import com.beat.databinding.HomeFragmentLayoutBinding
import com.beat.util.listener.DeletePlaylistListener
import com.beat.util.listener.PlaylistMenuClickListener
import com.beat.util.listener.PlaylistPopUpMenuClickListener
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.util.moveToArtistFragment
import com.beat.util.moveToEditPlaylistTrackFragment
import com.beat.util.moveToPlaylistFragment
import com.beat.util.shareOnSocial
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.HomeTabAdapter
import com.beat.view.dialog.PlaylistDeleteDialog
import com.beat.view.dialog.PlaylistMenuDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFragment : BaseDaggerFragment(), ReleasePopUpMenuClickListener,
    PlaylistPopUpMenuClickListener, PlaylistMenuClickListener {

    private lateinit var binding: HomeFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var mainViewModel: MainViewModel
    private lateinit var progressDialog: ProgressDialog

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment_layout, container, false)
        mainViewModel = ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        progressDialog = ProgressDialog(mContext)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getString(R.string.please_wait))
        mainViewModel.getHomePatchList().observe(viewLifecycleOwner, Observer<ArrayList<Patch>> {
            it?.let { arrayList ->
                binding.recyclerView.adapter =
                    HomeTabAdapter(mContext, arrayList, mainViewModel, this, this)
                binding.progressBar.visibility = View.GONE
            }
        })

        mainViewModel.getDeletePlaylistResponse()
            .observe(viewLifecycleOwner, Observer<Resource<String>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                            (binding.recyclerView.adapter as HomeTabAdapter).reloadPlaylistPatch()
                        }
                        Resource.Status.ERROR -> {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                        }
                        Resource.Status.LOADING -> {
                            progressDialog.show()
                        }
                    }
                }
            })

        mainViewModel.getHomePageContent()
    }

    override fun onFavorite(id: String, type: String, isFav: Boolean) {
        mainViewModel.patchFavorite(
            id,
            type,
            isFav
        )
        GlobalScope.launch {
            (binding.recyclerView.adapter as HomeTabAdapter).setFavorite(id, type, isFav)
        }
    }

    override fun onGoToArtist(artistId: String, artistName: String) {
        moveToArtistFragment(mContext, artistId)
    }

    override fun onPlaylistDownload(playlistId: String, download: Boolean) {
        if (download) {
            mainViewModel.downloadPlaylist(playlistId)
        } else {
            mainViewModel.removePlaylist(playlistId)
        }
    }

    override fun onAddToPlaylist(playlistId: String, playlistTitle: String) {
        moveToPlaylistFragment(
            mContext, playlistId,
            addToPlaylist = true,
            offline = false
        )
    }

    override fun onEditPlaylist(playlistId: String, playlistTitle: String) {
        moveToEditPlaylistTrackFragment(mContext, playlistId, playlistTitle)
    }

    override fun onDeletePlaylist(playlist: Playlist) {
        PlaylistDeleteDialog(mContext, playlist, object : DeletePlaylistListener {
            override fun onPlaylistDelete(id: String) {
                mainViewModel.deletePlaylist(id)
            }
        })
    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(mContext, url)
    }

    override fun onMenuClick(playlist: Playlist) {
        mainViewModel.isPlaylistDownloaded(playlist.playlistId) {
            PlaylistMenuDialog(
                mContext, playlist,
                isDownloaded = it,
                isOffline = false,
                playlistPopUpMenuClickListener = this
            )
        }
    }

}
