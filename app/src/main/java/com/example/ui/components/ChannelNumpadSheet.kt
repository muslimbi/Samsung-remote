package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TvChannel
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.RemoteSurface
import com.example.ui.theme.RemoteSurfaceElevated
import com.example.ui.theme.RemoteSurfacePill
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.SamsungGreen
import com.example.ui.theme.SamsungYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelNumpadSheet(
    sheetState: SheetState,
    favoriteChannels: List<TvChannel>,
    onTuneToChannel: (Int) -> Unit,
    onDigitPress: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var enteredDigits by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RemoteSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(RemoteBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("channel_numpad_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Direct Channel Tuning",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Digit entry readout
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RemoteDarkBg)
                    .border(1.5.dp, if (enteredDigits.isNotEmpty()) SamsungCyan else RemoteBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (enteredDigits.isEmpty()) "Enter Channel #" else enteredDigits,
                    fontSize = if (enteredDigits.isEmpty()) 14.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enteredDigits.isEmpty()) TextSecondary else SamsungCyan,
                    letterSpacing = 2.sp
                )
            }

            // Quick Favorites Row
            if (favoriteChannels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorites",
                        tint = SamsungYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Quick Favorites",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(favoriteChannels) { chan ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(RemoteSurfaceElevated)
                                .border(1.dp, RemoteBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    onTuneToChannel(chan.number)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CH ${chan.number}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SamsungCyan
                                )
                                Text(
                                    text = chan.name,
                                    fontSize = 10.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Numeric 3x4 Keypad
            Spacer(modifier = Modifier.height(16.dp))
            val keypadGrid = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6),
                listOf(7, 8, 9)
            )

            for (row in keypadGrid) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (num in row) {
                        KeypadButton(
                            label = num.toString(),
                            onClick = {
                                if (enteredDigits.length < 4) {
                                    enteredDigits += num.toString()
                                }
                                onDigitPress(num)
                            }
                        )
                    }
                }
            }

            // Bottom Row: Clear / 0 / Tune (Enter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Backspace / Clear
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(RemoteSurfaceElevated)
                        .border(1.dp, RemoteBorder, CircleShape)
                        .clickable {
                            if (enteredDigits.isNotEmpty()) {
                                enteredDigits = enteredDigits.dropLast(1)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 0
                KeypadButton(
                    label = "0",
                    onClick = {
                        if (enteredDigits.length < 4) {
                            enteredDigits += "0"
                        }
                        onDigitPress(0)
                    }
                )

                // Tune / Enter
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(SamsungGreen.copy(alpha = 0.25f))
                        .border(1.5.dp, SamsungGreen, CircleShape)
                        .clickable {
                            val num = enteredDigits.toIntOrNull()
                            if (num != null) {
                                onTuneToChannel(num)
                                onDismiss()
                            }
                        }
                        .testTag("keypad_tune_confirm_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tune Channel",
                        tint = SamsungGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(RemoteSurfacePill)
            .border(1.dp, RemoteBorder, CircleShape)
            .clickable(onClick = onClick)
            .testTag("keypad_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
