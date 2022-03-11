package com.beat.util.listener

import com.beat.data.model.Artist

interface ArtistClick {

    fun onArtistClick(artist: Artist, position: Int)

}