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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun FavoriteChannelsQuickSurf(
    favoriteChannels: List<TvChannel>,
    currentChannelNumber: Int,
    isFavoriteSurfingOnly: Boolean,
    onSelectChannel: (TvChannel) -> Unit,
    onSurfNext: () -> Unit,
    onSurfPrev: () -> Unit,
    onToggleFavoriteSurfingOnly: () -> Unit,
    onOpenAllChannels: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(RemoteSurface)
            .border(1.dp, RemoteBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
            .testTag("favorite_channels_quick_surf")
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = SamsungYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FAVORITE CHANNELS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RemoteSurfaceElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${favoriteChannels.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamsungCyan
                    )
                }
            }

            // Surfing Filter Pill Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isFavoriteSurfingOnly) SamsungCyan.copy(alpha = 0.2f) else RemoteSurfaceElevated)
                    .border(
                        1.dp,
                        if (isFavoriteSurfingOnly) SamsungCyan else RemoteBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onToggleFavoriteSurfingOnly)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .testTag("toggle_fav_surfing_pill"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFavoriteSurfingOnly) "Favs Only ◉" else "All Channels",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFavoriteSurfingOnly) SamsungCyan else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation surfing arrows + Carousel of favorites
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Favorite Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(RemoteSurfaceElevated)
                    .border(1.dp, RemoteBorder, CircleShape)
                    .clickable(onClick = onSurfPrev)
                    .testTag("surf_prev_fav_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Prev Favorite",
                    tint = SamsungCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Carousel list of favorites
            if (favoriteChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No favorites yet. Star channels in Channels tab!",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteChannels) { chan ->
                        val isCurrent = chan.number == currentChannelNumber
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) RemoteSurfacePill else RemoteDarkBg)
                                .border(
                                    1.2.dp,
                                    if (isCurrent) SamsungCyan else RemoteBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectChannel(chan) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("fav_chip_${chan.number}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isCurrent) SamsungCyan else RemoteSurfaceElevated)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${chan.number}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) RemoteDarkBg else SamsungCyan
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = chan.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Next Favorite Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(RemoteSurfaceElevated)
                    .border(1.dp, RemoteBorder, CircleShape)
                    .clickable(onClick = onSurfNext)
                    .testTag("surf_next_fav_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Favorite",
                    tint = SamsungCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
