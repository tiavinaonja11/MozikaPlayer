package com.example.mozika.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mozika.ui.home.HomeScreen
import com.example.mozika.ui.library.LibraryScreen
import com.example.mozika.ui.library.AlbumDetailScreen
import com.example.mozika.ui.library.ArtistDetailScreen
import com.example.mozika.ui.player.PlayerScreen
import com.example.mozika.ui.playlist.PlaylistsScreen
import com.example.mozika.ui.playlist.PlaylistDetailScreen
import com.example.mozika.ui.profile.ProfileScreen

@Composable
fun AppNav(
    nav: NavHostController = rememberNavController()
) {
    NavHost(navController = nav, startDestination = "library") {

        // Écran principal
        composable("home") {
            HomeScreen(nav = nav)
        }

        composable("library") {
            LibraryScreen(nav)
        }

        composable("playlists") {
            PlaylistsScreen(nav)
        }

        composable("profile") {
            ProfileScreen(nav = nav)
        }

        // Détails de playlist
        composable(
            route = "playlistDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("id") ?: 0L
            PlaylistDetailScreen(
                playlistId = playlistId,
                navController = nav
            )
        }

        // Route pour le lecteur avec ID
        composable(
            route = "player/{trackId}",
            arguments = listOf(navArgument("trackId") { type = NavType.LongType })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getLong("trackId")
            PlayerScreen(
                navController = nav,
                trackId = trackId
            )
        }

        // Détails d'album
        composable(
            route = "album/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            AlbumDetailScreen(
                navController = nav,
                albumId = albumId
            )
        }

        // Détails d'artiste
        composable(
            route = "artist/{artistId}",
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            ArtistDetailScreen(
                navController = nav,
                artistId = artistId
            )
        }
    }
}

// Fonctions d'extension simplifiées
fun NavHostController.navigateToAlbum(albumId: NavHostController) {
    this.navigate("album/$albumId") {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToArtist(artistId: String) {
    this.navigate("artist/$artistId") {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToTrack(trackId: Long) {
    this.navigate("player/$trackId") {
        launchSingleTop = true
    }
}

