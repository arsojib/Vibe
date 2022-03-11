package com.beat.core.data.storage.database.dao

import androidx.room.*
import com.beat.core.data.storage.database.entity.Track

@Dao
interface TrackDao {

    @Query("Select * From track")
    suspend fun getAll(): List<Track>

    @Query("Select * From track where trackId In (:ids) ")
    suspend fun getTracks(ids: Array<String>): List<Track>

    @Insert
    suspend fun insertTrack(track: Track)

    @Query("Update track Set favorite = :favorite Where trackId = :id")
    suspend fun updateFavoriteStatus(id: String, favorite: Boolean)

    @Query("Update track Set downloaded = :downloaded Where trackId = :id")
    suspend fun updateDownloadStatus(id: String, downloaded: Boolean)

    @Update
    suspend fun updateTrack(track: Track)

    @Query("Delete From track Where trackId = :id")
    suspend fun deleteTrack(id: String)

    @Query("Delete From track Where trackId In (:ids)")
    suspend fun deleteTracks(ids: Array<String>)

}