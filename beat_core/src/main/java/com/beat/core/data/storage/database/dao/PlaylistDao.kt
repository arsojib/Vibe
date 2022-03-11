package com.beat.core.data.storage.database.dao

import androidx.room.*
import com.beat.core.data.storage.database.entity.Playlist
import com.beat.core.data.storage.database.entity.PlaylistTrackCrossRef
import com.beat.core.data.storage.database.entity.PlaylistWithTracks

@Dao
interface PlaylistDao {

    @Query("Select * From playlist")
    suspend fun getAll(): List<Playlist>

    @Query("Select * From playlist Where playlistId == :id")
    suspend fun getPlaylist(id: String): Playlist

    @Transaction
    @Query("Select * From playlist Where playlistId = :id")
    suspend fun getPlaylistWithTracks(id: String): PlaylistWithTracks

    @Query("Select * From playlisttrackcrossref Where trackId = :trackId")
    suspend fun getPlaylistTrackCrossRefsByTrackId(trackId: String): List<PlaylistTrackCrossRef>

    @Query("Select * From playlisttrackcrossref Where playlistId = :playlistId")
    suspend fun getPlaylistTrackCrossRefsByPlaylistId(playlistId: String): List<PlaylistTrackCrossRef>

    @Query("SELECT * FROM playlisttrackcrossref where playlistId = :playlistId GROUP BY trackId HAVING COUNT(trackId) == 1")
    suspend fun getPlaylistTrackCrossRefsSingleton(playlistId: String): List<PlaylistTrackCrossRef>

    @Query("Select Exists(Select * From playlist Where playlistId = :id)")
    suspend fun isExist(id: String): Boolean

    @Insert
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistTracksCrossRef(playlistTrackCrossRefs: List<PlaylistTrackCrossRef>)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("Update playlist Set favorite = :favorite Where playlistId = :id")
    suspend fun updateFavoriteStatus(id: String, favorite: Boolean)

    @Query("Delete From playlist Where playlistId = :id")
    suspend fun deletePlaylist(id: String)

    @Query("Delete From playlist Where playlistId In (:ids)")
    suspend fun deletePlaylists(ids: Array<String>)

    @Query("Delete From playlisttrackcrossref Where playlistId In (:playlistIds)")
    suspend fun deletePlaylistTrackCrossRef(playlistIds: Array<String>)

}