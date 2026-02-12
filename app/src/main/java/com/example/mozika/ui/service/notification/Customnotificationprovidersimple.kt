package com.example.mozika.service.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.mozika.MainActivity
import com.example.mozika.R
import com.google.common.collect.ImmutableList

@UnstableApi
class CustomNotificationProvider(
    private val service: MediaSessionService
) : MediaNotification.Provider {

    companion object {
        const val CHANNEL_ID = "playback"
        const val ACTION_PLAY = "com.example.mozika.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.mozika.ACTION_PAUSE"
        const val ACTION_PLAY_PAUSE = "com.example.mozika.ACTION_PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.example.mozika.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.example.mozika.ACTION_NEXT"
        const val ACTION_STOP = "com.example.mozika.ACTION_STOP"

        /**
         * ✅ Méthode publique pour créer le canal depuis MainActivity
         */
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    CHANNEL_ID,
                    "Lecture en cours",
                    android.app.NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Affiche les informations de lecture en cours"
                    setShowBadge(false)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setSound(null, null)
                }

                val manager = context.getSystemService(android.app.NotificationManager::class.java)
                manager.createNotificationChannel(channel)
                println("🔔 DEBUG - Canal de notification créé depuis MainActivity")
            }
        }
    }

    init {
        println("🔔 DEBUG - CustomNotificationProvider initialisé")
    }

    override fun createNotification(
        mediaSession: MediaSession,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {

        println("🔔🔔🔔 DEBUG - CREATE NOTIFICATION APPELÉE!")

        val player = mediaSession.player
        val metadata = player.mediaMetadata

        // ✅ CORRECTION : Vérifier que les métadonnées existent
        val hasValidMetadata = metadata.title != null && metadata.title.toString().isNotEmpty()

        if (!hasValidMetadata) {
            println("⚠️ DEBUG - Pas de métadonnées valides, notification simple")
            return createSimpleNotification()
        }

        // Récupérer les informations de la chanson
        val title = metadata.title?.toString() ?: "Titre inconnu"
        val artist = metadata.artist?.toString() ?: "Artiste inconnu"
        val album = metadata.albumTitle?.toString() ?: ""

        println("🔔 DEBUG - Métadonnées: $title - $artist ($album)")
        println("🔔 DEBUG - État lecture: ${player.isPlaying}")

        // Intent pour ouvrir l'application
        val contentPi = createContentIntent()

        // Créer les actions pour les boutons média
        val actions = createMediaActions(player, mediaSession, actionFactory)

        println("🔔 DEBUG - Nombre total d'actions: ${actions.size}")

        // Construire la notification
        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(album)
            .setContentIntent(contentPi)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(player.isPlaying)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(mediaSession)
                    .setShowActionsInCompactView(0, 1, 2)  // Afficher Previous, Play/Pause, Next en mode compact
            )

        // Ajouter les actions
        actions.forEach { builder.addAction(it) }

        // Ajouter l'artwork
        addArtwork(builder, metadata)

        val notification = builder.build()
        println("🔔 DEBUG - Notification construite avec succès - ID: 1")

        return MediaNotification(1, notification)
    }

    /**
     * ✅ Crée une notification simple quand pas de métadonnées
     */
    private fun createSimpleNotification(): MediaNotification {
        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle("Mozika")
            .setContentText("Prêt à lire")
            .setContentIntent(createContentIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        return MediaNotification(1, builder.build())
    }

    /**
     * ✅ Crée le PendingIntent pour ouvrir l'app
     */
    private fun createContentIntent(): PendingIntent {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getActivity(
            service,
            0,
            Intent(service, MainActivity::class.java),
            flags
        )
    }

    /**
     * ✅ Crée les actions média (précédent, play/pause, suivant)
     */
    private fun createMediaActions(
        player: Player,
        mediaSession: MediaSession,
        actionFactory: MediaNotification.ActionFactory
    ): List<NotificationCompat.Action> {
        val actions = mutableListOf<NotificationCompat.Action>()

        // Bouton précédent
        val prevIcon = IconCompat.createWithResource(service, R.drawable.ic_skip_previous)
        val prevAction = actionFactory.createMediaAction(
            mediaSession,
            prevIcon,
            "Précédent",
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
        )
        actions.add(prevAction)
        println("🔔 DEBUG - Action Précédent ajoutée")

        // Bouton play/pause
        val playPauseIcon = if (player.isPlaying) {
            IconCompat.createWithResource(service, R.drawable.ic_pause)
        } else {
            IconCompat.createWithResource(service, R.drawable.ic_play)
        }

        val playPauseAction = actionFactory.createMediaAction(
            mediaSession,
            playPauseIcon,
            if (player.isPlaying) "Pause" else "Lecture",
            Player.COMMAND_PLAY_PAUSE
        )
        actions.add(playPauseAction)
        println("🔔 DEBUG - Action Play/Pause ajoutée: ${if (player.isPlaying) "Pause" else "Lecture"}")

        // Bouton suivant
        val nextIcon = IconCompat.createWithResource(service, R.drawable.ic_skip_next)
        val nextAction = actionFactory.createMediaAction(
            mediaSession,
            nextIcon,
            "Suivant",
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
        )
        actions.add(nextAction)
        println("🔔 DEBUG - Action Suivant ajoutée")

        return actions
    }

    /**
     * ✅ Ajoute l'artwork à la notification
     */
    private fun addArtwork(builder: NotificationCompat.Builder, metadata: androidx.media3.common.MediaMetadata) {
        val artworkData = metadata.artworkData

        if (artworkData != null && artworkData.isNotEmpty()) {
            try {
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                    artworkData,
                    0,
                    artworkData.size
                )
                builder.setLargeIcon(bitmap)
                println("🔔 DEBUG - Pochette ajoutée (${artworkData.size} bytes)")
            } catch (e: Exception) {
                println("⚠️ DEBUG - Erreur chargement pochette: ${e.message}")
                addDefaultArtwork(builder)
            }
        } else {
            addDefaultArtwork(builder)
        }
    }

    /**
     * ✅ Ajoute une pochette par défaut
     */
    private fun addDefaultArtwork(builder: NotificationCompat.Builder) {
        try {
            val defaultBitmap = createDefaultAlbumArt()
            builder.setLargeIcon(defaultBitmap)
            println("🔔 DEBUG - Pochette par défaut ajoutée")
        } catch (e: Exception) {
            println("⚠️ DEBUG - Erreur création pochette par défaut: ${e.message}")
        }
    }

    /**
     * ✅ Crée une image de pochette par défaut
     */
    private fun createDefaultAlbumArt(): android.graphics.Bitmap {
        val bitmap = android.graphics.Bitmap.createBitmap(
            512, 512,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(0xFF1DB954.toInt()) // Vert Spotify-like

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 200f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        canvas.drawText("♪", 256f, 300f, paint)
        return bitmap
    }

    // Ajoutez un bloc try-catch autour de l'extraction de l'image
    private fun loadArtwork(context: Context, uriString: String?): android.graphics.Bitmap {
        return try {
            if (uriString == null) return createDefaultAlbumArt()

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.parse(uriString))
            val art = retriever.embeddedPicture
            retriever.release()

            if (art != null) {
                android.graphics.BitmapFactory.decodeByteArray(art, 0, art.size)
            } else {
                createDefaultAlbumArt()
            }
        } catch (e: Exception) {
            // Si l'image est illisible, on renvoie une image par défaut au lieu de crasher
            createDefaultAlbumArt()
        }
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        println("🔔 DEBUG - Commande personnalisée: $action")
        return false
    }
}