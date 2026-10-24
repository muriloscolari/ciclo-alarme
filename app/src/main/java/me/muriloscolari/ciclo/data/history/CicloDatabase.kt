package me.muriloscolari.ciclo.data.history

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AlarmHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CicloDatabase : RoomDatabase() {
    abstract fun alarmHistoryDao(): AlarmHistoryDao
}
