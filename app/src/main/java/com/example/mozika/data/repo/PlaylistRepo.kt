package com.example.mozika.data.repo

import com.example.mozika.data.db.dao.PlaylistDao
import com.example.mozika.data.db.dao.TrackDao
import com.example.mozika.data.db.dao.FavoriteDao      // ✅ NOUVEAU
import com.example.mozika.data.db.dao.PlayCountDao     // ✅ NOUVEAU
import com.example.mozika.data.db.entity.FavoriteEntity
import com.example.mozika.data.db.entity.PlayCountEntity
import com.example.mozika.data.db.entity.Playlist as PlaylistEntity
import com.example.mozika.data.db.entity.PlaylistTrack
import com.example.mozika.data.db.entity.Track as TrackEntity
import com.example.mozika.domain.model.Playlist as DomainPlaylist
import com.example.mozika.domain.model.Track as DomainTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepo @Inject constructor(
    private val dao: PlaylistDao,
    private val trackDao: TrackDao,
    private val favoriteDao: FavoriteDao,      // ✅ INJECTÉ
    private val playCountDao: PlayCountDao     // ✅ INJECTÉ
) {

    /* ---------- Playlists ---------- */
    fun playlists(): Flow<List<DomainPlaylist>> =
        dao.getAll().map { entityList ->
            entityList.map { it.asDomain() }
        }

    suspend fun getPlaylistById(id: Long): DomainPlaylist? =
        dao.getById(id)?.asDomain()

    fun tracksFor(playlistId: Long): Flow<List<DomainTrack>> =
        dao.tracksFor(playlistId).map { entityList ->
            entityList.map { it.asDomain() }
        }

    suspend fun create(name: String): Long =
        dao.insert(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))

    suspend fun rename(playlistId: Long, newName: String) =
        dao.rename(playlistId, newName)

    suspend fun addTrack(playlistId: Long, trackId: Long) {
        if (!dao.isTrackInPlaylist(playlistId, trackId)) {
            val maxPosition = dao.getMaxPosition(playlistId) ?: -1
            dao.addTrack(
                PlaylistTrack(
                    playlistId = playlistId,
                    trackId = trackId,
                    position = maxPosition + 1
                )
            )
        }
    }

    suspend fun removeTrack(playlistId: Long, trackId: Long) =
        dao.removeTrack(playlistId, trackId)

    suspend fun delete(playlistId: Long) = dao.deletePlaylistById(playlistId)

    suspend fun delete(playlist: PlaylistEntity) = dao.delete(playlist)

    suspend fun getSongCount(playlistId: Long): Int = dao.trackCount(playlistId)

    fun allTracksCount(): Flow<Map<Long, Int>> =
        dao.getAll().map { playlists ->
            playlists.associate { it.id to dao.getTrackCountSync(it.id) }
        }

    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean =
        dao.isTrackInPlaylist(playlistId, trackId)

    fun searchPlaylists(query: String): Flow<List<DomainPlaylist>> =
        dao.searchPlaylists(query).map { list -> list.map { it.asDomain() } }

    suspend fun getTotalPlaylistCount(): Int = dao.getTotalPlaylistCount()

    suspend fun getMaxPosition(playlistId: Long): Int? = dao.getMaxPosition(playlistId)

    suspend fun updateTrackPosition(playlistId: Long, trackId: Long, newPosition: Int) =
        dao.updateTrackPosition(playlistId, trackId, newPosition)

    suspend fun updatePositionsAfterRemoval(playlistId: Long, removedPosition: Int) =
        dao.updatePositionsAfterRemoval(playlistId, removedPosition)

    // ===== FAVORIS =====
    suspend fun toggleFavorite(trackId: Long): Boolean =
        if (favoriteDao.isFavorite(trackId)) {
            favoriteDao.removeFavorite(trackId)
            false
        } else {
            favoriteDao.addFavorite(FavoriteEntity(trackId = trackId))
            true
        }

    suspend fun isFavorite(trackId: Long): Boolean = favoriteDao.isFavorite(trackId)

    fun getFavoriteTracks(): Flow<List<DomainTrack>> =
        favoriteDao.getFavoriteTrackIds().flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList())
            else getAllTracksFlow().map { allTracks ->
                ids.mapNotNull { id -> allTracks.find { it.id == id } }
            }
        }

    fun getFavoriteCount(): Flow<Int> =
        favoriteDao.getFavoriteTrackIds().map { it.size }

    // ===== STATS DE LECTURE =====
    suspend fun incrementPlayCount(trackId: Long) =
        playCountDao.incrementPlayCount(trackId)

    fun getTopPlayedTracks(limit: Int = 25): Flow<List<Pair<DomainTrack, Int>>> =
        playCountDao.getTopPlayed(limit).flatMapLatest { topList ->
            if (topList.isEmpty()) flowOf(emptyList())
            else getAllTracksFlow().map { allTracks ->
                topList.mapNotNull { entity ->
                    allTracks.find { it.id == entity.trackId }?.let { it to entity.playCount }
                }
            }
        }

    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<DomainTrack>> =
        playCountDao.getRecentlyPlayed(limit).flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList())
            else getAllTracksFlow().map { allTracks ->
                ids.mapNotNull { id -> allTracks.find { it.id == id } }
            }
        }

    fun getTopPlayedCount(): Flow<Int> =
        playCountDao.getTopPlayed(1).map { it.size }

    fun getRecentlyPlayedCount(): Flow<Int> =
        playCountDao.getRecentlyPlayed(1).map { it.size }

    // ===== HELPERS =====
    private fun getAllTracksFlow(): Flow<List<DomainTrack>> =
        trackDao.getAll().map { entities ->
            entities.map { it.asDomain() }
        }

    // ===== CONVERSIONS =====
    private fun PlaylistEntity.asDomain(): DomainPlaylist =
        DomainPlaylist(
            id = id,
            name = name,
            createdAt = createdAt,
            songCount = null
        )

    private fun TrackEntity.asDomain(): DomainTrack =
        DomainTrack(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            dateAdded = dateAdded,
            data = path
        )
}