package me.muriloscolari.ciclo.data.alarm

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveAlarmDao {

    @Query("SELECT * FROM active_alarms ORDER BY triggerTimeMillis ASC")
    fun getAllActiveAlarms(): Flow<List<ActiveAlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveAlarm(alarm: ActiveAlarmEntity): Long

    @Delete
    suspend fun deleteActiveAlarm(alarm: ActiveAlarmEntity)

    @Query("DELETE FROM active_alarms WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM active_alarms WHERE triggerTimeMillis < :thresholdMillis")
    suspend fun deleteExpired(thresholdMillis: Long)

    @Query("DELETE FROM active_alarms")
    suspend fun deleteAll()
}
