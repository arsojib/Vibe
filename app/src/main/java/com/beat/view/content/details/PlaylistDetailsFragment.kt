package com.beat.view.content.details

import android.app.ProgressDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.PlaylistDetailsWithTrackResponse
import com.beat.core.data.model.Resource
import com.beat.core.data.storage.OfflineDownloadConnection
import com.beat.core.utils.CoreConstants
import com.beat.data.model.OfflinePlaylistWithTracks
import com.beat.data.model.Playlist
import com.beat.data.model.Track
import com.beat.databinding.PlaylistDetailsFragmentLayoutBinding
import com.beat.util.isRadioInitialed
import com.beat.util.listener.*
import com.beat.util.shareOnSocial
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.MusicAdapter3
import com.beat.view.common.userPlaylist.AddToPlaylistFragment
import com.beat.view.common.userPlaylist.EditPlaylistTrackFragment
import com.beat.view.dialog.ContinuePlayingMenuDialog
import com.beat.view.dialog.PlaylistDeleteDialog
import com.beat.view.dialog.PlaylistMenuDialog
import com.beat.view.dialog.TrackMenuDialog
import com.example.android.uamp.media.extensions.isPlaying
import javax.inject.Inject

class PlaylistDetailsFragment : BaseDaggerFragment(), TrackClickListener,
    PlaylistPopUpMenuClickListener {

    private lateinit var binding: PlaylistDetailsFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var offlineDownloadConnection: OfflineDownloadConnection

    private lateinit var contentDetailsViewModel: ContentDetailsViewModel
    private lateinit var musicAdapter: MusicAdapter3
    private lateinit var progressDialog: ProgressDialog

    private lateinit var playlistId: String
    private lateinit var playlistTitle: String
    private var addToPlaylist: Boolean = false
    private var offline: Boolean = false

    companion object {
        fun newInstance(
            playlistId: String?,
            addToPlaylist: Boolean,
            offline: Boolean
        ): PlaylistDetailsFragment {
            val fragment = PlaylistDetailsFragment()
            val bundle = Bundle()
            bundle.putString("playlistId", playlistId)
            bundle.putBoolean("addToPlaylist", addToPlaylist)
            bundle.putBoolean("offline", offline)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.playlist_details_fragment_layout,
                container,
                false
            )
        if (!::contentDetailsViewModel.isInitialized) {
            addToPlaylist = arguments?.getBoolean("addToPlaylist", false)!!
            offline = arguments?.getBoolean("offline", false)!!
            contentDetailsViewModel =
                ViewModelProvider(this, providerFactory).get(ContentDetailsViewModel::class.java)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistId = arguments?.getString("playlistId", "")!!

        initialComponent()
    }

    private fun initialComponent() {
        progressDialog = ProgressDialog(mContext)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getString(R.string.please_wait))
//        binding.header.title.text = playlistTitle
        binding.header.title.text = getString(R.string.loading)

        musicAdapter = MusicAdapter3(mContext, viewLifecycleOwner, contentDetailsViewModel, this)
        binding.songsRecyclerView.adapter = musicAdapter

        contentDetailsViewModel.getGradientDrawable()
            .observe(viewLifecycleOwner, Observer<Drawable> {
                it?.let { drawable ->
                    binding.topLayout.background = drawable
                }
            })

        contentDetailsViewModel.getPlaylistDetailsWithTrackResponse()
            .observe(viewLifecycleOwner, Observer<Resource<PlaylistDetailsWithTrackResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.refreshLayout.isRefreshing = false
                            binding.layout.visibility = View.VISIBLE
                            val playlist =
                                contentDetailsViewModel.getPlaylistFromResponse(resource.data!!)
                            binding.data = playlist
                            playlistTitle = playlist?.playlistTitle!!
                            binding.header.title.text = playlistTitle
                            contentDetailsViewModel.setImage(
                                binding.playlistImage,
                                binding.data?.playlistImage,
                                resources.getDrawable(R.drawable.ic_ph_playlist, null)
                            )
                            musicAdapter.addAll(
                                contentDetailsViewModel.getTrackListFromPlaylistDetailsResponse(
                                    resource.data!!
                                )
                            )
                            //Update current playing item color after available
                            contentDetailsViewModel.fetchCurrentPlayingItem()

                            if (addToPlaylist) {
                                addToPlaylist = false
                                toAddToPlaylist(musicAdapter.getTrackIds())
                            }
                        }
                        Resource.Status.ERROR -> {
                            binding.layout.visibility = View.GONE
                            binding.refreshLayout.isRefreshing = false
                        }
                        Resource.Status.LOADING -> {
                            binding.refreshLayout.isRefreshing = true
                        }
                    }
                }
            })

        contentDetailsViewModel.getOfflinePlaylistWithTracks()
            .observe(viewLifecycleOwner, Observer<Resource<OfflinePlaylistWithTracks>> { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        binding.refreshLayout.isRefreshing = false
                        binding.layout.visibility = View.VISIBLE
                        binding.data = resource.data!!.playlist
                        contentDetailsViewModel.setImage(
                            binding.playlistImage,
                            binding.data?.playlistImage,
                            resources.getDrawable(R.drawable.ic_ph_playlist, null)
                        )
                        musicAdapter.addAll(resource.data!!.tracks)
                        //Update current playing item color after available
                        contentDetailsViewModel.fetchCurrentPlayingItem()
                    }
                    Resource.Status.ERROR -> {

                    }
                    Resource.Status.LOADING -> {
                        binding.refreshLayout.isRefreshing = true
                    }
                }
            })

        contentDetailsViewModel.getDeletePlaylistResponse()
            .observe(viewLifecycleOwner, Observer<Resource<String>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                            onNavBackClick()
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

        contentDetailsViewModel.getNewDownloadAddObserver(Observer {
            it?.let {
                musicAdapter.setDownloadObserverAdded(it)
            }
        }, viewLifecycleOwner)

        contentDetailsViewModel.getNowPlayingMediaItem()
            .observe(viewLifecycleOwner, Observer<Track> {
                musicAdapter.setCurrentPlayingItem(it)
            })

        contentDetailsViewModel.getPlaylistDetailsContent(playlistId, offline)

        binding.refreshLayout.setOnRefreshListener {
            contentDetailsViewModel.getPlaylistDetailsContent(playlistId, offline)
        }

        binding.favourite.setOnClickListener {
            if (binding.data != null) {
                val playlist = binding.data!!
                setFavorite(
                    playlist.playlistId,
                    CoreConstants.FAVORITE_PLAYLIST,
                    !playlist.favourite
                )
            }
        }

        binding.playButton.setOnClickListener {
            contentDetailsViewModel.hasPermission(mContext) {
                val list = musicAdapter.getAll()
                if (list.size != 0) {
                    contentDetailsViewModel.playTrackList(list, list[0])
                    binding.playButton.setImageResource(R.drawable.ic_pause)
                }
            }
        }

        binding.header.menu.setOnClickListener {
            if (binding.data != null) {
                contentDetailsViewModel.isPlaylistDownloaded(playlistId) {
                    PlaylistMenuDialog(mContext, binding.data!!, it, offline, this)
                }
            }
        }

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    private fun toArtist(artistId: String) {
        moveToFragmentWithBackStack(
            ArtistDetailsFragment.newInstance(
                artistId
            ),
            ArtistDetailsFragment::class.java.name,
            ArtistDetailsFragment::class.java.name
        )
    }

    private fun toAlbum(releaseId: String) {
        moveToFragmentWithBackStack(
            AlbumDetailsFragment.newInstance(
                releaseId
            ),
            AlbumDetailsFragment::class.java.name,
            AlbumDetailsFragment::class.java.name
        )
    }

    private fun toAddToPlaylist(trackId: String) {
        moveToFragmentWithBackStack(
            AddToPlaylistFragment.newInstance(
                trackId
            ),
            AddToPlaylistFragment::class.java.name,
            AddToPlaylistFragment::class.java.name
        )
    }

    private fun toEditPlaylist(playlistId: String, playlistTitle: String) {
        moveToFragmentWithBackStack(
            EditPlaylistTrackFragment.newInstance(
                playlistId, playlistTitle
            ),
            EditPlaylistTrackFragment::class.java.name,
            EditPlaylistTrackFragment::class.java.name
        )
    }

    private fun setFavorite(id: String, type: String, isFav: Boolean) {
        contentDetailsViewModel.hasPermission(mContext) {
            contentDetailsViewModel.patchFavorite(
                id,
                type,
                isFav
            )
            binding.data?.favourite = isFav
            binding.invalidateAll()
        }
    }

    override fun onTrackClick(track: Track, position: Int, isCurrentlyPlaying: Boolean) {
        contentDetailsViewModel.hasPermission(mContext) {
            if (!isRadioInitialed && contentDetailsViewModel.getPlaybackStateCompat().isPlaying && !isCurrentlyPlaying) {
                ContinuePlayingMenuDialog(mContext, track, object : ContinuePlayingMenuListener {
                    override fun onPlayNow(track: Track) {
                        contentDetailsViewModel.playTrackList(musicAdapter.getAll(), track, offline)
                    }

                    override fun onPlayNext(track: Track) {
                        contentDetailsViewModel.playTrackToNext(track, offline)
                    }

                    override fun addToQueue(track: Track) {
                        contentDetailsViewModel.addTrackToQueue(track, offline)
                    }
                })
            } else {
                contentDetailsViewModel.playTrackList(musicAdapter.getAll(), track, offline)
            }
        }
    }

    override fun onTrackMenuClick(track: Track) {
        TrackMenuDialog(mContext, track, object : TrackPopUpMenuClickListener {

            override fun onFavorite(id: String, type: String, isFav: Boolean) {
                contentDetailsViewModel.hasPermission(mContext) {
                    contentDetailsViewModel.patchFavorite(
                        id,
                        type,
                        isFav
                    )
                    musicAdapter.setFavorite(id, isFav)
                }
            }

            override fun onTrackAddToPlaylist(trackId: String) {
                contentDetailsViewModel.hasPermission(mContext) {
                    toAddToPlaylist(trackId)
                }
            }

            override fun onGoToArtist(artistId: String, artistName: String) {
                toArtist(artistId)
            }

            override fun onGotoAlbum(releaseId: String, artistId: String, artistName: String) {
                toAlbum(releaseId)
            }

            override fun onRemoveFromDownload(trackId: String) {

            }

            override fun onShareOnSocialMedia(url: String) {
                shareOnSocial(mContext, url)
            }

        }, false)
    }

    override fun onPlaylistDownload(playlistId: String, download: Boolean) {
        if (download) {
            contentDetailsViewModel.hasPermission(mContext) {
                contentDetailsViewModel.downloadPlaylist()
            }
        } else {
            contentDetailsViewModel.removePlaylist(playlistId)
            if (offline) onNavBackClick()
        }
    }

    override fun onAddToPlaylist(playlistId: String, playlistTitle: String) {
        contentDetailsViewModel.hasPermission(mContext) {
            toAddToPlaylist(musicAdapter.getTrackIds())
        }
    }

    override fun onEditPlaylist(playlistId: String, playlistTitle: String) {
        contentDetailsViewModel.hasPermission(mContext) {
            toEditPlaylist(playlistId, playlistTitle)
        }
    }

    override fun onDeletePlaylist(playlist: Playlist) {
        contentDetailsViewModel.hasPermission(mContext) {
            PlaylistDeleteDialog(mContext, playlist, object : DeletePlaylistListener {
                override fun onPlaylistDelete(id: String) {
                    contentDetailsViewModel.deletePlaylist(id)
                }
            })
        }
    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(mContext, url)
    }

}