package com.beat.util.listener

interface TrackPopUpMenuClickListener {

    fun onFavorite(id: String, type: String, isFav: Boolean)

    fun onTrackAddToPlaylist(trackId: String)

    fun onGoToArtist(artistId: String, artistName: String)

    fun onGotoAlbum(releaseId: String, artistId: String, artistName: String)

    fun onRemoveFromDownload(trackId: String)

    fun onShareOnSocialMedia(url: String)

}