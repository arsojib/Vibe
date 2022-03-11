package com.beat.core.data.storage.database.dao

import androidx.room.*
import com.beat.core.data.storage.database.entity.Release
import com.beat.core.data.storage.database.entity.ReleaseTrackCrossRef
import com.beat.core.data.storage.database.entity.ReleaseWithTracks

@Dao
interface ReleaseDao {

    @Query("Select * From `release`")
    suspend fun getAll(): List<Release>

    @Transaction
    @Query("Select * From `release` Where releaseId = :id")
    suspend fun getReleaseWithTracks(id: String): ReleaseWithTracks

    @Query("Select * From releasetrackcrossref Where trackId = :trackId")
    suspend fun getReleaseTrackCrossRef(trackId: String): List<ReleaseTrackCrossRef>

    @Query("SELECT * FROM releasetrackcrossref where releaseId = :releaseId GROUP BY trackId HAVING COUNT(trackId) == 1")
    suspend fun getReleaseTrackCrossRefsSingleton(releaseId: String): List<ReleaseTrackCrossRef>

    @Query("Select Exists(Select * From `release` Where releaseId = :id)")
    suspend fun isExist(id: String): Boolean

    @Insert
    suspend fun insertRelease(release: Release)

    @Insert
    suspend fun insertReleaseTracksCrossRef(releaseTrackCrossRef: ReleaseTrackCrossRef)

    @Update
    suspend fun updateRelease(release: Release)

    @Query("Update `release` Set favorite = :favorite Where releaseId = :id")
    suspend fun updateFavoriteStatus(id: String, favorite: Boolean)

    @Query("Delete From `release` Where releaseId = :id")
    suspend fun deleteRelease(id: String)

    @Query("Delete From `release` Where releaseId In (:ids)")
    suspend fun deleteReleases(ids: Array<String>)

    @Query("Delete From releasetrackcrossref Where releaseId In (:releaseIds)")
    suspend fun deleteReleaseTrackCrossRef(releaseIds: Array<String>)

}