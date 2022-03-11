package com.beat.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.beat.R
import com.beat.data.model.Release
import com.beat.data.model.Track
import com.beat.view.common.CommonActivity
import com.beat.view.content.ContentActivity
import com.beat.view.videoPlayer.FullScreenVideoPlayerActivity
import com.beat.view.videoPlayer.VideoPlayerActivity
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.common.parentId
import com.example.android.uamp.media.MusicService
import com.example.android.uamp.media.library.JsonMusic
import com.google.gson.Gson

//Global uses. don't change it manually.
var isPlayerInitialed = false
var isRadioInitialed = false

fun provideMusicServiceConnection(
    context: Context,
    auth: String,
    format: String
): MusicServiceConnection {
    return MusicServiceConnection.getInstance(
        context,
        ComponentName(context, MusicService::class.java),
        Constants.API_URL,
        auth,
        format
    )
}

fun moveToVideoPlayerActivity(context: Context, release: Release) {
    val intent = Intent(context, VideoPlayerActivity::class.java)
    intent.putExtra("release", release)
    context.startActivity(intent)
}

fun moveToFullScreenVideoPlayerActivity(context: Context, videoTitle: String) {
    val intent = Intent(context, FullScreenVideoPlayerActivity::class.java)
    intent.putExtra("videoTitle", videoTitle)
    context.startActivity(intent)
}

fun moveToSubscription(context: Context) {
    val intent = Intent(context, CommonActivity::class.java)
    intent.putExtra("key", Constants.SUBSCRIPTION)
    context.startActivity(intent)
}

fun moveToReleaseFragment(
    context: Context,
    releaseId: String
) {
    val intent = Intent(context, ContentActivity::class.java)
    intent.putExtra("key", Constants.ALBUM_DETAILS)
    intent.putExtra("releaseId", releaseId)
    context.startActivity(intent)
}

fun moveToPlaylistFragment(
    context: Context,
    playlistId: String,
    addToPlaylist: Boolean,
    offline: Boolean
) {
    val intent = Intent(context, ContentActivity::class.java)
    intent.putExtra("key", Constants.PLAYLIST_DETAILS)
    intent.putExtra("playlistId", playlistId)
    intent.putExtra("addToPlaylist", addToPlaylist)
    intent.putExtra("offline", offline)
    context.startActivity(intent)
}

fun moveToAddToPlaylistFragment(
    context: Context,
    trackId: String
) {
    val intent = Intent(context, CommonActivity::class.java)
    intent.putExtra("key", Constants.ADD_PLAYLIST_TRACK)
    intent.putExtra("trackId", trackId)
    context.startActivity(intent)
}

fun moveToEditPlaylistTrackFragment(
    context: Context,
    playlistId: String,
    playlistTitle: String
) {
    val intent = Intent(context, CommonActivity::class.java)
    intent.putExtra("key", Constants.EDIT_PLAYLIST_TRACK)
    intent.putExtra("playlistId", playlistId)
    intent.putExtra("playlistTitle", playlistTitle)
    context.startActivity(intent)
}

fun moveToArtistFragment(context: Context, artistId: String) {
    val intent = Intent(context, ContentActivity::class.java)
    intent.putExtra("key", Constants.ARTIST_DETAILS)
    intent.putExtra("artistId", artistId)
    context.startActivity(intent)
}

fun shareOnSocial(context: Context, shareBody: String) {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
    context.startActivity(
        Intent.createChooser(
            sharingIntent,
            context.getString(R.string.share_on_social)
        )
    )
}

fun idsFromTrackList(list: ArrayList<Track>): String {
    return if (list.size == 0) {
        "{\"tracks\":[]}"
    } else {
        val mapAsString = StringBuilder("{\"tracks\":[")
        list.forEach { track ->
            mapAsString.append("{\"id\"" + ":\"" + track.trackId + "\"},")
        }
        mapAsString.delete(mapAsString.length - 1, mapAsString.length).append("]}")
        return mapAsString.toString()
    }
}

fun idFromTrack(trackId: String): String {
    val mapAsString = StringBuilder("{\"tracks\":[")
    mapAsString.append("{\"id\":\"$trackId\"},")
    mapAsString.delete(mapAsString.length - 1, mapAsString.length).append("]}")
    return mapAsString.toString()
}

fun radioTracksToMusicJson(
    list: ArrayList<Track>,
    playlistId: String,
    playlistTitle: String,
    playlistImage: String
): ArrayList<JsonMusic> {
    val musicList = ArrayList<JsonMusic>()
    list.forEach { track ->
        track.releaseId = playlistId
        track.trackTitle = playlistTitle
        track.trackImage = playlistImage
        musicList.add(
            JsonMusic(
                track.trackId,
                playlistTitle,
                parentId,
                track.artistName,
                track.artistName,
                "https://www.demourl.com",
                playlistImage,
                0L,
                0L,
                track.duration.toLong(),
                "",
                Gson().toJson(track)
            )
        )
    }
    return musicList
}

fun tracksToMusicJson(list: ArrayList<Track>): ArrayList<JsonMusic> {
    val musicList = ArrayList<JsonMusic>()
    list.forEach { track ->
        musicList.add(
            JsonMusic(
                track.trackId,
                track.trackTitle,
                parentId,
                track.artistName,
                track.artistName,
                "https://www.demourl.com",
                track.trackImage,
                0L,
                0L,
                track.duration.toLong(),
                "",
                Gson().toJson(track)
            )
        )
    }
    return musicList
}

fun tracksToOfflineMusicJson(list: ArrayList<Track>): ArrayList<JsonMusic> {
    val musicList = ArrayList<JsonMusic>()
    list.forEach { track ->
        musicList.add(
            JsonMusic(
                track.trackId,
                track.trackTitle,
                parentId,
                track.artistName,
                track.artistName,
                "https://www.demourl.com",
                track.trackImage,
                0L,
                0L,
                track.duration.toLong(),
                "",
                Gson().toJson(track),
                true
            )
        )
    }
    return musicList
}

fun trackToMusicJson(track: Track): JsonMusic {
    return JsonMusic(
        track.trackId,
        track.trackTitle,
        parentId,
        track.artistName,
        track.artistName,
        "https://www.demourl.com",
        track.trackImage,
        0L,
        0L,
        track.duration.toLong(),
        "",
        Gson().toJson(track)
    )
}

fun trackToOfflineMusicJson(track: Track): JsonMusic {
    return JsonMusic(
        track.trackId,
        track.trackTitle,
        parentId,
        track.artistName,
        track.artistName,
        "https://www.demourl.com",
        track.trackImage,
        0L,
        0L,
        track.duration.toLong(),
        "",
        Gson().toJson(track),
        true
    )
}