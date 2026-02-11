package com.example.mozika.ui.playlist

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ✅ IMPORT DE LA FONCTION DE NAVIGATION SPÉCIALE (AJOUTÉ)
import com.example.mozika.ui.nav.navigateToSpecialPlaylist

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlaylistsScreen(
    navController: NavHostController
) {
    val vm: PlaylistVM = hiltViewModel()
    val playlistsWithCount by vm.playlistsWithCount.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var showMenuForPlaylist by remember { mutableStateOf<PlaylistWithCount?>(null) }

    // État pour la barre de recherche
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filtrer seulement les playlists utilisateur selon la recherche
    val filteredUserPlaylists = remember(playlistsWithCount, searchQuery) {
        if (searchQuery.isBlank()) {
            playlistsWithCount
        } else {
            playlistsWithCount.filter { playlist ->
                playlist.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Collections spéciales (déjà dynamiques)
    val favoriteTracks by vm.favoriteTracks.collectAsState()
    val topPlayedTracks by vm.topPlayedTracks.collectAsState()
    val recentlyPlayedTracks by vm.recentlyPlayedTracks.collectAsState()

    val specialPlaylists = listOf(
        SpecialPlaylist(
            id = -1L,
            title = "Favoris",
            songCount = favoriteTracks.size,
            icon = Icons.Filled.Favorite,
            gradientColors = listOf(Color(0xFFFF6B6B), Color(0xFFEE5A52)),
            iconBackground = Color(0xFFFF6B6B).copy(alpha = 0.2f),
            route = "favorites"
        ),
        SpecialPlaylist(
            id = -2L,
            title = "Top",
            songCount = topPlayedTracks.size,
            icon = Icons.Filled.TrendingUp,
            gradientColors = listOf(Color(0xFF4ECDC4), Color(0xFF44A08D)),
            iconBackground = Color(0xFF4ECDC4).copy(alpha = 0.2f),
            route = "most_played"
        ),
        SpecialPlaylist(
            id = -3L,
            title = "Récent",
            songCount = recentlyPlayedTracks.size,
            icon = Icons.Filled.History,
            gradientColors = listOf(Color(0xFF556270), Color(0xFF4ECDC4)),
            iconBackground = Color(0xFF556270).copy(alpha = 0.2f),
            route = "recently_played"
        ),
        SpecialPlaylist(
            id = -4L,
            title = "Top 25",
            songCount = minOf(topPlayedTracks.size, 25),
            icon = Icons.Filled.Star,
            gradientColors = listOf(Color(0xFFC779D0), Color(0xFFFEAC5E)),
            iconBackground = Color(0xFFC779D0).copy(alpha = 0.2f),
            route = "top_played"
        )
    )

    val totalSongs = filteredUserPlaylists.sumOf { it.songCount }
    val totalDuration = filteredUserPlaylists.sumOf { it.songCount * 180 }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                // Barre de recherche complète
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bouton retour
                        IconButton(
                            onClick = {
                                showSearchBar = false
                                searchQuery = ""
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Champ de recherche
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            placeholder = {
                                Text(
                                    "Rechercher une playlist...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { searchQuery = "" }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Effacer",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            } else {
                // TopAppBar normal
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                "Mes Playlists",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                "${filteredUserPlaylists.size} collections • ${formatTotalDuration(totalDuration)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.height(70.dp),
                    actions = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bouton de recherche
                            IconButton(
                                onClick = { showSearchBar = true }
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Bouton ajouter playlist
                            IconButton(
                                onClick = { showDialog = true }
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Ajouter une playlist",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!showSearchBar) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Statistiques - TOUJOURS VISIBLES
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value = filteredUserPlaylists.size.toString(),
                        label = "Playlists",
                        icon = Icons.Outlined.PlaylistPlay,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )

                    StatCard(
                        value = totalSongs.toString(),
                        label = "Titres",
                        icon = Icons.Outlined.MusicNote,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )

                    StatCard(
                        value = formatTotalDurationShort(totalDuration),
                        label = "Durée",
                        icon = Icons.Outlined.Schedule,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                }
            }

            // Collections spéciales - TOUJOURS VISIBLES
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Collections spéciales",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "${specialPlaylists.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(specialPlaylists) { specialPlaylist ->
                        SpecialPlaylistCard(
                            playlist = specialPlaylist,
                            onClick = {
                                // ✅ CORRECTION : utilisation de la fonction d'extension
                                navController.navigateToSpecialPlaylist(specialPlaylist.route)
                            }
                        )
                    }
                }
            }

            // Vos collections - visible toujours
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showSearchBar && searchQuery.isNotBlank()) {
                            "Résultats (${filteredUserPlaylists.size})"
                        } else {
                            "Vos collections"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (filteredUserPlaylists.isNotEmpty()) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "${filteredUserPlaylists.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Liste des playlists utilisateur
            if (filteredUserPlaylists.isEmpty()) {
                item {
                    if (showSearchBar && searchQuery.isNotBlank()) {
                        // Vue pour résultats de recherche vides
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Aucun résultat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Aucune playlist ne correspond à \"$searchQuery\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        EmptyPlaylistsView(
                            onCreateNewClick = { showDialog = true }
                        )
                    }
                }
            } else {
                items(
                    items = filteredUserPlaylists,
                    key = { playlist: PlaylistWithCount -> playlist.id }
                ) { playlist: PlaylistWithCount ->
                    UserPlaylistItem(
                        playlist = playlist,
                        onClick = {
                            navController.navigate("playlistDetail/${playlist.id}")
                        },
                        onOptionsClick = {
                            showMenuForPlaylist = playlist
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dialog pour créer une playlist - CORRIGÉ
    if (showDialog) {
        CreatePlaylistDialog(
            onDismiss = {
                showDialog = false
                newName = ""
            },
            onCreate = { name ->
                coroutineScope.launch {
                    val id = vm.create(name)
                    if (id > 0) {
                        showDialog = false
                        newName = ""
                    }
                }
            }
        )
    }

    // Menu contextuel pour les playlists
    showMenuForPlaylist?.let { playlist ->
        PlaylistOptionsMenu(
            playlist = playlist,
            onDismiss = { showMenuForPlaylist = null },
            onDelete = {
                vm.delete(playlist.id)
                showMenuForPlaylist = null
            },
            onRename = {
                // TODO: Implémenter le renommage
                showMenuForPlaylist = null
            }
        )
    }
}

// Dialog de création de playlist séparé pour plus de clarté
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Nouvelle collection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    placeholder = { Text("Chill, Workout...") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    isError = name.length > 50
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Collection personnalisée",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${name.length}/50",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (name.length > 50) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank() && name.length <= 50,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Créer", fontSize = 14.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Annuler", fontSize = 14.sp)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// COMPOSABLES

@Composable
fun SpecialPlaylistCard(
    playlist: SpecialPlaylist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = playlist.gradientColors
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = playlist.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${playlist.songCount} titres",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun UserPlaylistItem(
    playlist: PlaylistWithCount,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable(onClick = onClick)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon avec dégradé amélioré
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(10.dp),
                        clip = true
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Collections,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Info playlist
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${playlist.songCount} titres",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatDateShort(playlist.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Bouton play
                IconButton(
                    onClick = { /* TODO: Play playlist */ },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Jouer",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bouton options (trois points)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOptionsClick() }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Options",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistOptionsMenu(
    playlist: PlaylistWithCount,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                playlist.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                // Option Jouer
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: Jouer la playlist */ }
                        .padding(vertical = 12.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Jouer la playlist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Option Renommer
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRename() }
                        .padding(vertical = 12.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Renommer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Option Supprimer
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete() }
                        .padding(vertical = 12.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Supprimer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Fermer")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
fun EmptyPlaylistsView(
    onCreateNewClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.PlaylistAdd,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aucune collection",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Créez votre première collection",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onCreateNewClick,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "Créer une collection",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RowScope.StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(60.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = true
            ),
        shape = RoundedCornerShape(12.dp),
        color = color,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// Helper functions
private fun formatTotalDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) String.format("%dh %02dmin", hours, minutes) else String.format("%d min", minutes)
}

private fun formatTotalDurationShort(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val hours = seconds / 3600
    return if (hours > 0) String.format("%dh", hours) else String.format("%dmin", seconds / 60)
}

private fun formatDateShort(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM", Locale.FRENCH)
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Auj."
    }
}

// Modèles
data class SpecialPlaylist(
    val id: Long,
    val title: String,
    val songCount: Int,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val iconBackground: Color,
    val route: String
)

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int
)