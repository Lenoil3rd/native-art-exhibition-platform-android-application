package com.example.naepapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.naepapp.R
import com.example.naepapp.ui.theme.Onyx
import com.example.naepapp.ui.theme.OxBlood

// COMPONENT: LikeButton — heart toggle with count (FR-11)
@Composable
fun LikeButton(liked: Boolean, count: Int, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick, enabled = enabled) {
            Icon(
                painterResource(if (liked) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                stringResource(R.string.likes), tint = if (liked) OxBlood else Onyx)
        }
        Text("$count")
    }
}