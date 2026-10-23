package com.example.naepapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.core.common.PriceFormatter
import com.example.naepapp.ui.theme.*

// COMPONENT: AvailabilityBadge — "Readily Available" / "Unavailable"
@Composable
fun AvailabilityBadge(available: Boolean, modifier: Modifier = Modifier) {
    val color = if (available) BlackForest else OxBlood
    Text(
        text = stringResource(if (available) R.string.readily_available else R.string.unavailable),
        color = color,                                                   // actual color for the text
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))   // low-opacity variant behind it
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

// COMPONENT: AvailabilityToggle — one-tap switch used by artisans (FR-8)
@Composable
fun AvailabilityToggle(available: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        AvailabilityBadge(available)
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = available, onCheckedChange = { onToggle() }, enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = BlackForest, checkedThumbColor = BrightSnow,
                uncheckedTrackColor = DustGrey, uncheckedThumbColor = BrightSnow, uncheckedBorderColor = DustGrey),
        )
    }
}

// COMPONENT: PriceText — "MWK 150,000" or "Price on request"
@Composable
fun PriceText(price: Double?, currency: String, modifier: Modifier = Modifier,
              style: TextStyle = MaterialTheme.typography.titleMedium) {
    val priceOnRequest = stringResource(R.string.price_on_request)
    Text(
        text = PriceFormatter.format(price, currency, priceOnRequest),
        style = style, fontWeight = FontWeight.SemiBold, modifier = modifier)
}

// COMPONENT: StatsRow — heart + likes, eye + views
@Composable
fun StatsRow(likes: Int, views: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_favorite), stringResource(R.string.likes), Modifier.size(16.dp))
        Text("$likes", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.width(4.dp))
        Icon(painterResource(R.drawable.ic_visibility), stringResource(R.string.views), Modifier.size(16.dp))
        Text("$views", style = MaterialTheme.typography.labelMedium)
    }
}
