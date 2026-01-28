package com.example.mozika.data.db.dao

import androidx.room.*
import com.example.mozika.data.db.entity.Playlist
import com.example.mozika.data.db.entity.PlaylistTrack
import com.example.mozika.data.db.entity.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    /* ---------------- Playlists ---------------- */
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: Long): Playlist?

    @Insert
    suspend fun insert(playlist: Playlist): Long

    @Delete
    suspend fun delete(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun deletePlaylistById(id: Long) {
        // D'abord supprimer les références dans playlisttrack
        deletePlaylistTracks(id)
        // Puis supprimer la playlist elle-même
        deleteById(id)
    }

    /* ---------------- Playlist Tracks ---------------- */
    @Query("DELETE FROM playlisttrack WHERE playlistId = :playlistId")
    suspend fun deletePlaylistTracks(playlistId: Long)

    @Query("DELETE FROM playlisttrack WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrack(playlistId: Long, trackId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrack(pt: PlaylistTrack)

    /* ---------------- Tracks d'une playlist ---------------- */
    @Query("""
        SELECT tracks.* 
        FROM tracks 
        INNER JOIN playlisttrack ON tracks.id = playlisttrack.trackId 
        WHERE playlisttrack.playlistId = :playlistId 
        ORDER BY playlisttrack.position ASC
    """)
    fun tracksFor(playlistId: Long): Flow<List<Track>>

    /* ---------------- Utilitaires ---------------- */
    @Query("SELECT COUNT(*) FROM playlisttrack WHERE playlistId = :playlistId")
    suspend fun trackCount(playlistId: Long): Int

    @Query("SELECT COUNT(*) FROM playlisttrack WHERE playlistId = :playlistId")
    fun getTrackCountSync(playlistId: Long): Int

    /* ---------------- Mise à jour playlist ---------------- */
    @Update
    suspend fun update(playlist: Playlist)

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    /* ---------------- Gestion des positions ---------------- */
    @Query("SELECT MAX(position) FROM playlisttrack WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?

    @Query("UPDATE playlisttrack SET position = position - 1 WHERE playlistId = :playlistId AND position > :removedPosition")
    suspend fun updatePositionsAfterRemoval(playlistId: Long, removedPosition: Int)

    @Query("UPDATE playlisttrack SET position = :newPosition WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun updateTrackPosition(playlistId: Long, trackId: Long, newPosition: Int)

    /* ---------------- Recherche ---------------- */
    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    /* ---------------- Vérifications ---------------- */
    @Query("SELECT EXISTS(SELECT 1 FROM playlisttrack WHERE playlistId = :playlistId AND trackId = :trackId)")
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getTotalPlaylistCount(): Int
}