package com.example.naepapp.ui.theme

import com.example.naepapp.ui.theme.NaepTypography
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val NaepColorScheme = lightColorScheme(
    primary = BrightSnow,        onPrimary = Onyx,
    secondary = BlackForest,     onSecondary = BrightSnow,
    error = OxBlood,             onError = BrightSnow,
    background = BrightSnow,     onBackground = Onyx,
    surface = BrightSnow,        onSurface = Onyx,
    surfaceVariant = DustGrey.copy(alpha = 0.35f),
    onSurfaceVariant = Onyx.copy(alpha = 0.7f),
    outline = DustGrey, outlineVariant = DustGrey,
)


@Composable
fun NaepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NaepColorScheme,
        typography = NaepTypography,
        content = content
    )
}