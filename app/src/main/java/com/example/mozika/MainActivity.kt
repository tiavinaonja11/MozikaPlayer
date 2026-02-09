package com.example.mozika

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.example.mozika.service.notification.CustomNotificationProvider  // ✅ IMPORT
import com.example.mozika.service.PlaybackService
import com.example.mozika.ui.nav.MainScaffold
import com.example.mozika.ui.theme.SonicFlowTheme
import com.example.mozika.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("DEBUG - Permission audio accordée")
            startPlaybackService()
        } else {
            println("DEBUG - Permission audio refusée")
            // On démarre quand même le service
            startPlaybackService()
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Garder l'écran allumé pendant la lecture
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // ✅ CRITIQUE : Créer le canal de notification au démarrage
        CustomNotificationProvider.createNotificationChannel(this)

        // Vérifier et demander les permissions
        checkAndRequestPermissions()

        setContent {
            SonicFlowTheme {
                MainApp()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (!PermissionHelper.hasAudioPermission(this)) {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            requestPermissionLauncher.launch(permission)
        } else {
            println("DEBUG - Permission audio déjà accordée")
            startPlaybackService()
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun startPlaybackService() {
        try {
            println("DEBUG - Démarrage de PlaybackService")
            val serviceIntent = Intent(this, PlaybackService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            println("DEBUG - Service démarré avec succès")

        } catch (e: Exception) {
            println("DEBUG - Erreur: ${e.message}")
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun MainApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        MainScaffold()
    }
}