package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.RemoteSurfaceElevated
import com.example.ui.theme.RemoteSurfacePill
import com.example.ui.theme.RemoteSurfacePillPressed
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.SamsungGreen
import com.example.ui.theme.SamsungPowerRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun TactileVolumeRocker(
    volume: Int,
    isMuted: Boolean,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upInteraction = remember { MutableInteractionSource() }
    val isUpPressed by upInteraction.collectIsPressedAsState()

    val downInteraction = remember { MutableInteractionSource() }
    val isDownPressed by downInteraction.collectIsPressedAsState()

    // Auto-repeat when held down
    val currentOnVolumeUp by rememberUpdatedState(onVolumeUp)
    LaunchedEffect(isUpPressed) {
        if (isUpPressed) {
            delay(350)
            while (isUpPressed) {
                currentOnVolumeUp()
                delay(120)
            }
        }
    }

    val currentOnVolumeDown by rememberUpdatedState(onVolumeDown)
    LaunchedEffect(isDownPressed) {
        if (isDownPressed) {
            delay(350)
            while (isDownPressed) {
                currentOnVolumeDown()
                delay(120)
            }
        }
    }

    // Drag gesture tracking
    val dragAccumulator = remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .width(82.dp)
            .testTag("volume_rocker_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else if (volume > 50) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                contentDescription = "Volume Icon",
                tint = if (isMuted) SamsungPowerRed else SamsungCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "VOL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isMuted) SamsungPowerRed else SamsungCyan,
                letterSpacing = 1.sp
            )
        }

        // Tactile Rocker Switch Body
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(176.dp)
                .shadow(10.dp, RoundedCornerShape(36.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(36.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            RemoteSurfaceElevated,
                            RemoteSurfacePill,
                            RemoteSurfaceElevated
                        )
                    )
                )
                .border(1.5.dp, RemoteBorder, RoundedCornerShape(36.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { dragAccumulator.floatValue = 0f },
                        onDragCancel = { dragAccumulator.floatValue = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulator.floatValue += dragAmount.y
                            if (dragAccumulator.floatValue < -35f) {
                                currentOnVolumeUp()
                                dragAccumulator.floatValue = 0f
                            } else if (dragAccumulator.floatValue > 35f) {
                                currentOnVolumeDown()
                                dragAccumulator.floatValue = 0f
                            }
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Half: VOL + Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                        .background(
                            if (isUpPressed) RemoteSurfacePillPressed else Color.Transparent
                        )
                        .clickable(
                            interactionSource = upInteraction,
                            indication = null,
                            onClick = onVolumeUp
                        )
                        .testTag("volume_up_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Volume Up",
                            tint = if (isUpPressed) SamsungCyan else TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "+",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamsungCyan.copy(alpha = 0.8f)
                        )
                    }
                }

                // Center Recessed Pill: Mute / Sound toggle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            if (isMuted) SamsungPowerRed.copy(alpha = 0.2f) else RemoteDarkBg
                        )
                        .border(
                            1.dp,
                            if (isMuted) SamsungPowerRed else RemoteBorder,
                            CircleShape
                        )
                        .clickable(onClick = onToggleMute)
                        .testTag("volume_mute_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeMute,
                        contentDescription = "Mute Toggle",
                        tint = if (isMuted) SamsungPowerRed else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bottom Half: VOL - Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                        .background(
                            if (isDownPressed) RemoteSurfacePillPressed else Color.Transparent
                        )
                        .clickable(
                            interactionSource = downInteraction,
                            indication = null,
                            onClick = onVolumeDown
                        )
                        .testTag("volume_down_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "−",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamsungCyan.copy(alpha = 0.8f)
                        )
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Volume Down",
                            tint = if (isDownPressed) SamsungCyan else TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Live Volume Number & Gauge
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(RemoteSurfaceElevated)
                .border(1.dp, RemoteBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isMuted) "MUTED" else "$volume",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMuted) SamsungPowerRed else TextPrimary
            )
            if (!isMuted) {
                Text(
                    text = " / 100",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
