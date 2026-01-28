package com.example.mozika.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mozika.ui.home.HomeScreen
import com.example.mozika.ui.library.LibraryScreen
import com.example.mozika.ui.player.PlayerScreen
import com.example.mozika.ui.playlist.PlaylistsScreen
import com.example.mozika.ui.playlist.PlaylistDetailScreen
import com.example.mozika.ui.profile.ProfileScreen

@Composable
fun AppNav(
    nav: NavHostController = rememberNavController()
) {
    NavHost(navController = nav, startDestination = "library") {
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

        composable("playlistDetail/{id}") { back ->
            PlaylistDetailScreen(
                back.arguments?.getString("id")!!.toLong(),
                nav
            )
        }

        // Route pour le lecteur avec ID
        composable("player/{trackId}") { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId")?.toLongOrNull()
            PlayerScreen(
                navController = nav,
                trackId = trackId
            )
        }
    }
}