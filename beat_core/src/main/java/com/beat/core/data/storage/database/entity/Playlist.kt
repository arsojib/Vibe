package com.beat.core.data.storage.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Playlist(
    @PrimaryKey val playlistId: String,
    @ColumnInfo(name = "title") val playlistTitle: String,
    @ColumnInfo(name = "image") val playlistImage: String,
    @ColumnInfo(name = "trackCount") val trackCount: Int,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "permission") val permission: String,
    @ColumnInfo(name = "favorite") val favourite: Boolean
)