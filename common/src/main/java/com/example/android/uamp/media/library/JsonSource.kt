/*
 * Copyright 2017 Google Inc. All rights reserved.
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

package com.example.android.uamp.media.library

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat.STATUS_DOWNLOADED
import android.support.v4.media.MediaDescriptionCompat.STATUS_NOT_DOWNLOADED
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.example.android.uamp.media.extensions.album
import com.example.android.uamp.media.extensions.albumArtUri
import com.example.android.uamp.media.extensions.artist
import com.example.android.uamp.media.extensions.displayDescription
import com.example.android.uamp.media.extensions.displayIconUri
import com.example.android.uamp.media.extensions.displaySubtitle
import com.example.android.uamp.media.extensions.displayTitle
import com.example.android.uamp.media.extensions.downloadStatus
import com.example.android.uamp.media.extensions.duration
import com.example.android.uamp.media.extensions.flag
import com.example.android.uamp.media.extensions.genre
import com.example.android.uamp.media.extensions.id
import com.example.android.uamp.media.extensions.mediaUri
import com.example.android.uamp.media.extensions.title
import com.example.android.uamp.media.extensions.trackCount
import com.example.android.uamp.media.extensions.trackNumber
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Source of [MediaMetadataCompat] objects created from a basic JSON stream.
 *
 * The definition of the JSON is specified in the docs of [JsonMusic] in this file,
 * which is the object representation of it.
 */
class JsonSource : AbstractMusicSource() {

    private var catalog: List<MediaMetadataCompat> = emptyList()

    init {
        state = STATE_INITIALIZING
    }

    override fun iterator(): Iterator<MediaMetadataCompat> = catalog.iterator()

    override suspend fun load(list: List<JsonMusic>) {
        updateCatalog(list)?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            catalog = emptyList()
            state = STATE_ERROR
        }
    }

    override suspend fun newDataLoad(
        list: List<JsonMusic>,
        callback: ((list: List<MediaMetadataCompat>) -> Unit)
    ) {
        updateCatalog(list)?.let { updatedCatalog ->
            val prevList = ArrayList<MediaMetadataCompat>()
            prevList.addAll(updatedCatalog)
            catalog = prevList
            callback(updatedCatalog)
        }
    }

    override suspend fun addToNextData(
        jsonMusic: JsonMusic,
        currentPlayingPosition: Int,
        callback: ((Boolean, Int, mediaMetadataCompat: MediaMetadataCompat) -> Unit)
    ) {
        val addToQueue: Boolean
        val prevList = ArrayList<MediaMetadataCompat>()
        val mediaMetadataCompat = MediaMetadataCompat.Builder()
            .from(jsonMusic).build()
        mediaMetadataCompat.description.extras?.putAll(mediaMetadataCompat.bundle)
        catalog.toCollection(prevList)
        if (prevList.size <= currentPlayingPosition + 1 || currentPlayingPosition == -1) {
//            controller.addQueueItem(mediaMetadataCompat.description)
            addToQueue = true
            prevList.add(mediaMetadataCompat)
        } else {
            addToQueue = false
//            controller.addQueueItem(mediaMetadataCompat.description, currentPlayingPosition + 1)
            prevList.add(currentPlayingPosition + 1, mediaMetadataCompat)
        }
        catalog = prevList
        callback(addToQueue, currentPlayingPosition, mediaMetadataCompat)
    }

    override suspend fun addToQueueData(
        jsonMusic: JsonMusic,
        callback: ((mediaMetadataCompat: MediaMetadataCompat) -> Unit)
    ) {
        val prevList = ArrayList<MediaMetadataCompat>()
        val mediaMetadataCompat = MediaMetadataCompat.Builder()
            .from(jsonMusic).build()
        mediaMetadataCompat.description.extras?.putAll(mediaMetadataCompat.bundle)
//        controller.addQueueItem(mediaMetadataCompat.description)
        catalog.toCollection(prevList)
        prevList.add(mediaMetadataCompat)
        catalog = prevList
        callback(mediaMetadataCompat)
    }

    override suspend fun updateDataLoad(jsonMusic: JsonMusic, mediaId: String) {
        withContext(Dispatchers.Default) {
            val prevList = ArrayList<MediaMetadataCompat>()
            catalog.toCollection(prevList)
            prevList.forEachIndexed { index, mediaMetadataCompat ->
                if (mediaMetadataCompat.description.mediaId == mediaId) {
                    prevList[index] = MediaMetadataCompat.Builder()
                        .from(jsonMusic).build()
                }
            }
            catalog = prevList
        }
    }

    override suspend fun replaceDataLoad(list: List<JsonMusic>) {
        updateCatalog(list)?.let { updatedCatalog ->
            catalog = updatedCatalog
        }
    }

    override suspend fun removeDataLoad(controller: MediaControllerCompat, list: List<JsonMusic>) {
        val prevList = ArrayList<MediaMetadataCompat>()
        list.forEach {
            prevList.forEachIndexed { index, mediaMetadataCompat ->
                if (it.id == mediaMetadataCompat.id) {
                    controller.removeQueueItem(mediaMetadataCompat.description)
                    prevList.removeAt(index)
                }
            }
        }
        catalog = prevList
    }

    override suspend fun removeAll() {
        catalog = emptyList()
    }

    fun getCatalog(): List<MediaMetadataCompat> {
        return catalog
    }

    /**
     * Function to connect to a remote URI and download/process the JSON file that corresponds to
     * [MediaMetadataCompat] objects.
     */
    private suspend fun updateCatalog(list: List<JsonMusic>): List<MediaMetadataCompat>? {
        return withContext(Dispatchers.IO) {

            val mediaMetadataCompats = list.map { song ->
                // The JSON may have paths that are relative to the source of the JSON
                // itself. We need to fix them up here to turn them into absolute paths.
                MediaMetadataCompat.Builder()
                    .from(song)
                    .apply {
                        displayIconUri = song.image // Used by ExoPlayer and Notification
                        albumArtUri = song.image
                    }
                    .build()
            }.toList()
            // Add description keys to be used by the ExoPlayer MediaSession extension when
            // announcing metadata changes.
            mediaMetadataCompats.forEach { it.description.extras?.putAll(it.bundle) }
            mediaMetadataCompats
        }
    }

//    private suspend fun updateCatalog(catalogUri: Uri): List<MediaMetadataCompat>? {
//        return withContext(Dispatchers.IO) {
//            val musicCat = try {
//                downloadJson(catalogUri)
//            } catch (ioException: IOException) {
//                return@withContext null
//            }
//
//            // Get the base URI to fix up relative references later.
//            val baseUri = catalogUri.toString().removeSuffix(catalogUri.lastPathSegment ?: "")
//
//            val mediaMetadataCompats = musicCat.music.map { song ->
//                // The JSON may have paths that are relative to the source of the JSON
//                // itself. We need to fix them up here to turn them into absolute paths.
//                catalogUri.scheme?.let { scheme ->
//                    if (!song.source.startsWith(scheme)) {
//                        song.source = baseUri + song.source
//                    }
//                    if (!song.image.startsWith(scheme)) {
//                        song.image = baseUri + song.image
//                    }
//                }
//
//                MediaMetadataCompat.Builder()
//                    .from(song)
//                    .apply {
//                        displayIconUri = song.image // Used by ExoPlayer and Notification
//                        albumArtUri = song.image
//                    }
//                    .build()
//            }.toList()
//            // Add description keys to be used by the ExoPlayer MediaSession extension when
//            // announcing metadata changes.
//            mediaMetadataCompats.forEach { it.description.extras?.putAll(it.bundle) }
//            mediaMetadataCompats
//        }
//    }


    /**
     * Attempts to download a catalog from a given Uri.
     *
     * @param catalogUri URI to attempt to download the catalog form.
     * @return The catalog downloaded, or an empty catalog if an error occurred.
     */
    @Throws(IOException::class)
    private fun downloadJson(catalogUri: Uri): JsonCatalog {
        val catalogConn = URL(catalogUri.toString())
        val reader = BufferedReader(InputStreamReader(catalogConn.openStream()))
        return Gson().fromJson(reader, JsonCatalog::class.java)
    }
}

/**
 * Extension method for [MediaMetadataCompat.Builder] to set the fields from
 * our JSON constructed object (to make the code a bit easier to see).
 */
fun MediaMetadataCompat.Builder.from(jsonMusic: JsonMusic): MediaMetadataCompat.Builder {
    // The duration from the JSON is given in seconds, but the rest of the code works in
    // milliseconds. Here's where we convert to the proper units.
    val durationMs = TimeUnit.SECONDS.toMillis(jsonMusic.duration)

    id = jsonMusic.id
    title = jsonMusic.title
    artist = jsonMusic.artist
    album = jsonMusic.album
    duration = durationMs
    genre = jsonMusic.genre
    mediaUri = jsonMusic.source
    albumArtUri = jsonMusic.image
    trackNumber = jsonMusic.trackNumber
    trackCount = jsonMusic.totalTrackCount
    flag = MediaItem.FLAG_PLAYABLE

    // To make things easier for *displaying* these, set the display properties as well.
    displayTitle = jsonMusic.title
    displaySubtitle = jsonMusic.artist
    displayDescription = jsonMusic.mediaDetails
    displayIconUri = jsonMusic.image

    // Add downloadStatus to force the creation of an "extras" bundle in the resulting
    // MediaMetadataCompat object. This is needed to send accurate metadata to the
    // media session during updates.
    downloadStatus = if (jsonMusic.downloaded) STATUS_DOWNLOADED else STATUS_NOT_DOWNLOADED

    // Allow it to be used in the typical builder style.
    return this
}

/**
 * Wrapper object for our JSON in order to be processed easily by GSON.
 */
class JsonCatalog {
    var music: List<JsonMusic> = ArrayList()
}

/**
 * An individual piece of music included in our JSON catalog.
 * The format from the server is as specified:
 * ```
 *     { "music" : [
 *     { "title" : // Title of the piece of music
 *     "album" : // Album title of the piece of music
 *     "artist" : // Artist of the piece of music
 *     "genre" : // Primary genre of the music
 *     "source" : // Path to the music, which may be relative
 *     "image" : // Path to the art for the music, which may be relative
 *     "trackNumber" : // Track number
 *     "totalTrackCount" : // Track count
 *     "duration" : // Duration of the music in seconds
 *     "site" : // Source of the music, if applicable
 *     }
 *     ]}
 * ```
 *
 * `source` and `image` can be provided in either relative or
 * absolute paths. For example:
 * ``
 *     "source" : "https://www.example.com/music/ode_to_joy.mp3",
 *     "image" : "ode_to_joy.jpg"
 * ``
 *
 * The `source` specifies the full URI to download the piece of music from, but
 * `image` will be fetched relative to the path of the JSON file itself. This means
 * that if the JSON was at "https://www.example.com/json/music.json" then the image would be found
 * at "https://www.example.com/json/ode_to_joy.jpg".
 */
@Suppress("unused")
data class JsonMusic(
    var id: String = "",
    var title: String = "",
    var album: String = "",
    var artist: String = "",
    var genre: String = "",
    var source: String = "",
    var image: String = "",
    var trackNumber: Long = 0,
    var totalTrackCount: Long = 0,
    var duration: Long = -1,
    var site: String = "",
    var mediaDetails: String = "",
    var downloaded: Boolean = false
)
