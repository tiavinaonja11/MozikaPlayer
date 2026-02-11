package com.example.mozika.ui.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mozika.ui.player.MiniPlayerBar

// Couleurs
val CyanPrimary = Color(0xFF22D3EE)
val CyanAlpha12 = Color(0xFF22D3EE).copy(alpha = 0.12f)
val BackgroundBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF0A0A0A)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val isInPlayerScreen = currentRoute?.startsWith("player/") == true

    val showBottomNav = when (currentRoute?.substringBefore("/")) {
        "player", "album", "artist" -> false
        else -> true
    }

    // Surface racine noire
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBlack
    ) {
        Scaffold(
            containerColor = BackgroundBlack, // Fond noir du Scaffold
            bottomBar = {
                when {
                    isInPlayerScreen -> {
                        // Rien
                    }

                    currentRoute?.substringBefore("/") in listOf("album", "artist") -> {
                        MiniPlayerBar(
                            navController = navController,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier.background(BackgroundBlack)
                        ) {
                            MiniPlayerBar(
                                navController = navController,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )

                            if (showBottomNav) {
                                ModernBottomNavigation(navController = navController)
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack) // Fond noir derrière le contenu
                    .padding(paddingValues)
            ) {
                AppNav(nav = navController)
            }
        }
    }
}

@Composable
fun ModernBottomNavigation(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val items = listOf(
        NavItem("home", "Accueil", Icons.Rounded.Home),
        NavItem("library", "Bibliothèque", Icons.Rounded.LibraryMusic),
        NavItem("playlists", "Playlists", Icons.Rounded.PlaylistPlay),
        NavItem("profile", "Moi", Icons.Rounded.Person)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = SurfaceDark, // Fond très sombre pour la barre
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                ModernNavItem(
                    item = item,
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun ModernNavItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selected) CyanAlpha12
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected) CyanPrimary else Color(0xFF666666),
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (selected) CyanPrimary else Color(0xFF666666),
            maxLines = 1
        )
    }
}