package com.beat.core.data.storage.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["releaseId", "trackId"])
data class ReleaseTrackCrossRef(
    val releaseId: String,
    @ColumnInfo(index = true)
    val trackId: String
)