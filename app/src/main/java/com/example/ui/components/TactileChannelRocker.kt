package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun TactileChannelRocker(
    channelNumber: Int,
    channelName: String,
    onChannelUp: () -> Unit,
    onChannelDown: () -> Unit,
    onPreviousChannel: () -> Unit,
    onOpenNumpad: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upInteraction = remember { MutableInteractionSource() }
    val isUpPressed by upInteraction.collectIsPressedAsState()

    val downInteraction = remember { MutableInteractionSource() }
    val isDownPressed by downInteraction.collectIsPressedAsState()

    // Auto-repeat channel change when held down
    val currentOnChannelUp by rememberUpdatedState(onChannelUp)
    LaunchedEffect(isUpPressed) {
        if (isUpPressed) {
            delay(400)
            while (isUpPressed) {
                currentOnChannelUp()
                delay(200)
            }
        }
    }

    val currentOnChannelDown by rememberUpdatedState(onChannelDown)
    LaunchedEffect(isDownPressed) {
        if (isDownPressed) {
            delay(400)
            while (isDownPressed) {
                currentOnChannelDown()
                delay(200)
            }
        }
    }

    // Drag gesture tracking
    val dragAccumulator = remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .width(82.dp)
            .testTag("channel_rocker_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = "Channel Icon",
                tint = SamsungCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "CH",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SamsungCyan,
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
                                currentOnChannelUp()
                                dragAccumulator.floatValue = 0f
                            } else if (dragAccumulator.floatValue > 35f) {
                                currentOnChannelDown()
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
                // Top Half: CH ▲ Button
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
                            onClick = onChannelUp
                        )
                        .testTag("channel_up_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Channel Up",
                            tint = if (isUpPressed) SamsungCyan else TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "▲",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamsungCyan.copy(alpha = 0.8f)
                        )
                    }
                }

                // Center Recessed Pill: 123 / PRE-CH toggle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(RemoteDarkBg)
                        .border(1.dp, RemoteBorder, CircleShape)
                        .clickable(onClick = onPreviousChannel)
                        .testTag("channel_pre_ch_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Previous Channel",
                            tint = SamsungGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "PRE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }

                // Bottom Half: CH ▼ Button
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
                            onClick = onChannelDown
                        )
                        .testTag("channel_down_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "▼",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamsungCyan.copy(alpha = 0.8f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Channel Down",
                            tint = if (isDownPressed) SamsungCyan else TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Live Channel Badge
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(RemoteSurfaceElevated)
                .border(1.dp, RemoteBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .clickable(onClick = onOpenNumpad)
                .testTag("channel_quick_tune_badge")
        ) {
            Text(
                text = "CH $channelNumber",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SamsungCyan
            )
        }
    }
}
