package com.beat.core.data.storage.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Track(
    @PrimaryKey val trackId: String,
    @ColumnInfo(name = "title") val trackTitle: String,
    @ColumnInfo(name = "image") val trackImage: String,
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "releaseId") val releaseId: String?,
    @ColumnInfo(name = "playlistId") val playlistId: String?,
    @ColumnInfo(name = "artistId") val artistId: String,
    @ColumnInfo(name = "artistName") val artistName: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "favorite") val favorite: Boolean,
    @ColumnInfo(name = "downloaded") val downloaded: Boolean
)