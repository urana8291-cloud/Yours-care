package com.example.screentracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatEntity(
        @PrimaryKey val date: String,
            val reelsCount: Int = 0,
                val tiktokCount: Int = 0,
                    val totalScreenTimeMs: Long = 0L
)

)