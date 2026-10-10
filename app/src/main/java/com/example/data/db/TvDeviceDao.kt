package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.SamsungTVDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface TvDeviceDao {
    @Query("SELECT * FROM tv_devices ORDER BY isLastConnected DESC, lastSeenTimestamp DESC")
    fun getAllDevices(): Flow<List<SamsungTVDevice>>

    @Query("SELECT * FROM tv_devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: Long): SamsungTVDevice?

    @Query("SELECT * FROM tv_devices WHERE isLastConnected = 1 LIMIT 1")
    suspend fun getLastConnectedDevice(): SamsungTVDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: SamsungTVDevice): Long

    @Update
    suspend fun updateDevice(device: SamsungTVDevice)

    @Query("UPDATE tv_devices SET isLastConnected = 0")
    suspend fun clearLastConnected()

    @Query("UPDATE tv_devices SET isLastConnected = 1 WHERE id = :id")
    suspend fun setLastConnected(id: Long)

    @Query("DELETE FROM tv_devices WHERE id = :id")
    suspend fun deleteDeviceById(id: Long)
}
