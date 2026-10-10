package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.RemoteSurface
import com.example.ui.theme.RemoteSurfaceElevated
import com.example.ui.theme.RemoteSurfacePill
import com.example.ui.theme.SamsungBlueLight
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.SamsungGreen
import com.example.ui.theme.SamsungPowerRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.atan2

enum class JogDialTarget {
    VOLUME,
    CHANNEL
}

@Composable
fun JogWheelVolumeChannelDial(
    volume: Int,
    isMuted: Boolean,
    channelNumber: Int,
    channelName: String,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onChannelUp: () -> Unit,
    onChannelDown: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialTarget by remember { mutableStateOf(JogDialTarget.VOLUME) }
    var currentAngle by remember { mutableFloatStateOf(0f) }
    var previousAngle by remember { mutableFloatStateOf(0f) }
    var angleAccumulator by remember { mutableFloatStateOf(0f) }

    val angleStepThreshold = 18f // degrees per tick

    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .testTag("jog_wheel_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toggle Switch between Volume and Channel
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(RemoteSurfaceElevated)
                .border(1.dp, RemoteBorder, RoundedCornerShape(24.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (dialTarget == JogDialTarget.VOLUME) SamsungCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { dialTarget = JogDialTarget.VOLUME }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("jog_target_volume_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (dialTarget == JogDialTarget.VOLUME) SamsungCyan else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VOL ($volume)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (dialTarget == JogDialTarget.VOLUME) FontWeight.Bold else FontWeight.Normal,
                        color = if (dialTarget == JogDialTarget.VOLUME) SamsungCyan else TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (dialTarget == JogDialTarget.CHANNEL) SamsungGreen.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { dialTarget = JogDialTarget.CHANNEL }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("jog_target_channel_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = if (dialTarget == JogDialTarget.CHANNEL) SamsungGreen else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CH ($channelNumber)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (dialTarget == JogDialTarget.CHANNEL) FontWeight.Bold else FontWeight.Normal,
                        color = if (dialTarget == JogDialTarget.CHANNEL) SamsungGreen else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Circular Rotary Jog Dial
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(12.dp, CircleShape, spotColor = Color.Black)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RemoteSurfacePill,
                            RemoteSurface,
                            RemoteDarkBg
                        )
                    )
                )
                .border(
                    2.dp,
                    if (dialTarget == JogDialTarget.VOLUME) SamsungCyan.copy(alpha = 0.6f) else SamsungGreen.copy(alpha = 0.6f),
                    CircleShape
                )
                .pointerInput(dialTarget) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            previousAngle = calculateAngle(offset, center)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val newAngle = calculateAngle(change.position, center)
                            var delta = newAngle - previousAngle
                            if (delta > 180f) delta -= 360f
                            if (delta < -180f) delta += 360f

                            angleAccumulator += delta
                            currentAngle = (currentAngle + delta) % 360f
                            previousAngle = newAngle

                            if (angleAccumulator >= angleStepThreshold) {
                                if (dialTarget == JogDialTarget.VOLUME) {
                                    onVolumeUp()
                                } else {
                                    onChannelUp()
                                }
                                angleAccumulator = 0f
                            } else if (angleAccumulator <= -angleStepThreshold) {
                                if (dialTarget == JogDialTarget.VOLUME) {
                                    onVolumeDown()
                                } else {
                                    onChannelDown()
                                }
                                angleAccumulator = 0f
                            }
                        }
                    )
                }
                .testTag("jog_wheel_rotary_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Drawn tick marks around the ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f - 14.dp.toPx()
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val activeColor = if (dialTarget == JogDialTarget.VOLUME) SamsungCyan else SamsungGreen

                // Outer progress arc
                drawCircle(
                    color = RemoteBorder,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // 24 Dial notches
                for (i in 0 until 24) {
                    val angleDeg = i * 15f
                    val angleRad = (angleDeg * PI / 180f).toFloat()
                    val tickLen = if (i % 6 == 0) 12.dp.toPx() else 6.dp.toPx()
                    val p1 = Offset(
                        centerOffset.x + (radius - tickLen) * kotlin.math.cos(angleRad),
                        centerOffset.y + (radius - tickLen) * kotlin.math.sin(angleRad)
                    )
                    val p2 = Offset(
                        centerOffset.x + radius * kotlin.math.cos(angleRad),
                        centerOffset.y + radius * kotlin.math.sin(angleRad)
                    )
                    draw投Line(
                        color = if (i % 6 == 0) activeColor.copy(alpha = 0.8f) else TextSecondary.copy(alpha = 0.4f),
                        start = p1,
                        end = p2,
                        strokeWidth = if (i % 6 == 0) 2.5.dp.toPx() else 1.5.dp.toPx()
                    )
                }
            }

            // Center Interactive Knob
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(RemoteSurfaceElevated, RemoteSurfacePill, RemoteDarkBg)
                        )
                    )
                    .border(1.5.dp, RemoteBorder, CircleShape)
                    .clickable {
                        if (dialTarget == JogDialTarget.VOLUME) {
                            onToggleMute()
                        } else {
                            onChannelUp()
                        }
                    }
                    .testTag("jog_wheel_center_knob"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (dialTarget == JogDialTarget.VOLUME) {
                        Text(
                            text = if (isMuted) "MUTED" else "$volume",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMuted) SamsungPowerRed else SamsungCyan
                        )
                        Text(
                            text = if (isMuted) "Tap to unmute" else "VOLUME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Text(
                            text = "CH $channelNumber",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamsungGreen
                        )
                        Text(
                            text = channelName.take(12),
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Quick Click Step buttons (+ and - buttons below wheel)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(RemoteSurfaceElevated)
                    .border(1.dp, RemoteBorder, CircleShape)
                    .clickable {
                        if (dialTarget == JogDialTarget.VOLUME) onVolumeDown() else onChannelDown()
                    }
                    .testTag("jog_step_down_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Step Down",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Rotate wheel to scrub",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(RemoteSurfaceElevated)
                    .border(1.dp, RemoteBorder, CircleShape)
                    .clickable {
                        if (dialTarget == JogDialTarget.VOLUME) onVolumeUp() else onChannelUp()
                    }
                    .testTag("jog_step_up_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Step Up",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun calculateAngle(touch: Offset, center: Offset): Float {
    val x = touch.x - center.x
    val y = touch.y - center.y
    val rad = atan2(y, x)
    var deg = (rad * 180.0 / PI).toFloat()
    if (deg < 0) deg += 360f
    return deg
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw投Line(
    color: Color,
    start: Offset,
    end: Offset,
    strokeWidth: Float
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}
