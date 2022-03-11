package com.beat.core.utils

interface VideoPlayerResponseListener {

    fun onVideoMediaFetchingError(errorCode: Int, errorMessage: String)

}