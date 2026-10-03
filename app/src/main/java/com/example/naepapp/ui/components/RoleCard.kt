package com.example.naepapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.naepapp.ui.theme.BlackForest
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.DustGrey

// COMPONENT: RoleCard — selectable card for Art Enthusiast / Artisan (Role Selection)
@Composable
fun RoleCard(title: String, description: String, selected: Boolean, onClick: () -> Unit) =
    Card(onClick, Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrightSnow),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) BlackForest else DustGrey)) {
        Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(description) }
    }