package com.example.mozika.data.repo

import com.example.mozika.data.db.dao.*
import com.example.mozika.data.db.entity.Track
import com.example.mozika.data.scanner.Scanner
import com.example.mozika.domain.model.Album as DomainAlbum
import com.example.mozika.domain.model.Artist as DomainArtist
import com.example.mozika.domain.model.Track as DomainTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepo @Inject constructor(
    private val scanner: Scanner,
    private val trackDao: TrackDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao
) {

    suspend fun refreshTracks() {
        val scannedTracks = scanner.scan()
        trackDao.clear()
        trackDao.insertAll(scannedTracks)
    }

    fun tracks(): Flow<List<DomainTrack>> =
        trackDao.getAll().map { entityList ->
            entityList.map { track ->
                DomainTrack(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    duration = track.duration,
                    dateAdded = track.dateAdded,
                    data = track.path
                )
            }
        }

    fun albums(): Flow<List<DomainAlbum>> =
        albumDao.getAll().map { entityList ->
            entityList.map { album ->
                DomainAlbum(
                    id = album.id,
                    title = album.title,
                    artist = album.artist,
                    trackCount = album.trackCount
                )
            }
        }

    fun artists(): Flow<List<DomainArtist>> =
        artistDao.getAll().map { entityList ->
            entityList.map { artist ->
                DomainArtist(
                    id = artist.id,
                    name = artist.name,
                    albumCount = artist.albumCount,
                    trackCount = artist.trackCount
                )
            }
        }

    fun searchTracks(query: String): Flow<List<DomainTrack>> =
        trackDao.search(query).map { entityList ->
            entityList.map { track ->
                DomainTrack(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    duration = track.duration,
                    dateAdded = track.dateAdded,
                    data = track.path
                )
            }
        }

    fun searchAlbums(query: String): Flow<List<DomainAlbum>> =
        albumDao.search(query).map { entityList ->
            entityList.map { album ->
                DomainAlbum(
                    id = album.id,
                    title = album.title,
                    artist = album.artist,
                    trackCount = album.trackCount
                )
            }
        }

    fun searchArtists(query: String): Flow<List<DomainArtist>> =
        artistDao.search(query).map { entityList ->
            entityList.map { artist ->
                DomainArtist(
                    id = artist.id,
                    name = artist.name,
                    albumCount = artist.albumCount,
                    trackCount = artist.trackCount
                )
            }
        }
}