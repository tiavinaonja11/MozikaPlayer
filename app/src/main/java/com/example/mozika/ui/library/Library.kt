package com.example.mozika.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mozika.ui.player.PlayerVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Chansons", "Albums", "Artistes")

    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    var isSearchVisible by remember { mutableStateOf(false) }

    LaunchedEffect(scanResult) {
        if (scanResult != null) {
            delay(3000)
            viewModel.clearScanResult()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!isSearchVisible) {
                            Text(
                                text = "Bibliothèque",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }

                        if (isSearchVisible) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(42.dp), // Réduit de 48 à 42
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0x20FFFFFF),
                                tonalElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp), // Réduit de 16 à 14
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = "Rechercher",
                                        tint = Color(0xFFB3B3B3),
                                        modifier = Modifier.size(18.dp) // Réduit de 20 à 18
                                    )
                                    Spacer(modifier = Modifier.width(10.dp)) // Réduit de 12 à 10
                                    Text(
                                        text = if (query.isEmpty()) "Rechercher chansons, albums..." else query,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp, // Réduit
                                            color = if (query.isEmpty()) Color(0xFF808080)
                                            else Color.White,
                                            fontWeight = if (query.isEmpty()) FontWeight.Normal else FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF121212),
                    scrolledContainerColor = Color(0xFF121212)
                ),
                actions = {
                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier
                            .padding(end = 4.dp) // Réduit de 8 à 4
                            .size(40.dp) // Réduit de 44 à 40
                    ) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = if (isSearchVisible) "Fermer la recherche" else "Rechercher",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp) // Réduit de 24 à 22
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(paddingValues)
        ) {
            scanResult?.let { result ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp), // Réduit vertical de 8 à 6
                    shape = RoundedCornerShape(10.dp), // Réduit de 12 à 10
                    color = Color(0xFF1DB954).copy(alpha = 0.2f),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp), // Réduit de 12 à 10
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(18.dp) // Réduit de 20 à 18
                        )
                        Spacer(modifier = Modifier.width(10.dp)) // Réduit de 12 à 10
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp // Réduit
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp), // Réduit vertical de 8 à 6
                containerColor = Color.Transparent,
                contentColor = Color(0xFF1DB954),
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(2.5.dp) // Réduit de 3 à 2.5
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)) // Réduit de 3 à 2
                            .background(Color(0xFF1DB954))
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        modifier = Modifier.height(44.dp) // Réduit de 48 à 44
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp // Réduit de 15 à 14
                            ),
                            color = if (selectedTab == index)
                                Color(0xFF1DB954)
                            else
                                Color(0xFFB3B3B3)
                        )
                    }
                }
            }

            Divider(
                color = Color(0xFF404040),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        if (tracks.isEmpty()) {
                            EmptyLibraryScreen(
                                onScanClick = { viewModel.scanTracks() },
                                isScanning = isScanning
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), // Réduit de 12 à 10
                                verticalArrangement = Arrangement.spacedBy(6.dp) // Réduit de 8 à 6
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp), // Réduit de 12 à 10
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Toutes les chansons",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp // Réduit de 22 à 20
                                            ),
                                            color = Color.White
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(10.dp), // Réduit de 12 à 10
                                            color = Color(0xFF1E1E1E),
                                            modifier = Modifier.padding(vertical = 3.dp) // Réduit de 4 à 3
                                        ) {
                                            Text(
                                                text = "${tracks.size} titres",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 12.sp, // Réduit de 13 à 12
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = Color(0xFFB3B3B3),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp) // Réduit
                                            )
                                        }
                                    }
                                }
                                items(tracks) { track ->
                                    TrackItem(track = track, navController = nav)
                                }
                            }
                        }
                    }

                    1 -> {
                        if (albums.isEmpty()) {
                            EmptyLibraryScreen(
                                onScanClick = { viewModel.scanTracks() },
                                isScanning = isScanning
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), // Réduit de 12 à 10
                                verticalArrangement = Arrangement.spacedBy(6.dp) // Réduit de 8 à 6
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp), // Réduit de 12 à 10
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Albums",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp // Réduit de 22 à 20
                                            ),
                                            color = Color.White
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(10.dp), // Réduit de 12 à 10
                                            color = Color(0xFF1E1E1E),
                                            modifier = Modifier.padding(vertical = 3.dp) // Réduit de 4 à 3
                                        ) {
                                            Text(
                                                text = "${albums.size} albums",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 12.sp, // Réduit de 13 à 12
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = Color(0xFFB3B3B3),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp) // Réduit
                                            )
                                        }
                                    }
                                }
                                items(albums) { album ->
                                    AlbumItem(album = album, navController = nav)
                                }
                            }
                        }
                    }

                    2 -> {
                        if (artists.isEmpty()) {
                            EmptyLibraryScreen(
                                onScanClick = { viewModel.scanTracks() },
                                isScanning = isScanning
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), // Réduit de 12 à 10
                                verticalArrangement = Arrangement.spacedBy(6.dp) // Réduit de 8 à 6
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp), // Réduit de 12 à 10
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Artistes",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp // Réduit de 22 à 20
                                            ),
                                            color = Color.White
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(10.dp), // Réduit de 12 à 10
                                            color = Color(0xFF1E1E1E),
                                            modifier = Modifier.padding(vertical = 3.dp) // Réduit de 4 à 3
                                        ) {
                                            Text(
                                                text = "${artists.size} artistes",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 12.sp, // Réduit de 13 à 12
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = Color(0xFFB3B3B3),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp) // Réduit
                                            )
                                        }
                                    }
                                }
                                items(artists) { artist ->
                                    ArtistItem(artist = artist, navController = nav)
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
fun TrackItem(
    track: com.example.mozika.domain.model.Track,
    navController: androidx.navigation.NavHostController
) {
    val playerVM: PlayerVM = hiltViewModel()

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp), // Réduit de 12 à 10
        color = Color(0xFF1E1E1E),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = {
            playerVM.load(track.id, autoPlay = true)
            navController.navigate("player/${track.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp), // Réduit de 16/12 à 14/10
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp) // Réduit de 48 à 44
                    .clip(RoundedCornerShape(8.dp)), // Réduit de 10 à 8
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1DB954).copy(alpha = 0.3f),
                                    Color(0xFF1DB954).copy(alpha = 0.1f)
                                )
                            )
                        )
                )
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(22.dp) // Réduit de 24 à 22
                )
            }

            Spacer(modifier = Modifier.width(12.dp)) // Réduit de 14 à 12

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp // Réduit de 16 à 15
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(3.dp)) // Réduit de 4 à 3

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp // Réduit de 14 à 13
                        ),
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp)) // Réduit de 8 à 6

                    Text(
                        text = formatDuration(track.duration),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp, // Réduit de 12 à 11
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF808080),
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp)) // Réduit de 8 à 6

            IconButton(
                onClick = { /* Action simple du menu */ },
                modifier = Modifier.size(34.dp) // Réduit de 36 à 34
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu",
                    tint = Color(0xFF808080),
                    modifier = Modifier.size(18.dp) // Réduit de 20 à 18
                )
            }
        }
    }
}

@Composable
fun AlbumItem(
    album: com.example.mozika.domain.model.Album,
    navController: androidx.navigation.NavHostController
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp), // Réduit de 12 à 10
        color = Color(0xFF1E1E1E),
        tonalElevation = 0.dp,
        onClick = {
            navController.navigate("album/${album.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp), // Réduit de 16 à 14
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp) // Réduit de 56 à 52
                    .clip(RoundedCornerShape(8.dp)), // Réduit de 10 à 8
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1DB954).copy(alpha = 0.2f))
                )
                Icon(
                    Icons.Rounded.Album,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(26.dp) // Réduit de 28 à 26
                )
            }

            Spacer(modifier = Modifier.width(14.dp)) // Réduit de 16 à 14

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp // Réduit de 16 à 15
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp)) // Réduit de 6 à 5

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp // Réduit de 14 à 13
                        ),
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp)) // Réduit de 8 à 6

                    Text(
                        text = "${album.trackCount} pistes",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp, // Réduit de 12 à 11
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF1DB954)
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF808080),
                modifier = Modifier.size(18.dp) // Réduit de 20 à 18
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: com.example.mozika.domain.model.Artist,
    navController: androidx.navigation.NavHostController
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp), // Réduit de 12 à 10
        color = Color(0xFF1E1E1E),
        tonalElevation = 0.dp,
        onClick = {
            navController.navigate("artist/${artist.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp), // Réduit de 16 à 14
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp) // Réduit de 56 à 52
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1DB954).copy(alpha = 0.2f))
                )
                Text(
                    text = artist.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp // Réduit de 22 à 20
                    ),
                    color = Color(0xFF1DB954)
                )
            }

            Spacer(modifier = Modifier.width(14.dp)) // Réduit de 16 à 14

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp // Réduit de 16 à 15
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(5.dp)) // Réduit de 6 à 5

                Text(
                    text = "${artist.albumCount} albums • ${artist.trackCount} titres",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp // Réduit de 14 à 13
                    ),
                    color = Color(0xFFB3B3B3)
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF808080),
                modifier = Modifier.size(18.dp) // Réduit de 20 à 18
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
            .background(Color(0xFF121212))
            .padding(32.dp), // Réduit de 40 à 32
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp) // Réduit de 120 à 100
                .shadow(6.dp, CircleShape) // Réduit de 8 à 6
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1DB954).copy(alpha = 0.2f),
                            Color(0xFF1E1E1E).copy(alpha = 0.8f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.MusicNote,
                contentDescription = "Music library",
                tint = Color(0xFF1DB954),
                modifier = Modifier.size(48.dp) // Réduit de 56 à 48
            )
        }

        Spacer(modifier = Modifier.height(28.dp)) // Réduit de 36 à 28

        Text(
            text = "Bibliothèque vide",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp // Réduit de 26 à 22
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp), // Réduit de 10 à 8
            color = Color.White
        )

        Text(
            text = "Commencez par scanner votre musique",
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp, // Réduit de 22 à 20
                fontSize = 14.sp // Réduit de 15 à 14
            ),
            color = Color(0xFFB3B3B3),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 28.dp) // Réduit de 36 à 28
        )

        Button(
            onClick = onScanClick,
            enabled = !isScanning,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(48.dp), // Réduit de 54 à 48
            shape = RoundedCornerShape(12.dp), // Réduit de 14 à 12
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DB954)
            )
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), // Réduit de 22 à 20
                    strokeWidth = 2.5.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp)) // Réduit de 14 à 12
                Text(
                    "Scan en cours...",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp, // Réduit de 15 à 14
                        fontWeight = FontWeight.SemiBold
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp) // Réduit de 24 à 22
                )
                Spacer(modifier = Modifier.width(12.dp)) // Réduit de 14 à 12
                Text(
                    "Scanner ma musique",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp, // Réduit de 15 à 14
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

private fun formatDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}