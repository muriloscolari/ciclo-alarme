package me.muriloscolari.ciclo.data.history

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val dao: AlarmHistoryDao
) {
    val historyFlow: Flow<List<AlarmHistoryEntity>> = dao.getAllHistory()

    suspend fun recordAlarm(entry: AlarmHistoryEntity): Long {
        return dao.insert(entry)
    }

    suspend fun deleteEntry(id: Long) {
        dao.deleteById(id)
    }

    suspend fun deleteEntry(entry: AlarmHistoryEntity) {
        dao.delete(entry)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }

    suspend fun findRecentDuplicate(wakeHour: Int, wakeMinute: Int, windowMillis: Long = 12 * 60 * 60 * 1000L): AlarmHistoryEntity? {
        val since = System.currentTimeMillis() - windowMillis
        return dao.findRecentSameTime(wakeHour, wakeMinute, since)
    }
}
