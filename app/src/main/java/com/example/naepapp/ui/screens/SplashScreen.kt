package com.example.naepapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.NAEPAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.9f) }

    LaunchedEffect(Unit) {
        val job1 = launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        val job2 = launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        job1.join()
        job2.join()
        delay(1200L)
        onTimeout()
    }

    SplashContent(
        alpha = alpha.value,
        scale = scale.value,
        modifier = modifier
    )
}

@Composable
fun SplashContent(
    alpha: Float,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrightSnow),
        contentAlignment = Alignment.Center
    ) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val logoSize = screenWidth * 0.6f

        Image(
            painter = painterResource(id = R.drawable.naep_logo),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .size(logoSize)
                .graphicsLayer(
                    alpha = alpha,
                    scaleX = scale,
                    scaleY = scale
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    NAEPAppTheme {
        SplashContent(alpha = 1f, scale = 1f)
    }
}
