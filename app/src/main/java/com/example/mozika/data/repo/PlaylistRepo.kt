package com.example.mozika.data.repo

import com.example.mozika.data.db.dao.PlaylistDao
import com.example.mozika.data.db.entity.Playlist as PlaylistEntity
import com.example.mozika.data.db.entity.PlaylistTrack
import com.example.mozika.data.db.entity.Track as TrackEntity
import com.example.mozika.domain.model.Playlist as DomainPlaylist
import com.example.mozika.domain.model.Track as DomainTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepo @Inject constructor(
    private val dao: PlaylistDao
) {

    /* Flow : toutes les playlists */
    fun playlists(): Flow<List<DomainPlaylist>> =
        dao.getAll().map { entityList ->
            entityList.map { it.asDomain() }
        }

    /* Récupérer une playlist par son ID */
    suspend fun getPlaylistById(id: Long): DomainPlaylist? {
        return dao.getById(id)?.asDomain()
    }

    /* Flow : pistes d'une playlist */
    fun tracksFor(playlistId: Long): Flow<List<DomainTrack>> =
        dao.tracksFor(playlistId).map { entityList ->
            entityList.map { it.asDomain() }
        }

    /* Créer une playlist → retourne son id */
    suspend fun create(name: String): Long =
        dao.insert(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))

    /* Renommer une playlist */
    suspend fun rename(playlistId: Long, newName: String) {
        dao.rename(playlistId, newName)
    }

    /* Ajouter une piste dans une playlist */
    suspend fun addTrack(playlistId: Long, trackId: Long) {
        // Vérifier si le track n'est pas déjà dans la playlist
        if (!dao.isTrackInPlaylist(playlistId, trackId)) {
            // Calculer la prochaine position
            val currentCount = dao.trackCount(playlistId)
            val position = currentCount + 1
            dao.addTrack(PlaylistTrack(playlistId, trackId, position))
        }
    }

    /* Retirer une piste */
    suspend fun removeTrack(playlistId: Long, trackId: Long) {
        dao.removeTrack(playlistId, trackId)
    }

    /* Supprimer une playlist par son ID (avec cascade) */
    suspend fun delete(playlistId: Long) {
        dao.deletePlaylistById(playlistId)
    }

    /* Supprimer une playlist par son entité (alternative) */
    suspend fun delete(playlist: PlaylistEntity) =
        dao.delete(playlist)

    /* Obtenir le nombre de chansons dans une playlist */
    suspend fun getSongCount(playlistId: Long): Int {
        return dao.trackCount(playlistId)
    }

    /* Obtenir tous les counts de chansons par playlist */
    fun allTracksCount(): Flow<Map<Long, Int>> {
        return dao.getAll().map { playlists ->
            playlists.associate { playlist ->
                playlist.id to dao.getTrackCountSync(playlist.id)
            }
        }
    }

    /* Vérifier si un track est déjà dans une playlist */
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return dao.isTrackInPlaylist(playlistId, trackId)
    }

    /* Rechercher des playlists */
    fun searchPlaylists(query: String): Flow<List<DomainPlaylist>> =
        dao.searchPlaylists(query).map { entityList ->
            entityList.map { it.asDomain() }
        }

    /* Obtenir le nombre total de playlists */
    suspend fun getTotalPlaylistCount(): Int {
        return dao.getTotalPlaylistCount()
    }

    /* Obtenir la position maximale dans une playlist */
    suspend fun getMaxPosition(playlistId: Long): Int? {
        return dao.getMaxPosition(playlistId)
    }

    /* Mettre à jour la position d'un track dans une playlist */
    suspend fun updateTrackPosition(playlistId: Long, trackId: Long, newPosition: Int) {
        dao.updateTrackPosition(playlistId, trackId, newPosition)
    }

    /* Mettre à jour les positions après suppression d'un track */
    suspend fun updatePositionsAfterRemoval(playlistId: Long, removedPosition: Int) {
        dao.updatePositionsAfterRemoval(playlistId, removedPosition)
    }

    /* Fonctions de conversion */
    private fun PlaylistEntity.asDomain(): DomainPlaylist =
        DomainPlaylist(
            id = id,
            name = name,
            createdAt = createdAt,
            songCount = null // Sera rempli séparément
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