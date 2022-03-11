package com.beat.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track(
    val trackId: String,
    var trackTitle: String,
    var trackImage: String,
    val duration: Int,
    var releaseId: String,
    val artistId: String,
    val artistName: String,
    val url: String,
    var favorite: Boolean
) : Parcelable