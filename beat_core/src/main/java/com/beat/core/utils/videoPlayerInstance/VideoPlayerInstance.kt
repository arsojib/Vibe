package com.beat.core.utils.videoPlayerInstance

import android.content.Context
import com.beat.core.R
import com.beat.core.data.model.Resource
import com.beat.core.data.rest.Repository
import com.beat.core.di.scope.CoreScope
import com.beat.core.utils.VideoInitial
import com.beat.core.utils.VideoPlayerResponseListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor
import com.google.android.exoplayer2.extractor.ts.TsExtractor
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@CoreScope
class VideoPlayerInstance @Inject constructor(
    private val context: Context,
    private val repository: Repository
) {

    private var videoPlayer: SimpleExoPlayer? = null
    private val videoPlayerResponseListenerList: ArrayList<VideoPlayerResponseListener> =
        ArrayList()
    private val videoFormat = "h264/*"

    init {
//        CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL))
//        val trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory())
//        val defaultRenderersFactory = DefaultRenderersFactory(context)
//        val newSimpleInstance: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
//            context,
//            defaultRenderersFactory,
//            trackSelector,
//            DefaultLoadControl(),
//            null,
//            DefaultBandwidthMeter()
//        )
//        videoPlayer = newSimpleInstance
        TsExtractor.DEFAULT_TIMESTAMP_SEARCH_BYTES
        val extractorsFactory = DefaultExtractorsFactory()
            .setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)
        videoPlayer = SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context, extractorsFactory)).build()

//        videoPlayer?.addListener(object : Player.EventListener {
//
//        })
    }

    fun addReleaseSource(releaseId: String) {
        videoPlayer?.stop(true)
        EventBus.getDefault().post(VideoInitial(true))
        GlobalScope.launch {
            val response = async { repository.getReleaseDetailsWithTrack(releaseId) }
            val trackResponse = response.await()
            if (trackResponse.status == Resource.Status.SUCCESS) {
                val trackId: String? = trackResponse.data?.release?.tracks?.get(0)?.id
                if (!trackId.isNullOrEmpty()) {
                    val mediaResponse =
                        async { repository.getOnlineStream(trackId, videoFormat, releaseId) }
                    val mediaStreamResponse = mediaResponse.await()
                    if (mediaStreamResponse.status == Resource.Status.SUCCESS) {
                        val mediaUrl: String? = mediaStreamResponse.data?.stream?.url
                        if (!mediaUrl.isNullOrEmpty()) {
                            withContext(Dispatchers.Main) {
                                addMediaSource(mediaUrl)
                            }
                        } else {
                            emitListener(
                                0,
                                context.getString(R.string.something_went_wrong_please_try_again)
                            )
                        }
                    } else {
                        emitListener(
                            mediaStreamResponse.code ?: 0,
                            context.getString(R.string.something_went_wrong_please_try_again)
                        )
                    }
                } else {
                    emitListener(
                        0,
                        context.getString(R.string.something_went_wrong_please_try_again)
                    )
                }
            } else {
                emitListener(0, context.getString(R.string.something_went_wrong_please_try_again))
            }
        }
    }

    private fun addMediaSource(mediaUrl: String) {
//        buildMediaSource(mediaUrl)?.let { it ->
//            videoPlayer?.prepare(it)
//        }
        val mediaItem: MediaItem = MediaItem.Builder()
            .setUri(mediaUrl)
            .setMimeType(MimeTypes.VIDEO_MPEG2)
            .build()

        videoPlayer?.setMediaItem(mediaItem)
//        videoPlayer?.setMediaItem(MediaItem.fromUri(mediaUrl))
        videoPlayer?.prepare()
    }

    fun getPlayer(): SimpleExoPlayer? {
        return videoPlayer
    }

    private fun buildMediaSource(videoUrl: String): MediaSource? {
//        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSourceFactory("blvibe")
// Create a SmoothStreaming media source pointing to a manifest uri.
// Create a SmoothStreaming media source pointing to a manifest uri.
//        return SsMediaSource.Factory(dataSourceFactory)
//      .createMediaSource(Uri.parse(videoUrl))
//        val dataSourceFactory = DefaultDataSourceFactory(context, "bl-vibe")
//        val dataSourceFactory: DataSource.Factory =
//            DefaultDataSourceFactory(context, Util.getUserAgent(context, "bl-vibe"))
//        val extractorsFactory = DefaultExtractorsFactory()
//            .setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)
//            .setConstantBitrateSeekingEnabled(true)
        // This is the MediaSource representing the media to be played.
        // This is the MediaSource representing the media to be played.
//        val mediaSource: MediaSource =
//            ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
//                .createMediaSource(Uri.parse(videoUrl))
//
//        return mediaSource
//        val cacheDataSourceFactory = CacheDataSourceFactory(
//            ExoplayerCache.getInstance(context),
//            DefaultHttpDataSourceFactory("bl-vibe")
//        )
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context, "bl-vibe")
// Create a progressive media source pointing to a stream uri.
// Create a progressive media source pointing to a stream uri.
//        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//            .createMediaSource(MediaItem.fromUri(videoUrl))
//        return mediaSource
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoUrl))
//        return DashMediaSource.Factory(
//            DefaultDashChunkSource.Factory(DefaultDataSourceFactory(context, "bl-vibe")),
//            null
//        ).createMediaSource(Uri.parse(videoUrl))
//        val uriArr = arrayOf(Uri.parse(videoUrl))
//
//        return ExtractorMediaSource.Factory(
//            DefaultDataSourceFactory(
//                context,
//                DefaultHttpDataSourceFactory("bl-vibe")
//            )
//        )
//            .createMediaSource(uriArr[0])
    }


//    fun initializePlayer() {
//        if (this.player == null) {
//        val trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory())
//        val defaultRenderersFactory = DefaultRenderersFactory(context)
//        defaultRenderersFactory.setExtensionRendererMode(EXTENSION_RENDERER_MODE_OFF)
//
//        SimpleExoPlayer.Builder(context).build()
//
//        val newSimpleInstance: SimpleExoPlayer = SimpleExoPlayer.Builder(
//            context,
//            defaultRenderersFactory
//        ).build()
//        videoPlayer = newSimpleInstance
//        newSimpleInstance.addListener(PlayerEventListener())
//        this.player.setPlayWhenReady(this.shouldAutoPlay)
//        this.playerView.setPlayer(this.player)
//        this.playerView.setPlaybackPreparer(this)
//        }
//        val stream2: Stream = this.stream
//        if (stream2 != null) {
//            val uriArr = arrayOf(Uri.parse(stream2.url))
//            val strArr = arrayOf("")
//            if (!Util.maybeRequestReadExternalStoragePermission(this, uriArr)) {
//                val mediaSourceArr = arrayOfNulls<MediaSource>(1)
//                for (i in 0..0) {
//                    mediaSourceArr[i] = buildMediaSource(uriArr[i], strArr[i])
//                }
//                val mediaSource = mediaSourceArr[0]
//                val i2: Int = this.resumeWindow
//                val z = i2 != -1
//                if (z) {
//                    this.player.seekTo(i2, this.resumePosition)
//                }
//                this.player.prepare(mediaSource, true xor z, false)
//                this.inErrorState = false
//            }
//        }
//    }
//
//    private fun buildMediaSource(uri: Uri, str: String): MediaSource? {
//        val i: Int
//        i = if (TextUtils.isEmpty(str)) {
//            Util.inferContentType(uri)
//        } else {
//            Util.inferContentType(".$str")
//        }
//        if (i == 0) {
//            return DashMediaSource.Factory(
//                DefaultDashChunkSource.Factory(DefaultDataSourceFactory(context, "bl-vibe")),
//                null
//            ).createMediaSource(uri)
//        }
//        if (i == 3) {
//            return ExtractorMediaSource.Factory(this.mediaDataSourceFactory).createMediaSource(uri)
//        }
//        throw IllegalStateException("Unsupported type: $i")
//    }


    fun addPlayerListener(
        eventListener: Player.EventListener,
        videoPlayerResponseListener: VideoPlayerResponseListener
    ) {
        videoPlayer?.addListener(eventListener)
        videoPlayerResponseListenerList.add(videoPlayerResponseListener)
    }

    private fun removePlayerResponseListener(
        eventListener: Player.EventListener,
        videoPlayerResponseListener: VideoPlayerResponseListener
    ) {
        videoPlayer?.removeListener(eventListener)
        videoPlayerResponseListenerList.forEachIndexed { index, listener ->
            if (listener == videoPlayerResponseListener) {
                videoPlayerResponseListenerList.removeAt(index)
                return
            }
        }
    }

    private suspend fun emitListener(errorCode: Int, errorMessage: String) {
        withContext(Dispatchers.Main) {
            videoPlayerResponseListenerList.forEach { videoPlayerResponseListener ->
                videoPlayerResponseListener.onVideoMediaFetchingError(errorCode, errorMessage)
            }
        }
    }

    fun onResume() {
        videoPlayer?.playWhenReady = true
    }

    fun onStop(
        isFinishing: Boolean,
        eventListener: Player.EventListener,
        videoPlayerResponseListener: VideoPlayerResponseListener
    ) {
        videoPlayer?.playWhenReady = false
        if (isFinishing) {
            removePlayerResponseListener(eventListener, videoPlayerResponseListener)
            stopPlayer()
        }
    }

    fun stopPlayer() {
        videoPlayer?.stop()
    }

    fun releasePlayer() {
        videoPlayer?.release()
    }

}