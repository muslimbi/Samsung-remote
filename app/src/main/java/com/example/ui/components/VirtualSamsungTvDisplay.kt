package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TvState
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.SamsungGreen
import com.example.ui.theme.SamsungPowerRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun VirtualSamsungTvDisplay(
    state: TvState,
    onTogglePower: () -> Unit,
    onAppSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("virtual_tv_display_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TV Bezel Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9.2f)
                .shadow(16.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF00E5FF).copy(alpha = if (state.isPoweredOn) 0.25f else 0.05f))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070B14))
                .border(2.dp, Color(0xFF26334D), RoundedCornerShape(12.dp))
        ) {
            if (state.isPoweredOn) {
                // TV Screen Content (Powered ON)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0B192E),
                                    Color(0xFF0F264A),
                                    Color(0xFF081220)
                                )
                            )
                        )
                ) {
                    // Top Info Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SamsungCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = state.currentInputSource.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SamsungCyan
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Hd,
                                contentDescription = "4K HDR",
                                tint = SamsungGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SAMSUNG Neo QLED",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Connected",
                                tint = SamsungCyan,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Center Channel Banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CH ${state.currentChannelNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamsungCyan,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = state.currentChannelName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = state.currentShow,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Dynamic On-Screen Volume HUD (Samsung Tizen style floating pill)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.volumeOverlayVisible || state.isMuted,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(12.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xE60A101D))
                                .border(1.5.dp, if (state.isMuted) SamsungPowerRed else SamsungCyan, RoundedCornerShape(20.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (state.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (state.isMuted) SamsungPowerRed else SamsungCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                if (state.isMuted) {
                                    Text(
                                        text = "MUTED",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = SamsungPowerRed
                                    )
                                } else {
                                    Column {
                                        Text(
                                            text = "VOL ${state.volume}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        // Mini volume progress bar
                                        Box(
                                            modifier = Modifier
                                                .width(70.dp)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0xFF26334D))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(state.volume / 100f)
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(SamsungCyan)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Quick Streaming App Launchers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0x99050810))
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickAppChip("Netflix", Color(0xFFE50914)) { onAppSelected("Netflix") }
                        QuickAppChip("YouTube", Color(0xFFFF0000)) { onAppSelected("YouTube") }
                        QuickAppChip("Disney+", Color(0xFF1E40AF)) { onAppSelected("Disney+") }
                        QuickAppChip("Prime", Color(0xFF0284C7)) { onAppSelected("Prime Video") }
                        QuickAppChip("TV+", Color(0xFF0077FF)) { onAppSelected("Samsung TV+") }
                    }
                }
            } else {
                // TV Screen Content (Powered OFF / Standby)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF05070D)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Power Off",
                            tint = SamsungPowerRed.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TV in Standby",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = "Tap Power to wake with Wake-on-LAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }

        // TV Stand / Bottom Bezel Chin & Power LED
        Row(
            modifier = Modifier
                .width(140.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(Color(0xFF1B2438))
                .border(1.dp, Color(0xFF283652), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAMSUNG",
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                letterSpacing = 1.2.sp
            )

            // Power status LED indicator
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (state.isPoweredOn) SamsungGreen else SamsungPowerRed)
            )
        }
    }
}

@Composable
private fun QuickAppChip(
    name: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.25f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
