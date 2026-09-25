package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.Track
import kotlin.math.cos
import kotlin.math.sin

/**
 * Signature Apple Music Adaptive Blurred Mesh Gradient Background.
 * Renders floating, smoothly undulating vibrant color orbs derived from the track's artwork
 * underneath a dark frosted-glass overlay.
 */
@Composable
fun AppleAdaptiveBlurBackground(
    track: Track?,
    modifier: Modifier = Modifier,
    blurIntensity: Float = 0.85f
) {
    val primaryTarget = if (track != null) Color(track.primaryColorHex) else Color(0xFFE52D27)
    val secondaryTarget = if (track != null) Color(track.secondaryColorHex) else Color(0xFF5856D6)
    val tertiaryTarget = if (track != null) Color(track.primaryColorHex).copy(alpha = 0.7f) else Color(0xFFFFA500)

    val primaryColor by animateColorAsState(
        targetValue = primaryTarget,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "primaryColor"
    )

    val secondaryColor by animateColorAsState(
        targetValue = secondaryTarget,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "secondaryColor"
    )

    val tertiaryColor by animateColorAsState(
        targetValue = tertiaryTarget,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "tertiaryColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "meshMovement")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val maxRadius = size.maxDimension * 0.7f

            // Orb 1: Upper dominant
            val x1 = width * 0.3f + (width * 0.2f) * cos(phase1)
            val y1 = height * 0.25f + (height * 0.15f) * sin(phase1)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.55f),
                        primaryColor.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(x1, y1),
                    radius = maxRadius
                ),
                center = Offset(x1, y1),
                radius = maxRadius
            )

            // Orb 2: Lower-right secondary
            val x2 = width * 0.7f + (width * 0.25f) * cos(phase2)
            val y2 = height * 0.7f + (height * 0.2f) * sin(phase2)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.55f),
                        secondaryColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(x2, y2),
                    radius = maxRadius * 0.95f
                ),
                center = Offset(x2, y2),
                radius = maxRadius * 0.95f
            )

            // Orb 3: Central accent
            val x3 = width * 0.5f + (width * 0.15f) * sin(phase1 * 0.8f)
            val y3 = height * 0.5f + (height * 0.15f) * cos(phase2 * 0.7f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tertiaryColor.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(x3, y3),
                    radius = maxRadius * 0.75f
                ),
                center = Offset(x3, y3),
                radius = maxRadius * 0.75f
            )
        }

        // Frosted Glass Dark Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xBB08080C),
                            Color(0x990B0C12),
                            Color(0xDD050508)
                        )
                    )
                )
        )
    }
}
