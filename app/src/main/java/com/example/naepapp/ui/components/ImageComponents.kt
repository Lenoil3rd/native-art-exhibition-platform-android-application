package com.example.naepapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.DustGrey
import com.example.naepapp.ui.theme.Onyx

// COMPONENT: ArtImage — placeholder where a photo goes.
// TODO (when connecting images): replace the body with Coil's AsyncImage(model = url, contentScale = ContentScale.Crop, ...)
@Composable
fun ArtImage(modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(modifier = modifier.background(DustGrey), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(R.drawable.ic_empty),
            contentDescription = contentDescription,
            modifier = Modifier.size(40.dp),
            tint = Onyx.copy(alpha = 0.35f)
        )
    }
}

// COMPONENT: AvatarImage — round profile picture, falls back to the person's initial
@Composable
fun AvatarImage(name: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(DustGrey),
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(1).uppercase(), color = Onyx, fontWeight = FontWeight.Bold)
    }
}

// COMPONENT: ImageCarousel — swipeable photos with page dots (FR-9)
@Composable
fun ImageCarousel(pageCount: Int, modifier: Modifier = Modifier) {
    val pager = rememberPagerState { pageCount.coerceAtLeast(1) }
    Box(modifier = modifier) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) {
            ArtImage(modifier = Modifier.fillMaxSize())
        }
        if (pageCount > 1) Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(pageCount) { i ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (pager.currentPage == i) BrightSnow else BrightSnow.copy(alpha = 0.5f))
                )
            }
        }
    }
}