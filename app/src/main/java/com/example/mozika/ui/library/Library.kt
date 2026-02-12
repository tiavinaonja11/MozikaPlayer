package com.example.mozika.ui.library

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mozika.ui.common.formatDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// Couleurs
val CyanPrimary = Color(0xFF22D3EE)
val CyanAlpha15 = Color(0xFF22D3EE).copy(alpha = 0.15f)
val CyanAlpha20 = Color(0xFF22D3EE).copy(alpha = 0.20f)
val CyanAlpha12 = Color(0xFF22D3EE).copy(alpha = 0.12f)
val BackgroundBlack = Color(0xFF000000)
val CardBlack = Color(0xFF141414)
val SurfaceBlack = Color(0xFF0A0A0A)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    nav: NavHostController,
    viewModel: LibraryVM = hiltViewModel()
) {
    val tracks by viewModel.tracks.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val query by viewModel.query.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val currentlyPlayingTrackId by viewModel.currentlyPlayingTrackId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var isSearchVisible by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    LaunchedEffect(scanResult) {
        if (scanResult != null) {
            delay(3000)
            viewModel.clearScanResult()
        }
    }

    // Focus sur la barre de recherche quand elle apparaît
    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBlack
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundBlack)
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    // Ligne supérieure avec titre ou recherche
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isSearchVisible) {
                            Text(
                                text = "Musique",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White
                            )
                        } else {
                            // Barre de recherche fonctionnelle
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
                                        tint = Color(0xFF666666),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))

                                    BasicTextField(
                                        value = query,
                                        onValueChange = { viewModel.onQueryChange(it) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        cursorBrush = SolidColor(CyanPrimary),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                            onSearch = { focusManager.clearFocus() }
                                        ),
                                        decorationBox = { innerTextField ->
                                            Box {
                                                if (query.isEmpty()) {
                                                    Text(
                                                        text = "Rechercher chansons, artistes...",
                                                        color = Color(0xFF666666),
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )

                                    if (query.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.clearQuery() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = "Effacer",
                                                tint = Color(0xFF666666),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    isSearchVisible = !isSearchVisible
                                    if (!isSearchVisible) {
                                        viewModel.clearQuery()
                                        focusManager.clearFocus()
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSearchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
                                    contentDescription = "Rechercher",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (!isSearchVisible) {
                                IconButton(
                                    onClick = { viewModel.scanTracks() },
                                    enabled = !isScanning,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    if (isScanning) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = CyanPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = "Scanner",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Filtres chips (Chansons, Albums, Artistes)
                    if (!isSearchVisible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                },
                                label = { Text("Chansons") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAlpha15,
                                    selectedLabelColor = CyanPrimary,
                                    containerColor = CardBlack,
                                    labelColor = Color(0xFF888888)
                                ),
                                border = null
                            )

                            FilterChip(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                },
                                label = { Text("Albums") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAlpha15,
                                    selectedLabelColor = CyanPrimary,
                                    containerColor = CardBlack,
                                    labelColor = Color(0xFF888888)
                                ),
                                border = null
                            )

                            FilterChip(
                                selected = selectedTab == 2,
                                onClick = {
                                    selectedTab = 2
                                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                                },
                                label = { Text("Artistes") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAlpha15,
                                    selectedLabelColor = CyanPrimary,
                                    containerColor = CardBlack,
                                    labelColor = Color(0xFF888888)
                                ),
                                border = null
                            )
                        }
                    }

                    // Options de tri et filtres
                    if (!isSearchVisible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterText(
                                    text = "Tout",
                                    selected = true,
                                    onClick = { }
                                )
                                FilterText(
                                    text = "Favoris",
                                    selected = false,
                                    onClick = { }
                                )
                                FilterText(
                                    text = "Récents",
                                    selected = false,
                                    onClick = { }
                                )
                            }

                            // Menu de tri
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showSortMenu = true }
                                ) {
                                    Icon(
                                        imageVector = when (sortOrder) {
                                            LibraryVM.SortOrder.AZ, LibraryVM.SortOrder.ZA -> Icons.Rounded.SortByAlpha
                                            LibraryVM.SortOrder.DATE -> Icons.Rounded.AccessTime
                                            LibraryVM.SortOrder.ARTIST -> Icons.Rounded.Person
                                            LibraryVM.SortOrder.DURATION -> Icons.Rounded.Timer
                                            else -> Icons.Rounded.Sort
                                        },
                                        contentDescription = "Trier",
                                        tint = if (sortOrder != LibraryVM.SortOrder.NONE) CyanPrimary else Color(0xFF666666),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (sortOrder) {
                                            LibraryVM.SortOrder.AZ -> "A-Z"
                                            LibraryVM.SortOrder.ZA -> "Z-A"
                                            LibraryVM.SortOrder.ARTIST -> "Artiste"
                                            LibraryVM.SortOrder.DATE -> "Date"
                                            LibraryVM.SortOrder.DURATION -> "Durée"
                                            else -> "Trier"
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp
                                        ),
                                        color = if (sortOrder != LibraryVM.SortOrder.NONE) CyanPrimary else Color(0xFF666666)
                                    )
                                }

                                // Dropdown menu de tri
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(CardBlack),
                                    containerColor = CardBlack
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Titre A-Z", color = if (sortOrder == LibraryVM.SortOrder.AZ) CyanPrimary else Color.White) },
                                        onClick = {
                                            viewModel.setSortOrder(LibraryVM.SortOrder.AZ)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.SortByAlpha, null, tint = if (sortOrder == LibraryVM.SortOrder.AZ) CyanPrimary else Color.White)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Titre Z-A", color = if (sortOrder == LibraryVM.SortOrder.ZA) CyanPrimary else Color.White) },
                                        onClick = {
                                            viewModel.setSortOrder(LibraryVM.SortOrder.ZA)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.SortByAlpha, null, tint = if (sortOrder == LibraryVM.SortOrder.ZA) CyanPrimary else Color.White)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Artiste", color = if (sortOrder == LibraryVM.SortOrder.ARTIST) CyanPrimary else Color.White) },
                                        onClick = {
                                            viewModel.sortByArtist()
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Person, null, tint = if (sortOrder == LibraryVM.SortOrder.ARTIST) CyanPrimary else Color.White)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Date d'ajout", color = if (sortOrder == LibraryVM.SortOrder.DATE) CyanPrimary else Color.White) },
                                        onClick = {
                                            viewModel.sortByDateAdded()
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.AccessTime, null, tint = if (sortOrder == LibraryVM.SortOrder.DATE) CyanPrimary else Color.White)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Durée", color = if (sortOrder == LibraryVM.SortOrder.DURATION) CyanPrimary else Color.White) },
                                        onClick = {
                                            viewModel.sortByDuration()
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Timer, null, tint = if (sortOrder == LibraryVM.SortOrder.DURATION) CyanPrimary else Color.White)
                                        }
                                    )
                                    Divider(color = Color(0xFF2A2A2A))
                                    DropdownMenuItem(
                                        text = { Text("Par défaut", color = Color.White) },
                                        onClick = {
                                            viewModel.clearSort()
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Afficher le nombre de résultats si recherche active
                    if (query.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${when (selectedTab) {
                                    0 -> tracks.size
                                    1 -> albums.size
                                    else -> artists.size
                                }} résultat${if ((when (selectedTab) {
                                        0 -> tracks.size
                                        1 -> albums.size
                                        else -> artists.size
                                    }) > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = Color(0xFF666666)
                                )
                            )

                            TextButton(
                                onClick = { viewModel.clearQuery() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Effacer",
                                    color = CyanPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Divider(
                        color = Color(0xFF1A1A1A),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            containerColor = BackgroundBlack
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack)
                    .padding(paddingValues)
            ) {
                // Notification
                scanResult?.let { result ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = CyanAlpha15,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CyanAlpha20),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { page -> "pager_page_$page" }  // ✅ Clé simple et unique
                ) { page ->
                    when (page) {
                        0 -> {
                            if (tracks.isEmpty() && !isScanning) {
                                EmptyLibraryScreen(
                                    onScanClick = { viewModel.scanTracks() },
                                    isScanning = isScanning
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    items(
                                        items = tracks,
                                        key = { track -> "track_${track.id}_${track.hashCode()}" }  // ✅ Clé unique avec hash
                                    ) { track ->
                                        val isCurrentTrack = track.id.toString() == currentlyPlayingTrackId
                                        TrackItemWithPlayingIndicator(
                                            track = track,
                                            isPlaying = isCurrentTrack && isPlaying,
                                            isCurrentTrack = isCurrentTrack,
                                            navController = nav
                                        )
                                    }
                                    item(key = "tracks_spacer_bottom") {
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (albums.isEmpty() && !isScanning) {
                                EmptyLibraryScreen(
                                    onScanClick = { viewModel.scanTracks() },
                                    isScanning = isScanning
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    items(
                                        items = albums,
                                        key = { album ->
                                            // ✅ PROTECTION : Gérer les IDs null ou "<unknown>"
                                            val safeId = album.id?.takeIf { it != "<unknown>" } ?: "idx_${albums.indexOf(album)}"
                                            "album_${safeId}_${album.hashCode()}"
                                        }
                                    ) { album ->
                                        AlbumItemMinimal(album = album, navController = nav)
                                    }
                                    item(key = "albums_spacer_bottom") {
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                        2 -> {
                            if (artists.isEmpty() && !isScanning) {
                                EmptyLibraryScreen(
                                    onScanClick = { viewModel.scanTracks() },
                                    isScanning = isScanning
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    items(
                                        items = artists,
                                        key = { artist ->
                                            // ✅ PROTECTION : Gérer les IDs null ou "<unknown>"
                                            val safeId = artist.id?.takeIf { it != "<unknown>" } ?: "idx_${artists.indexOf(artist)}"
                                            "artist_${safeId}_${artist.hashCode()}"
                                        }
                                    ) { artist ->
                                        ArtistItemMinimal(artist = artist, navController = nav)
                                    }
                                    item(key = "artists_spacer_bottom") {
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp
        ),
        color = if (selected) Color.White else Color(0xFF666666),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun TrackItemWithPlayingIndicator(
    track: com.example.mozika.domain.model.Track,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                onClick = { navController.navigate("player/${track.id}") },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Box avec indicateur
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isCurrentTrack) CyanAlpha15 else CardBlack
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCurrentTrack && isPlaying -> {
                    PlayingBarsIndicator()
                }
                isCurrentTrack -> {
                    Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = CyanPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }


        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isCurrentTrack) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isCurrentTrack) CyanPrimary else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Afficher la durée si disponible
                track.duration?.let { duration ->
                    if (duration > 0) {
                        Text(
                            text = " • ${formatDuration(duration)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF444444)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = { },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = Color(0xFF444444),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PlayingBarsIndicator(
    barCount: Int = 4,
    color: Color = CyanPrimary
) {
    Row(
        modifier = Modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "bar$index")

            val height by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = 14f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400 + (index * 120),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "height$index"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun AlbumItemMinimal(
    album: com.example.mozika.domain.model.Album,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                onClick = { navController.navigate("album/${album.id}") },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CardBlack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Album,
                contentDescription = null,
                tint = CyanPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${album.artist} • ${album.trackCount} pistes",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Color(0xFF666666),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = { },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = Color(0xFF444444),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ArtistItemMinimal(
    artist: com.example.mozika.domain.model.Artist,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                onClick = { navController.navigate("artist/${artist.id}") },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CardBlack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = CyanPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${artist.albumCount} albums • ${artist.trackCount} titres",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Color(0xFF666666),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = { },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = Color(0xFF444444),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EmptyLibraryScreen(
    onScanClick: () -> Unit,
    isScanning: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(32.dp),
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
                Icons.Rounded.MusicNote,
                contentDescription = "Music library",
                tint = CyanPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Bibliothèque vide",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Commencez par scanner votre musique",
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp,
                fontSize = 14.sp
            ),
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onScanClick,
            enabled = !isScanning,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = BackgroundBlack
            )
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = BackgroundBlack
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Scan en cours...",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Scanner ma musique",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}


// Compatibilité
@Composable
fun AlbumItem(
    album: com.example.mozika.domain.model.Album,
    navController: NavHostController
) {
    AlbumItemMinimal(album = album, navController = navController)
}

@Composable
fun ArtistItem(
    artist: com.example.mozika.domain.model.Artist,
    navController: NavHostController
) {
    ArtistItemMinimal(artist = artist, navController = navController)
}

@Composable
fun TrackItemModern(
    track: com.example.mozika.domain.model.Track,
    navController: NavHostController
) {
    TrackItemMinimal(track = track, navController = navController)
}

@Composable
fun TrackItemMinimal(
    track: com.example.mozika.domain.model.Track,
    navController: NavHostController
) {
    TrackItemWithPlayingIndicator(
        track = track,
        isPlaying = false,
        isCurrentTrack = false,
        navController = navController
    )
}