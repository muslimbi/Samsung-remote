package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.RemoteSurface
import com.example.ui.theme.RemoteSurfaceElevated
import com.example.ui.theme.RemoteSurfacePill
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.SamsungGreen
import com.example.ui.theme.SamsungPowerRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.VoiceRemoteController

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceControlDialog(
    isListening: Boolean,
    recognizedText: String,
    feedbackMessage: String?,
    voiceRmsDb: Float = 0f,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var customCommandText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartListening()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RemoteSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SamsungCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Remote",
                            tint = SamsungCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Samsung Voice Control",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Microphone TV Commands",
                            style = MaterialTheme.typography.labelSmall,
                            color = SamsungCyan
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_control_modal"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Pulsing Mic Center Button
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .scale(if (isListening) pulseScale else 1f)
                        .shadow(14.dp, CircleShape, spotColor = if (isListening) SamsungCyan else Color.Transparent)
                        .clip(CircleShape)
                        .background(if (isListening) SamsungCyan.copy(alpha = 0.25f) else RemoteSurfaceElevated)
                        .border(2.dp, if (isListening) SamsungCyan else RemoteBorder, CircleShape)
                        .clickable {
                            if (isListening) {
                                onStopListening()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    onStartListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("voice_dialog_mic_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = if (isListening) "Listening" else "Tap to Speak",
                        tint = if (isListening) SamsungCyan else TextPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated audio wave bars when listening
                if (isListening) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(20.dp)
                    ) {
                        val barHeights = listOf(8, 14, 20, 16, 12, 18, 10)
                        barHeights.forEachIndexed { index, baseHeight ->
                            val dynamicHeight = (baseHeight * (0.8f + (voiceRmsDb.coerceAtLeast(0f) / 10f))).coerceIn(4f, 22f)
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(dynamicHeight.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(SamsungCyan)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Status & Prompt
                Text(
                    text = if (isListening) "Listening... speak TV command now" else "Tap microphone to speak into device",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isListening) SamsungCyan else TextSecondary,
                    textAlign = TextAlign.Center
                )

                // Recognized Speech Card
                if (recognizedText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(RemoteDarkBg)
                            .border(1.2.dp, SamsungCyan, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SPOKEN COMMAND",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SamsungCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"$recognizedText\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Feedback Message (TV action result)
                if (!feedbackMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (feedbackMessage.startsWith("Action:")) SamsungGreen.copy(alpha = 0.15f)
                                else RemoteSurfaceElevated
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = feedbackMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (feedbackMessage.startsWith("Action:")) SamsungGreen else Color(0xFFFBBF24),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Text input field fallback for typing voice command (ideal for emulator and silent environments)
                OutlinedTextField(
                    value = customCommandText,
                    onValueChange = { customCommandText = it },
                    placeholder = {
                        Text(
                            "Type voice command (e.g. 'Open Netflix')",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (customCommandText.isNotBlank()) {
                                    onExecuteCommand(customCommandText.trim())
                                    customCommandText = ""
                                    focusManager.clearFocus()
                                }
                            },
                            enabled = customCommandText.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Command",
                                tint = if (customCommandText.isNotBlank()) SamsungCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (customCommandText.isNotBlank()) {
                                onExecuteCommand(customCommandText.trim())
                                customCommandText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SamsungCyan,
                        unfocusedBorderColor = RemoteBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = RemoteDarkBg,
                        unfocusedContainerColor = RemoteDarkBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_custom_input_field")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Voice Commands tester / suggestions
                Text(
                    text = "Try Saying / Quick Tap:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                val sampleCommands = VoiceRemoteController.SAMPLE_COMMANDS

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleCommands.forEach { cmd ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(RemoteSurfacePill)
                                .border(1.dp, RemoteBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    onExecuteCommand(cmd)
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("voice_sample_${cmd.replace(" ", "_")}")
                        ) {
                            Text(
                                text = cmd,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SamsungCyan
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SamsungCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}
