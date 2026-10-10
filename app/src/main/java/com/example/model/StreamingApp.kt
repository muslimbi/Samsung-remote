package com.example.model

import androidx.compose.ui.graphics.Color

enum class StreamingApp(
    val id: String,
    val displayName: String,
    val brandColor: Color,
    val accentColor: Color,
    val subtitle: String,
    val samsungAppId: String
) {
    NETFLIX(
        id = "netflix",
        displayName = "NETFLIX",
        brandColor = Color(0xFFE50914),
        accentColor = Color(0xFFB81D24),
        subtitle = "Movies & Series",
        samsungAppId = "3201907018807"
    ),
    YOUTUBE(
        id = "youtube",
        displayName = "YouTube",
        brandColor = Color(0xFFFF0000),
        accentColor = Color(0xFFCC0000),
        subtitle = "Videos & Music",
        samsungAppId = "111299001912"
    ),
    DISNEY_PLUS(
        id = "disney",
        displayName = "Disney+",
        brandColor = Color(0xFF1E40AF),
        accentColor = Color(0xFF172554),
        subtitle = "Disney • Pixar • Marvel",
        samsungAppId = "3201901017640"
    ),
    PRIME_VIDEO(
        id = "prime",
        displayName = "prime video",
        brandColor = Color(0xFF0284C7),
        accentColor = Color(0xFF0369A1),
        subtitle = "Amazon Originals",
        samsungAppId = "3201512006785"
    ),
    SAMSUNG_TV_PLUS(
        id = "tvplus",
        displayName = "TV Plus",
        brandColor = Color(0xFF0077FF),
        accentColor = Color(0xFF0052B3),
        subtitle = "100+ Free Channels",
        samsungAppId = "org.tizen.tv-plus"
    ),
    SPOTIFY(
        id = "spotify",
        displayName = "Spotify",
        brandColor = Color(0xFF10B981),
        accentColor = Color(0xFF047857),
        subtitle = "Music & Podcasts",
        samsungAppId = "3201606009684"
    )
}
