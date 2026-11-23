package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_checkins")
data class DailyCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayName: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val dateLabel: String, // "Oct 8"
    val readinessLevel: Int, // 0 - 100
    val feelingTag: String, // "Fired Up", "Super Ready", etc.
    val timestamp: Long = System.currentTimeMillis()
)
