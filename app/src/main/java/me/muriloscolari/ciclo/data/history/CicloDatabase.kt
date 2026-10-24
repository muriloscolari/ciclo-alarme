package me.muriloscolari.ciclo.data.history

import androidx.room.Database
import androidx.room.RoomDatabase
import me.muriloscolari.ciclo.data.alarm.ActiveAlarmDao
import me.muriloscolari.ciclo.data.alarm.ActiveAlarmEntity

@Database(
    entities = [
        AlarmHistoryEntity::class,
        ActiveAlarmEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CicloDatabase : RoomDatabase() {
    abstract fun alarmHistoryDao(): AlarmHistoryDao
    abstract fun activeAlarmDao(): ActiveAlarmDao
}
