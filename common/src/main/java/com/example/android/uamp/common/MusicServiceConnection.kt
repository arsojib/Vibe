/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.uamp.common

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.media.MediaBrowserServiceCompat
import com.example.android.uamp.common.MusicServiceConnection.MediaBrowserConnectionCallback
import com.example.android.uamp.media.NETWORK_FAILURE
import com.example.android.uamp.media.extensions.id
import com.example.android.uamp.media.library.JsonMusic
import com.example.android.uamp.media.library.JsonSource
import com.example.android.uamp.media.listener.MediaUpdateListener
import com.example.android.uamp.media.listener.event.PlayerInitial
import com.example.android.uamp.media.listener.event.RadioInitial
import com.example.android.uamp.rest.PlayerApiService
import com.example.android.uamp.rest.getRetrofit
import com.example.android.uamp.rest.model.StreamResponse
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import retrofit2.Response

/**
 * Class that manages a connection to a [MediaBrowserServiceCompat] instance, typically a
 * [MusicService] or one of its subclasses.
 *
 * Typically it's best to construct/inject dependencies either using DI or, as UAMP does,
 * using [InjectorUtils] in the app module. There are a few difficulties for that here:
 * - [MediaBrowserCompat] is a final class, so mocking it directly is difficult.
 * - A [MediaBrowserConnectionCallback] is a parameter into the construction of
 *   a [MediaBrowserCompat], and provides callbacks to this class.
 * - [MediaBrowserCompat.ConnectionCallback.onConnected] is the best place to construct
 *   a [MediaControllerCompat] that will be used to control the [MediaSessionCompat].
 *
 *  Because of these reasons, rather than constructing additional classes, this is treated as
 *  a black box (which is why there's very little logic here).
 *
 *  This is also why the parameters to construct a [MusicServiceConnection] are simple
 *  parameters, rather than private properties. They're only required to build the
 *  [MediaBrowserConnectionCallback] and [MediaBrowserCompat] objects.
 */
class MusicServiceConnection(
    context: Context,
    serviceComponent: ComponentName,
    baseUrl: String,
    mAuth: String,
    mAudioFormat: String
) {

    var jsonSource = JsonSource()
    var mediaUpdateListener: MediaUpdateListener? = null

    private val playerApiService = getRetrofit(baseUrl).create(PlayerApiService::class.java)

    val isConnected = MutableLiveData<Boolean>()
        .apply { postValue(false) }
    val networkFailure = MutableLiveData<Boolean>()
        .apply { postValue(false) }

    val rootMediaId: String get() = mediaBrowser.root

    val playbackState = MutableLiveData<PlaybackStateCompat>()
        .apply { postValue(EMPTY_PLAYBACK_STATE) }
    val nowPlaying = MutableLiveData<MediaMetadataCompat>()
        .apply { postValue(NOTHING_PLAYING) }
    val shuffleMode = MutableLiveData<Int>()
        .apply { postValue(DEFAULT_SHUFFLE_MODE) }
    val repeatMode = MutableLiveData<Int>()
        .apply { postValue(DEFAULT_REPEAT_MODE) }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private var player: Player? = null

    private var auth: String = mAuth
    private var audioFormat: String = mAudioFormat

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        mediaBrowserConnectionCallback, null
    ).apply { connect() }
    private lateinit var mediaController: MediaControllerCompat

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    fun sendCommand(command: String, parameters: Bundle?) =
        sendCommand(command, parameters) { _, _ -> }

    fun sendCommand(
        command: String,
        parameters: Bundle?,
        resultCallback: ((Int, Bundle?) -> Unit)
    ) = if (mediaBrowser.isConnected) {
        mediaController.sendCommand(command, parameters, object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                resultCallback(resultCode, resultData)
            }
        })
        true
    } else {
        false
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        /**
         * Invoked after [MediaBrowserCompat.connect] when the request has successfully
         * completed.
         */
        override fun onConnected() {
            // Get a MediaController for the MediaSession.
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            isConnected.postValue(true)
        }

        /**
         * Invoked when the client is disconnected from the media browser.
         */
        override fun onConnectionSuspended() {
            isConnected.postValue(false)
        }

        /**
         * Invoked when the connection to the media browser failed.
         */
        override fun onConnectionFailed() {
            isConnected.postValue(false)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onShuffleModeChanged(mode: Int) {
            shuffleMode.postValue(mode)
        }

        override fun onRepeatModeChanged(mode: Int) {
            repeatMode.postValue(mode)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            // When ExoPlayer stops we will receive a callback with "empty" metadata. This is a
            // metadata object which has been instantiated with default values. The default value
            // for media ID is null so we assume that if this value is null we are not playing
            // anything.
            nowPlaying.postValue(
                if (metadata?.id == null) {
                    NOTHING_PLAYING
                } else {
                    metadata
                }
            )
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                NETWORK_FAILURE -> networkFailure.postValue(true)
            }
        }

        /**
         * Normally if a [MediaBrowserServiceCompat] drops its connection the callback comes via
         * [MediaControllerCompat.Callback] (here). But since other connection status events
         * are sent to [MediaBrowserCompat.ConnectionCallback], we catch the disconnect here and
         * send it on to the other callback.
         */
        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: MusicServiceConnection? = null

        fun getInstance(
            context: Context,
            serviceComponent: ComponentName,
            baseUrl: String,
            auth: String,
            format: String
        ) =
            instance ?: synchronized(this) {
                instance ?: MusicServiceConnection(context, serviceComponent, baseUrl, auth, format)
                    .also {
                        instance = it
                    }
            }
    }

    fun addRadioMediaToPlayer(
        isPlayerInitial: Boolean,
        list: List<JsonMusic>,
        nowPlay: Boolean,
        mediaId: String
    ) {
        serviceScope.launch {
            if (jsonSource.getCatalog().isEmpty()) {
                jsonSource.load(list)
                EventBus.getDefault().post(RadioInitial(true))
            } else {
                jsonSource.newDataLoad(list) {
                    mediaUpdateListener?.onMediaAdded(parentId, it)
                }
                if (isPlayerInitial) EventBus.getDefault().post(RadioInitial(true))
            }
            if (nowPlay) transportControls.playFromMediaId(mediaId, null)
        }
    }

    fun addMediaToPlayer(
        isRadioInitial: Boolean,
        list: List<JsonMusic>,
        nowPlay: Boolean,
        mediaId: String
    ) {
        serviceScope.launch {
            if (jsonSource.getCatalog().isEmpty()) {
                jsonSource.load(list)
                EventBus.getDefault().post(PlayerInitial(true))
            } else {
                jsonSource.newDataLoad(list) {
                    mediaUpdateListener?.onMediaAdded(parentId, it)
                }
                if (isRadioInitial) EventBus.getDefault().post(PlayerInitial(true))
            }
            if (nowPlay) transportControls.playFromMediaId(mediaId, null)
        }
    }

    fun addNextMediaToPlayer(jsonMusic: JsonMusic, nowPlay: Boolean) {
        serviceScope.launch {
            val currentPosition = player?.currentWindowIndex ?: -1
            jsonSource.addToNextData(
                jsonMusic,
                currentPosition
            ) { addToQueue, position, mediaItem ->
                if (addToQueue) {
                    mediaUpdateListener?.addToQueue(parentId, mediaItem)
                } else {
                    mediaUpdateListener?.addToNext(parentId, mediaItem, position)
                }
            }
//            jsonSource.addToNextData(mediaController, jsonMusic, currentPosition)
//            mediaUpdateListener?.onMediaAdded(parentId)
            if (nowPlay) transportControls.playFromMediaId(jsonMusic.id, null)
        }
    }

    fun addQueueMediaToPlayer(jsonMusic: JsonMusic) {
        serviceScope.launch {
            jsonSource.addToQueueData(jsonMusic) { mediaItem ->
                mediaUpdateListener?.addToQueue(parentId, mediaItem)
            }
//            mediaUpdateListener?.onMediaAdded(parentId)
        }
    }

    fun replaceDataLoad(list: List<JsonMusic>, mediaId: String) {
        serviceScope.launch {
            jsonSource.replaceDataLoad(list)
            mediaUpdateListener?.onMediaRemoved(parentId)
            transportControls.playFromMediaId(mediaId, null)
        }
    }

    fun setAuth(auth: String) {
        this.auth = auth
    }

    fun setAudioFormat(format: String) {
        this.audioFormat = format
    }

    fun setPlayer(player: Player) {
        this.player = player
    }

    private fun getAuth(): String {
        return auth
    }

    private fun getFormat(): String {
        return audioFormat
    }

    fun setFavorite(jsonMusic: JsonMusic, mediaId: String) {
        serviceScope.launch {
            jsonSource.updateDataLoad(jsonMusic, mediaId)
        }
    }

    suspend fun getStreamingUrl(trackId: String): Response<StreamResponse> {
        return withContext(Dispatchers.IO) {
            playerApiService.getOnlineStream(
                trackId,
                getAuth(),
                getFormat()
            )
        }
    }

}

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

@Suppress("PropertyName")
val DEFAULT_SHUFFLE_MODE: Int = PlaybackStateCompat.SHUFFLE_MODE_NONE

@Suppress("PropertyName")
val DEFAULT_REPEAT_MODE: Int = PlaybackStateCompat.REPEAT_MODE_NONE

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()

const val parentId = "vibe"