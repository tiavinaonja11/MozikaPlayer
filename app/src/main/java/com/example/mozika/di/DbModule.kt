package com.example.mozika.di

import android.app.Application
import androidx.room.Room
import com.example.mozika.data.db.AppDatabase
import com.example.mozika.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "sonic_v2.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTrackDao(db: AppDatabase): TrackDao = db.trackDao()
    @Provides
    fun provideAlbumDao(db: AppDatabase): AlbumDao = db.albumDao()
    @Provides
    fun provideArtistDao(db: AppDatabase): ArtistDao = db.artistDao()
    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()
    @Provides
    fun provideWaveformDao(db: AppDatabase): WaveformDao = db.waveformDao()
    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides
    fun providePlayCountDao(db: AppDatabase): PlayCountDao = db.playCountDao()
}