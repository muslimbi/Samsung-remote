package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SamsungKey
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.RemoteSurfaceElevated
import com.example.ui.theme.RemoteSurfacePill
import com.example.ui.theme.RemoteSurfacePillPressed
import com.example.ui.theme.SamsungBlue
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBtnBlue
import com.example.ui.theme.TvBtnGreen
import com.example.ui.theme.TvBtnRed
import com.example.ui.theme.TvBtnYellow

@Composable
fun RemoteDpad(
    onSendKey: (SamsungKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Quick Function Buttons Row: BACK, HOME, SOURCE
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickPillButton(
                label = "BACK",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                testTag = "dpad_back_button"
            ) {
                onSendKey(SamsungKey.RETURN)
            }

            QuickPillButton(
                label = "HOME",
                icon = Icons.Default.Home,
                accentColor = SamsungCyan,
                testTag = "dpad_home_button"
            ) {
                onSendKey(SamsungKey.HOME)
            }

            QuickPillButton(
                label = "SOURCE",
                icon = Icons.Default.Input,
                testTag = "dpad_source_button"
            ) {
                onSendKey(SamsungKey.SOURCE)
            }
        }

        // Circular Navigation D-Pad
        Box(
            modifier = Modifier
                .size(190.dp)
                .shadow(12.dp, CircleShape, spotColor = Color.Black)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RemoteSurfacePill,
                            RemoteSurfaceElevated,
                            RemoteDarkBg
                        )
                    )
                )
                .border(1.5.dp, RemoteBorder, CircleShape)
                .testTag("dpad_container"),
            contentAlignment = Alignment.Center
        ) {
            // UP
            DpadDirectionButton(
                icon = Icons.Default.KeyboardArrowUp,
                contentDescription = "Up",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .size(54.dp, 44.dp)
                    .testTag("dpad_up_button")
            ) { onSendKey(SamsungKey.UP) }

            // DOWN
            DpadDirectionButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(54.dp, 44.dp)
                    .testTag("dpad_down_button")
            ) { onSendKey(SamsungKey.DOWN) }

            // LEFT
            DpadDirectionButton(
                icon = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Left",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(44.dp, 54.dp)
                    .testTag("dpad_left_button")
            ) { onSendKey(SamsungKey.LEFT) }

            // RIGHT
            DpadDirectionButton(
                icon = Icons.Default.KeyboardArrowRight,
                contentDescription = "Right",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(44.dp, 54.dp)
                    .testTag("dpad_right_button")
            ) { onSendKey(SamsungKey.RIGHT) }

            // CENTER OK / ENTER BUTTON
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(RemoteSurfaceElevated, Color(0xFF1F2B45))
                        )
                    )
                    .border(1.5.dp, SamsungCyan.copy(alpha = 0.5f), CircleShape)
                    .clickable { onSendKey(SamsungKey.ENTER) }
                    .testTag("dpad_ok_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SamsungCyan,
                    letterSpacing = 1.sp
                )
            }
        }

        // Color Buttons Bar (A, B, C, D)
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorKeyDot("A", TvBtnRed) { onSendKey(SamsungKey.RED) }
            ColorKeyDot("B", TvBtnGreen) { onSendKey(SamsungKey.GREEN) }
            ColorKeyDot("C", TvBtnYellow) { onSendKey(SamsungKey.YELLOW) }
            ColorKeyDot("D", TvBtnBlue) { onSendKey(SamsungKey.BLUE) }
        }
    }
}

@Composable
private fun QuickPillButton(
    label: String,
    icon: ImageVector,
    accentColor: Color = TextSecondary,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(RemoteSurfaceElevated)
            .border(1.dp, RemoteBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun DpadDirectionButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed = interaction.collectIsPressedAsState().value

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPressed) RemoteSurfacePillPressed else Color.Transparent)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isPressed) SamsungCyan else TextPrimary,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun ColorKeyDot(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp, 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.25f))
            .border(1.2.dp, color, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
