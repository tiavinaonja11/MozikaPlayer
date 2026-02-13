package com.example.mozika.ui.playlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import com.example.mozika.ui.nav.navigateToSpecialPlaylist
import com.example.mozika.ui.theme.CyanPrimary
import com.example.mozika.ui.theme.CyanAlpha15
import com.example.mozika.ui.theme.CyanAlpha20
import com.example.mozika.ui.theme.BackgroundBlack
import com.example.mozika.ui.theme.CardBlack
import com.example.mozika.ui.theme.TextGray
import com.example.mozika.ui.theme.TextGrayLight
import com.example.mozika.ui.common.MenuOptionItem
import com.example.mozika.ui.theme.SurfaceBlack

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
            icon = Icons.Rounded.Favorite,
            gradientColors = listOf(Color(0xFFFF6B6B), Color(0xFFEE5A52)),
            route = "favorites"
        ),
        SpecialPlaylist(
            id = -2L,
            title = "Top",
            songCount = topPlayedTracks.size,
            icon = Icons.Rounded.TrendingUp,
            gradientColors = listOf(Color(0xFF4ECDC4), Color(0xFF44A08D)),
            route = "most_played"
        ),
        SpecialPlaylist(
            id = -3L,
            title = "Récent",
            songCount = recentlyPlayedTracks.size,
            icon = Icons.Rounded.History,
            gradientColors = listOf(Color(0xFF556270), Color(0xFF4ECDC4)),
            route = "recently_played"
        ),
        SpecialPlaylist(
            id = -4L,
            title = "Top 25",
            songCount = minOf(topPlayedTracks.size, 25),
            icon = Icons.Rounded.Star,
            gradientColors = listOf(Color(0xFFC779D0), Color(0xFFFEAC5E)),
            route = "top_played"
        )
    )

    val totalSongs = filteredUserPlaylists.sumOf { it.songCount }
    val totalDuration = filteredUserPlaylists.sumOf { it.songCount * 180 }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBlack
    ) {
        Scaffold(
            topBar = {
                if (showSearchBar) {
                    // Barre de recherche style Library
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        color = BackgroundBlack,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    showSearchBar = false
                                    searchQuery = ""
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(22.dp),
                                color = CardBlack,
                                tonalElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = TextGrayLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))

                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier.weight(1f),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontSize = 15.sp
                                        ),
                                        singleLine = true,
                                        decorationBox = { innerTextField ->
                                            Box {
                                                if (searchQuery.isEmpty()) {
                                                    Text(
                                                        "Rechercher une playlist...",
                                                        color = TextGrayLight,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )

                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = "Effacer",
                                                tint = TextGrayLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TopAppBar style Library
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundBlack)
                            .padding(top = 8.dp, bottom = 4.dp),
                        color = BackgroundBlack
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Playlists",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = Color.White
                                    )
                                    if (filteredUserPlaylists.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "${filteredUserPlaylists.size} collections • ${formatTotalDurationShort(totalDuration)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                color = TextGrayLight
                                            )
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { showSearchBar = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Search,
                                            contentDescription = "Rechercher",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { showDialog = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Add,
                                            contentDescription = "Ajouter",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (!showSearchBar) {
                    FloatingActionButton(
                        onClick = { showDialog = true },
                        containerColor = CyanPrimary,
                        contentColor = BackgroundBlack,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            containerColor = BackgroundBlack
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack)
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Statistiques style Library
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCardModern(
                            value = filteredUserPlaylists.size.toString(),
                            label = "Playlists",
                            icon = Icons.Rounded.PlaylistPlay
                        )
                        StatCardModern(
                            value = totalSongs.toString(),
                            label = "Titres",
                            icon = Icons.Rounded.MusicNote
                        )
                        StatCardModern(
                            value = formatTotalDurationShort(totalDuration),
                            label = "Durée",
                            icon = Icons.Rounded.Schedule
                        )
                    }
                }

                // Collections spéciales
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Collections spéciales",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${specialPlaylists.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(specialPlaylists) { specialPlaylist ->
                            SpecialPlaylistCardModern(
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
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showSearchBar && searchQuery.isNotBlank()) {
                                "Résultats (${filteredUserPlaylists.size})"
                            } else {
                                "Vos collections"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                        if (filteredUserPlaylists.isNotEmpty()) {
                            Badge(
                                containerColor = CyanAlpha15,
                                contentColor = CyanPrimary
                            ) {
                                Text(
                                    "${filteredUserPlaylists.size}",
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
                            EmptySearchResult(query = searchQuery)
                        } else {
                            EmptyPlaylistsViewModern(onCreateNewClick = { showDialog = true })
                        }
                    }
                } else {
                    items(
                        items = filteredUserPlaylists,
                        key = { it.id }
                    ) { playlist ->
                        UserPlaylistItemModern(
                            playlist = playlist,
                            onClick = { navController.navigate("playlistDetail/${playlist.id}") },
                            onOptionsClick = { showMenuForPlaylist = playlist }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }

    // Dialogue de création modernisé
    if (showDialog) {
        CreatePlaylistDialogModern(
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
        PlaylistOptionsMenuModern(
            playlist = playlist,
            onDismiss = { showMenuForPlaylist = null },
            onDelete = {
                vm.delete(playlist.id)
                showMenuForPlaylist = null
            },
            onRename = {
                showMenuForPlaylist = null
            }
        )
    }
}

// Dans Playlists.kt, remplacer la fonction StatCardModern par :

@Composable
fun StatCardModern(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier  // ← Ajouter le modifier en paramètre
) {
    Surface(
        modifier = modifier  // ← Utiliser le modifier passé
            .height(72.dp),
        shape = RoundedCornerShape(12.dp),
        color = CardBlack,
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
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = CyanPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = TextGrayLight,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}
@Composable
fun SpecialPlaylistCardModern(
    playlist: SpecialPlaylist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
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
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = playlist.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${playlist.songCount} titres",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun UserPlaylistItemModern(
    playlist: PlaylistWithCount,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vignette avec première lettre
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CyanAlpha15),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = playlist.name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = CyanPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${playlist.songCount} titres",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextGrayLight,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = TextGrayLight
                    )
                    Text(
                        text = formatDateShort(playlist.createdAt),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextGrayLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bouton Play
        IconButton(
            onClick = { /* TODO: jouer la playlist */ },
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(CyanAlpha15),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Menu trois points
        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = TextGrayLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyPlaylistsViewModern(
    onCreateNewClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CyanAlpha15),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.PlaylistAdd,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = CyanPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aucune collection",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Créez votre première playlist pour organiser votre musique",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextGrayLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateNewClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = BackgroundBlack
            )
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Créer une collection",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun EmptySearchResult(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = TextGrayLight
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aucun résultat",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aucune playlist ne correspond à \"$query\"",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextGrayLight
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun CreatePlaylistDialogModern(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Nouvelle collection",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color.White
            )
        },
        text = {
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBlack
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (name.isEmpty()) {
                                    Text(
                                        "Nom de la playlist...",
                                        color = TextGrayLight,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Collection personnalisée",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGrayLight
                    )
                    Text(
                        "${name.length}/50",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (name.length > 50) Color(0xFFCF6679) else TextGrayLight
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank() && name.length <= 50,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = BackgroundBlack,
                    disabledContainerColor = CyanAlpha15,
                    disabledContentColor = TextGrayLight
                )
            ) {
                Text(
                    "Créer",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2A2A2A)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Annuler", fontSize = 15.sp)
            }
        },
        containerColor = SurfaceBlack,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
fun PlaylistOptionsMenuModern(
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column {
                MenuOptionItem(
                    icon = Icons.Rounded.PlayArrow,
                    text = "Jouer la playlist",
                    iconTint = CyanPrimary,
                    onClick = { /* TODO */ }
                )
                MenuOptionItem(
                    icon = Icons.Rounded.Edit,
                    text = "Renommer",
                    onClick = onRename
                )
                MenuOptionItem(
                    icon = Icons.Rounded.Delete,
                    text = "Supprimer",
                    textColor = Color(0xFFCF6679),
                    iconTint = Color(0xFFCF6679),
                    onClick = onDelete
                )
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2A2A2A)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextGrayLight
                )
            ) {
                Text("Fermer")
            }
        },
        containerColor = SurfaceBlack,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
fun MenuOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    iconTint: Color = Color.White,
    textColor: Color = Color.White
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}

// ==================== HELPER FUNCTIONS ====================

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