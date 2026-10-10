package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tv_channels")
data class TvChannel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: Int,
    val name: String,
    val category: String = "General",
    val currentShow: String = "Live Broadcast",
    val isFavorite: Boolean = false,
    val icon: String = "tv"
)
