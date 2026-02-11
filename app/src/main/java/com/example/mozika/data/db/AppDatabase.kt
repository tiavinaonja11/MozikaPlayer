package com.example.mozika.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mozika.data.db.dao.*
import com.example.mozika.data.db.entity.*

@Database(
    entities = [
        Track::class,
        Album::class,
        Artist::class,
        Playlist::class,
        PlaylistTrack::class,
        WaveformEntity::class,
        FavoriteEntity::class,
        PlayCountEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun waveformDao(): WaveformDao
    abstract fun favoriteDao(): FavoriteDao   // ✅ maintenant résolu
    abstract fun playCountDao(): PlayCountDao // ✅ maintenant résolu
}