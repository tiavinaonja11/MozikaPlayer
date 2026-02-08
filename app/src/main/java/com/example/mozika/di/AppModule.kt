package com.example.mozika.di

import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import androidx.core.content.getSystemService
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.mozika.domain.usecase.GetTracks
import com.example.mozika.data.repo.TrackRepo

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager =
        context.getSystemService()!!

    @Provides
    @Singleton
    fun provideGetTracks(trackRepo: TrackRepo): GetTracks {
        return GetTracks(trackRepo)
    }

}