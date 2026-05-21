package com.mintly.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mintly.app.R
import com.mintly.app.ui.theme.거지방Colors
import kotlinx.coroutines.delay

@androidx.compose.runtime.Composable
fun SplashScreen(
    vm: SplashViewModel = hiltViewModel(),
    onNavigate: (isLoggedIn: Boolean) -> Unit,
) {
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        delay(1200)
        onNavigate(vm.isLoggedIn())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(거지방Colors.Mint400),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_splash_logo),
            contentDescription = "거지방",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale.value),
        )
    }
}
