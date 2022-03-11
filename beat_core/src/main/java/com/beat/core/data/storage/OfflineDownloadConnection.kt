package com.beat.core.data.storage

import android.content.Context
import androidx.lifecycle.*
import com.beat.core.data.model.Resource
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.database.entity.*
import com.beat.core.di.scope.CoreScope
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@CoreScope
class OfflineDownloadConnection @Inject constructor(
    private val context: Context,
    private val preferenceManager: PreferenceManager,
    private val repository: Repository,
    private val databaseClient: DatabaseClient
) {

    private val tracksHash = ConcurrentHashMap<String, MutableLiveData<Int>>()
    private val downloadIdAdded = MutableLiveData<String>()
    private val downloadDataUpdated = MutableLiveData<Boolean>()
    private val offlineDirectory = "offline"

    init {
        PRDownloader.initialize(context)
        initialAllOfflineTracks()
    }

    private fun initialAllOfflineTracks() {
        GlobalScope.launch {
            val list = getAllTrack()
            list.forEach {
                val progress = MutableLiveData<Int>()
                if (it.downloaded) {
                    progress.postValue(100)
                } else {
                    progress.postValue(-1)
                    val response = repository.getOfflineStream(
                        it.trackId,
                        preferenceManager.getAudioQuality()
                    )
                    if (response.status == Resource.Status.SUCCESS) {
                        startDownload(it.trackId, response.data!!.stream.url, progress, false)
                    }
                }
                tracksHash[it.trackId] = progress
                downloadIdAdded.postValue(it.trackId)
            }
        }
    }

    suspend fun getAllTrack(): List<Track> {
        return withContext(Dispatchers.IO) { databaseClient.getAppDatabase().trackDao().getAll() }
    }

    suspend fun getAllRelease(): List<Release> {
        return withContext(Dispatchers.IO) { databaseClient.getAppDatabase().releaseDao().getAll() }
    }

    suspend fun getAllPlaylist(): List<Playlist> {
        return withContext(Dispatchers.IO) {
            databaseClient.getAppDatabase().playListDao().getAll()
        }
    }

    fun downloadTrack(track: Track) {
        GlobalScope.launch {
            val already = tracksHash.containsKey(track.trackId)
            if (already && tracksHash[track.trackId]?.value == -1) {
                val response = withContext(Dispatchers.IO) {
                    repository.getOfflineStream(
                        track.trackId,
                        preferenceManager.getAudioQuality()
                    )
                }
                if (response.status == Resource.Status.SUCCESS) {
                    startDownload(
                        track.trackId,
                        response.data!!.stream.url,
                        tracksHash[track.trackId]!!,
                        false
                    )
                }
            } else {
                val progress = MutableLiveData<Int>().also {
                    it.postValue(-1)
                }
                launch(Dispatchers.IO) {
                    databaseClient.getAppDatabase().trackDao().insertTrack(track)
                }
                val response = repository.getOfflineStream(
                    track.trackId,
                    preferenceManager.getAudioQuality()
                )
                if (response.status == Resource.Status.SUCCESS) {
                    startDownload(track.trackId, response.data!!.stream.url, progress, false)
                }
                tracksHash[track.trackId] = progress
                downloadIdAdded.postValue(track.trackId)
            }
        }
    }

    //Album/Release
    private fun downloadRelease(release: Release, track: Track) {
        GlobalScope.launch {
            if (!databaseClient.getAppDatabase().releaseDao().isExist(release.releaseId)) {
                databaseClient.getAppDatabase().releaseDao().insertRelease(release)
                databaseClient.getAppDatabase().releaseDao().insertReleaseTracksCrossRef(
                    ReleaseTrackCrossRef(release.releaseId, track.trackId)
                )
            }
            downloadTrack(track)
        }
    }

    fun downloadPlaylist(playlist: Playlist, tracks: List<Track>) {
        GlobalScope.launch {
            launch(Dispatchers.IO) {
                if (!databaseClient.getAppDatabase().playListDao().isExist(playlist.playlistId)) {
                    databaseClient.getAppDatabase().playListDao().insertPlaylist(playlist)
                    databaseClient.getAppDatabase().playListDao().insertPlaylistTracksCrossRef(
                        getPlaylistTrackCrossRefList(
                            playlist,
                            tracks
                        )
                    )
                }
            }
            tracks.forEach {
                downloadTrack(it)
            }
        }
    }

    fun removeTrack(trackId: String) {
        GlobalScope.launch {
            launch(Dispatchers.IO) {
                val rRefs = databaseClient.getAppDatabase().releaseDao()
                    .getReleaseTrackCrossRef(trackId)
                val pRefs = databaseClient.getAppDatabase().playListDao()
                    .getPlaylistTrackCrossRefsByTrackId(trackId)
                databaseClient.getAppDatabase().releaseDao()
                    .deleteReleaseTrackCrossRef(getReleaseIds(rRefs))
                databaseClient.getAppDatabase().releaseDao().deleteReleases(getReleaseIds(rRefs))
                databaseClient.getAppDatabase().playListDao()
                    .deletePlaylistTrackCrossRef(getPlaylistIds(pRefs))
                databaseClient.getAppDatabase().playListDao()
                    .deletePlaylists(getPlaylistIds(pRefs))
                databaseClient.getAppDatabase().trackDao().deleteTrack(trackId)
            }
            deleteFile(trackId)
            tracksHash.remove(trackId)
            downloadDataUpdated.postValue(true)
        }
    }

    //Remove track from files and hashmap
    fun removePlaylist(playlistId: String) {
        GlobalScope.launch {
            val refList = databaseClient.getAppDatabase().playListDao()
                .getPlaylistTrackCrossRefsSingleton(playlistId)
            val trackIds = getPTrackIds(refList)
            databaseClient.getAppDatabase().playListDao().deletePlaylistTrackCrossRef(trackIds)
            databaseClient.getAppDatabase().playListDao().deletePlaylists(getPlaylistIds(refList))
            databaseClient.getAppDatabase().trackDao().deleteTracks(trackIds)

            trackIds.forEach {
                deleteFile(it)
                tracksHash.remove(it)
            }
            downloadDataUpdated.postValue(true)
        }
    }

    //Album/Release
    //Remove track from files and hashmap
    fun removeRelease(releaseId: String) {
        GlobalScope.launch {
            val refList = databaseClient.getAppDatabase().releaseDao()
                .getReleaseTrackCrossRefsSingleton(releaseId)
            val trackIds = getRTrackIds(refList)
            databaseClient.getAppDatabase().releaseDao()
                .deleteReleaseTrackCrossRef(getRTrackIds(refList))
            databaseClient.getAppDatabase().playListDao().deletePlaylists(getReleaseIds(refList))
            databaseClient.getAppDatabase().trackDao().deleteTracks(getRTrackIds(refList))

            trackIds.forEach {
                deleteFile(it)
                tracksHash.remove(it)
            }
            downloadDataUpdated.postValue(true)
        }
    }

    fun removeAll() {
        databaseClient.getAppDatabase().clearAllTables()
    }

    suspend fun setTrackFavorite(id: String, fav: Boolean) {
        withContext(Dispatchers.IO) {
            databaseClient.getAppDatabase().trackDao().updateFavoriteStatus(id, fav)
        }
    }

    suspend fun setReleaseFavorite(id: String, fav: Boolean) {
        withContext(Dispatchers.IO) {
            databaseClient.getAppDatabase().releaseDao().updateFavoriteStatus(id, fav)
        }
    }

    suspend fun setPlaylistFavorite(id: String, fav: Boolean) {
        withContext(Dispatchers.IO) {
            databaseClient.getAppDatabase().playListDao().updateFavoriteStatus(id, fav)
        }
    }

    suspend fun isPlaylistExist(id: String) =
        databaseClient.getAppDatabase().playListDao().isExist(id)

    private fun startDownload(
        trackId: String,
        url: String,
        progress: MutableLiveData<Int>,
        forceDownload: Boolean
    ) {
        checkFileExist(trackId) { isExist, path ->
            if (!isExist || forceDownload) {
                PRDownloader.download(url, path, getTrackFilename(trackId))
                    .build()
                    .setOnStartOrResumeListener { }
                    .setOnPauseListener { }
                    .setOnCancelListener {
                        progress.postValue(-1)
                    }
                    .setOnProgressListener {
                        progress.postValue(((it.currentBytes.toDouble() / it.totalBytes.toDouble()) * 100).toInt())
                    }
                    .start(object : OnDownloadListener {
                        override fun onDownloadComplete() {
                            progress.postValue(100)
                            GlobalScope.launch(Dispatchers.IO) {
                                databaseClient.getAppDatabase().trackDao()
                                    .updateDownloadStatus(trackId, true)
                            }
                        }

                        override fun onError(error: com.downloader.Error?) {
                            progress.postValue(-1)
                        }
                    })
            } else {
                progress.postValue(100)
                GlobalScope.launch(Dispatchers.IO) {
                    databaseClient.getAppDatabase().trackDao()
                        .updateDownloadStatus(trackId, true)
                }
            }
        }
    }

    private fun getPlaylistTrackCrossRefList(
        playlist: Playlist,
        tracks: List<Track>
    ): List<PlaylistTrackCrossRef> {
        val playlistTrackCrossRefs = ArrayList<PlaylistTrackCrossRef>()
        tracks.forEach {
            playlistTrackCrossRefs.add(PlaylistTrackCrossRef(playlist.playlistId, it.trackId))
        }
        return playlistTrackCrossRefs
    }

    suspend fun getPlaylist(playlistId: String) =
        databaseClient.getAppDatabase().playListDao().getPlaylist(playlistId)

    suspend fun getTracks(playlistId: String): List<Track> {
        val refs = databaseClient.getAppDatabase().playListDao()
            .getPlaylistTrackCrossRefsByPlaylistId(playlistId)
        return databaseClient.getAppDatabase().trackDao().getTracks(getPTrackIds(refs))
    }

    private fun getPlaylistIds(list: List<PlaylistTrackCrossRef>): Array<String> {
        return list.map { it.playlistId }.toTypedArray()
    }

    private fun getReleaseIds(list: List<ReleaseTrackCrossRef>): Array<String> {
        return list.map { it.releaseId }.toTypedArray()
    }

    private fun getPTrackIds(list: List<PlaylistTrackCrossRef>): Array<String> {
        return list.map { it.trackId }.toTypedArray()
    }

    private fun getRTrackIds(list: List<ReleaseTrackCrossRef>): Array<String> {
        return list.map { it.trackId }.toTypedArray()
    }

    private fun checkFileExist(trackId: String, callBack: ((Boolean, String) -> Unit)) {
        mkDirIfNotExist()
        val file = File(getFileDirectory(), getTrackFilename(trackId))
        callBack(file.exists(), getFileDirectory().absolutePath)
    }

    private fun deleteFile(trackId: String) {
        File(getFileDirectory(), getTrackFilename(trackId)).delete()
    }

    private fun mkDirIfNotExist() {
        getFileDirectory().mkdirs()
    }

    private fun getTrackFilename(str: String): String {
        return "track_$str.mp3"
    }

    private fun getFileDirectory() = File(context.filesDir, offlineDirectory)

    private fun getTrackPath(trackId: String): String {
        File(context.filesDir, offlineDirectory)
        val file = File(File(context.filesDir, offlineDirectory), "track_$trackId.mp3")
        return if (file.exists()) file.absolutePath else ""
    }

    fun getObserver(trackId: String, observer: Observer<Int>, lifecycleOwner: LifecycleOwner) {
        tracksHash[trackId]?.observe(lifecycleOwner, observer)
    }

    fun getNewIdAddObserver(observer: Observer<String>, lifecycleOwner: LifecycleOwner) {
        downloadIdAdded.observe(lifecycleOwner, observer)
    }

    fun getDataUpdatedObserver(observer: Observer<Boolean>, lifecycleOwner: LifecycleOwner) {
        downloadDataUpdated.observe(lifecycleOwner, observer)
    }

    fun removeObserver(trackId: String, observer: Observer<Int>) {
        tracksHash[trackId]?.removeObserver(observer)
    }

}