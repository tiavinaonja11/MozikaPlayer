package com.example.mozika

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.example.mozika.service.notification.CustomNotificationProvider
import com.example.mozika.service.PlaybackService
import com.example.mozika.ui.nav.MainScaffold
import com.example.mozika.ui.player.PlayerVM
import com.example.mozika.ui.theme.SonicFlowTheme
import com.example.mozika.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ✅ AJOUT : ViewModel pour restaurer l'état du player
    private val playerVM: PlayerVM by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("DEBUG - Permission audio accordée")
            startPlaybackService()
        } else {
            println("DEBUG - Permission audio refusée")
            showPermissionDeniedWarning()
            startPlaybackService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Garder l'écran allumé pendant la lecture
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // ✅ CRITIQUE : Créer le canal de notification au démarrage
        CustomNotificationProvider.createNotificationChannel(this)

        // Vérifier et demander les permissions
        checkAndRequestPermissions()

        // ✅ AJOUT : Restaurer l'état du player au démarrage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playerVM.restorePlayerState()
        }

        setContent {
            SonicFlowTheme {
                MainApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Nettoyage des flags d'écran allumé
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

    private fun showPermissionDeniedWarning() {
        println("Avertissement: Permission audio refusée. Certaines fonctionnalités peuvent être limitées.")
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun startPlaybackService() {
        try {
            println("DEBUG - Démarrage de PlaybackService")
            val serviceIntent = Intent(this, PlaybackService::class.java)

            if (!isPlaybackServiceRunning()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                println("DEBUG - Service démarré avec succès")
            } else {
                println("DEBUG - Service déjà en cours d'exécution")
            }

        } catch (e: Exception) {
            println("DEBUG - Erreur lors du démarrage du service: ${e.message}")
            e.printStackTrace()
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun isPlaybackServiceRunning(): Boolean {
        val manager = getSystemService(android.app.ActivityManager::class.java)
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == PlaybackService::class.java.name }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun MainApp() {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        MainScaffold()
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun StartPlaybackServiceIfNeeded() {
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!PermissionHelper.hasAudioPermission(context)) {
            return@LaunchedEffect
        }

        try {
            val serviceIntent = Intent(context, PlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            println("Erreur dans StartPlaybackServiceIfNeeded: ${e.message}")
        }
    }
}