package com.example.mozika.ui.playlist

import androidx.compose.foundation.BorderStroke
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
import androidx.tv.material3.OutlinedButtonDefaults
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Navigation extension (inchangée)
import com.example.mozika.ui.nav.navigateToSpecialPlaylist

@OptIn(ExperimentalMaterial3Api::class)
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

    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredUserPlaylists = remember(playlistsWithCount, searchQuery) {
        if (searchQuery.isBlank()) playlistsWithCount
        else playlistsWithCount.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

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
            route = "favorites"
        ),
        SpecialPlaylist(
            id = -2L,
            title = "Top",
            songCount = topPlayedTracks.size,
            icon = Icons.Filled.TrendingUp,
            gradientColors = listOf(Color(0xFF4ECDC4), Color(0xFF44A08D)),
            route = "most_played"
        ),
        SpecialPlaylist(
            id = -3L,
            title = "Récent",
            songCount = recentlyPlayedTracks.size,
            icon = Icons.Filled.History,
            gradientColors = listOf(Color(0xFF556270), Color(0xFF4ECDC4)),
            route = "recently_played"
        ),
        SpecialPlaylist(
            id = -4L,
            title = "Top 25",
            songCount = minOf(topPlayedTracks.size, 25),
            icon = Icons.Filled.Star,
            gradientColors = listOf(Color(0xFFC779D0), Color(0xFFFEAC5E)),
            route = "top_played"
        )
    )

    val totalSongs = filteredUserPlaylists.sumOf { it.songCount }
    val totalDuration = filteredUserPlaylists.sumOf { it.songCount * 180 }

    Scaffold(
        modifier = Modifier.background(Color(0xFF121212)), // Fond global
        topBar = {
            if (showSearchBar) {
                // Barre de recherche
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color(0xFF121212)),
                    color = Color(0xFF121212),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                tint = Color.White
                            )
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            placeholder = {
                                Text(
                                    "Rechercher une playlist...",
                                    color = Color(0xFFB3B3B3)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Effacer",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1DB954),
                                unfocusedBorderColor = Color(0xFF404040),
                                focusedContainerColor = Color(0xFF1E1E1E),
                                unfocusedContainerColor = Color(0xFF1E1E1E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF1DB954)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            } else {
                // TopAppBar normale
                TopAppBar(
                    title = {
                        Column(modifier = Modifier.padding(bottom = 4.dp)) {
                            Text(
                                "Mes Playlists",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                "${filteredUserPlaylists.size} collections • ${formatTotalDuration(totalDuration)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB3B3B3)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF121212)
                    ),
                    modifier = Modifier.height(70.dp),
                    actions = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { showDialog = true }) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Ajouter une playlist",
                                    tint = Color.White
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
                    containerColor = Color(0xFF1DB954),
                    contentColor = Color.White,
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
                .background(Color(0xFF121212)),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Statistiques
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
                        icon = Icons.Outlined.PlaylistPlay
                    )
                    StatCard(
                        value = totalSongs.toString(),
                        label = "Titres",
                        icon = Icons.Outlined.MusicNote
                    )
                    StatCard(
                        value = formatTotalDurationShort(totalDuration),
                        label = "Durée",
                        icon = Icons.Outlined.Schedule
                    )
                }
            }

            // Collections spéciales
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
                        color = Color.White
                    )
                    Text(
                        text = "${specialPlaylists.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1DB954),
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
                                navController.navigateToSpecialPlaylist(specialPlaylist.route)
                            }
                        )
                    }
                }
            }

            // Vos collections
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
                        color = Color.White
                    )
                    if (filteredUserPlaylists.isNotEmpty()) {
                        Badge(
                            containerColor = Color(0xFF1DB954).copy(alpha = 0.1f),
                            contentColor = Color(0xFF1DB954)
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

            // Liste des playlists
            if (filteredUserPlaylists.isEmpty()) {
                item {
                    if (showSearchBar && searchQuery.isNotBlank()) {
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
                                tint = Color(0xFF808080)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucun résultat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune playlist ne correspond à \"$searchQuery\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB3B3B3)
                            )
                        }
                    } else {
                        EmptyPlaylistsView(onCreateNewClick = { showDialog = true })
                    }
                }
            } else {
                items(
                    items = filteredUserPlaylists,
                    key = { it.id }
                ) { playlist ->
                    UserPlaylistItem(
                        playlist = playlist,
                        onClick = { navController.navigate("playlistDetail/${playlist.id}") },
                        onOptionsClick = { showMenuForPlaylist = playlist }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    // Dialogue de création
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

    // Menu contextuel
    showMenuForPlaylist?.let { playlist ->
        PlaylistOptionsMenu(
            playlist = playlist,
            onDismiss = { showMenuForPlaylist = null },
            onDelete = {
                vm.delete(playlist.id)
                showMenuForPlaylist = null
            },
            onRename = {
                // TODO: renommage
                showMenuForPlaylist = null
            }
        )
    }
}

// ==================== COMPOSANTS ====================

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
                color = Color.White
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom", color = Color(0xFFB3B3B3)) },
                    placeholder = { Text("Chill, Workout...", color = Color(0xFF808080)) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1DB954),
                        unfocusedBorderColor = Color(0xFF404040),
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF1DB954)
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
                        color = Color(0xFFB3B3B3)
                    )
                    Text(
                        "${name.length}/50",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (name.length > 50) Color(0xFFCF6679) else Color(0xFFB3B3B3)
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
                    containerColor = Color(0xFF1DB954)
                )
            ) {
                Text("Créer", fontSize = 14.sp, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF404040)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Annuler", fontSize = 14.sp)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(playlist.gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vignette avec la première lettre
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1DB954).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playlist.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1DB954)
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
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
                            tint = Color(0xFF1DB954).copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${playlist.songCount} titres",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1DB954),
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
                            tint = Color(0xFFB3B3B3)
                        )
                        Text(
                            text = formatDateShort(playlist.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB3B3B3),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bouton Play
            IconButton(
                onClick = { /* TODO: jouer la playlist */ },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFF1DB954).copy(alpha = 0.1f)),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color(0xFF1DB954)
                )
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Menu trois points
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFFB3B3B3),
                    modifier = Modifier.size(18.dp)
                )
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
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        },
        text = {
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: jouer */ }
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
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Jouer la playlist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
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
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Renommer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
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
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Supprimer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCF6679)
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
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Text("Fermer", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color.White,
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
                .background(Color(0xFF1DB954).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.PlaylistAdd,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF1DB954)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aucune collection",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Créez votre première collection",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB3B3B3)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onCreateNewClick,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DB954)
            )
        ) {
            Text(
                "Créer une collection",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun RowScope.StatCard(
    value: String,
    label: String,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E1E),
        tonalElevation = 0.dp
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
                    tint = Color(0xFF1DB954)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = Color(0xFFB3B3B3),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// ==================== HELPER FUNCTIONS ====================

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

// ==================== DATA CLASSES ====================

data class SpecialPlaylist(
    val id: Long,
    val title: String,
    val songCount: Int,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val route: String
)

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int
)