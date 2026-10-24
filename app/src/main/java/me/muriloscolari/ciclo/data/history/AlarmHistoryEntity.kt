package me.muriloscolari.ciclo.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_history")
data class AlarmHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAtMillis: Long,
    val mode: String, // NOW, BEDTIME, WAKE_AT, SIMPLE
    val bedTimeMillis: Long,
    val wakeTimeMillis: Long,
    val wakeHour: Int,
    val wakeMinute: Int,
    val cycles: Int,
    val cycleMinutes: Int,
    val fallAsleepMinutes: Int,
    val wasRecommended: Boolean,
    val clockPackage: String?
)
