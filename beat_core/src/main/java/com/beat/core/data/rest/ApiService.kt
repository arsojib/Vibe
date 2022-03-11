package com.beat.core.data.rest

import com.beat.core.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    //Authentication Api start
    @POST("oauth2/token")
    @FormUrlEncoded
    suspend fun loginToken(
        @Field("grant_type") grantType: String?,
        @Field("client_id") clientId: String?,
        @Field("client_secret") clientSecret: String?,
        @Field("username") username: String?,
        @Field("password") password: String?
    ): Response<LoginTokenResponse>

    @POST("oauth2/token")
    @FormUrlEncoded
    suspend fun signUpToken(
        @Field("grant_type") grantType: String?,
        @Field("client_id") clientId: String?,
        @Field("client_secret") clientSecret: String?,
        @Field("msisdn") msisdn: String?,
        @Field("code") code: String?
    ): Response<LoginTokenResponse>

    @POST("oauth2/token")
    @FormUrlEncoded
    suspend fun guestToken(
        @Field("grant_type") grantType: String?,
        @Field("client_id") clientId: String?,
        @Field("client_secret") clientSecret: String?
    ): Response<LoginTokenResponse>

    @POST("oauth2/token")
    @FormUrlEncoded
    suspend fun refreshToken(
        @Field("grant_type") grantType: String?,
        @Field("client_id") clientId: String?,
        @Field("client_secret") clientSecret: String?,
        @Field("refresh_token") refreshToken: String?
    ): Response<LoginTokenResponse>

    @POST("codes")
    suspend fun sendVerificationCode(
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, String>
    ): Response<String>

    @POST("users")
    suspend fun addNewUser(
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, String>
    ): Response<String>

    @POST("passwords/requests")
    suspend fun initiateResetPassword(
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, String>
    ): Response<String>

    @POST("passwords")
    suspend fun resetPassword(
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, String>
    ): Response<String>
    //Authentication Api end

    //Main Page Api start
    @GET("users/{user_id}")
    suspend fun getUser(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String
    ): Response<UserResponse>

    @GET("banners/groups/{group_id}")
    suspend fun getRadioList(
        @Path(value = "group_id", encoded = true) groupId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Banner-Image-Sizes") imageSize: String
    ): Response<RadioListResponse>

    @GET("banners/groups/{group_id}")
    suspend fun getTopBannerList(
        @Path(value = "group_id", encoded = true) groupId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Banner-Image-Sizes") imageSize: String
    ): Response<TopBannerResponse>

    @GET("banners/groups/{group_id}")
    suspend fun getGenresList(
        @Path(value = "group_id", encoded = true) groupId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Banner-Image-Sizes") imageSize: String
    ): Response<GenresListResponse>

    @GET("banners/groups/{group_id}")
    suspend fun getTrendingArtistList(
        @Path(value = "group_id", encoded = true) groupId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Banner-Image-Sizes") imageSize: String
    ): Response<TrendingArtistResponse>

    @GET("banners/groups/{group_id}")
    suspend fun getVideoBannerList(
        @Path(value = "group_id", encoded = true) groupId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Banner-Image-Sizes") imageSize: String
    ): Response<VideoBannerResponse>

    @GET("banners/groups/{group_id}")
    suspend fun getVideoReleaseGroup(
        @Path(value = "group_id", encoded = true) groupId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Banner-Image-Sizes") imageSize: String
    ): Response<VideoReleaseGroupResponse>

    //Free Music patch for Vibe
    @GET("radio/programs/{program_id}")
    suspend fun getProgram(
        @Path(value = "program_id", encoded = true) programId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<ProgramResponse>

    //Top Playlists for BLVibe
    @GET("playlists/featured")
    suspend fun getFeaturedPlayList(
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<FeaturedPlaylistResponse>

    //Playlist For your Mood for BLVIbe
    @GET("playlists/groups/featured")
    suspend fun getFeaturedGroupPlayList(
        @Header("Authorization") authorization: String,
        @Header("X-Playlist-Group-Image-Sizes") imageSize: String
    ): Response<FeaturedGroupPlaylistResponse>

    @GET("releases/groups/featured")
    suspend fun getFeaturedGroupRelease(
        @Header("Authorization") authorization: String
    ): Response<FeaturedGroupReleaseResponse>

    @GET("releases/groups/{group_id}")
    suspend fun getReleaseGroup(
        @Path(value = "group_id", encoded = true) groupId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<ReleaseGroupResponse>

    @GET("playlists")
    suspend fun getSearchPlaylist(
        @Header("Authorization") authorization: String,
        @Header("X-Playlist-Cover-Sizes") imageSize: String,
        @Header("Range") range: String,
        @Query("query") query: String
    ): Response<SearchPlaylistResponse>

    @GET("artists")
    suspend fun getSearchArtist(
        @Header("Authorization") authorization: String,
        @Header("X-Artist-Image-Sizes") imageSize: String,
        @Header("Range") range: String,
        @Query("query") query: String
    ): Response<SearchArtistResponse>

    @GET("releases")
    suspend fun getSearchRelease(
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String,
        @Header("Range") range: String,
        @Query("query") query: String
    ): Response<SearchReleaseResponse>

    @GET("tracks")
    suspend fun getSearchTrack(
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String,
        @Header("Range") range: String,
        @Query("query") query: String
    ): Response<SearchTrackResponse>

    @GET("releases/{release_id}")
    suspend fun getReleaseDetailsWithTrack(
        @Path(value = "release_id", encoded = true) releaseId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<ReleaseDetailsWithTrackResponse>

    @GET("artists/{artist_id}/releases/top")
    suspend fun getTopReleaseByArtist(
        @Path(value = "artist_id", encoded = true) artistId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<TopReleaseByArtistResponse>

    @GET("artists/{artist_id}/releases/top")
    suspend fun getTopReleaseByTypeByArtist(
        @Path(value = "artist_id", encoded = true) artistId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String,
        @Query("types") types: String
    ): Response<TopReleaseByArtistResponse>


    @GET("playlists/{playlist_id}")
    suspend fun getPlaylistDetailsWithTrack(
        @Path(value = "playlist_id", encoded = true) playlistId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Playlist-Group-Image-Sizes") playlistImageSize: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<PlaylistDetailsWithTrackResponse>

    @GET("artists/{artist_id}/tracks/top")
    suspend fun getTopTrackByArtist(
        @Path(value = "artist_id", encoded = true) artistId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<TopTrackByArtistResponse>

    @GET("artists/{artist_id}")
    suspend fun getArtistDetails(
        @Path(value = "artist_id", encoded = true) artistId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Artist-Image-Sizes") imageSize: String
    ): Response<ArtistDetailsResponse>

    @GET("artists/{artist_id}/bio")
    suspend fun getArtistBio(
        @Path(value = "artist_id", encoded = true) artistId: String,
        @Header("Authorization") authorization: String
    ): Response<ArtistBioResponse>

    @GET("artists/{artist_id}/releases/top")
    suspend fun getTopVideoByArtist(
        @Path(value = "artist_id", encoded = true) artistId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String,
        @Query("types") types: String
    ): Response<TopVideoByArtistResponse>


    //Need to change the model
    @GET("users/{user_id}/favourites/tracks")
    suspend fun getFavoriteTracks(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<FavoriteTracksResponse>

    @GET("users/{user_id}/favourites/releases")
    suspend fun getFavoriteRelease(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String,
        @Query("types") types: String
    ): Response<FavoriteReleaseResponse>

    @GET("users/{user_id}/favourites/releases")
    suspend fun getFavoriteVideos(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String,
        @Query("types") types: String
    ): Response<FavoriteVideosResponse>

    @GET("users/{user_id}/favourites/artists")
    suspend fun getFavoriteArtists(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Artist-Image-Sizes") imageSize: String
    ): Response<FavoriteArtistResponse>

    @GET("users/{user_id}/favourites/playlists")
    suspend fun getFavoritePlaylist(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String
    ): Response<FavoritePlaylistResponse>

    @GET("users/{user_id}/playlists")
    suspend fun getUserPlaylist(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Release-Cover-Sizes") imageSize: String,
        @Header("Range") range: String
    ): Response<UserPlaylistResponse>

    @POST("playlists")
    suspend fun createUserPlaylist(
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, String>
    ): Response<CreateUserPlaylistResponse>

    @PATCH("playlists/{playlist_id}")
    suspend fun addTracksToPlaylist(
        @Path(value = "playlist_id", encoded = true) playlistId: String,
        @Header("Authorization") authorization: String,
        @Body data: String
    ): Response<String>

    //Favorite add/remove apis
    @PATCH("tracks/{track_id}")
    suspend fun patchTrackFavorite(
        @Path(value = "track_id", encoded = true) trackId: String,
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, Boolean>
    ): Response<String>

    @PATCH("releases/{release_id}")
    suspend fun patchReleaseFavorite(
        @Path(value = "release_id", encoded = true) releaseId: String,
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, Boolean>
    ): Response<String>

    @PATCH("artists/{artist_id}")
    suspend fun patchArtistFavorite(
        @Path(value = "artist_id", encoded = true) artistId: String,
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, Boolean>
    ): Response<String>

    @PATCH("playlists/{playlist_id}")
    suspend fun patchPlaylistFavorite(
        @Path(value = "playlist_id", encoded = true) playlistId: String,
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, Boolean>
    ): Response<String>

    @DELETE("playlists/{playlist_id}")
    suspend fun deletePlaylist(
        @Path(value = "playlist_id", encoded = true) playlistId: String,
        @Header("Authorization") authorization: String
    ): Response<String>

    //Formats = {*/high, */normal, */low, h264/*}
    @GET("streams/online/{track_id}")
    suspend fun getOnlineStream(
        @Path(value = "track_id", encoded = true) trackId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Stream-Formats") streamFormats: String,
        @Header("X-Stream-Context") streamContext: String
    ): Response<StreamResponse>

    @GET("streams/offline/{track_id}")
    suspend fun getOfflineStream(
        @Path(value = "track_id", encoded = true) trackId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Stream-Formats") streamFormats: String
    ): Response<StreamResponse>

    @GET("products")
    suspend fun getSubscriptionProducts(
        @Header("Authorization") authorization: String
    ): Response<SubscriptionProductResponse>

    @GET("users/{user_id}/subscriptions")
    suspend fun getCurrentSubscription(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String
    ): Response<CurrentSubscriptionResponse>

    @POST("users/{user_id}/migrations")
    suspend fun activateSubscription(
        @Path(value = "user_id", encoded = true) userId: String,
        @Header("Authorization") authorization: String,
        @Body hashMap: HashMap<String, String>
    ): Response<String>

    @DELETE("users/{user_id}/subscriptions/{subscription_id}")
    suspend fun deleteSubscription(
        @Path(value = "user_id", encoded = true) userId: String,
        @Path(value = "subscription_id", encoded = true) subscriptionId: String,
        @Header("Authorization") authorization: String
    ): Response<String>

    @GET("rewards/balances")
    suspend fun getRewardPoint(
        @Header("Authorization") authorization: String
    ): Response<RewardPointResponse>

}