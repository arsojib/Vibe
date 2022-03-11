package com.beat.view.videoPlayer

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.beat.R
import com.beat.base.BaseDaggerActivity
import com.beat.core.utils.VideoPlayerResponseListener
import com.beat.core.utils.videoPlayerInstance.VideoPlayerInstance
import com.beat.databinding.ActivityFullScreenVideoPlayerBinding
import com.beat.util.Logger
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import javax.inject.Inject

class FullScreenVideoPlayerActivity : BaseDaggerActivity(), Player.EventListener,
    VideoPlayerResponseListener {

    private lateinit var binding: ActivityFullScreenVideoPlayerBinding

    @Inject
    lateinit var videoPlayerInstance: VideoPlayerInstance

    private lateinit var videoTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_screen_video_player)
        videoTitle = intent.getStringExtra("videoTitle") ?: ""
        initializePlayer()
    }

    private fun initializePlayer() {
        binding.header.videoTitleWithArtist.text = videoTitle
        binding.header.fullScreen.setImageResource(R.drawable.ic_fullscreen_exit)
        binding.videoPlayer.player = videoPlayerInstance.getPlayer()
        videoPlayerInstance.addPlayerListener(this, this)

        Handler().postDelayed({
            videoPlayerInstance.onResume()
        }, 500)

        binding.videoPlayer.setControllerVisibilityListener { visibility ->
            binding.header.topLayout.visibility = visibility
        }

        binding.header.fullScreen.setOnClickListener {
            finish()
        }
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
        showToast(errorMessage, Toast.LENGTH_LONG)
    }

}
