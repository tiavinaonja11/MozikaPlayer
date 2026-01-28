package com.example.mozika.service.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.mozika.MainActivity
import com.google.common.collect.ImmutableList

@UnstableApi
class CustomNotificationProvider(
    private val service: MediaSessionService
) : MediaNotification.Provider {

    private val channelId = "playback"

    init {
        val mgr = NotificationManagerCompat.from(service)
        if (mgr.getNotificationChannel(channelId) == null) {
            mgr.createNotificationChannel(
                NotificationChannelCompat
                    .Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
                    .setName("Lecture en cours")
                    .build()
            )
        }
    }

    /*  ↓  RETOUR SIMPLE  ↓  */
    override fun createNotification(
        mediaSession: MediaSession,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {

        val contentPi = PendingIntent.getActivity(
            service, 0,
            Intent(service, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(service, channelId)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentPi)
            .setStyle(MediaStyleNotificationHelper.MediaStyle(mediaSession))
            .build()

        return MediaNotification(1, notification)
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean = false
}