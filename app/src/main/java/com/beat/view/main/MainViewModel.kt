package com.beat.view.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beat.core.data.model.*
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.OfflineDownloadConnection
import com.beat.core.data.storage.PreferenceManager
import com.beat.data.model.*
import com.beat.di.module.main.MainScope
import com.beat.util.*
import com.example.android.uamp.common.EMPTY_PLAYBACK_STATE
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.common.NOTHING_PLAYING
import com.example.android.uamp.media.extensions.displayDescription
import com.example.android.uamp.media.extensions.duration
import com.example.android.uamp.media.extensions.id
import com.google.gson.Gson
import kotlinx.coroutines.*
import javax.inject.Inject

@MainScope
class MainViewModel @Inject constructor(
    context: Context,
    private val repository: Repository,
    private val preferenceManager: PreferenceManager,
    private val offlineDownloadConnection: OfflineDownloadConnection
) : ViewModel() {

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

    private var searchJob: Job? = null

    private var userResponse = MutableLiveData<Resource<UserResponse>>()
    private var currentSubscriptionResponse = MutableLiveData<Resource<SubscriptionPlan>>()
    private var radioListResponse = MutableLiveData<Resource<RadioListResponse>>()
    private var homePatchList = MutableLiveData<ArrayList<Patch>>()
    private var videoPatchList = MutableLiveData<ArrayList<Patch>>()
    private var searchPatchList = MutableLiveData<ArrayList<SearchPatch>>()
    private var videoBannerResponse = MutableLiveData<Resource<VideoBannerResponse>>()
    private var deletePlaylistResponse = MutableLiveData<Resource<String>>()
    private val playlistDetailsWithTrackResponse =
        MutableLiveData<Resource<PlaylistDetailsWithTrackResponse>>()
    var videoTabChange = MutableLiveData<Boolean>()
    var videoTabFullScreen = MutableLiveData<Boolean>()

    private var isVideoTabLoaded = false
    private var isRadioTabLoaded = false

    fun navigateToDeepLink(context: Context, appLinkAction: String?, appLinkData: Uri?) {
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            val id = getId(appLinkData.toString())
            when (appLinkData.path!!) {
                "/playlists" -> {
                    moveToPlaylistFragment(context, id, addToPlaylist = false, offline = false)
                }
                "/releases" -> {
                    moveToReleaseFragment(context, id)
                }
                "/artists" -> {
                    moveToArtistFragment(context, id)
                }
            }
        }
    }

    private fun getId(url: String): String {
        var index = url.lastIndexOf("/")
        return url.substring(++index, url.lastIndex)
    }

    fun getHomePageContent() {
        viewModelScope.launch {
            val resource = repository.getFeaturedGroupRelease()
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    val list: ArrayList<FeaturedReleaseGroup> =
                        getFeaturedGroupReleaseFromResponse(resource.data!!)
                    setHomePatchList(list)
                }
                Resource.Status.ERROR -> {
                    setHomePatchList(ArrayList())
                }
                Resource.Status.LOADING -> {

                }
            }
        }
    }

    fun getVideoPageContent() {
        if (isVideoTabLoaded) return
        isVideoTabLoaded = true
        viewModelScope.launch {
            val resource = repository.getVideoGroupRelease(Constants.VIDEO_GROUP_ID)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    val list: ArrayList<FeaturedReleaseGroup> =
                        getVideoReleaseGroupFromResponse(resource.data!!)
                    setVideoPatchList(list)
                }
                Resource.Status.ERROR -> {
                    setVideoPatchList(ArrayList())
                }
                Resource.Status.LOADING -> {

                }
            }
        }
    }

    fun radioListRequest() {
        if (isRadioTabLoaded) return
        isRadioTabLoaded = true
        viewModelScope.launch {
            radioListResponse.value = Resource.loading(null)
            radioListResponse.value = repository.getRadioList(
                "6"
            )
        }
    }

    fun getAllTrackOfPlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistDetailsWithTrackResponse.value = Resource.loading(null)
            playlistDetailsWithTrackResponse.value =
                repository.getPlaylistDetailsWithTrack(playlistId)
        }
    }

    fun downloadPlaylist(playlistId: String) {
        viewModelScope.launch {
            val resource = repository.getPlaylistDetailsWithTrack(playlistId)
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

    fun getSearchContent(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            searchPatchList.value = ArrayList()
            return
        }
        searchJob = viewModelScope.launch {
            val trackResponseTask = async {
                repository.getSearchTrack(query)
            }
            val releaseResponseTask = async {
                repository.getSearchRelease(query)
            }
            val artistResponseTask = async {
                repository.getSearchArtist(query)
            }
            val trackResponse = trackResponseTask.await()
            val releaseResponse = releaseResponseTask.await()
            val artistResponse = artistResponseTask.await()

            val trackList = getSearchTrackListFromResponse(trackResponse.data!!)
            val releaseList = getSearchReleaseListFromResponse(releaseResponse.data!!)
            val artistList = getSearchArtistListFromResponse(artistResponse.data!!)
            val list = ArrayList<SearchPatch>()
            if (trackList.size != 0) list.add(
                SearchPatch(
                    Constants.SONGS,
                    Constants.MUSIC_PATCH,
                    trackList
                )
            )
            if (releaseList.size != 0) list.add(
                SearchPatch(
                    Constants.ALBUMS,
                    Constants.ALBUM_PATCH,
                    releaseList
                )
            )
            if (trackList.size != 0) list.add(
                SearchPatch(
                    Constants.ARTISTS,
                    Constants.ARTIST_PATCH,
                    artistList
                )
            )
            searchPatchList.value = list
        }
    }

    fun getUserRequest() {
        viewModelScope.launch {
            userResponse.value = repository.getUser()
        }
    }

    fun currentSubscription() {
        viewModelScope.launch {
            val productsResponse = repository.getSubscriptionProducts()
            val currentPlanResponse = repository.getCurrentSubscription()
            if (productsResponse.status == Resource.Status.SUCCESS) {
                var productId = ""
                var subscriptionId = ""
                var nextBillingDate = ""
                var state = 0
                if (currentPlanResponse.status == Resource.Status.SUCCESS) {
                    currentPlanResponse.data.let {
                        it?.subscriptions.let { list ->
                            list?.let {
                                if (list.isNotEmpty()) {
                                    list[0].let { subscription ->
                                        productId = subscription.product_id
                                        subscriptionId = subscription.id
                                        nextBillingDate = subscription.next_billing_date
                                        state = subscription.state
                                        preferenceManager.setSubscription(subscription)
                                    }
                                }
                            }
                        }
                    }
                }
                getSubscriptionPlanFromResponse(
                    productsResponse.data,
                    productId,
                    subscriptionId,
                    nextBillingDate,
                    state
                )
            } else {
                currentSubscriptionResponse.value = Resource.error(null, "", -1)
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

    fun playTrack(track: Track) {
        val list = ArrayList<Track>()
        list.add(track)
        musicServiceConnection.addMediaToPlayer(
            isRadioInitialed,
            tracksToMusicJson(list),
            true,
            track.trackId
        )
    }

    fun playRadio(
        list: ArrayList<Track>,
        track: Track,
        playlistId: String,
        playlistTitle: String,
        playlistImage: String
    ) {
        musicServiceConnection.addRadioMediaToPlayer(
            isPlayerInitialed,
            radioTracksToMusicJson(list, playlistId, playlistTitle, playlistImage),
            true,
            track.trackId
        )
    }

    fun hasPermission(context: Context, callback: () -> Unit) {
        if (preferenceManager.hasGenericPermission()) {
            callback()
        } else {
            moveToSubscription(context)
        }
    }

    fun removePlaylist(playlistId: String) {
        offlineDownloadConnection.removePlaylist(playlistId)
    }

    fun isPlaylistDownloaded(playlistId: String, callBack: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val isExist = offlineDownloadConnection.isPlaylistExist(playlistId)
            withContext(Dispatchers.Main) { callBack(isExist) }
        }
    }

    fun playTrackToNext(track: Track, nowPlay: Boolean) {
        musicServiceConnection.addNextMediaToPlayer(trackToMusicJson(track), nowPlay)
    }

    fun addTrackToQueue(track: Track) {
        musicServiceConnection.addQueueMediaToPlayer(trackToMusicJson(track))
    }

    fun removeAudioPlayer() {
        musicServiceConnection.transportControls.stop()
        viewModelScope.launch {
            musicServiceConnection.jsonSource.removeAll()
        }
    }

    suspend fun topBannerRequest(groupId: String): Resource<TopBannerResponse> {
        return repository.getTopBannerList(groupId)
    }

    suspend fun genresListRequest(groupId: String): Resource<GenresListResponse> {
        return repository.getGenresList(groupId)
    }

    suspend fun trendingArtistListRequest(groupId: String): Resource<TrendingArtistResponse> {
        return repository.getTrendingArtistList(groupId)
    }

    fun videoBannerListRequest() {
        viewModelScope.launch {
            repository.getVideoBannerList(videoBannerResponse, Constants.VIDEO_BANNER_GROUP_ID)
        }
    }

    suspend fun releaseGroupRequest(groupId: String): Resource<ReleaseGroupResponse> {
        return repository.getReleaseGroup(groupId)
    }

    suspend fun programRequest(programId: String): Resource<ProgramResponse> {
        return repository.getProgram(programId)
    }

    suspend fun featuredPlayListRequest(): Resource<FeaturedPlaylistResponse> {
        return repository.getFeaturedPlayList()
    }

    suspend fun featuredGroupPlayListRequest(): Resource<FeaturedGroupPlaylistResponse> {
        return repository.getFeaturedGroupPlayList()
    }

    fun getHomePatchList(): LiveData<ArrayList<Patch>> {
        return homePatchList
    }

    fun getVideoPatchList(): LiveData<ArrayList<Patch>> {
        return videoPatchList
    }

    fun getSearchPatchList(): LiveData<ArrayList<SearchPatch>> {
        return searchPatchList
    }

    fun getVideoBannerResponse(): LiveData<Resource<VideoBannerResponse>> {
        return videoBannerResponse
    }

    fun getRadioListResponse(): LiveData<Resource<RadioListResponse>> {
        return radioListResponse
    }

    fun getPlaylistDetailsWithTrackResponse(): LiveData<Resource<PlaylistDetailsWithTrackResponse>> {
        return playlistDetailsWithTrackResponse
    }

    fun getUserResponse(): LiveData<Resource<UserResponse>> {
        return userResponse
    }

    fun getCurrentSubscriptionResponse(): LiveData<Resource<SubscriptionPlan>> {
        return currentSubscriptionResponse
    }

    fun getDeletePlaylistResponse(): LiveData<Resource<String>> {
        return deletePlaylistResponse
    }

    fun onVideoTabChange(): LiveData<Boolean> {
        return videoTabChange
    }

    fun onVideoTabFullScreen(): LiveData<Boolean> {
        return videoTabFullScreen
    }

    fun getNowPlayingMediaItem(): LiveData<Track> {
        return nowPlayingMediaItem
    }

    fun getPlaybackStateCompat(): PlaybackStateCompat {
        return playbackState
    }

    private fun setHomePatchList(groupList: ArrayList<FeaturedReleaseGroup>) {
        val arrayList: ArrayList<Patch> = ArrayList()
        arrayList.add(Patch("", "", Constants.BILLBOARD_PATCH))
        arrayList.add(Patch("1", "Free Music", Constants.MUSIC_PATCH))
        arrayList.add(Patch("", "Top Playlists", Constants.FEATURED_PLAYLIST_PATCH))
        for (group in groupList) {
            arrayList.add(Patch(group.groupId, group.groupTitle, Constants.RELEASE_PATCH))
        }
        arrayList.add(Patch("", "Special Playlists", Constants.FEATURED_PLAYLIST_GROUP_PATCH))
        arrayList.add(Patch("", "Genres", Constants.GENRES_PATCH))
        arrayList.add(Patch("", "Trending Artists", Constants.TRENDING_ARTIST_PATCH))
        homePatchList.value = arrayList
    }

    private fun setVideoPatchList(groupList: ArrayList<FeaturedReleaseGroup>) {
        val arrayList: ArrayList<Patch> = ArrayList()
        for (group in groupList) {
            arrayList.add(Patch(group.groupId, group.groupTitle, Constants.VIDEO_PATCH))
        }
        videoPatchList.value = arrayList
    }

    fun getTopBannerFromResponse(topBannerResponse: TopBannerResponse?): ArrayList<TopBanner> {
        val list: ArrayList<TopBanner> = ArrayList()
        topBannerResponse?.group?.banners?.forEach { banner ->
            if (banner.link.release != null) {
                list.add(
                    TopBanner(
                        banner.id,
                        banner.image.w50,
                        banner.link.release?.id!!,
                        banner.link.release?.title!!,
                        banner.link.release?.artist?.id!!,
                        banner.link.release?.artist?.name!!,
                        "",
                        "",
                        banner.link.release?.type!!,
                        banner.link.release?.favourite!!
                    )
                )
            } else if (banner.link.playlist != null) {
                list.add(
                    TopBanner(
                        banner.id,
                        banner.image.w50,
                        "",
                        "",
                        "",
                        "",
                        banner.link.playlist?.id!!,
                        banner.link.playlist?.title!!,
                        banner.link.playlist?.type!!,
                        banner.link.playlist?.favourite!!
                    )
                )
            }
        }
        return list
    }

    fun getVideoBannerFromResponse(videoBannerResponse: VideoBannerResponse?): ArrayList<Release> {
        val list: ArrayList<Release> = ArrayList()
        videoBannerResponse?.group?.banners?.forEach { banner ->
            list.add(
                Release(
                    banner.link.release.id,
                    banner.link.release.title,
                    banner.image.w50,
                    banner.link.release.release_date,
                    banner.link.release.artist.id,
                    banner.link.release.artist.name,
                    banner.link.release.type,
                    banner.link.release.copyright,
                    banner.link.release.url,
                    banner.link.release.favourite
                )
            )
        }
        return list
    }

    fun getGenresFromResponse(genresListResponse: GenresListResponse?): ArrayList<Genres> {
        val list: ArrayList<Genres> = ArrayList()
        genresListResponse?.group?.banners?.forEach { banner ->
            list.add(
                Genres(
                    banner.id,
                    banner.image.w50,
                    banner.link.playlist.id,
                    banner.link.playlist.title,
                    banner.link.playlist.favourite
                )
            )
        }
        return list
    }

    fun getTrendingArtistFromResponse(trendingArtistResponse: TrendingArtistResponse?): ArrayList<Artist> {
        val list: ArrayList<Artist> = ArrayList()
        trendingArtistResponse?.group?.banners?.forEach { banner ->
            list.add(
                Artist(
                    banner.id,
                    banner.image.w50,
                    banner.link.artist.id,
                    banner.link.artist.name,
                    banner.link.artist.favourite
                )
            )
        }
        return list
    }

    fun getReleaseGroupFromResponse(releaseGroupResponse: ReleaseGroupResponse?): ArrayList<Release> {
        val list: ArrayList<Release> = ArrayList()
        releaseGroupResponse?.group?.releases?.forEach { release ->
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

    fun getTrackFromProgramResponse(programResponse: ProgramResponse?): ArrayList<Track> {
        val list: ArrayList<Track> = ArrayList()
        programResponse?.program?.tracks?.forEach { track ->
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

    private fun getFeaturedGroupReleaseFromResponse(featuredGroupReleaseResponse: FeaturedGroupReleaseResponse?): ArrayList<FeaturedReleaseGroup> {
        val list: ArrayList<FeaturedReleaseGroup> = ArrayList()
        featuredGroupReleaseResponse?.groups?.forEach { group ->
            list.add(
                FeaturedReleaseGroup(
                    group.id,
                    group.name
                )
            )
        }
        return list
    }

    private fun getVideoReleaseGroupFromResponse(videoReleaseGroupResponse: VideoReleaseGroupResponse?): ArrayList<FeaturedReleaseGroup> {
        val list: ArrayList<FeaturedReleaseGroup> = ArrayList()
        videoReleaseGroupResponse?.group?.banners?.forEach { banner ->
            list.add(
                FeaturedReleaseGroup(
                    banner.link.releaseGroup.id,
                    banner.title
                )
            )
        }
        return list
    }

    fun getFeaturedPlaylistFromResponse(featuredPlaylistResponse: FeaturedPlaylistResponse?): ArrayList<Playlist> {
        val list: ArrayList<Playlist> = ArrayList()
        featuredPlaylistResponse?.playlists?.forEach { playlist ->
            list.add(
                Playlist(
                    playlist.id,
                    playlist.title,
                    if (playlist.covers.isNotEmpty()) playlist.covers[0].width else "",
                    playlist.track_count,
                    playlist.url,
                    playlist.permission,
                    playlist.favourite
                )
            )
        }
        return list
    }

    fun getFeaturedGroupPlaylistFromResponse(featuredGroupPlaylistResponse: FeaturedGroupPlaylistResponse?): ArrayList<FeaturedGroupPlaylist> {
        val list: ArrayList<FeaturedGroupPlaylist> = ArrayList()
        featuredGroupPlaylistResponse?.groups?.forEach { playlistGroup ->
            list.add(
                FeaturedGroupPlaylist(
                    playlistGroup.id,
                    playlistGroup.name,
                    playlistGroup.image.w50,
                    playlistGroup.playlists[0].id
                )
            )
        }
        return list
    }

    private fun getPlaylistFromResponse(playlistDetailsWithTrackResponse: PlaylistDetailsWithTrackResponse?): Playlist? {
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

    fun getRadioListFromResponse(radioListResponse: RadioListResponse?): ArrayList<Radio> {
        val list: ArrayList<Radio> = ArrayList()
        radioListResponse?.group?.banners?.forEach { banner ->
            list.add(
                Radio(
                    banner.id,
                    banner.image.w50,
                    banner.link.playlist.id,
                    banner.link.playlist.title,
                    banner.link.playlist.trackCount,
                    banner.link.playlist.favourite
                )
            )
        }
        return list
    }

    private fun getSearchTrackListFromResponse(searchTrackResponse: SearchTrackResponse?): ArrayList<Track> {
        val list: ArrayList<Track> = ArrayList()
        searchTrackResponse?.tracks?.forEach { track ->
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

    private fun getSearchReleaseListFromResponse(searchReleaseResponse: SearchReleaseResponse?): ArrayList<Release> {
        val list: ArrayList<Release> = ArrayList()
        searchReleaseResponse?.releases?.forEach { release ->
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

    private fun getSearchArtistListFromResponse(artistResponse: SearchArtistResponse?): ArrayList<Artist> {
        val list: ArrayList<Artist> = ArrayList()
        artistResponse?.artists?.forEach { artist ->
            list.add(
                Artist(
                    "",
                    artist.image.width,
                    artist.id,
                    artist.name,
                    artist.favourite
                )
            )
        }
        return list
    }

    private fun getSubscriptionPlanFromResponse(
        subscriptionProductResponse: SubscriptionProductResponse?,
        productId: String,
        subscriptionId: String,
        nextBillingDate: String,
        state: Int
    ) {
        var subscriptionPlan: SubscriptionPlan? = null
        subscriptionProductResponse?.products?.forEach {
            if (productId == it.id && it.description != null) {
                subscriptionPlan = SubscriptionPlan(
                    it.id,
                    subscriptionId,
                    it.name,
                    it.description,
                    it.surface_id,
                    it.type,
                    state,
                    it.recurring_price,
                    it.recurring_interval,
                    it.recurring_interval_unit,
                    it.trial_price,
                    nextBillingDate
                )
            }
        }
        if (subscriptionPlan != null) {
            currentSubscriptionResponse.value = Resource.success(
                subscriptionPlan!!
            )
        } else {
            currentSubscriptionResponse.value = Resource.error(null, "", -1)
        }
    }

    fun getUserFromResponse(userResponse: UserResponse?): User? {
        var user: User? = null
        if (userResponse != null) {
            user = User(userResponse.user.id, userResponse.user.username, userResponse.user.msisdn)
            preferenceManager.setUserResponse(userResponse)
        }
        return user
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