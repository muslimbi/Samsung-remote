package com.example.data

import com.example.data.db.AppDatabase
import com.example.model.SamsungTVDevice
import com.example.model.TvChannel
import kotlinx.coroutines.flow.Flow

class TvRepository(private val db: AppDatabase) {

    val allDevices: Flow<List<SamsungTVDevice>> = db.tvDeviceDao().getAllDevices()
    val allChannels: Flow<List<TvChannel>> = db.channelDao().getAllChannels()
    val favoriteChannels: Flow<List<TvChannel>> = db.channelDao().getFavoriteChannels()

    suspend fun getLastConnectedDevice(): SamsungTVDevice? {
        return db.tvDeviceDao().getLastConnectedDevice()
    }

    suspend fun saveDevice(device: SamsungTVDevice): Long {
        db.tvDeviceDao().clearLastConnected()
        val deviceToSave = device.copy(isLastConnected = true, lastSeenTimestamp = System.currentTimeMillis())
        return db.tvDeviceDao().insertDevice(deviceToSave)
    }

    suspend fun selectDevice(id: Long) {
        db.tvDeviceDao().clearLastConnected()
        db.tvDeviceDao().setLastConnected(id)
    }

    suspend fun updateDeviceToken(id: Long, token: String) {
        val dev = db.tvDeviceDao().getDeviceById(id)
        if (dev != null) {
            db.tvDeviceDao().updateDevice(dev.copy(token = token))
        }
    }

    suspend fun deleteDevice(id: Long) {
        db.tvDeviceDao().deleteDeviceById(id)
    }

    suspend fun toggleFavoriteChannel(id: Long, isFav: Boolean) {
        db.channelDao().updateFavorite(id, !isFav)
    }

    suspend fun addChannel(channel: TvChannel): Long {
        return db.channelDao().insertChannel(channel)
    }

    suspend fun deleteChannel(id: Long) {
        db.channelDao().deleteChannel(id)
    }

    suspend fun getNextChannel(currentNumber: Int): TvChannel? {
        val next = db.channelDao().getNextChannel(currentNumber)
        return next ?: db.channelDao().getFirstChannel()
    }

    suspend fun getPreviousChannel(currentNumber: Int): TvChannel? {
        val prev = db.channelDao().getPreviousChannel(currentNumber)
        return prev ?: db.channelDao().getLastChannel()
    }

    suspend fun getChannelByNumber(number: Int): TvChannel? {
        return db.channelDao().getChannelByNumber(number)
    }

    suspend fun ensureDefaultData() {
        val count = db.channelDao().getChannelCount()
        if (count == 0) {
            val initialChannels = listOf(
                TvChannel(number = 5, name = "ABC HD", category = "Entertainment", currentShow = "Prime Time Special", isFavorite = true),
                TvChannel(number = 7, name = "BBC World News", category = "News", currentShow = "Global Headlines Live", isFavorite = true),
                TvChannel(number = 12, name = "Discovery", category = "Documentary", currentShow = "Planet Earth Special", isFavorite = true),
                TvChannel(number = 15, name = "HBO Max", category = "Movies", currentShow = "Blockbuster Cinema", isFavorite = true),
                TvChannel(number = 24, name = "ESPN Live", category = "Sports", currentShow = "Championship Finals", isFavorite = true),
                TvChannel(number = 33, name = "F1 Racing TV", category = "Sports", currentShow = "Grand Prix Qualifying", isFavorite = false),
                TvChannel(number = 4, name = "CNN International", category = "News", currentShow = "State of the World", isFavorite = false),
                TvChannel(number = 18, name = "Nat Geo Wild", category = "Documentary", currentShow = "Deep Ocean Odyssey", isFavorite = false),
                TvChannel(number = 29, name = "Cartoon Network", category = "Kids", currentShow = "Animated Adventures", isFavorite = false),
                TvChannel(number = 50, name = "Samsung TV Plus", category = "Samsung", currentShow = "Curated 4K Entertainment", isFavorite = true),
                TvChannel(number = 88, name = "Music Hits TV", category = "Music", currentShow = "Top 40 Countdown", isFavorite = false)
            )
            db.channelDao().insertAll(initialChannels)

            val virtualTv = SamsungTVDevice(
                name = "Samsung QLED 4K (Living Room)",
                ipAddress = "192.168.1.105",
                macAddress = "E4:7C:F9:11:22:33",
                port = 8001,
                modelName = "QN90B Neo QLED",
                isLastConnected = true,
                isVirtual = true
            )
            db.tvDeviceDao().insertDevice(virtualTv)
        }
    }
}
