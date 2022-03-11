package com.beat.view.content.favourite

import android.app.ProgressDialog
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
import com.beat.data.model.Alert
import com.beat.data.model.Patch
import com.beat.data.model.Playlist
import com.beat.data.model.Track
import com.beat.databinding.FavoriteAudioFragmentLayoutBinding
import com.beat.util.*
import com.beat.util.listener.*
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.FavoriteAudioTabAdapter
import com.beat.view.common.userPlaylist.AddToPlaylistFragment
import com.beat.view.content.details.AlbumDetailsFragment
import com.beat.view.content.details.ArtistDetailsFragment
import com.beat.view.dialog.ContinuePlayingMenuDialog
import com.beat.view.dialog.PlaylistDeleteDialog
import com.beat.view.dialog.PlaylistMenuDialog
import com.example.android.uamp.media.extensions.isPlaying
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class FavoriteAudioFragment : BaseDaggerFragment(), TrackPopUpMenuClickListener,
    ReleasePopUpMenuClickListener, PlaylistPopUpMenuClickListener, TrackClickListener,
    PlaylistMenuClickListener {

    private lateinit var binding: FavoriteAudioFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var favoriteAudioTabAdapter: FavoriteAudioTabAdapter

    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.favorite_audio_fragment_layout,
            container,
            false
        )
        favoriteViewModel =
            ViewModelProvider(this, providerFactory).get(FavoriteViewModel::class.java)
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
        favoriteAudioTabAdapter = FavoriteAudioTabAdapter(
            mContext,
            this, this, this, this
        )
        binding.recyclerView.adapter = favoriteAudioTabAdapter

        favoriteViewModel.getFavoriteAudioPatch()
            .observe(viewLifecycleOwner, Observer<Resource<ArrayList<Patch>>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.GONE
                            favoriteAudioTabAdapter.addAll(resource.data!!)
                        }
                        Resource.Status.ERROR -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.VISIBLE
                            binding.alertLayout.data =
                                Alert(
                                    getString(R.string.no_favorite_audios),
                                    getString(R.string.no_favorite_demo_text),
                                    resources.getDrawable(R.drawable.ic_ph_album, null)
                                )
                        }
                        Resource.Status.LOADING -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.alertLayout.layout.visibility = View.GONE
                        }
                    }
                }
            })

        favoriteViewModel.getDeletePlaylistResponse()
            .observe(viewLifecycleOwner, Observer<Resource<String>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                            favoriteAudioTabAdapter.addAll(ArrayList())
                            favoriteViewModel.getFavoriteAudios(mContext)
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

        favoriteViewModel.getNowPlayingMediaItem()
            .observe(viewLifecycleOwner, Observer<Track> {
                favoriteAudioTabAdapter.setCurrentPlayingItem(it)
            })

        favoriteViewModel.fetchCurrentPlayingItem()
        favoriteViewModel.getFavoriteAudios(mContext)
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

    override fun onFavorite(id: String, type: String, isFav: Boolean) {
        favoriteViewModel.hasPermission(mContext) {
            favoriteViewModel.patchFavorite(id, type, isFav)
            GlobalScope.launch {
                favoriteAudioTabAdapter.setFavorite(id, type, isFav)
            }
        }
    }

    override fun onTrackAddToPlaylist(trackId: String) {
        favoriteViewModel.hasPermission(mContext) {
//            moveToAddToPlaylistFragment(mContext, trackId)
            toAddToPlaylist(trackId)
        }
    }

    override fun onGoToArtist(artistId: String, artistName: String) {
//        moveToArtistFragment(mContext, artistId, artistName)
        toArtist(artistId, artistName)
    }

    override fun onGotoAlbum(releaseId: String, artistId: String, artistName: String) {
//        moveToReleaseFragment(mContext, releaseId, artistId, artistName)
        toAlbum(releaseId)
    }

    override fun onRemoveFromDownload(trackId: String) {

    }

    override fun onPlaylistDownload(playlistId: String, download: Boolean) {
        favoriteViewModel.hasPermission(mContext) {
            if (download) {
                favoriteViewModel.downloadPlaylist(playlistId)
            } else {
                favoriteViewModel.removePlaylist(playlistId)
            }
        }
    }

    override fun onAddToPlaylist(playlistId: String, playlistTitle: String) {
        favoriteViewModel.hasPermission(mContext) {
            moveToPlaylistFragment(
                mContext, playlistId,
                addToPlaylist = true,
                offline = false
            )
        }
    }

    override fun onEditPlaylist(playlistId: String, playlistTitle: String) {
        favoriteViewModel.hasPermission(mContext) {
            moveToEditPlaylistTrackFragment(mContext, playlistId, playlistTitle)
        }
    }

    override fun onDeletePlaylist(playlist: Playlist) {
        favoriteViewModel.hasPermission(mContext) {
            PlaylistDeleteDialog(mContext, playlist, object : DeletePlaylistListener {
                override fun onPlaylistDelete(id: String) {
                    favoriteViewModel.deletePlaylist(id)
                }
            })
        }
    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(mContext, url)
    }

    override fun onTrackClick(track: Track, position: Int, isCurrentlyPlaying: Boolean) {
        favoriteViewModel.hasPermission(mContext) {
            if (!isRadioInitialed && favoriteViewModel.getPlaybackStateCompat().isPlaying && !isCurrentlyPlaying) {
                ContinuePlayingMenuDialog(mContext, track, object : ContinuePlayingMenuListener {
                    override fun onPlayNow(track: Track) {
                        favoriteViewModel.playTrackToNext(track, true)
                    }

                    override fun onPlayNext(track: Track) {
                        favoriteViewModel.playTrackToNext(track, false)
                    }

                    override fun addToQueue(track: Track) {
                        favoriteViewModel.addTrackToQueue(track)
                    }
                })
            } else {
                favoriteViewModel.playTrack(track)
            }
        }
    }

    override fun onTrackMenuClick(track: Track) {
        //No Action Needed
        //Already served by other listener
    }

    override fun onMenuClick(playlist: Playlist) {
        favoriteViewModel.isPlaylistDownloaded(playlist.playlistId) {
            PlaylistMenuDialog(
                mContext, playlist,
                isDownloaded = it,
                isOffline = false,
                playlistPopUpMenuClickListener = this
            )
        }
    }

}