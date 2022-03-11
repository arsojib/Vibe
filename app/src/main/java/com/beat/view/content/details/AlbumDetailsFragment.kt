package com.beat.view.content.details

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
import com.beat.core.data.model.ReleaseDetailsWithTrackResponse
import com.beat.core.data.model.Resource
import com.beat.core.data.model.TopReleaseByArtistResponse
import com.beat.core.utils.CoreConstants
import com.beat.data.model.Release
import com.beat.data.model.Track
import com.beat.databinding.AlbumDetailsFragmentLayoutBinding
import com.beat.util.Constants
import com.beat.util.isRadioInitialed
import com.beat.util.listener.*
import com.beat.util.moveToVideoPlayerActivity
import com.beat.util.shareOnSocial
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.MusicAdapter2
import com.beat.view.adapter.SimilarReleaseAdapter
import com.beat.view.common.userPlaylist.AddToPlaylistFragment
import com.beat.view.dialog.ContinuePlayingMenuDialog
import com.beat.view.dialog.ReleaseMenuDialog
import com.beat.view.dialog.TrackMenuDialog
import com.example.android.uamp.media.extensions.isPlaying
import javax.inject.Inject

class AlbumDetailsFragment : BaseDaggerFragment(), ReleaseClickListener, TrackClickListener,
    ReleasePopUpMenuClickListener {

    private lateinit var binding: AlbumDetailsFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var contentDetailsViewModel: ContentDetailsViewModel
    private lateinit var musicAdapter: MusicAdapter2

    private lateinit var releaseId: String
    private lateinit var artistId: String
    private lateinit var artistName: String

    companion object {
        fun newInstance(
            releaseId: String?
        ): AlbumDetailsFragment {
            val fragment = AlbumDetailsFragment()
            val bundle = Bundle()
            bundle.putString("releaseId", releaseId)
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
                R.layout.album_details_fragment_layout,
                container,
                false
            )
        if (!::contentDetailsViewModel.isInitialized) contentDetailsViewModel =
            ViewModelProvider(this, providerFactory).get(ContentDetailsViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        releaseId = arguments?.getString("releaseId", "")!!

        initialComponent()
    }

    private fun initialComponent() {
//        binding.header.title.text = artistName
        binding.header.title.text = getString(R.string.loading)
        musicAdapter = MusicAdapter2(mContext, this)
        binding.songsRecyclerView.adapter = musicAdapter

        contentDetailsViewModel.getGradientDrawable().observe(viewLifecycleOwner,
            Observer<Drawable> { drawable -> binding.topLayout.background = drawable })

        contentDetailsViewModel.getReleaseDetailsWithTrackResponse().observe(viewLifecycleOwner,
            Observer<Resource<ReleaseDetailsWithTrackResponse>> { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        binding.refreshLayout.isRefreshing = false
                        binding.layout.visibility = View.VISIBLE
                        val release =
                            contentDetailsViewModel.getReleaseFromResponse(resource.data!!)
                        artistId = release?.artistId!!
                        artistName = release?.artistName!!
                        contentDetailsViewModel.getArtistsTopRelease(artistId)
                        binding.data = release
                        binding.header.title.text = artistName
                        contentDetailsViewModel.setImage(
                            binding.releaseImage,
                            binding.data?.releaseImage,
                            resources.getDrawable(R.drawable.ic_ph_album, null)
                        )
                        musicAdapter.addAll(
                            contentDetailsViewModel.getTrackListFromResponse(
                                resource.data!!
                            )
                        )
                    }
                    Resource.Status.ERROR -> {
                        binding.layout.visibility = View.GONE
                        binding.refreshLayout.isRefreshing = false
                    }
                    Resource.Status.LOADING -> {
                        binding.refreshLayout.isRefreshing = true
                    }
                }
            })

        contentDetailsViewModel.getTopReleaseByArtistResponse().observe(
            viewLifecycleOwner,
            Observer<Resource<TopReleaseByArtistResponse>> { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        binding.similarLayout.visibility = View.VISIBLE
                        binding.similarRecyclerView.adapter = SimilarReleaseAdapter(
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
            })

        contentDetailsViewModel.getNowPlayingMediaItem()
            .observe(viewLifecycleOwner, Observer<Track> {
                musicAdapter.setCurrentPlayingItem(it)
            })

        contentDetailsViewModel.fetchCurrentPlayingItem()
        contentDetailsViewModel.getAlbumDetailsContent(false, releaseId)

        binding.refreshLayout.setOnRefreshListener {
            contentDetailsViewModel.getAlbumDetailsContent(true, releaseId)
        }

        binding.favourite.setOnClickListener {
            if (binding.data != null) {
                val release = binding.data!!
                setFavorite(
                    release.releaseId,
                    CoreConstants.FAVORITE_RELEASE,
                    !release.favorite
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
            if (binding.data != null)
                ReleaseMenuDialog(mContext, binding.data!!, this)
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
            newInstance(
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
            binding.data?.favorite = isFav
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

    override fun onFavorite(id: String, type: String, isFav: Boolean) {
        setFavorite(id, type, isFav)
    }

    override fun onGoToArtist(artistId: String, artistName: String) {
        toArtist(artistId)
    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(mContext, url)
    }

}