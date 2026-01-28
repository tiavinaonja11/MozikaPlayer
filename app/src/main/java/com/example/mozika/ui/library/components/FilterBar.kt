package com.example.mozika.ui.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSortAZ: () -> Unit,
    onSortDate: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Rechercher") },
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onSortAZ) {
            Icon(Icons.Default.Abc, contentDescription = "Trier A-Z")
        }

        IconButton(onClick = onSortDate) {
            Icon(Icons.Default.DateRange, contentDescription = "Trier par date")
        }
    }
}