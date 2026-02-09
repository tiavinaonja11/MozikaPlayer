package com.example.mozika.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.mozika.R
import com.example.mozika.service.notification.CustomNotificationProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var mediaSession: MediaSession

    private lateinit var notificationProvider: CustomNotificationProvider

    // ✅ Listener pour détecter les changements d'état du player
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            println("🔔 DEBUG - État de lecture changé: $playbackState")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            println("🔔 DEBUG - isPlaying changé: $isPlaying")
            // ✅ Forcer la mise à jour quand la lecture démarre/s'arrête
            if (::notificationProvider.isInitialized) {
                invalidateNotification()
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
            println("🔔 DEBUG - Métadonnées changées: ${mediaMetadata.title}")
            // ✅ CRITIQUE: Forcer la mise à jour quand les métadonnées changent
            if (::notificationProvider.isInitialized) {
                invalidateNotification()
            }
        }
    }

    /**
     * ✅ Force la mise à jour de la notification
     * Appelle cette méthode pour déclencher createNotification()
     */
    private fun invalidateNotification() {
        try {
            // Notifier le système qu'il faut rafraîchir la notification
            val notification = notificationProvider.createNotification(
                mediaSession,
                com.google.common.collect.ImmutableList.of(),
                object : MediaNotification.ActionFactory {
                    // ✅ MÉTHODE REQUISE: Créer un PendingIntent pour les commandes média
                    override fun createMediaActionPendingIntent(
                        mediaSession: MediaSession,
                        command: Long
                    ): android.app.PendingIntent {
                        val intent = Intent(this@PlaybackService, PlaybackService::class.java)
                        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            android.app.PendingIntent.FLAG_IMMUTABLE
                        } else {
                            0
                        }
                        return android.app.PendingIntent.getService(
                            this@PlaybackService,
                            command.toInt(),
                            intent,
                            flags
                        )
                    }

                    override fun createMediaAction(
                        session: MediaSession,
                        icon: androidx.core.graphics.drawable.IconCompat,
                        title: CharSequence,
                        command: Int
                    ): NotificationCompat.Action {
                        val intent = Intent(this@PlaybackService, PlaybackService::class.java)
                        val pi = android.app.PendingIntent.getService(
                            this@PlaybackService,
                            command,
                            intent,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                android.app.PendingIntent.FLAG_IMMUTABLE
                            } else {
                                0
                            }
                        )
                        return NotificationCompat.Action.Builder(icon, title, pi).build()
                    }

                    override fun createCustomAction(
                        session: MediaSession,
                        icon: androidx.core.graphics.drawable.IconCompat,
                        title: CharSequence,
                        customAction: String,
                        extras: android.os.Bundle
                    ): NotificationCompat.Action {
                        val intent = Intent(this@PlaybackService, PlaybackService::class.java)
                        val pi = android.app.PendingIntent.getService(
                            this@PlaybackService,
                            0,
                            intent,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                android.app.PendingIntent.FLAG_IMMUTABLE
                            } else {
                                0
                            }
                        )
                        return NotificationCompat.Action.Builder(icon, title, pi).build()
                    }

                    override fun createCustomActionFromCustomCommandButton(
                        mediaSession: MediaSession,
                        customCommandButton: CommandButton
                    ): NotificationCompat.Action {
                        val intent = Intent(this@PlaybackService, PlaybackService::class.java)
                        val pi = android.app.PendingIntent.getService(
                            this@PlaybackService,
                            0,
                            intent,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                android.app.PendingIntent.FLAG_IMMUTABLE
                            } else {
                                0
                            }
                        )
                        return NotificationCompat.Action.Builder(
                            customCommandButton.iconResId,
                            customCommandButton.displayName,
                            pi
                        ).build()
                    }
                },
                object : MediaNotification.Provider.Callback {
                    override fun onNotificationChanged(notification: MediaNotification) {
                        // Callback vide
                    }
                }
            )

            // Mettre à jour la notification foreground
            startForeground(NOTIFICATION_ID, notification.notification)
            println("🔔 DEBUG - Notification mise à jour manuellement")
        } catch (e: Exception) {
            println("⚠️ DEBUG - Erreur mise à jour notification: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()
        println("DEBUG - PlaybackService.onCreate()")

        // Créer le canal de notification
        createNotificationChannel()

        // ✅ CORRECTION 1: Ajouter le listener AVANT d'initialiser la notification
        mediaSession.player.addListener(playerListener)

        // Initialiser le fournisseur de notification
        notificationProvider = CustomNotificationProvider(this)
        setMediaNotificationProvider(notificationProvider)
        println("✅ DEBUG - PlaybackService: setMediaNotificationProvider appelé")

        // ✅ CORRECTION 2: IMPORTANT - Démarrer en foreground immédiatement
        // Requis par Android quand on utilise startForegroundService()
        // La notification sera mise à jour automatiquement quand une chanson sera chargée
        val notification = createDefaultNotification()
        startForeground(NOTIFICATION_ID, notification)
        println("✅ DEBUG - PlaybackService: startForeground() appelé avec notification par défaut")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        println("DEBUG - PlaybackService.onGetSession()")
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // ✅ CORRECTION 3: Gérer la suppression de la tâche
        val player = mediaSession.player
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Arrêter le service si pas de lecture en cours
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        println("DEBUG - PlaybackService.onDestroy()")
        // ✅ Retirer le listener
        mediaSession.player.removeListener(playerListener)
        mediaSession.release()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CustomNotificationProvider.CHANNEL_ID,
                "Lecture en cours",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Affiche les informations de lecture en cours"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            println("✅ DEBUG - Canal de notification créé: ${CustomNotificationProvider.CHANNEL_ID}")
        }
    }

    /**
     * ✅ Crée une notification par défaut pour le démarrage du service
     * Cette notification sera remplacée automatiquement quand une chanson sera chargée
     */
    private fun createDefaultNotification(): Notification {
        return NotificationCompat.Builder(this, CustomNotificationProvider.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle("Mozika")
            .setContentText("Prêt à lire")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}