package com.beat.view.videoPlayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerActivity
import com.beat.core.data.rest.Repository
import com.beat.core.utils.VideoPlayerResponseListener
import com.beat.core.utils.videoPlayerInstance.VideoPlayerInstance
import com.beat.data.model.Release
import com.beat.databinding.ActivityVideoPlayerBinding
import com.beat.util.*
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.activity.SupportActivity
import com.beat.view.common.CommonActivity
import com.beat.view.dialog.ReleaseMenuDialog
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import javax.inject.Inject

class VideoPlayerActivity : BaseDaggerActivity(), Player.EventListener,
    VideoPlayerResponseListener, ReleasePopUpMenuClickListener {

    private lateinit var binding: ActivityVideoPlayerBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var repository: Repository

    private lateinit var videoPlayerInstance: VideoPlayerInstance

    private lateinit var videoPlayerViewModel: VideoPlayerViewModel

//    private var sampleUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"

    private lateinit var release: Release

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        videoPlayerViewModel =
            ViewModelProvider(this, providerFactory).get(VideoPlayerViewModel::class.java)
        release = intent.getParcelableExtra("release")
        initializePlayer()
    }

    private fun initializePlayer() {
        videoPlayerInstance = VideoPlayerInstance(this, repository)
        binding.header.videoTitle.text = release.releaseTitle
        binding.videoPlayer.player = videoPlayerInstance.getPlayer()
        videoPlayerInstance.addPlayerListener(this, this)

        videoPlayerInstance.addReleaseSource(release.releaseId)

        binding.videoPlayer.setControllerVisibilityListener { visibility ->
            binding.header.topLayout.visibility = visibility
        }

        binding.header.menu.setOnClickListener {
            videoPlayerInstance.onStop(isFinishing, this, this)
            ReleaseMenuDialog(this, release, this)
        }

        binding.header.navBack.setOnClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        videoPlayerInstance.onResume()
    }

    override fun onStop() {
        super.onStop()
        videoPlayerInstance.onStop(isFinishing, this, this)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        Logger.d("", "")
    }

    override fun onVideoMediaFetchingError(errorCode: Int, errorMessage: String) {
        if (errorCode == 403) {
            moveToSubscription(this)
            finish()
        } else {
            showToast(errorMessage, Toast.LENGTH_LONG)
        }
    }

    override fun onFavorite(id: String, type: String, isFav: Boolean) {
        videoPlayerViewModel.patchFavorite(
            id,
            type,
            isFav
        )
        release.favorite = isFav
    }

    override fun onGoToArtist(artistId: String, artistName: String) {
        moveToArtistFragment(this, artistId)
    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(this, url)
    }

}