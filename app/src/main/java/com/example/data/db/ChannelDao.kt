package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.TvChannel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM tv_channels ORDER BY isFavorite DESC, number ASC")
    fun getAllChannels(): Flow<List<TvChannel>>

    @Query("SELECT * FROM tv_channels WHERE isFavorite = 1 ORDER BY number ASC")
    fun getFavoriteChannels(): Flow<List<TvChannel>>

    @Query("SELECT * FROM tv_channels WHERE number = :number LIMIT 1")
    suspend fun getChannelByNumber(number: Int): TvChannel?

    @Query("SELECT * FROM tv_channels WHERE number > :currentNumber ORDER BY number ASC LIMIT 1")
    suspend fun getNextChannel(currentNumber: Int): TvChannel?

    @Query("SELECT * FROM tv_channels WHERE number < :currentNumber ORDER BY number DESC LIMIT 1")
    suspend fun getPreviousChannel(currentNumber: Int): TvChannel?

    @Query("SELECT * FROM tv_channels ORDER BY number ASC LIMIT 1")
    suspend fun getFirstChannel(): TvChannel?

    @Query("SELECT * FROM tv_channels ORDER BY number DESC LIMIT 1")
    suspend fun getLastChannel(): TvChannel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: TvChannel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<TvChannel>)

    @Update
    suspend fun updateChannel(channel: TvChannel)

    @Query("UPDATE tv_channels SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Query("DELETE FROM tv_channels WHERE id = :id")
    suspend fun deleteChannel(id: Long)

    @Query("SELECT COUNT(*) FROM tv_channels")
    suspend fun getChannelCount(): Int
}
