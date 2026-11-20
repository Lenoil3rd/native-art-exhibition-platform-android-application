package com.example.naepapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.DustGrey
import com.example.naepapp.ui.model.CreationUi

// COMPONENT: CreationCard — tile used in Marketplace, Artisan Profile and Gallery
@Composable
fun CreationCard(item: CreationUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrightSnow), border = BorderStroke(1.dp, DustGrey),
    ) {
        Column {
            ArtImage(imageUrl = item.imageUrl, width = 600, modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentDescription = item.title)
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.artisanName, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                PriceText(item.price, item.currency, style = MaterialTheme.typography.bodyMedium)
                AvailabilityBadge(item.available)
                SyncStatePill(item.syncState)
                StatsRow(item.likes, item.views)
            }
        }
    }
}
