package com.example.mozika.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mozika.ui.player.MiniPlayerBar

@Composable
fun MainScaffold() {
    val navController = rememberNavController()

    // Récupérer la route actuelle
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Déterminer si on est sur l'écran Player
    val isInPlayerScreen = currentRoute?.startsWith("player/") == true

    // Déterminer si on doit montrer la bottom navigation
    val showBottomNav = when (currentRoute?.substringBefore("/")) {
        "player", "album", "artist" -> false
        else -> true
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        bottomBar = {
            // Logique pour déterminer ce qui doit être montré dans le bottomBar
            when {
                // Sur l'écran Player : ne rien montrer du tout
                isInPlayerScreen -> {
                    // Rien - on cache tout sur l'écran Player
                }

                // Sur les écrans album/artist : montrer seulement MiniPlayerBar
                currentRoute?.substringBefore("/") in listOf("album", "artist") -> {
                    MiniPlayerBar(
                        navController = navController,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Sur les autres écrans : montrer MiniPlayerBar + BottomNavigation
                else -> {
                    Column {
                        MiniPlayerBar(
                            navController = navController,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        if (showBottomNav) {
                            AppBottomNavigation(navController = navController)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenu principal avec padding ajusté
            AppNav(nav = navController)
        }
    }
}

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    // Récupérer la route actuelle pour déterminer l'item sélectionné
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color.White
    ) {
        // Home
        NavigationBarItem(
            icon = {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Home,
                    contentDescription = "Accueil"
                )
            },
            label = { Text("Accueil") },
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1DB954),
                selectedTextColor = Color(0xFF1DB954),
                unselectedIconColor = Color(0xFFB3B3B3),
                unselectedTextColor = Color(0xFFB3B3B3),
                indicatorColor = Color.Transparent
            )
        )

        // Library
        NavigationBarItem(
            icon = {
                Icon(
                    androidx.compose.material.icons.Icons.Default.MusicNote,
                    contentDescription = "Bibliothèque"
                )
            },
            label = { Text("Bibliothèque") },
            selected = currentRoute == "library",
            onClick = {
                if (currentRoute != "library") {
                    navController.navigate("library") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1DB954),
                selectedTextColor = Color(0xFF1DB954),
                unselectedIconColor = Color(0xFFB3B3B3),
                unselectedTextColor = Color(0xFFB3B3B3),
                indicatorColor = Color.Transparent
            )
        )

        // Playlists
        NavigationBarItem(
            icon = {
                Icon(
                    androidx.compose.material.icons.Icons.Default.PlaylistPlay,
                    contentDescription = "Playlists"
                )
            },
            label = { Text("Playlists") },
            selected = currentRoute == "playlists",
            onClick = {
                if (currentRoute != "playlists") {
                    navController.navigate("playlists") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1DB954),
                selectedTextColor = Color(0xFF1DB954),
                unselectedIconColor = Color(0xFFB3B3B3),
                unselectedTextColor = Color(0xFFB3B3B3),
                indicatorColor = Color.Transparent
            )
        )

        // Profile
        NavigationBarItem(
            icon = {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Person,
                    contentDescription = "Profil"
                )
            },
            label = { Text("Profil") },
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1DB954),
                selectedTextColor = Color(0xFF1DB954),
                unselectedIconColor = Color(0xFFB3B3B3),
                unselectedTextColor = Color(0xFFB3B3B3),
                indicatorColor = Color.Transparent
            )
        )
    }
}

// Définir les items de navigation (optionnel - si utilisé ailleurs)
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "Accueil", androidx.compose.material.icons.Icons.Default.Home)
    object Library : BottomNavItem("library", "Bibliothèque", androidx.compose.material.icons.Icons.Default.MusicNote)
    object Playlists : BottomNavItem("playlists", "Playlists", androidx.compose.material.icons.Icons.Default.PlaylistPlay)
    object Profile : BottomNavItem("profile", "Profil", androidx.compose.material.icons.Icons.Default.Person)
}