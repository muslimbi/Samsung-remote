package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.SamsungTVDevice
import com.example.model.TvChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SamsungTVDevice::class, TvChannel::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tvDeviceDao(): TvDeviceDao
    abstract fun channelDao(): ChannelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "samremote_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(database: AppDatabase) {
                val channelDao = database.channelDao()
                val tvDao = database.tvDeviceDao()

                // Default initial channels
                val initialChannels = listOf(
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
                channelDao.insertAll(initialChannels)

                // Virtual Samsung Smart TV entry ready out of the box
                val virtualTv = SamsungTVDevice(
                    name = "Samsung QLED 4K (Living Room)",
                    ipAddress = "192.168.1.105",
                    macAddress = "E4:7C:F9:11:22:33",
                    port = 8001,
                    modelName = "QN90B Neo QLED",
                    isLastConnected = true,
                    isVirtual = true
                )
                tvDao.insertDevice(virtualTv)
            }
        }
    }
}
