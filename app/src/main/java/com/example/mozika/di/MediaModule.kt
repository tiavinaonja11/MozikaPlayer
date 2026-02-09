package com.example.mozika.di

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.mozika.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
    }

    @Provides
    @Singleton
    fun provideExoPlayer(
        app: Application,
        audioAttributes: AudioAttributes
    ): ExoPlayer {
        println("DEBUG - MediaModule: ExoPlayer créé")
        return ExoPlayer.Builder(app)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideMediaSession(
        app: Application,
        exoPlayer: ExoPlayer
    ): MediaSession {
        println("DEBUG - MediaModule: MediaSession créée")

        val sessionActivityIntent = Intent(app, MainActivity::class.java)
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            app,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return MediaSession.Builder(app, exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }
}