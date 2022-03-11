package com.beat.view.content.details

import android.graphics.drawable.Drawable
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
import com.beat.core.data.model.TopReleaseByArtistResponse
import com.beat.core.data.model.TopTrackByArtistResponse
import com.beat.core.data.model.TopVideoByArtistResponse
import com.beat.core.utils.CoreConstants
import com.beat.data.model.Artist
import com.beat.data.model.Release
import com.beat.data.model.Track
import com.beat.databinding.ArtistDetailsFragmentLayoutBinding
import com.beat.util.Constants
import com.beat.util.isRadioInitialed
import com.beat.util.listener.ContinuePlayingMenuListener
import com.beat.util.listener.ReleaseClickListener
import com.beat.util.listener.TrackClickListener
import com.beat.util.listener.TrackPopUpMenuClickListener
import com.beat.util.moveToVideoPlayerActivity
import com.beat.util.shareOnSocial
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.MusicAdapter3
import com.beat.view.adapter.SimilarReleaseAdapter
import com.beat.view.adapter.SimilarVideoAdapter
import com.beat.view.common.userPlaylist.AddToPlaylistFragment
import com.beat.view.dialog.ContinuePlayingMenuDialog
import com.beat.view.dialog.TrackMenuDialog
import com.example.android.uamp.media.extensions.isPlaying
import javax.inject.Inject

class ArtistDetailsFragment : BaseDaggerFragment(), ReleaseClickListener, TrackClickListener {

    private lateinit var binding: ArtistDetailsFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var contentDetailsViewModel: ContentDetailsViewModel
    private lateinit var musicAdapter: MusicAdapter3

    private lateinit var artistId: String
    private lateinit var artistName: String

    companion object {
        fun newInstance(artistId: String?): ArtistDetailsFragment {
            val fragment = ArtistDetailsFragment()
            val bundle = Bundle()
            bundle.putString("artistId", artistId)
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
                R.layout.artist_details_fragment_layout,
                container,
                false
            )
        if (!::contentDetailsViewModel.isInitialized) contentDetailsViewModel =
            ViewModelProvider(this, providerFactory).get(ContentDetailsViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        artistId = arguments?.getString("artistId", "")!!

        initialComponent()
    }

    private fun initialComponent() {
//        binding.header.title.text = artistName
        binding.header.title.text = getString(R.string.loading)
        musicAdapter = MusicAdapter3(mContext, viewLifecycleOwner, contentDetailsViewModel, this)
        binding.songsRecyclerView.adapter = musicAdapter

        contentDetailsViewModel.getGradientDrawable()
            .observe(viewLifecycleOwner, Observer<Drawable> {
                it?.let { drawable ->
                    binding.topLayout.background = drawable
                }
            })

        contentDetailsViewModel.getArtistDetailsResponse()
            .observe(viewLifecycleOwner, Observer<Resource<Artist>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.refreshLayout.isRefreshing = false
                            binding.layout.visibility = View.VISIBLE
                            binding.data = resource.data
                            artistName = resource.data?.artistTitle!!
                            binding.header.title.text = artistName
                            contentDetailsViewModel.setImage(
                                binding.artistImage,
                                binding.data?.bannerImage,
                                resources.getDrawable(R.drawable.ic_ph_artist, null)
                            )
                            binding.run {
                                bio.setShowingLine(3);
                                bio.addShowMoreText(getString(R.string.see_more))
                                bio.addShowLessText("")
                            }
                        }
                        Resource.Status.ERROR -> {
                            binding.refreshLayout.isRefreshing = false
                            binding.layout.visibility = View.GONE
                        }
                        Resource.Status.LOADING -> {
                            binding.refreshLayout.isRefreshing = true
                        }
                    }
                }
            })

        contentDetailsViewModel.getTopTrackByArtistResponse()
            .observe(viewLifecycleOwner, Observer<Resource<TopTrackByArtistResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.topSongLayout.visibility = View.VISIBLE
                            musicAdapter.addAll(
                                contentDetailsViewModel.getTrackListFromArtistResponse(
                                    resource.data!!
                                )
                            )
                        }
                        Resource.Status.ERROR -> {
                            binding.topSongLayout.visibility = View.GONE
                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        contentDetailsViewModel.getTopReleaseByTypeByArtistResponse()
            .observe(viewLifecycleOwner, Observer<Resource<TopReleaseByArtistResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.topReleaseLayout.visibility = View.VISIBLE
                            binding.topReleaseRecyclerView.adapter = SimilarReleaseAdapter(
                                mContext,
                                contentDetailsViewModel.getReleaseListFromResponse(resource.data!!),
                                this
                            )
                        }
                        Resource.Status.ERROR -> {

                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        contentDetailsViewModel.getTopVideoByArtistResponse()
            .observe(viewLifecycleOwner, Observer<Resource<TopVideoByArtistResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.topVideosLayout.visibility = View.VISIBLE
                            binding.topVideosRecyclerView.adapter = SimilarVideoAdapter(
                                mContext,
                                contentDetailsViewModel.getReleaseListFromVideoResponse(resource.data!!)
                            )
                        }
                        Resource.Status.ERROR -> {

                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        contentDetailsViewModel.getNowPlayingMediaItem()
            .observe(viewLifecycleOwner, Observer<Track> {
                musicAdapter.setCurrentPlayingItem(it)
            })

        contentDetailsViewModel.fetchCurrentPlayingItem()
        contentDetailsViewModel.getArtistDetailsContent(false, artistId)

        binding.refreshLayout.setOnRefreshListener {
            contentDetailsViewModel.getArtistDetailsContent(true, artistId)
        }

        binding.favourite.setOnClickListener {
            if (binding.data != null) {
                val artist = binding.data!!
                setFavorite(
                    artist.artistId,
                    CoreConstants.FAVORITE_PLAYLIST,
                    !artist.favourite
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

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    private fun toArtist(artistId: String) {
        moveToFragmentWithBackStack(
            newInstance(
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

    override fun onReleaseClick(release: Release, position: Int) {
        when (release.type) {
            Constants.ALBUM -> {
                toAlbum(release.releaseId)
            }
            Constants.VIDEO_SINGLE -> {
                moveToVideoPlayerActivity(mContext, release)
            }
        }
    }

    override fun onTrackClick(track: Track, position: Int, isCurrentlyPlaying: Boolean) {
        contentDetailsViewModel.hasPermission(mContext) {
            if (!isRadioInitialed && contentDetailsViewModel.getPlaybackStateCompat().isPlaying && !isCurrentlyPlaying) {
                ContinuePlayingMenuDialog(mContext, track, object : ContinuePlayingMenuListener {
                    override fun onPlayNow(track: Track) {
                        contentDetailsViewModel.playTrackList(musicAdapter.getAll(), track)
                    }

                    override fun onPlayNext(track: Track) {
                        contentDetailsViewModel.playTrackToNext(track)
                    }

                    override fun addToQueue(track: Track) {
                        contentDetailsViewModel.addTrackToQueue(track)
                    }
                })
            } else {
                contentDetailsViewModel.playTrackList(musicAdapter.getAll(), track)
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

}