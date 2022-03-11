package com.beat.util.listener

interface ReleasePopUpMenuClickListener {

    fun onFavorite(id: String, type: String, isFav: Boolean)

    fun onGoToArtist(artistId: String, artistName: String)

    fun onShareOnSocialMedia(url: String)

}