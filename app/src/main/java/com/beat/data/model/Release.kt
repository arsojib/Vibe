package com.beat.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Release(
    val releaseId: String,
    val releaseTitle: String,
    val releaseImage: String,
    val releaseDate: String,
    val artistId: String,
    val artistName: String,
    val type: String,
    val copyRight: String,
    val url: String,
    var favorite: Boolean
) : Parcelable