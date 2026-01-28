package com.example.mozika.data.scanner

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.mozika.data.db.entity.Track
import com.example.mozika.utils.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Scanner @Inject constructor(
    private val contentResolver: ContentResolver,
    private val context: Context
) {

    companion object {
        private const val TAG = "AudioScanner"
        private const val BATCH_SIZE = 50  // Traiter par lots pour éviter le blocage
    }

    suspend fun scan(): List<Track> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Début du scan audio sur le thread IO")

        // Vérifier la permission
        if (!PermissionHelper.hasAudioPermission(context)) {
            Log.w(TAG, "Permission refusée")
            return@withContext emptyList()
        }

        val tracks = mutableListOf<Track>()

        try {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATA
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            // Limiter le nombre de résultats pour les premiers tests
            // val limit = "100"  // Décommentez pour limiter pendant le débogage

            contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val count = cursor.count
                Log.d(TAG, "Nombre total de fichiers audio: $count")

                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                var processed = 0
                val startTime = System.currentTimeMillis()

                while (cursor.moveToNext() && processed < 100) {  // Limite à 100 pour le test
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown"
                    val artist = cursor.getString(artistCol) ?: "Unknown"
                    val album = cursor.getString(albumCol) ?: "Unknown"
                    val duration = cursor.getInt(durationCol)
                    val dateAdded = cursor.getLong(dateCol) * 1000
                    val path = cursor.getString(pathCol) ?: ""

                    tracks.add(Track(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        dateAdded = dateAdded,
                        path = path
                    ))

                    processed++

                    // Log tous les 10 éléments
                    if (processed % 10 == 0) {
                        Log.d(TAG, "Traité $processed pistes sur $count")
                    }
                }

                val endTime = System.currentTimeMillis()
                Log.d(TAG, "Scan terminé en ${endTime - startTime}ms")
                Log.d(TAG, "Total pistes récupérées: ${tracks.size}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du scan", e)
            return@withContext emptyList()
        }

        tracks
    }
}