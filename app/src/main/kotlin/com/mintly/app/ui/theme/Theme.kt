package com.mintly.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun 거지방Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = 거지방LightColorScheme,
        typography = 거지방Typography,
        shapes = 거지방Shapes,
        content = content,
    )
}
