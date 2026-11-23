package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCheckInDao {
    @Query("SELECT * FROM daily_checkins ORDER BY timestamp ASC")
    fun getAllCheckIns(): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_checkins ORDER BY timestamp DESC LIMIT 7")
    fun getRecentCheckIns(): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_checkins ORDER BY timestamp DESC LIMIT 1")
    fun getLatestCheckIn(): Flow<DailyCheckInEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckInEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checkIns: List<DailyCheckInEntity>)

    @Query("SELECT COUNT(*) FROM daily_checkins")
    suspend fun getCount(): Int
}
