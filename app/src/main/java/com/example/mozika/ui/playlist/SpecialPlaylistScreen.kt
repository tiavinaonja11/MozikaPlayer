package com.example.mozika.ui.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mozika.ui.library.components.TrackItem   // ✅ Utilisation du TrackItem existant
import com.example.mozika.ui.playlist.viewmodel.SpecialPlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialPlaylistScreen(
    playlistType: String,
    navController: NavController,
    viewModel: SpecialPlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Charger les données au changement de type
    LaunchedEffect(playlistType) {
        viewModel.loadSpecialPlaylist(playlistType)
    }

    // Titre lisible
    val title = when (playlistType) {
        "favorites" -> "Favoris"
        "recently_played" -> "Récemment écoutés"
        "most_played" -> "Les plus écoutés"
        else -> playlistType.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Erreur : ${uiState.error}")
                    }
                }
                uiState.tracks.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Aucune piste dans cette playlist")
                    }
                }
                else -> {
                    LazyColumn {
                        items(uiState.tracks) { track ->
                            TrackItem(
                                track = track,
                                navController = navController,  // ✅ Passage du NavController
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}