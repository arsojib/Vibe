package com.beat.view.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.Resource
import com.beat.core.data.model.VideoBannerResponse
import com.beat.core.utils.VideoPlayerResponseListener
import com.beat.core.utils.videoPlayerInstance.VideoPlayerInstance
import com.beat.data.model.Patch
import com.beat.data.model.Release
import com.beat.databinding.VideoFragmentLayoutBinding
import com.beat.util.*
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.util.listener.VideoClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.activity.SupportActivity
import com.beat.view.adapter.VideoBannerAdapter
import com.beat.view.adapter.VideoTabAdapter
import com.beat.view.common.CommonActivity
import com.beat.view.customView.ZoomOutTransformation
import com.beat.view.dialog.ReleaseMenuDialog
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import javax.inject.Inject

class VideoFragment : BaseDaggerFragment(), VideoClickListener, Player.EventListener,
    VideoPlayerResponseListener, ReleasePopUpMenuClickListener {

    private lateinit var binding: VideoFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var videoPlayerInstance: VideoPlayerInstance

    private lateinit var mainViewModel: MainViewModel

    private var dynamicHeight: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.video_fragment_layout,
            container,
            false
        )
        mainViewModel = ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initialComponent() {
        videoPlayerInstance.addPlayerListener(this, this)
        binding.viewPager.post {
            val width: Int = binding.viewPager.width
            Log.d("Width yourView", "" + width)
            dynamicHeight = (width * 0.565).toInt()
            val params: ViewGroup.LayoutParams = binding.viewPager.layoutParams
            params.height = dynamicHeight
            binding.viewPager.layoutParams = params
            binding.videoPlayer.layoutParams = params
            binding.tabDots.setupWithViewPager(binding.viewPager, true);
            binding.viewPager.setPageTransformer(true, ZoomOutTransformation())
        }

        mainViewModel.onVideoTabChange().observe(viewLifecycleOwner, Observer<Boolean> {
            it?.let { videoTabChange ->
                if (videoTabChange && !mainViewModel.videoTabFullScreen.value!!) {
                    videoPlayerInstance.stopPlayer()
                    binding.playerLayout.visibility = View.GONE
                }
            }
        })

        mainViewModel.onVideoTabFullScreen().observe(viewLifecycleOwner, Observer<Boolean> {
            it?.let { videoTabFullScreen ->
                if (videoTabFullScreen) {
                    (mContext as AppCompatActivity).window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    (mContext as AppCompatActivity).window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    (mContext as AppCompatActivity).requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    (mContext as AppCompatActivity).window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    (mContext as AppCompatActivity).window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    (mContext as AppCompatActivity).requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        })

        mainViewModel.getVideoBannerResponse()
            .observe(viewLifecycleOwner, Observer<Resource<VideoBannerResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.bannerProgressBar.visibility = View.GONE
                            binding.viewPager.adapter = VideoBannerAdapter(
                                mContext,
                                mainViewModel.getVideoBannerFromResponse(resource.data!!),
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

        mainViewModel.getVideoPatchList().observe(viewLifecycleOwner, Observer<ArrayList<Patch>> {
            it?.let { arrayList ->
                binding.layout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                mainViewModel.videoBannerListRequest()
                binding.recyclerView.adapter =
                    VideoTabAdapter(mContext, arrayList, mainViewModel, this)
                binding.progressBar.visibility = View.GONE
            }
        })

        binding.videoPlayer.setControllerVisibilityListener { visibility ->
            binding.header.topLayout.visibility = visibility
        }

        binding.header.fullScreen.setOnClickListener {
            mainViewModel.videoTabFullScreen.value = !mainViewModel.onVideoTabFullScreen().value!!
        }

        binding.menu.setOnClickListener {
            ReleaseMenuDialog(mContext, binding.data!!, this)
        }

    }

    private fun doLayout() {
        val playerParams = binding.videoPlayer.layoutParams
        if (mainViewModel.videoTabFullScreen.value!!) {
            playerParams.width = MATCH_PARENT
            playerParams.height = MATCH_PARENT
            binding.header.fullScreen.setImageResource(R.drawable.ic_fullscreen_exit)

            binding.recyclerView.visibility = View.GONE
            binding.tabDots.visibility = View.GONE
            binding.viewPager.visibility = View.GONE
            binding.infoLayout.visibility = View.GONE
        } else {
            playerParams.width = MATCH_PARENT
            playerParams.height = dynamicHeight
            binding.header.fullScreen.setImageResource(R.drawable.ic_fullscreen)

            binding.recyclerView.visibility = View.VISIBLE
            binding.tabDots.visibility = View.VISIBLE
            binding.viewPager.visibility = View.VISIBLE
            binding.infoLayout.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            binding.videoPlayer.player =
                videoPlayerInstance.getPlayer()
            videoPlayerInstance.onResume()
        }, 500)
    }

    override fun onVideoClick(release: Release, position: Int) {
        mainViewModel.hasPermission(mContext) {

            binding.playerLayout.visibility = View.VISIBLE
            binding.data = release
            binding.header.videoTitleWithArtist.text =
                getString(R.string.title_with_artist_name, release.releaseTitle, release.artistName)
            binding.videoPlayer.player =
                videoPlayerInstance.getPlayer()
            videoPlayerInstance.addReleaseSource(release.releaseId)
            videoPlayerInstance.onResume()
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            binding.playerProgressBar.visibility = View.VISIBLE
        } else {
            binding.playerProgressBar.visibility = View.INVISIBLE
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        Logger.d("exoError", error.message)
    }

    override fun onVideoMediaFetchingError(errorCode: Int, errorMessage: String) {
        if (errorCode == 403) {
            moveToSubscription(mContext)
        } else {
            showToast(errorMessage, Toast.LENGTH_LONG)
        }
    }

    override fun onStop() {
        super.onStop()
        videoPlayerInstance.onStop((mContext as AppCompatActivity).isFinishing, this, this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        doLayout()
    }

    override fun onFavorite(id: String, type: String, isFav: Boolean) {
        mainViewModel.hasPermission(mContext) {
            mainViewModel.patchFavorite(
                id,
                type,
                isFav
            )
            binding.data?.favorite = isFav
        }
    }

    override fun onGoToArtist(artistId: String, artistName: String) {
        moveToArtistFragment(mContext, artistId)
    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(mContext, url)
    }

}
