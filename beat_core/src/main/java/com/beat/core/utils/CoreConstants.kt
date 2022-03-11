package com.beat.core.utils

object CoreConstants {

    //Shared Preference name and keys
    const val SHARED_PREFERENCES_NAME = "shared_preferences"
    const val ACCESS_TOKEN = "access_token"
    const val EXPIRE_IN = "expires_in"
    const val TOKEN_TYPE = "token_type"
    const val REFRESH_TOKEN = "refresh_token"
    const val USER_ID = "user_id"

    //APP Settings
    const val AUDIO_QUALITY = "audio_quality"
    const val PLAY_ON_OFFLINE = "play_on_offline"
    const val STREAM_ONLY_ON_OFFLINE = "stream_only_on_offline"
    const val DOWNLOAD_ONLY_ON_OFFLINE = "download_only_on_offline"

    //GRANT_TYPES
    const val GRANT_TYPE_PASSWORD = "password"
    const val GRANT_TYPE_VERIFICATION_CODE = "verification_code"
    const val GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials"
    const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"

    //Error Code
    const val UNAUTHORIZED_CODE = 401

    //ImageSize
    const val IMAGE_SIZE_WIDTH_PARAMETER = "w500"
    const val IMAGE_SIZE_HEIGHT_PARAMETER = "h500"
    const val IMAGE_SIZE = "$IMAGE_SIZE_WIDTH_PARAMETER,$IMAGE_SIZE_HEIGHT_PARAMETER"

    //Favorite
    const val FAVORITE_TRACK = "track"
    const val FAVORITE_RELEASE = "release"
    const val FAVORITE_ARTIST = "artist"
    const val FAVORITE_PLAYLIST = "playlist"

}