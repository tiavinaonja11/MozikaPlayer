package com.example.mozika.ui.playlist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mozika.domain.model.Playlist // Import du modèle DOMAINE

@Composable
fun PlaylistCard(
    playlist: com.example.mozika.domain.model.Playlist, // Utilisez le modèle de DOMAINE
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = playlist.name,
            modifier = Modifier.padding(16.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
    }
}