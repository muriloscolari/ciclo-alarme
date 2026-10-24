package me.muriloscolari.ciclo.data.history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmHistoryDao {

    @Query("SELECT * FROM alarm_history ORDER BY createdAtMillis DESC")
    fun getAllHistory(): Flow<List<AlarmHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AlarmHistoryEntity): Long

    @Delete
    suspend fun delete(entry: AlarmHistoryEntity)

    @Query("DELETE FROM alarm_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM alarm_history")
    suspend fun clearAll()

    /**
     * Busca alarmes criados para o mesmo horário [wakeHour]:[wakeMinute]
     * a partir de [sinceMillis] (ex.: nas últimas 12 horas) para detectar duplicidades.
     */
    @Query("SELECT * FROM alarm_history WHERE wakeHour = :wakeHour AND wakeMinute = :wakeMinute AND createdAtMillis >= :sinceMillis ORDER BY createdAtMillis DESC LIMIT 1")
    suspend fun findRecentSameTime(wakeHour: Int, wakeMinute: Int, sinceMillis: Long): AlarmHistoryEntity?
}
