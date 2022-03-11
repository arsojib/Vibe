package com.beat.core.data.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beat.core.data.storage.database.dao.PlaylistDao
import com.beat.core.data.storage.database.dao.ReleaseDao
import com.beat.core.data.storage.database.dao.TrackDao
import com.beat.core.data.storage.database.entity.*

@Database(
    entities = [Track::class, Release::class, Playlist::class, ReleaseTrackCrossRef::class, PlaylistTrackCrossRef::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    abstract fun releaseDao(): ReleaseDao

    abstract fun playListDao(): PlaylistDao

}