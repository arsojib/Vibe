package com.beat.core.data.rest

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.beat.core.R
import com.beat.core.data.model.*
import com.beat.core.data.storage.PreferenceManager
import com.beat.core.di.module.WithOauth
import com.beat.core.di.module.WithoutOauth
import com.beat.core.utils.CoreConstants
import javax.inject.Inject

class Repository @Inject constructor(
    private val context: Context,
    @WithOauth private val withOauthApiService: ApiService,
    @WithoutOauth private val withOutOauthApiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    private fun getAccessToken() = preferenceManager.getAuth()

    private fun getUserId() = preferenceManager.getUserId()

    private fun getImageSize(): String {
        return "w50,h50"
    }

    private fun getBigImageSize(): String {
        return CoreConstants.IMAGE_SIZE
    }

    suspend fun getLoginToken(
        loginTokenResponse: MutableLiveData<Resource<LoginTokenResponse>>,
        grantType: String,
        clientId: String,
        clientSecret: String,
        username: String,
        password: String
    ) {
        loginTokenResponse.value = Resource.loading(null)
        try {
            val response = withOutOauthApiService.loginToken(
                grantType,
                clientId,
                clientSecret,
                username,
                password
            )

            if (response.isSuccessful) {
                loginTokenResponse.value = Resource.success(response.body()!!)
            } else {
                val errorText: String =
                    if (response.code() == CoreConstants.UNAUTHORIZED_CODE) context.getString(R.string.the_mobile_number_or_password_is_incorrect) else context.getString(
                        R.string.something_went_wrong_please_try_again
                    )
                loginTokenResponse.value = Resource.error(null, errorText, response.code())
            }
        } catch (e: Exception) {
            loginTokenResponse.value = Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getSignUpToken(
        signUpTokenResponse: MutableLiveData<Resource<LoginTokenResponse>>,
        grantType: String,
        clientId: String,
        clientSecret: String,
        msisdn: String,
        code: String
    ) {
        signUpTokenResponse.value = Resource.loading(null)
        try {
            val response = withOutOauthApiService.signUpToken(
                grantType,
                clientId,
                clientSecret,
                msisdn,
                code
            )

            if (response.isSuccessful) {
                signUpTokenResponse.value = Resource.success(response.body()!!)
            } else {
                val errorText: String =
                    if (response.code() == CoreConstants.UNAUTHORIZED_CODE) context.getString(R.string.you_entered_the_wrong_code_try_again) else context.getString(
                        R.string.something_went_wrong_please_try_again
                    )
                signUpTokenResponse.value = Resource.error(null, errorText, response.code())
            }
        } catch (e: Exception) {
            signUpTokenResponse.value = Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    //addNewUserResponse getting its response because send verification is part of signUp/addUser flow
    suspend fun sendVerificationCode(
        addNewUserResponse: MutableLiveData<Resource<String>>,
        mobileNumber: String
    ) {
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["msisdn"] = mobileNumber
        try {
            val response = withOauthApiService.sendVerificationCode(
                getAccessToken(), hashMap
            )

            if (response.isSuccessful) {
                addNewUserResponse.value = Resource.success(response.body()!!)
            } else {
                addNewUserResponse.value = Resource.error(
                    null, context.getString(
                        R.string.something_went_wrong_please_try_again
                    ), response.code()
                )
            }
        } catch (e: Exception) {
            addNewUserResponse.value = Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun addNewUser(
        addNewUserResponse: MutableLiveData<Resource<String>>,
        mobileNumber: String
    ) {
        addNewUserResponse.value = Resource.loading(null)
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["msisdn"] = mobileNumber
        try {
            val response = withOauthApiService.addNewUser(
                getAccessToken(), hashMap
            )

            if (response.isSuccessful) {
                sendVerificationCode(addNewUserResponse, mobileNumber)
            } else {
                if (response.code() == 409) {
                    sendVerificationCode(addNewUserResponse, mobileNumber)
                } else {
                    addNewUserResponse.value = Resource.error(
                        null, context.getString(
                            R.string.something_went_wrong_please_try_again
                        ), response.code()
                    )
                }
            }
        } catch (e: Exception) {
            addNewUserResponse.value = Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getGuestToken(
        grantType: String,
        clientId: String,
        clientSecret: String
    ): Resource<LoginTokenResponse> {
        try {
            val response = withOutOauthApiService.guestToken(grantType, clientId, clientSecret)

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null, context.getString(
                        R.string.something_went_wrong_please_login_to_continue
                    ), response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getRefreshToken(
        grantType: String,
        clientId: String,
        clientSecret: String,
        refreshToken: String
    ): Resource<LoginTokenResponse> {
        try {
            val response =
                withOutOauthApiService.refreshToken(grantType, clientId, clientSecret, refreshToken)
            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null, context.getString(
                        R.string.something_went_wrong_please_login_to_continue
                    ), response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun initiateResetPassword(
        initiateResetPasswordResponse: MutableLiveData<Event<Resource<String>>>,
        mobileNumber: String
    ) {
        initiateResetPasswordResponse.value = Event(Resource.loading(null))
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["msisdn"] = mobileNumber
        try {
            val response = withOauthApiService.initiateResetPassword(
                getAccessToken(), hashMap
            )

            if (response.isSuccessful) {
                initiateResetPasswordResponse.value = Event(Resource.success(response.body()!!))
            } else {
                initiateResetPasswordResponse.value = Event(
                    Resource.error(
                        null, context.getString(
                            R.string.something_went_wrong_please_try_again
                        ), response.code()
                    )
                )
            }
        } catch (e: Exception) {
            initiateResetPasswordResponse.value = Event(
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    -1
                )
            )
        }
    }

    suspend fun resetPassword(
        resetPasswordResponse: MutableLiveData<Event<Resource<String>>>,
        mobileNumber: String,
        code: String,
        newPassword: String
    ) {
        resetPasswordResponse.value = Event(Resource.loading(null))
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["msisdn"] = mobileNumber
        hashMap["code"] = code
        hashMap["password"] = newPassword
        try {
            val response = withOauthApiService.resetPassword(
                getAccessToken(), hashMap
            )

            if (response.isSuccessful) {
                resetPasswordResponse.value = Event(Resource.success(response.body()!!))
            } else {
                val errorMessage: String = if (response.code() == 400) {
                    context.getString(
                        R.string.you_entered_the_wrong_code_try_again
                    )
                } else {
                    context.getString(
                        R.string.something_went_wrong_please_try_again
                    )
                }
                resetPasswordResponse.value = Event(
                    Resource.error(
                        null, errorMessage, response.code()
                    )
                )
            }
        } catch (e: Exception) {
            resetPasswordResponse.value = Event(
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    -1
                )
            )
        }
    }

    suspend fun getUser(): Resource<UserResponse> {
        try {
            val response =
                withOauthApiService.getUser(
                    getUserId(),
                    getAccessToken()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getRadioList(groupId: String): Resource<RadioListResponse> {
        try {
            val response =
                withOauthApiService.getRadioList(groupId, getAccessToken(), getImageSize())

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getTopBannerList(groupId: String): Resource<TopBannerResponse> {
        try {
            val response =
                withOauthApiService.getTopBannerList(groupId, getAccessToken(), getImageSize())

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getGenresList(groupId: String): Resource<GenresListResponse> {
        try {
            val response =
                withOauthApiService.getGenresList(groupId, getAccessToken(), getImageSize())

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getTrendingArtistList(groupId: String): Resource<TrendingArtistResponse> {
        try {
            val response =
                withOauthApiService.getTrendingArtistList(groupId, getAccessToken(), getImageSize())

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getVideoBannerList(
        videoBannerResponse: MutableLiveData<Resource<VideoBannerResponse>>,
        groupId: String
    ) {
        try {
            val response =
                withOauthApiService.getVideoBannerList(groupId, getAccessToken(), getImageSize())

            if (response.isSuccessful) {
                videoBannerResponse.value = Resource.success(response.body()!!)
            } else {
                videoBannerResponse.value = Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            videoBannerResponse.value = Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getReleaseGroup(groupId: String): Resource<ReleaseGroupResponse> {
        try {
            val response =
                withOauthApiService.getReleaseGroup(groupId, getAccessToken(), getBigImageSize())

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getFeaturedGroupRelease(): Resource<FeaturedGroupReleaseResponse> {
        try {
            val response = withOauthApiService.getFeaturedGroupRelease(getAccessToken())

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getVideoGroupRelease(groupId: String): Resource<VideoReleaseGroupResponse> {
        try {
            val response =
                withOauthApiService.getVideoReleaseGroup(
                    groupId,
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getProgram(programId: String): Resource<ProgramResponse> {
        try {
            val response =
                withOauthApiService.getProgram(
                    programId,
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getFeaturedPlayList(): Resource<FeaturedPlaylistResponse> {
        try {
            val response =
                withOauthApiService.getFeaturedPlayList(getAccessToken(), getBigImageSize())

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getFeaturedGroupPlayList(): Resource<FeaturedGroupPlaylistResponse> {
        try {
            val response =
                withOauthApiService.getFeaturedGroupPlayList(getAccessToken(), getImageSize())

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getSearchTrack(query: String): Resource<SearchTrackResponse> {
        try {
            val response =
                withOauthApiService.getSearchTrack(
                    getAccessToken(),
                    getBigImageSize(),
                    "tracks=0-3",
                    query
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getSearchRelease(query: String): Resource<SearchReleaseResponse> {
        try {
            val response =
                withOauthApiService.getSearchRelease(
                    getAccessToken(),
                    getBigImageSize(),
                    "releases=0-3",
                    query
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getSearchArtist(query: String): Resource<SearchArtistResponse> {
        try {
            val response =
                withOauthApiService.getSearchArtist(
                    getAccessToken(),
                    getBigImageSize(),
                    "artists=0-3",
                    query
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getReleaseDetailsWithTrack(
        releaseId: String
    ): Resource<ReleaseDetailsWithTrackResponse> {
        try {
            val response =
                withOauthApiService.getReleaseDetailsWithTrack(
                    releaseId,
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getTopReleaseByArtist(
        artistId: String
    ): Resource<TopReleaseByArtistResponse> {
        try {
            val response =
                withOauthApiService.getTopReleaseByArtist(
                    artistId,
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getTopReleaseByTypeByArtist(
        artistId: String
    ): Resource<TopReleaseByArtistResponse> {
        try {
            val response =
                withOauthApiService.getTopReleaseByTypeByArtist(
                    artistId,
                    getAccessToken(),
                    getBigImageSize(),
                    "audio"
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getPlaylistDetailsWithTrack(
        playlistId: String
    ): Resource<PlaylistDetailsWithTrackResponse> {
        try {
            val response =
                withOauthApiService.getPlaylistDetailsWithTrack(
                    playlistId,
                    getAccessToken(),
                    getBigImageSize(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getTopTrackByArtist(
        artistId: String
    ): Resource<TopTrackByArtistResponse> {
        try {
            val response =
                withOauthApiService.getTopTrackByArtist(
                    artistId,
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getArtistDetails(
        artistId: String
    ): Resource<ArtistDetailsResponse> {
        try {
            val response =
                withOauthApiService.getArtistDetails(
                    artistId,
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getArtistBio(
        artistId: String
    ): Resource<ArtistBioResponse> {
        try {
            val response =
                withOauthApiService.getArtistBio(
                    artistId,
                    getAccessToken()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getTopVideoByArtist(
        artistId: String
    ): Resource<TopVideoByArtistResponse> {
        try {
            val response =
                withOauthApiService.getTopVideoByArtist(
                    artistId,
                    getAccessToken(),
                    getBigImageSize(),
                    "video"
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getFavoriteTracks(): Resource<FavoriteTracksResponse> {
        try {
            val response =
                withOauthApiService.getFavoriteTracks(
                    getUserId(),
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getFavoriteRelease(): Resource<FavoriteReleaseResponse> {
        try {
            val response =
                withOauthApiService.getFavoriteRelease(
                    getUserId(),
                    getAccessToken(),
                    getBigImageSize(),
                    "audio"
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getFavoriteVideos(): Resource<FavoriteVideosResponse> {
        try {
            val response =
                withOauthApiService.getFavoriteVideos(
                    getUserId(),
                    getAccessToken(),
                    getBigImageSize(),
                    "video"
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getFavoriteArtists(): Resource<FavoriteArtistResponse> {
        try {
            val response =
                withOauthApiService.getFavoriteArtists(
                    getUserId(),
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getFavoritePlaylist(): Resource<FavoritePlaylistResponse> {
        try {
            val response =
                withOauthApiService.getFavoritePlaylist(
                    getUserId(),
                    getAccessToken(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getUserPlaylist(range: String): Resource<UserPlaylistResponse> {
        try {
            val response =
                withOauthApiService.getUserPlaylist(
                    getUserId(),
                    getAccessToken(),
                    getBigImageSize(),
                    range
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun createUserPlaylist(title: String): Event<Resource<CreateUserPlaylistResponse>> {
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["title"] = title
        try {
            val response =
                withOauthApiService.createUserPlaylist(
                    getAccessToken(),
                    hashMap
                )

            return if (response.isSuccessful) {
                Event(Resource.success(response.body()!!))
            } else {
                Event(
                    Resource.error(
                        null,
                        context.getString(R.string.something_went_wrong_please_try_again),
                        response.code()
                    )
                )
            }
        } catch (e: Exception) {
            return Event(
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    -1
                )
            )
        }
    }

    suspend fun addTracksToUserPlaylist(
        playlistId: String,
        data: String
    ): Resource<String> {
        try {
            val response =
                withOauthApiService.addTracksToPlaylist(
                    playlistId,
                    getAccessToken(),
                    data
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun patchFavorite(
        id: String,
        type: String,
        favorite: Boolean
    ): Resource<String> {
        val hashMap: HashMap<String, Boolean> = HashMap()
        hashMap["favourite"] = favorite
        try {
            val response = when (type) {
                CoreConstants.FAVORITE_RELEASE -> {
                    withOauthApiService.patchReleaseFavorite(
                        id,
                        getAccessToken(),
                        hashMap
                    )
                }
                CoreConstants.FAVORITE_ARTIST -> {
                    withOauthApiService.patchArtistFavorite(
                        id,
                        getAccessToken(),
                        hashMap
                    )
                }
                CoreConstants.FAVORITE_PLAYLIST -> {
                    withOauthApiService.patchPlaylistFavorite(
                        id,
                        getAccessToken(),
                        hashMap
                    )
                }
                else -> {
                    withOauthApiService.patchTrackFavorite(
                        id,
                        getAccessToken(),
                        hashMap
                    )
                }
            }

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun deletePlaylist(
        playlistId: String
    ): Resource<String> {
        try {
            val response =
                withOauthApiService.deletePlaylist(
                    playlistId,
                    getAccessToken()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getModifiedPlaylistDetailsWithTrack(
        playlistId: String,
        data: String
    ): Resource<String> {
        try {
            val response =
                withOauthApiService.getPlaylistDetailsWithTrack(
                    playlistId,
                    getAccessToken(),
                    getBigImageSize(),
                    getBigImageSize()
                )

            return if (response.isSuccessful) {
                addTracksToUserPlaylist(
                    playlistId,
                    idsFromTrackList(data, response.body()!!)
                )
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getOnlineStream(
        trackId: String,
        format: String,
        releaseId: String
    ): Resource<StreamResponse> {
        try {
            val response =
                withOauthApiService.getOnlineStream(
                    trackId,
                    getAccessToken(),
                    format,
                    "release/$releaseId"
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getOfflineStream(
        trackId: String,
        format: String
    ): Resource<StreamResponse> {
        try {
            val response =
                withOauthApiService.getOfflineStream(
                    trackId,
                    getAccessToken(),
                    format
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getSubscriptionProducts(): Resource<SubscriptionProductResponse> {
        try {
            val response =
                withOauthApiService.getSubscriptionProducts(
                    getAccessToken()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getCurrentSubscription(): Resource<CurrentSubscriptionResponse> {
        try {
            val response =
                withOauthApiService.getCurrentSubscription(
                    getUserId(),
                    getAccessToken()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun activateSubscription(productId: String): Resource<String> {
        try {
            val hashMap: HashMap<String, String> = HashMap()
            hashMap["product_id"] = productId
            val response =
                withOauthApiService.activateSubscription(
                    getUserId(),
                    getAccessToken(),
                    hashMap
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun deleteSubscription(subscriptionId: String): Resource<String> {
        try {
            val response =
                withOauthApiService.deleteSubscription(
                    getUserId(),
                    subscriptionId,
                    getAccessToken()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    suspend fun getRewardPoint(): Resource<RewardPointResponse> {
        try {
            val response =
                withOauthApiService.getRewardPoint(
                    getAccessToken()
                )

            return if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(
                    null,
                    context.getString(R.string.something_went_wrong_please_try_again),
                    response.code()
                )
            }
        } catch (e: Exception) {
            return Resource.error(
                null,
                context.getString(R.string.something_went_wrong_please_try_again),
                -1
            )
        }
    }

    private fun idsFromTrackList(
        data: String,
        playlistDetailsWithTrackResponse: PlaylistDetailsWithTrackResponse
    ): String {
        val mapAsString = StringBuilder(data)
        mapAsString.delete(mapAsString.length - 2, mapAsString.length).append(",")
        playlistDetailsWithTrackResponse.playlist.tracks.forEach { tracks ->
            mapAsString.append("{\"id\"" + ":\"" + tracks.id + "\"},")
        }
        mapAsString.delete(mapAsString.length - 1, mapAsString.length).append("]}")
        return mapAsString.toString()
    }

}