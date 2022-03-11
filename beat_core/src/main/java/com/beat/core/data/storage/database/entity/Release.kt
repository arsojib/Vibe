package com.beat.core.data.storage.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Release(
    @PrimaryKey val releaseId: String,
    @ColumnInfo(name = "title") val releaseTitle: String,
    @ColumnInfo(name = "image") val releaseImage: String,
    @ColumnInfo(name = "date") val releaseDate: String,
    @ColumnInfo(name = "artistId") val artistId: String,
    @ColumnInfo(name = "artistName") val artistName: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "copyRight") val copyRight: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "favorite") val favorite: Boolean
)