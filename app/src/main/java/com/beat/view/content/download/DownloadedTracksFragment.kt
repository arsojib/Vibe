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
import com.beat.data.model.Track
import com.beat.databinding.TracksFragmentLayoutBinding
import com.beat.util.*
import com.beat.util.listener.TrackClickListener
import com.beat.util.listener.TrackPopUpMenuClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.DownloadedTrackAdapter
import com.beat.view.common.userPlaylist.AddToPlaylistFragment
import com.beat.view.content.details.AlbumDetailsFragment
import com.beat.view.content.details.ArtistDetailsFragment
import com.beat.view.dialog.TrackMenuDialog
import javax.inject.Inject

class DownloadedTracksFragment : BaseDaggerFragment(), TrackClickListener,
    TrackPopUpMenuClickListener {

    private lateinit var binding: TracksFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory
    private lateinit var downloadViewModel: DownloadViewModel
    private lateinit var downloadedTrackAdapter: DownloadedTrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.tracks_fragment_layout, container, false)
        downloadViewModel =
            ViewModelProvider(this, providerFactory).get(DownloadViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        downloadedTrackAdapter =
            DownloadedTrackAdapter(mContext, viewLifecycleOwner, downloadViewModel, this)
        binding.recyclerView.adapter = downloadedTrackAdapter

        downloadViewModel.getTrackList()
            .observe(viewLifecycleOwner, Observer<Resource<List<Track>>> {
                it?.let {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.GONE
                            downloadedTrackAdapter.addAll(it.data!!)
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
            downloadViewModel.fetchTrackList()
        }, viewLifecycleOwner)

        downloadViewModel.fetchTrackList()
    }

    private fun toArtist(artistId: String, artistName: String) {
        moveToFragmentWithBackStack(
            ArtistDetailsFragment.newInstance(
                artistId
            ),
            ArtistDetailsFragment::class.java.name,
            ArtistDetailsFragment::class.java.name
        )
    }

    private fun toAlbum(releaseId: String, artistId: String, artistName: String) {
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

    override fun onTrackClick(track: Track, position: Int, isCurrentlyPlaying: Boolean) {
//        if (!isRadioInitialed && contentDetailsViewModel.getPlaybackStateCompat().isPlaying && !isCurrentlyPlaying) {
//            ContinuePlayingMenuDialog(mContext, track, object : ContinuePlayingMenuListener {
//                override fun onPlayNow(track: Track) {
//                    contentDetailsViewModel.playTrackList(musicAdapter.getAll(), track)
//                }
//
//                override fun onPlayNext(track: Track) {
//                    contentDetailsViewModel.playTrackToNext(track)
//                }
//
//                override fun addToQueue(track: Track) {
//                    contentDetailsViewModel.addTrackToQueue(track)
//                }
//            })
//        } else {
        downloadViewModel.playTrackList(downloadedTrackAdapter.getAll(), track)
//        }
    }

    override fun onTrackMenuClick(track: Track) {
        TrackMenuDialog(mContext, track, this, true)
    }

    override fun onFavorite(id: String, type: String, isFav: Boolean) {
        downloadViewModel.hasPermission(mContext) {
            downloadViewModel.patchFavorite(
                id,
                type,
                isFav
            )
            downloadedTrackAdapter.setFavorite(id, isFav)
        }
    }

    override fun onTrackAddToPlaylist(trackId: String) {
//        moveToAddToPlaylistFragment(mContext, trackId)
        downloadViewModel.hasPermission(mContext) {
            toAddToPlaylist(trackId)
        }
    }

    override fun onGoToArtist(artistId: String, artistName: String) {
//        moveToArtistFragment(mContext, artistId, artistName)
        toArtist(artistId, artistName)
    }

    override fun onGotoAlbum(releaseId: String, artistId: String, artistName: String) {
//        moveToReleaseFragment(mContext, releaseId, artistId, artistName)
        toAlbum(releaseId, artistId, artistName)
    }

    override fun onRemoveFromDownload(trackId: String) {
        downloadedTrackAdapter.removeById(trackId)
        downloadViewModel.removeTrack(trackId)
    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(mContext, url)
    }

}