package com.example.screentracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

        @Insert(onConflict = OnConflictStrategy.IGNORE)
            suspend fun insertIfNotExists(stat: DailyStatEntity)

                @Query("SELECT * FROM daily_stats WHERE date = :date")
                    fun observeDailyStat(date: String): Flow<DailyStatEntity?>

                        @Query("SELECT * FROM daily_stats WHERE date = :date")
                            suspend fun getDailyStat(date: String): DailyStatEntity?

                                @Query("UPDATE daily_stats SET reelsCount = reelsCount + 1 WHERE date = :date")
                                    suspend fun incrementReels(date: String)

                                        @Query("UPDATE daily_stats SET tiktokCount = tiktokCount + 1 WHERE date = :date")
                                            suspend fun incrementTikTok(date: String)

                                                @Query("UPDATE daily_stats SET totalScreenTimeMs = :screenTimeMs WHERE date = :date")
                                                    suspend fun updateScreenTime(date: String, screenTimeMs: Long)

                                                        @Query("SELECT * FROM daily_stats WHERE date LIKE :yearMonth || '%' ORDER BY date DESC")
                                                            suspend fun getMonthlyStats(yearMonth: String): List<DailyStatEntity>
}

}