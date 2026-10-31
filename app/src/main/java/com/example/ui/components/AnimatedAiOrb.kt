package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.VoiceEngineState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GlowPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SolarGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedAiOrb(
    state: VoiceEngineState,
    rmsDb: Float = 0f,
    size: Dp = 180.dp,
    assistantName: String = "YESMIN",
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_rotation"
    )

    val rmsOffset = (rmsDb.coerceIn(0f, 15f) / 15f) * 0.15f
    val dynamicScale = when (state) {
        is VoiceEngineState.Listening -> pulseScale + rmsOffset
        is VoiceEngineState.Speaking -> pulseScale + 0.08f
        is VoiceEngineState.Thinking -> 1.02f
        else -> pulseScale
    }

    val primaryGlow = when (state) {
        is VoiceEngineState.Listening -> CyberCyan
        is VoiceEngineState.Speaking -> CyberMagenta
        is VoiceEngineState.Thinking -> SolarGold
        else -> NeonViolet
    }

    val secondaryGlow = when (state) {
        is VoiceEngineState.Listening -> ElectricPurple
        is VoiceEngineState.Speaking -> GlowPink
        is VoiceEngineState.Thinking -> CyberCyan
        else -> CyberMagenta
    }

    Box(
        modifier = Modifier
            .size(size)
            .scale(dynamicScale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Outer aura canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.85f

            // Outer subtle atmospheric ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryGlow.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = size.toPx() / 2f
                ),
                radius = size.toPx() / 2f
            )

            // Orbiting particle points
            val angleRad = Math.toRadians((if (state is VoiceEngineState.Thinking) thinkingRotation else rotationAngle).toDouble())
            for (i in 0 until 4) {
                val offsetAngle = angleRad + (i * Math.PI / 2.0)
                val orbitRadius = baseRadius + 12f * (i % 2)
                val px = center.x + (orbitRadius * cos(offsetAngle)).toFloat()
                val py = center.y + (orbitRadius * sin(offsetAngle)).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) primaryGlow else secondaryGlow,
                    radius = 4.5f,
                    center = Offset(px, py)
                )
            }

            // High-tech holographic ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = 0.7f),
                        secondaryGlow.copy(alpha = 0.9f),
                        primaryGlow.copy(alpha = 0.1f),
                        secondaryGlow.copy(alpha = 0.8f)
                    ),
                    center = center
                ),
                radius = baseRadius,
                style = Stroke(width = 2.5f)
            )
        }

        // Core orb sphere
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            primaryGlow,
                            secondaryGlow,
                            Color(0xFF090616)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner core text / icon
            Text(
                text = when (state) {
                    is VoiceEngineState.Listening -> "LISTENING"
                    is VoiceEngineState.Thinking -> "THINKING"
                    is VoiceEngineState.Speaking -> "SPEAKING"
                    else -> assistantName.take(8).uppercase()
                },
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}
