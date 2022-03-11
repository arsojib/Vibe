package com.beat.core.data.storage.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ReleaseWithTracks(
    @Embedded val release: Release,
    @Relation(
        parentColumn = "releaseId",
        entityColumn = "trackId",
        associateBy = Junction(ReleaseTrackCrossRef::class)
    ) val tracks: List<Track>
)