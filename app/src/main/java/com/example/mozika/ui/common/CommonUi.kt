package com.example.mozika.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mozika.ui.library.CyanAlpha15
import com.example.mozika.ui.library.CyanPrimary
import com.example.mozika.ui.library.CardBlack

@Composable
fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    isHighlighted: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isHighlighted) CyanAlpha15 else CardBlack,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlighted) CyanPrimary else Color(0xFF888888),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$value $label",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (isHighlighted) CyanPrimary else Color(0xFFAAAAAA)
            )
        }
    }
}