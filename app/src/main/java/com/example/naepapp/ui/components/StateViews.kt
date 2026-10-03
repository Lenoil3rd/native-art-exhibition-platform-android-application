package com.example.naepapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.theme.BlackForest
import com.example.naepapp.ui.theme.DustGrey
import com.example.naepapp.ui.theme.OxBlood

// COMPONENT: LoadingView / ErrorView / EmptyView — full-area status screens
@Composable fun LoadingView(modifier: Modifier = Modifier) =
    Box(modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = BlackForest) }

@Composable fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) =
    Column(modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(painterResource(R.drawable.ic_error), null, Modifier.size(48.dp), tint = OxBlood)
        Spacer(Modifier.height(8.dp)); Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp)); NaepButton(stringResource(R.string.retry), onRetry)
    }

@Composable fun EmptyView(modifier: Modifier = Modifier, message: String = stringResource(R.string.nothing_here_yet)) =
    Column(modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(painterResource(R.drawable.ic_empty), null, Modifier.size(48.dp), tint = DustGrey)
        Spacer(Modifier.height(8.dp)); Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }