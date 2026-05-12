package com.example.flowerai.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun SoftAnimatedBackdrop(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "soft-backdrop")
    val driftA by transition.animateFloat(
        initialValue = -18f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift-a"
    )
    val driftB by transition.animateFloat(
        initialValue = 16f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift-b"
    )
    val glow by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.52f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF8F8),
                        Color(0xFFFFEEF4),
                        Color(0xFFF4EEF9),
                        Color(0xFFEFF8FB)
                    )
                )
            )
    ) {
        SoftGlow(
            modifier = Modifier
                .offset(x = (-34).dp, y = (42 + driftA).dp)
                .blur(58.dp),
            colors = listOf(Color(0xFFFF8E82), Color(0xFFE8B6F0), Color.Transparent),
            size = 250,
            alpha = glow
        )
        SoftGlow(
            modifier = Modifier
                .offset(x = (152 + driftB).dp, y = 84.dp)
                .blur(64.dp),
            colors = listOf(Color(0xFFB8D9FF), Color(0xFFFFC9B8), Color.Transparent),
            size = 260,
            alpha = 0.42f
        )
        SoftGlow(
            modifier = Modifier
                .offset(x = (42 + driftA / 2).dp, y = 430.dp)
                .blur(70.dp),
            colors = listOf(Color(0xFFE8B6F0), Color(0xFFBDEBE3), Color.Transparent),
            size = 300,
            alpha = 0.32f
        )
    }
}

@Composable
private fun SoftGlow(
    modifier: Modifier,
    colors: List<Color>,
    size: Int,
    alpha: Float
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .graphicsLayer(alpha = alpha)
            .background(
                brush = Brush.radialGradient(colors),
                shape = RoundedCornerShape(999.dp)
            )
    )
}
