package me.muriloscolari.ciclo.data.alarm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_alarms")
data class ActiveAlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerTimeMillis: Long,
    val wakeHour: Int,
    val wakeMinute: Int,
    val label: String,
    val cycles: Int,
    val isSimple: Boolean,
    val engine: String, // "INTERNAL" ou "NATIVE"
    val clockPackage: String? = null,
    val sunriseEnabled: Boolean = false,
    val sunriseLeadMinutes: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis()
)
