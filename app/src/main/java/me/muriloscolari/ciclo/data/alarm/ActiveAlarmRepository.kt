package me.muriloscolari.ciclo.data.alarm

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveAlarmRepository @Inject constructor(
    private val activeAlarmDao: ActiveAlarmDao
) {
    val activeAlarmsFlow: Flow<List<ActiveAlarmEntity>> = activeAlarmDao.getAllActiveAlarms()

    suspend fun recordActiveAlarm(alarm: ActiveAlarmEntity): Long {
        return activeAlarmDao.insertActiveAlarm(alarm)
    }

    suspend fun removeActiveAlarm(id: Long) {
        activeAlarmDao.deleteById(id)
    }

    suspend fun removeActiveAlarm(alarm: ActiveAlarmEntity) {
        activeAlarmDao.deleteActiveAlarm(alarm)
    }

    suspend fun cleanupExpired(thresholdMillis: Long) {
        activeAlarmDao.deleteExpired(thresholdMillis)
    }

    suspend fun clearAll() {
        activeAlarmDao.deleteAll()
    }
}
