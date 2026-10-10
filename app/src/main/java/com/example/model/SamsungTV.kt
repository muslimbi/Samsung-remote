package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tv_devices")
data class SamsungTVDevice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ipAddress: String,
    val macAddress: String = "",
    val port: Int = 8001,
    val token: String? = null,
    val modelName: String = "Samsung Smart TV",
    val isLastConnected: Boolean = false,
    val isVirtual: Boolean = false,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)
