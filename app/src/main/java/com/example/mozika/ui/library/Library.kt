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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
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


// 🎨 Couleurs Premium
val CyanPrimary = Color(0xFF22D3EE)
val CyanSecondary = Color(0xFF06B6D4)
val CyanAlpha15 = Color(0xFF22D3EE).copy(alpha = 0.15f)
val CyanAlpha20 = Color(0xFF22D3EE).copy(alpha = 0.20f)
val CyanAlpha12 = Color(0xFF22D3EE).copy(alpha = 0.12f)
val BackgroundBlack = Color(0xFF000000)
val CardBlack = Color(0xFF141414)
val SurfaceBlack = Color(0xFF0A0A0A)
val SurfaceElevated = Color(0xFF1A1A1A)

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
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    BackgroundBlack,
                                    BackgroundBlack.copy(alpha = 0.98f)
                                )
                            )
                        )
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
                                    fontSize = 32.sp,
                                    letterSpacing = (-0.8).sp
                                ),
                                color = Color.White
                            )
                        } else {
                            // Barre de recherche premium
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                color = SurfaceElevated,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    SurfaceElevated,
                                                    CardBlack
                                                )
                                            )
                                        )
                                        .padding(horizontal = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = CyanPrimary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))

                                    BasicTextField(
                                        value = query,
                                        onValueChange = { viewModel.onQueryChange(it) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
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
                                                        color = Color(0xFF777777),
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
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = "Effacer",
                                                tint = Color(0xFF888888),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bouton recherche/fermer avec animation
                            Surface(
                                onClick = {
                                    isSearchVisible = !isSearchVisible
                                    if (!isSearchVisible) {
                                        viewModel.clearQuery()
                                        focusManager.clearFocus()
                                    }
                                },
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = if (isSearchVisible) CyanAlpha15 else Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isSearchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
                                        contentDescription = "Rechercher",
                                        tint = if (isSearchVisible) CyanPrimary else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            if (!isSearchVisible) {
                                Surface(
                                    onClick = { viewModel.scanTracks() },
                                    enabled = !isScanning,
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = if (isScanning) CyanAlpha15 else Color.Transparent
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isScanning) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = CyanPrimary,
                                                strokeWidth = 2.5.dp
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
                    }

                    // Filter Chips améliorés
                    if (!isSearchVisible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PremiumFilterChip(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                },
                                label = "Chansons"
                            )

                            PremiumFilterChip(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                },
                                label = "Albums"
                            )

                            PremiumFilterChip(
                                selected = selectedTab == 2,
                                onClick = {
                                    selectedTab = 2
                                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                                },
                                label = "Artistes"
                            )
                        }
                    }

                    // Options de tri avec design amélioré
                    if (!isSearchVisible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
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

                            // Menu de tri premium
                            Box {
                                Surface(
                                    onClick = { showSortMenu = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (sortOrder != LibraryVM.SortOrder.NONE) CyanAlpha12 else Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                            tint = if (sortOrder != LibraryVM.SortOrder.NONE) CyanPrimary else Color(0xFF777777),
                                            modifier = Modifier.size(18.dp)
                                        )
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
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = if (sortOrder != LibraryVM.SortOrder.NONE) CyanPrimary else Color(0xFF777777)
                                        )
                                    }
                                }

                                // Dropdown menu premium
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier
                                        .background(SurfaceElevated)
                                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                                    containerColor = SurfaceElevated,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Titre A-Z",
                                                color = if (sortOrder == LibraryVM.SortOrder.AZ) CyanPrimary else Color.White,
                                                fontWeight = if (sortOrder == LibraryVM.SortOrder.AZ) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(LibraryVM.SortOrder.AZ)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.SortByAlpha, null, tint = if (sortOrder == LibraryVM.SortOrder.AZ) CyanPrimary else Color(0xFF999999))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Titre Z-A",
                                                color = if (sortOrder == LibraryVM.SortOrder.ZA) CyanPrimary else Color.White,
                                                fontWeight = if (sortOrder == LibraryVM.SortOrder.ZA) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(LibraryVM.SortOrder.ZA)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.SortByAlpha, null, tint = if (sortOrder == LibraryVM.SortOrder.ZA) CyanPrimary else Color(0xFF999999))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Artiste",
                                                color = if (sortOrder == LibraryVM.SortOrder.ARTIST) CyanPrimary else Color.White,
                                                fontWeight = if (sortOrder == LibraryVM.SortOrder.ARTIST) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(LibraryVM.SortOrder.ARTIST)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Person, null, tint = if (sortOrder == LibraryVM.SortOrder.ARTIST) CyanPrimary else Color(0xFF999999))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Date d'ajout",
                                                color = if (sortOrder == LibraryVM.SortOrder.DATE) CyanPrimary else Color.White,
                                                fontWeight = if (sortOrder == LibraryVM.SortOrder.DATE) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(LibraryVM.SortOrder.DATE)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.AccessTime, null, tint = if (sortOrder == LibraryVM.SortOrder.DATE) CyanPrimary else Color(0xFF999999))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Durée",
                                                color = if (sortOrder == LibraryVM.SortOrder.DURATION) CyanPrimary else Color.White,
                                                fontWeight = if (sortOrder == LibraryVM.SortOrder.DURATION) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(LibraryVM.SortOrder.DURATION)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Timer, null, tint = if (sortOrder == LibraryVM.SortOrder.DURATION) CyanPrimary else Color(0xFF999999))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = BackgroundBlack
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                scanResult?.let { result ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        containerColor = CyanAlpha20,
                        contentColor = CyanPrimary
                    ) {
                        Text(result, color = CyanPrimary)
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
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
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(
                                        items = tracks,
                                        key = { track -> "track_${track.id}_${track.hashCode()}" }
                                    ) { track ->
                                        TrackItemWithPlayingIndicator(
                                            track = track,
                                            isPlaying = isPlaying && currentlyPlayingTrackId == track.id.toString(),
                                            isCurrentTrack = currentlyPlayingTrackId == track.id.toString(),
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
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(
                                        items = albums,
                                        key = { album ->
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
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(
                                        items = artists,
                                        key = { artist ->
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
fun PremiumFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) CyanAlpha15 else CardBlack,
        modifier = Modifier.height(42.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = if (selected) CyanPrimary else Color(0xFF888888)
            )
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
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        ),
        color = if (selected) Color.White else Color(0xFF777777),
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
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { navController.navigate("player/${track.id}") },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrentTrack) CyanAlpha12 else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail premium
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = if (isCurrentTrack) 8.dp else 2.dp,
                        shape = RoundedCornerShape(10.dp),
                        spotColor = if (isCurrentTrack) CyanPrimary else Color.Transparent
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isCurrentTrack) {
                            Brush.linearGradient(
                                colors = listOf(CyanPrimary.copy(alpha = 0.3f), CyanSecondary.copy(alpha = 0.2f))
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(CardBlack, SurfaceBlack)
                            )
                        }
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = CyanPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = if (isCurrentTrack) CyanPrimary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF888888),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    track.duration?.let { duration ->
                        if (duration > 0) {
                            Text(
                                text = " • ${formatDuration(duration)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF555555)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(20.dp)
                )
            }

            com.example.mozika.ui.components.TrackOptionsMenu(
                track = track,
                expanded = menuExpanded,
                onDismiss = { menuExpanded = false }
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
        modifier = Modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "bar$index")

            val height by infiniteTransition.animateFloat(
                initialValue = 5f,
                targetValue = 16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 450 + (index * 130),
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
    Surface(
        onClick = { navController.navigate("album/${album.id}") },
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CardBlack,
                                SurfaceBlack
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Album,
                    contentDescription = null,
                    tint = CyanPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${album.artist} • ${album.trackCount} pistes",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF888888),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ArtistItemMinimal(
    artist: com.example.mozika.domain.model.Artist,
    navController: NavHostController
) {
    Surface(
        onClick = { navController.navigate("artist/${artist.id}") },
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CyanPrimary.copy(alpha = 0.2f),
                                CyanSecondary.copy(alpha = 0.15f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = CyanPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${artist.albumCount} albums • ${artist.trackCount} titres",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF888888),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(20.dp)
                )
            }
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
                .size(120.dp)
                .shadow(12.dp, CircleShape, spotColor = CyanPrimary)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyanAlpha20,
                            CyanAlpha15,
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.MusicNote,
                contentDescription = "Music library",
                tint = CyanPrimary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Bibliothèque vide",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = (-0.5).sp
            ),
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Commencez par scanner votre musique",
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                fontSize = 15.sp
            ),
            color = Color(0xFF888888),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onScanClick,
            enabled = !isScanning,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .shadow(
                    elevation = if (isScanning) 0.dp else 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = CyanPrimary
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = BackgroundBlack,
                disabledContainerColor = CyanAlpha15
            )
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = CyanPrimary
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    "Scan en cours...",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyanPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    "Scanner ma musique",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
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