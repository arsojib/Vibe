package com.beat.view.content.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.beat.core.data.model.*
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.OfflineDownloadConnection
import com.beat.core.data.storage.PreferenceManager
import com.beat.data.model.*
import com.beat.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.example.android.uamp.common.EMPTY_PLAYBACK_STATE
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.common.NOTHING_PLAYING
import com.example.android.uamp.media.extensions.displayDescription
import com.example.android.uamp.media.extensions.duration
import com.example.android.uamp.media.extensions.id
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContentDetailsViewModel @Inject constructor(
    context: Context,
    private val repository: Repository,
    private val preferenceManager: PreferenceManager,
    private val offlineDownloadConnection: OfflineDownloadConnection
) :
    ViewModel() {

    private val nowPlayingMediaItem = MutableLiveData<Track>()
    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    private val serviceConnection: MusicServiceConnection = provideMusicServiceConnection(
        context,
        preferenceManager.getAuth(),
        getAudioQuality()
    )

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(it)
    }

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = serviceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(metadata)
    }

    private val musicServiceConnection = serviceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    private val releaseDetailsWithTrackResponse =
        MutableLiveData<Resource<ReleaseDetailsWithTrackResponse>>()
    private val topReleaseByArtistResponse = MutableLiveData<Resource<TopReleaseByArtistResponse>>()
    private val topReleaseByTypeByArtistResponse =
        MutableLiveData<Resource<TopReleaseByArtistResponse>>()
    private val topVideoByArtistResponse = MutableLiveData<Resource<TopVideoByArtistResponse>>()
    private val playlistDetailsWithTrackResponse =
        MutableLiveData<Resource<PlaylistDetailsWithTrackResponse>>()
    private val offlinePlaylistWithTracks =
        MutableLiveData<Resource<OfflinePlaylistWithTracks>>()
    private val artistDetailsResponse = MutableLiveData<Resource<Artist>>()
    private val artistTopTrackResponse = MutableLiveData<Resource<TopTrackByArtistResponse>>()
    private var deletePlaylistResponse = MutableLiveData<Resource<String>>()

    private val gradientDrawable = MutableLiveData<Drawable>()

    private var albumDetailsLoaded = false
    private var artistDetailsLoaded = false

    fun getAlbumDetailsContent(forceLoad: Boolean, releaseId: String) {
        if (!albumDetailsLoaded || forceLoad) {
            albumDetailsLoaded = true
            getAllTrackOfRelease(releaseId)
        }
    }

    fun getPlaylistDetailsContent(playlistId: String, offline: Boolean) {
        if (offline) {
            getPlaylistOfflineDetailsContent(playlistId)
        } else {
            getAllTrackOfPlaylist(playlistId)
        }
    }

    private fun getPlaylistOfflineDetailsContent(playlistId: String) {
        viewModelScope.launch {
            offlinePlaylistWithTracks.value = Resource.loading(null)
            val rawPlaylist =
                withContext(Dispatchers.IO) { offlineDownloadConnection.getPlaylist(playlistId) }
            val rawTracks =
                withContext(Dispatchers.IO) { offlineDownloadConnection.getTracks(playlistId) }

            val playlistWithTracks = OfflinePlaylistWithTracks(
                getPlaylistFromRawData(rawPlaylist),
                getTrackListFromRawData(rawTracks)
            )
            offlinePlaylistWithTracks.value = Resource.success(playlistWithTracks)
        }
    }

    fun getArtistDetailsContent(forceLoad: Boolean, artistId: String) {
        if (!artistDetailsLoaded || forceLoad) {
            artistDetailsLoaded = true
            getArtistDetails(artistId)
            getTopTrackOfArtist(artistId)
            getArtistsTopReleaseByType(artistId)
            getArtistsTopVideo(artistId)
        }
    }

    fun downloadPlaylist() {
        viewModelScope.launch {
            if (playlistDetailsWithTrackResponse.value != null) {
                playlistDetailsWithTrackResponse.value.let {
                    it?.let { resource ->
                        if (resource.status == Resource.Status.SUCCESS) {
                            val playlist = getPlaylistFromResponse(resource.data!!)
                            val tracks = getTrackListFromPlaylistDetailsResponse(resource.data!!)
                            if (playlist != null && tracks.size != 0) {
                                processPlaylistDownloadData(
                                    playlist,
                                    tracks
                                ) { downPlaylist, downTracks ->
                                    offlineDownloadConnection.downloadPlaylist(
                                        downPlaylist,
                                        downTracks
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun getDownloadObserver(
        trackId: String,
        observer: Observer<Int>,
        lifecycleOwner: LifecycleOwner
    ) {
        offlineDownloadConnection.getObserver(trackId, observer, lifecycleOwner)
    }

    fun getNewDownloadAddObserver(
        observer: Observer<String>,
        lifecycleOwner: LifecycleOwner
    ) {
        offlineDownloadConnection.getNewIdAddObserver(observer, lifecycleOwner)
    }

    fun setImage(view: ImageView, imageUrl: String?, drawable: Drawable?) {
        if (imageUrl != null && drawable != null) {
            val circularProgressDrawable = CircularProgressDrawable(view.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
            val options: RequestOptions
            options = RequestOptions()
                .fitCenter()
                .placeholder(circularProgressDrawable)
                .error(drawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .circleCrop()
                .dontAnimate()
                .dontTransform()

            Glide.with(view.context).load(imageUrl)
                .apply(options)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (resource != null) onDraw(resource)
                        return false
                    }
                })
                .into(view)
        }
    }

    private fun onDraw(bitmapDrawable: Drawable) {
        val bitmap = bitmapDrawable.toBitmap(100, 100)
        setGradientLayout(bitmap)
    }

    private fun setGradientLayout(bitmap: Bitmap?) {
        if (bitmap != null) {
            val builder = Palette.Builder(bitmap)
            builder.generate { palette: Palette? ->
                if (palette != null) {
                    val arrayOfSwatch: Array<Palette.Swatch?> =
                        arrayOf(
                            palette.darkMutedSwatch,
                            palette.vibrantSwatch,
                            palette.lightVibrantSwatch,
                            palette.darkVibrantSwatch,
                            palette.dominantSwatch,
                            palette.mutedSwatch
                        )
                    var colorPopulation = 0
                    var colorRgb = 0
                    arrayOfSwatch.forEach { swatch ->
                        val color = getColor(swatch)
                        if (color != 0) {
                            val population = swatch?.population ?: 0
                            if (population > colorPopulation) {
                                colorPopulation = population
                                colorRgb = color
                            }
                        }
                    }
                    val drawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(
                            colorRgb, Color.TRANSPARENT
                        )
                    )
                    drawable.alpha = 128
                    gradientDrawable.value = drawable

//                    for (element in arrayOfSwatch) {
//                        val color = getColor(element)
//                        if (color != 0) {
//                            val drawable = GradientDrawable(
//                                GradientDrawable.Orientation.TOP_BOTTOM,
//                                intArrayOf(
//                                    color, Color.TRANSPARENT
//                                )
//                            )
//                            drawable.alpha = 128
//                            gradientDrawable.value = drawable
//                            break
//                        }
//                    }
                }
            }
        }
    }

    private fun getColor(swatch: Palette.Swatch?): Int {
        return swatch?.rgb ?: 0
    }

    private fun getAllTrackOfRelease(releaseId: String) {
        viewModelScope.launch {
            releaseDetailsWithTrackResponse.value = Resource.loading(null)
            releaseDetailsWithTrackResponse.value = repository.getReleaseDetailsWithTrack(releaseId)
        }
    }

    fun getArtistsTopRelease(artistId: String) {
        viewModelScope.launch {
            topReleaseByArtistResponse.value = repository.getTopReleaseByArtist(artistId)
        }
    }

    private fun getArtistsTopReleaseByType(artistId: String) {
        viewModelScope.launch {
            topReleaseByTypeByArtistResponse.value =
                repository.getTopReleaseByTypeByArtist(artistId)
        }
    }

    private fun getArtistsTopVideo(artistId: String) {
        viewModelScope.launch {
            topVideoByArtistResponse.value = repository.getTopVideoByArtist(artistId)
        }
    }

    private fun getAllTrackOfPlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistDetailsWithTrackResponse.value = Resource.loading(null)
            playlistDetailsWithTrackResponse.value =
                repository.getPlaylistDetailsWithTrack(playlistId)
        }
    }

    private fun getTopTrackOfArtist(artistId: String) {
        viewModelScope.launch {
            artistTopTrackResponse.value =
                repository.getTopTrackByArtist(artistId)
        }
    }

    private fun getArtistDetails(artistId: String) {
        viewModelScope.launch {
            artistDetailsResponse.value = Resource.loading(null)
            val details = async { repository.getArtistDetails(artistId) }
            val bio = async { repository.getArtistBio(artistId) }
            val detailsResponse: Resource<ArtistDetailsResponse> = details.await()
            val bioResponse: Resource<ArtistBioResponse> = bio.await()
            val artist = getArtistDetailsFromResponse(detailsResponse.data, bioResponse.data)
            if (artist != null) {
                artistDetailsResponse.value = Resource.success(artist)
            } else {
                artistDetailsResponse.value = Resource.error(null, "", -1)
            }
        }
    }

    fun patchFavorite(id: String, type: String, favorite: Boolean) {
        viewModelScope.launch {
            repository.patchFavorite(id, type, favorite)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            deletePlaylistResponse.value = Resource.loading(null)
            deletePlaylistResponse.value = repository.deletePlaylist(playlistId)
        }
    }

    fun fetchCurrentPlayingItem() {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        if (!nowPlaying?.id.isNullOrEmpty()) {
            updateState(nowPlaying!!)
        }
    }

    fun playTrackList(list: ArrayList<Track>, track: Track) {
        musicServiceConnection.addMediaToPlayer(
            isRadioInitialed,
            tracksToMusicJson(list),
            true,
            track.trackId
        )
    }

    fun playTrackList(list: ArrayList<Track>, track: Track, offline: Boolean) {
        musicServiceConnection.addMediaToPlayer(
            isRadioInitialed,
            if (offline) tracksToOfflineMusicJson(list) else tracksToMusicJson(list),
            true,
            track.trackId
        )
    }

    fun isPlaylistDownloaded(playlistId: String, callBack: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val isExist = offlineDownloadConnection.isPlaylistExist(playlistId)
            withContext(Dispatchers.Main) { callBack(isExist) }
        }
    }

    fun removePlaylist(playlistId: String) {
        offlineDownloadConnection.removePlaylist(playlistId)
    }

    fun playTrackToNext(track: Track) {
        musicServiceConnection.addNextMediaToPlayer(trackToMusicJson(track), false)
    }

    fun playTrackToNext(track: Track, offline: Boolean) {
        musicServiceConnection.addNextMediaToPlayer(
            if (offline) trackToOfflineMusicJson(track) else trackToMusicJson(track), false
        )
    }

    fun addTrackToQueue(track: Track) {
        musicServiceConnection.addQueueMediaToPlayer(trackToMusicJson(track))
    }

    fun addTrackToQueue(track: Track, offline: Boolean) {
        musicServiceConnection.addQueueMediaToPlayer(
            if (offline) trackToOfflineMusicJson(track) else trackToMusicJson(track)
        )
    }

    fun getReleaseDetailsWithTrackResponse(): LiveData<Resource<ReleaseDetailsWithTrackResponse>> {
        return releaseDetailsWithTrackResponse
    }

    fun getTopReleaseByArtistResponse(): LiveData<Resource<TopReleaseByArtistResponse>> {
        return topReleaseByArtistResponse
    }

    fun getTopReleaseByTypeByArtistResponse(): LiveData<Resource<TopReleaseByArtistResponse>> {
        return topReleaseByTypeByArtistResponse
    }

    fun getTopVideoByArtistResponse(): LiveData<Resource<TopVideoByArtistResponse>> {
        return topVideoByArtistResponse
    }

    fun getPlaylistDetailsWithTrackResponse(): LiveData<Resource<PlaylistDetailsWithTrackResponse>> {
        return playlistDetailsWithTrackResponse
    }

    fun getOfflinePlaylistWithTracks(): LiveData<Resource<OfflinePlaylistWithTracks>> {
        return offlinePlaylistWithTracks
    }

    fun getArtistDetailsResponse(): LiveData<Resource<Artist>> {
        return artistDetailsResponse
    }

    fun getTopTrackByArtistResponse(): LiveData<Resource<TopTrackByArtistResponse>> {
        return artistTopTrackResponse
    }

    fun getGradientDrawable(): LiveData<Drawable> {
        return gradientDrawable
    }

    fun getDeletePlaylistResponse(): LiveData<Resource<String>> {
        return deletePlaylistResponse
    }

    fun getNowPlayingMediaItem(): LiveData<Track> {
        return nowPlayingMediaItem
    }

    fun getPlaybackStateCompat(): PlaybackStateCompat {
        return playbackState
    }

    fun getReleaseFromResponse(releaseDetailsWithTrackResponse: ReleaseDetailsWithTrackResponse?): Release? {
        return if (releaseDetailsWithTrackResponse != null) {
            Release(
                releaseDetailsWithTrackResponse.release.id,
                releaseDetailsWithTrackResponse.release.title,
                releaseDetailsWithTrackResponse.release.cover.width,
                releaseDetailsWithTrackResponse.release.release_date,
                releaseDetailsWithTrackResponse.release.artist.id,
                releaseDetailsWithTrackResponse.release.artist.name,
                releaseDetailsWithTrackResponse.release.type,
                releaseDetailsWithTrackResponse.release.copyright,
                releaseDetailsWithTrackResponse.release.url,
                releaseDetailsWithTrackResponse.release.favourite
            )
        } else {
            null
        }
    }

    fun getPlaylistFromResponse(playlistDetailsWithTrackResponse: PlaylistDetailsWithTrackResponse?): Playlist? {
        return if (playlistDetailsWithTrackResponse != null) {
            Playlist(
                playlistDetailsWithTrackResponse.playlist.id,
                playlistDetailsWithTrackResponse.playlist.title,
                if (playlistDetailsWithTrackResponse.playlist.tracks.isNotEmpty()) playlistDetailsWithTrackResponse.playlist.tracks[0].release.cover.width else "",
                playlistDetailsWithTrackResponse.playlist.track_count,
                playlistDetailsWithTrackResponse.playlist.url,
                playlistDetailsWithTrackResponse.playlist.permission,
                playlistDetailsWithTrackResponse.playlist.favourite
            )
        } else {
            null
        }
    }

    private fun getArtistDetailsFromResponse(
        artistDetailsResponse: ArtistDetailsResponse?,
        artistBioResponse: ArtistBioResponse?
    ): Artist? {
        var artist: Artist? = null
        if (artistDetailsResponse?.artist != null) {
            artist = Artist(
                "",
                artistDetailsResponse.artist.image.width,
                artistDetailsResponse.artist.id,
                artistDetailsResponse.artist.name,
                artistDetailsResponse.artist.favourite,
                ""
            )
            if (artistBioResponse?.bio != null) {
                artist.bio = artistBioResponse.bio.summary
            }
        }
        return artist
    }

    fun getTrackListFromResponse(releaseDetailsWithTrackResponse: ReleaseDetailsWithTrackResponse?): ArrayList<Track> {
        val list: ArrayList<Track> = ArrayList()
        releaseDetailsWithTrackResponse?.release?.tracks?.forEach { track ->
            list.add(
                Track(
                    track.id,
                    track.title,
                    track.release.cover.width,
                    track.duration,
                    track.release.id,
                    track.artist.id,
                    track.artist.name,
                    track.url,
                    track.favourite
                )
            )
        }
        return list
    }

    fun getTrackListFromPlaylistDetailsResponse(playlistDetailsWithTrackResponse: PlaylistDetailsWithTrackResponse?): ArrayList<Track> {
        val list: ArrayList<Track> = ArrayList()
        playlistDetailsWithTrackResponse?.playlist?.tracks?.forEach { track ->
            list.add(
                Track(
                    track.id,
                    track.title,
                    track.release.cover.width,
                    track.duration,
                    track.release.id,
                    track.artist.id,
                    track.artist.name,
                    track.url,
                    track.favourite
                )
            )
        }
        return list
    }

    fun getTrackListFromArtistResponse(topTrackByArtistResponse: TopTrackByArtistResponse?): ArrayList<Track> {
        val list: ArrayList<Track> = ArrayList()
        topTrackByArtistResponse?.tracks?.forEach { track ->
            list.add(
                Track(
                    track.id,
                    track.title,
                    track.release.cover.width,
                    track.duration,
                    track.release.id,
                    track.artist.id,
                    track.artist.name,
                    track.url,
                    track.favourite
                )
            )
        }
        return list
    }

    fun getReleaseListFromResponse(topReleaseByArtistResponse: TopReleaseByArtistResponse?): ArrayList<Release> {
        val list: ArrayList<Release> = ArrayList()
        topReleaseByArtistResponse?.releases?.forEach { release ->
            list.add(
                Release(
                    release.id,
                    release.title,
                    release.cover.width,
                    release.release_date,
                    release.artist.id,
                    release.artist.name,
                    release.type,
                    release.copyright,
                    release.url,
                    release.favourite
                )
            )
        }
        return list
    }

    fun getReleaseListFromVideoResponse(topVideoByArtistResponse: TopVideoByArtistResponse?): ArrayList<Release> {
        val list: ArrayList<Release> = ArrayList()
        topVideoByArtistResponse?.releases?.forEach { release ->
            list.add(
                Release(
                    release.id,
                    release.title,
                    release.cover.width,
                    release.release_date,
                    release.artist.id,
                    release.artist.name,
                    release.type,
                    release.copyright,
                    release.url,
                    release.favourite
                )
            )
        }
        return list
    }

    private fun getPlaylistFromRawData(rawData: com.beat.core.data.storage.database.entity.Playlist): Playlist {
        return Playlist(
            rawData.playlistId,
            rawData.playlistTitle,
            rawData.playlistImage,
            rawData.trackCount,
            rawData.url,
            rawData.permission,
            rawData.favourite
        )
    }

    private fun getTrackListFromRawData(rawData: List<com.beat.core.data.storage.database.entity.Track>): ArrayList<Track> {
        val trackList = ArrayList<Track>()
        rawData.forEach {
            trackList.add(
                Track(
                    it.trackId,
                    it.trackTitle,
                    it.trackImage,
                    it.duration,
                    it.releaseId ?: "",
                    it.artistId,
                    it.artistName,
                    it.url,
                    it.favorite
                )
            )
        }
        return trackList
    }

    private suspend fun processPlaylistDownloadData(
        playlist: Playlist,
        tracks: List<Track>,
        callBack: ((
            com.beat.core.data.storage.database.entity.Playlist,
            List<com.beat.core.data.storage.database.entity.Track>
        ) -> Unit)
    ) = withContext(Dispatchers.Default) {
        val downPlaylist =
            com.beat.core.data.storage.database.entity.Playlist(
                playlist.playlistId,
                playlist.playlistTitle,
                playlist.playlistImage,
                playlist.trackCount,
                playlist.url,
                playlist.permission,
                playlist.favourite
            )
        val downTracks =
            ArrayList<com.beat.core.data.storage.database.entity.Track>()
        tracks.forEach { track ->
            downTracks.add(
                com.beat.core.data.storage.database.entity.Track(
                    track.trackId,
                    track.trackTitle,
                    track.trackImage,
                    track.duration,
                    track.releaseId,
                    "",
                    track.artistId,
                    track.artistName,
                    track.url,
                    track.favorite,
                    false
                )
            )
        }
        callBack(downPlaylist, downTracks)
    }

    fun hasPermission(context: Context, callback: () -> Unit) {
        if (preferenceManager.hasGenericPermission()) {
            callback()
        } else {
            moveToSubscription(context)
        }
    }

    private fun getAudioQuality(): String {
        val quality = preferenceManager.getAudioQuality()
        return if (quality.isEmpty()) Constants.NORMAL else quality
    }

    private fun updateState(
        mediaMetadata: MediaMetadataCompat
    ) {
        // Only update media item once we have duration available
        if (mediaMetadata.duration != 0L && mediaMetadata.id != null) {
            val nowPlayingMetadata: Track =
                Gson().fromJson(
                    mediaMetadata.displayDescription,
                    Track::class.java
                )
            this.nowPlayingMediaItem.value = nowPlayingMetadata
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)
    }

}